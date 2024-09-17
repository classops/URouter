package io.github.classops.urouter.result;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.core.app.ActivityOptionsCompat;

import java.util.ArrayList;
import java.util.List;

import io.github.classops.urouter.Interceptor;
import io.github.classops.urouter.Router;
import io.github.classops.urouter.UriRequest;
import io.github.classops.urouter.interceptor.RealInterceptorChain;
import io.github.classops.urouter.interceptor.RouteInterceptor;

/**
 * RouterActivityResultLauncher
 *
 * @author wangmingshuo
 * @since 2023/08/25 13:44
 */
public class RouterResultLauncher extends ActivityResultLauncher<UriRequest> {

    @NonNull
    private final Context context;
    @NonNull
    private final Router router;
    @NonNull
    private final ActivityResultLauncher<Intent> launcher;
    @NonNull
    private final ActivityResultCallback<?> resultCallback;

    private final Handler handler = new Handler(Looper.getMainLooper());

    public RouterResultLauncher(@NonNull Context context,
                                @NonNull ActivityResultLauncher<Intent> launcher,
                                @NonNull ActivityResultCallback<?> resultCallback) {
        this.context = context;
        this.router = Router.get();
        this.launcher = launcher;
        this.resultCallback = resultCallback;
    }

    @Override
    public void launch(final UriRequest input, @Nullable final ActivityOptionsCompat options) {
        // 执行异步的拦截器，线程池执行 拦截器
        router.getExecutor().execute(() -> launchRequest(input, options));
    }

    @MainThread
    @Override
    public void unregister() {
        this.launcher.unregister();
    }

    @NonNull
    @Override
    public ActivityResultContract<UriRequest, ?> getContract() {
        throw new UnsupportedOperationException("unsupported operation");
    }

    @WorkerThread
    private void launchRequest(UriRequest input, @Nullable final ActivityOptionsCompat options) {
        try {
            // 执行拦截器
            List<Interceptor> interceptors = new ArrayList<>(router.getInterceptors());
            interceptors.add(new RouteInterceptor(context, null));
            // 不启动Activity，仅处理最后结果
            UriRequest request = input.newBuilder(router)
                    .routeOnly()
                    .build();
            Interceptor.Chain chain = new RealInterceptorChain(router, 0, interceptors, request);
            // 拦截
            final Intent intent = (Intent) chain.proceed(request);
            if (intent == null) {
                throw new ActivityNotFoundException();
            }
            handler.post(() -> launcher.launch(intent, options));
        } catch (Exception e) {
            e.printStackTrace();
            handler.post(() -> resultCallback.onActivityResult(null));
        }
    }

}
