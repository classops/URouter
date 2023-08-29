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
import io.github.classops.urouter.RouteInterceptor;
import io.github.classops.urouter.Router;
import io.github.classops.urouter.UriRequest;
import io.github.classops.urouter.interceptor.RealInterceptorChain;

/**
 * RouterActivityResultLauncher
 *
 * @author wangmingshuo
 * @since 2023/08/25 13:44
 */
public class RouterActivityResultLauncher extends ActivityResultLauncher<UriRequest> {

    @NonNull
    private final Context context;
    @NonNull
    private final ActivityResultLauncher<Intent> launcher;
    @NonNull
    private final ActivityResultCallback<?> resultCallback;

    private final Handler handler = new Handler(Looper.getMainLooper());

    public RouterActivityResultLauncher(@NonNull Context context,
                                        @NonNull ActivityResultLauncher<Intent> launcher,
                                        @NonNull ActivityResultCallback<?> resultCallback) {
        this.context = context;
        this.launcher = launcher;
        this.resultCallback = resultCallback;
    }

    @Override
    public void launch(final UriRequest input, @Nullable final ActivityOptionsCompat options) {
        // 执行异步的拦截器，线程池执行 拦截器
        Router.get().getExecutor().execute(new Runnable() {
            @WorkerThread
            @Override
            public void run() {
                try {
                    // 执行拦截器
                    List<Interceptor> interceptors = new ArrayList<>(Router.get().getInterceptors());
                    interceptors.add(new RouteInterceptor(context, null));
                    Interceptor.Chain chain = new RealInterceptorChain(0, interceptors, input);
                    // 不启动Activity，仅处理最后结果
                    input.setStartActivity(false);
                    // 拦截
                    final Intent intent = (Intent) chain.proceed(input);
                    if (intent == null) {
                        throw new ActivityNotFoundException();
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            launcher.launch(intent, options);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            resultCallback.onActivityResult(null);
                        }
                    });
                }
            }
        });
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

}
