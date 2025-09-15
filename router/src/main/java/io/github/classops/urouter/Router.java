package io.github.classops.urouter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.AnyThread;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.collection.ArrayMap;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.github.classops.urouter.inject.Injector;
import io.github.classops.urouter.interceptor.RealInterceptorChain;
import io.github.classops.urouter.interceptor.RouteInterceptor;
import io.github.classops.urouter.result.RouterResultLauncher;
import io.github.classops.urouter.route.IRouteTable;
import io.github.classops.urouter.route.IServiceTable;
import io.github.classops.urouter.route.RouteInfo;
import io.github.classops.urouter.route.RouteType;
import io.github.classops.urouter.service.DefaultServiceFactory;
import io.github.classops.urouter.service.ServiceLoader;

/**
 * Router
 *
 * @author classops
 * @since 2022/11/17 19:12
 */
public class Router {

    private static volatile Router sRouter;

    public static Router get() {
        if (sRouter == null) {
            synchronized (Router.class) {
                if (sRouter == null) {
                    sRouter = new Router();
                }
            }
        }
        return sRouter;
    }

    private Application context;
    private final Map<String, RouteInfo> routeTables = Collections.synchronizedMap(new ArrayMap<>());
    private final Map<Class<?>, Injector> injectorMap = Collections.synchronizedMap(new ArrayMap<>());
    private final Map<String, RouteInfo> serviceMap = Collections.synchronizedMap(new ArrayMap<>());
    private final List<Interceptor> interceptors = new CopyOnWriteArrayList<>();
    private ServiceLoader serviceLoader;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Executor executor;

    public Router() {
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    }

    public Context getContext() {
        return this.context;
    }

    public List<Interceptor> getInterceptors() {
        return this.interceptors;
    }

    public Executor getExecutor() {
        return this.executor;
    }

    public void init(@NonNull Application application) {
        this.context = application;
        this.serviceLoader = new ServiceLoader(new DefaultServiceFactory(context));
        // 初始化工作
        loadRouter();
    }

    private void loadRouter() {
        // 初始化加载路由表
        try {
            Class<?> clazz = Class.forName("io.github.classops.urouter.init.RouteInit");
            Method m = clazz.getMethod("load", Router.class);
            m.invoke(null, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void log() {
        for (String s : routeTables.keySet()) {
            Log.d("router", "path: " + s + ", route: " + routeTables.get(s));
        }
    }

    public void register(@Nullable String className) {
        if (className == null) return;
        try {
            Class<?> clazz = Class.forName(className);
            Object obj = clazz.getConstructor().newInstance();
            if (obj instanceof IRouteTable) {
                registerTable((IRouteTable) obj);
            } else if (obj instanceof IServiceTable) {
                registerService((IServiceTable) obj);
            } else {
                Log.d("Router", "unknown type!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 方法手动添加路由表信息
     *
     * @param path      path
     * @param routeInfo 路由信息
     */
    @AnyThread
    public void register(@NonNull String path, @NonNull RouteInfo routeInfo) {
        routeTables.put(path, routeInfo);
        if (routeInfo.getType() == RouteType.SERVICE) {
            serviceMap.put(routeInfo.getClazz().getName(), routeInfo);
        }
    }

    @AnyThread
    public void registerTable(@NonNull IRouteTable route) {
        route.load(routeTables);
    }

    @AnyThread
    public void registerService(@NonNull IServiceTable route) {
        route.load(serviceMap);
    }

    /**
     * 获取参数注入的实现类
     *
     * @param targetClazz 注入目标的Activity/Fragment类
     */
    @Nullable
    private Injector getInjector(Class<?> targetClazz) {
        Injector injector = injectorMap.get(targetClazz);
        if (injector != null) {
            return injector;
        }
        try {
            String injectClass = targetClazz.getCanonicalName() + "$$Router$$Injector";
            Class<?> clazz = Class.forName(injectClass);
            injector = (Injector) clazz.newInstance();
            injectorMap.put(targetClazz, injector);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return injector;
    }

    public void inject(Object object) {
        Injector injector = getInjector(object.getClass());
        if (injector != null) {
            injector.inject(object);
        }
    }

    public void addInterceptor(Interceptor interceptor) {
        this.interceptors.add(interceptor);
    }

    public UriRequest.Builder build(@NonNull String path) {
        return new UriRequest.Builder(path);
    }

    public UriRequest.Builder build(@NonNull Uri uri) {
        return new UriRequest.Builder(uri);
    }

    public <T> ActivityResultLauncher<UriRequest> registerForResult(@NonNull ComponentActivity activity,
                                                                    @NonNull ActivityResultContract<Intent, T> contract,
                                                                    @NonNull ActivityResultCallback<T> callback) {

        return new RouterResultLauncher(activity,
                activity.registerForActivityResult(contract, callback),
                callback);
    }

    public <T> ActivityResultLauncher<UriRequest> registerForResult(@NonNull Fragment fragment,
                                                                    @NonNull ActivityResultContract<Intent, T> contract,
                                                                    @NonNull ActivityResultCallback<T> callback) {
        return new RouterResultLauncher(fragment.requireContext(),
                fragment.registerForActivityResult(contract, callback),
                callback);
    }

    @AnyThread
    @Nullable
    public Object route(@Nullable final Context context, @NonNull final UriRequest request,
                        @Nullable final NavigationCallback callback) {
        return this.route(context, request, callback, true);
    }

    @AnyThread
    @Nullable
    public Object route(@Nullable final Context context, @NonNull final UriRequest request,
                        @Nullable final NavigationCallback callback, boolean intercept) {
        if (!intercept || request.isIgnoreInterceptor()) {
            return routeInternal(context, request, callback);
        } else {
            this.executor.execute(() -> routeRequest(context, callback, request));
            return null;
        }
    }

    @Nullable
    public <T> T navigate(Class<? extends T> clazz) {
        return this.route(clazz);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T route(Class<? extends T> clazz) {
        // 服务发现
        RouteInfo routeInfo = serviceMap.get(clazz.getName());
        if (routeInfo == null) return null;

        return (T) serviceLoader.getService(routeInfo.getClazz());
    }

    @WorkerThread
    void routeRequest(@Nullable Context context, @Nullable NavigationCallback callback,
                      @NonNull UriRequest request) {
        try {
            List<Interceptor> interceptors = new ArrayList<>(Router.this.interceptors);
            interceptors.add(new RouteInterceptor(context, callback));
            Interceptor.Chain chain = new RealInterceptorChain(this, 0, interceptors, request);
            chain.proceed(request);
        } catch (Exception e) {
            e.printStackTrace();
            if (callback != null) {
                runOnMainThread(() -> callback.onInterrupt(request));
            }
        }
    }

    @AnyThread
    Object routeInternal(@Nullable Context context, @NonNull final UriRequest request,
                         @Nullable final NavigationCallback callback) {
        String path = request.getPath();
        // 路径是空，直接返回
        if (path == null) {
            if (callback != null) {
                runOnMainThread(() -> callback.onLost(request));
            }
            return null;
        }
        RouteInfo routeInfo = routeTables.get(request.getPath());
        // 未找到路由信息
        int routeType = request.getRouteType();
        if (routeInfo == null || (routeType != RouteType.UNKNOWN && routeInfo.getType() != routeType)) {
            if (callback != null) {
                runOnMainThread(() -> callback.onLost(request));
            }
            return null;
        }
        if (callback != null) {
            runOnMainThread(() -> callback.onFound(request));
        }
        return routeInternal(context != null ? context : this.context, request, routeInfo, callback);
    }

    @AnyThread
    @SuppressLint("WrongConstant")
    Object routeInternal(@NonNull final Context context, @NonNull final UriRequest request,
                         @NonNull RouteInfo routeInfo, @Nullable final NavigationCallback callback) {
        switch (routeInfo.getType()) {
            case RouteType.ACTIVITY:
                // Activity
                final Intent intent = new Intent(context, routeInfo.getClazz());
                Bundle extras = getExtras(request, routeInfo);
                intent.putExtras(extras);
                intent.setFlags(request.getFlags());
                if (!(context instanceof Activity)) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                if (request.isStartActivity()) {
                    runOnMainThread(() -> {
                        routeActivity(context, intent, request);
                        if (callback != null) {
                            callback.onArrival(request);
                        }
                    });
                }
                return intent;

            case RouteType.FRAGMENT:
                return routeFragment(context, request, routeInfo);

            case RouteType.SERVICE:
                return routeService(context, request, routeInfo);
        }
        return null;
    }

    @Nullable
    Intent routeIntent(@NonNull Context context, @NonNull UriRequest request) {
        String path = request.getPath();
        // 路径是空，直接返回
        if (path == null) {
            return null;
        }
        RouteInfo routeInfo = routeTables.get(request.getPath());
        // 未找到路由信息
        if (routeInfo == null) {
            return null;
        }
        if (routeInfo.getType() != RouteType.ACTIVITY) {
            return null;
        }

        Intent intent = new Intent(context, routeInfo.getClazz());
        Bundle extras = getExtras(request, routeInfo);
        intent.putExtras(extras);
        return intent;
    }


    @MainThread
    private void routeActivity(@NonNull Context context, @NonNull Intent intent, @NonNull UriRequest request) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            if (request.getRequestCode() >= 0) {
                ActivityCompat.startActivityForResult(activity, intent, request.getRequestCode(),
                        request.getActivityOptions());
            } else {
                ActivityCompat.startActivity(activity, intent, request.getActivityOptions());
            }
            if (request.getEnterAnim() != -1 && request.getExitAnim() != -1) {
                activity.overridePendingTransition(request.getEnterAnim(), request.getExitAnim());
            }
        } else {
            ActivityCompat.startActivity(context, intent, request.getActivityOptions());
        }
    }

    @SuppressWarnings("deprecation")
    private Object routeFragment(@NonNull Context context, @NonNull UriRequest request,
                                 @NonNull RouteInfo routeInfo) {
        Class<?> clazz = routeInfo.getClazz();
        try {
            Object fragment = clazz.getConstructor().newInstance();
            Bundle extras = getExtras(request, routeInfo);
            // set arguments
            if (fragment instanceof android.app.Fragment) {
                ((android.app.Fragment) fragment).setArguments(extras);
            } else if (fragment instanceof androidx.fragment.app.Fragment) {
                ((androidx.fragment.app.Fragment) fragment).setArguments(extras);
            }
            return fragment;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Object routeService(@NonNull Context context, @NonNull UriRequest request,
                                @NonNull RouteInfo routeInfo) {
        Class<?> clazz = routeInfo.getClazz();
        return serviceLoader.getService(clazz);
    }

    private void runOnMainThread(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            handler.post(runnable);
        }
    }

    @NonNull
    private Bundle getExtras(@NonNull UriRequest request, @NonNull RouteInfo routeInfo) {
        Injector injector = getInjector(routeInfo.getClazz());
        Bundle extras;
        if (injector != null) {
            extras = injector.query(request, routeInfo);
        } else {
            extras = request.getExtras();
        }
        return extras != null ? extras : Bundle.EMPTY;
    }

}
