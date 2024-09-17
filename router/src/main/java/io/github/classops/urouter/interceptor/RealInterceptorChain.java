package io.github.classops.urouter.interceptor;

import androidx.annotation.Nullable;

import java.util.List;

import io.github.classops.urouter.Interceptor;
import io.github.classops.urouter.Router;
import io.github.classops.urouter.UriRequest;

public class RealInterceptorChain implements Interceptor.Chain {

    private final Router router;
    private final int index;
    private final List<Interceptor> interceptors;
    private final UriRequest request;

    public RealInterceptorChain(Router router, int index, List<Interceptor> interceptors,
                                UriRequest request) {
        this.router = router;
        this.index = index;
        this.interceptors = interceptors;
        this.request = request;
    }

    @Override
    public UriRequest request() {
        return this.request;
    }

    @Nullable
    @Override
    public Object proceed(UriRequest request) throws Exception {
        Interceptor.Chain next = getNext(request);
        Interceptor interceptor = this.interceptors.get(this.index);
        return interceptor.intercept(router, next);
    }

    private RealInterceptorChain getNext(UriRequest request) {
        return new RealInterceptorChain(router, this.index + 1, this.interceptors, request);
    }

}
