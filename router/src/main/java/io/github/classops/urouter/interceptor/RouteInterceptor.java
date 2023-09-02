package io.github.classops.urouter.interceptor;

import android.content.Context;

import androidx.annotation.Nullable;

import io.github.classops.urouter.Interceptor;
import io.github.classops.urouter.NavigationCallback;
import io.github.classops.urouter.Router;

public class RouteInterceptor implements Interceptor {

    private final Context context;
    @Nullable
    private final NavigationCallback navigationCallback;

    public RouteInterceptor(@Nullable Context context, @Nullable NavigationCallback callback) {
        this.context = context;
        this.navigationCallback = callback;
    }

    @Nullable
    @Override
    public Object intercept(Chain chain) throws Exception {
        return Router.get().route(context, chain.request(), navigationCallback, false);
    }
}
