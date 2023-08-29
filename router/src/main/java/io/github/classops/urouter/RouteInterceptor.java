package io.github.classops.urouter;

import android.content.Context;

import androidx.annotation.Nullable;

public class RouteInterceptor implements Interceptor {

    private final Context context;
    @Nullable
    private final NavigationCallback navigationCallback;

    public RouteInterceptor(Context context, @Nullable NavigationCallback callback) {
        this.context = context;
        this.navigationCallback = callback;
    }

    @Nullable
    @Override
    public Object intercept(Chain chain) throws Exception {
        return Router.get().routeInternal(context, chain.request(), navigationCallback);
    }
}
