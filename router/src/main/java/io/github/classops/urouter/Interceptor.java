package io.github.classops.urouter;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

public interface Interceptor {

    @WorkerThread
    @Nullable
    Object intercept(final Router router, Chain chain) throws Exception;

    interface Chain {

        UriRequest request();

        @WorkerThread
        @Nullable
        Object proceed(UriRequest request) throws Exception;

    }

}
