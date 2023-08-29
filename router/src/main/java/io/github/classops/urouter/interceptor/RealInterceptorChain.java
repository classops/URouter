package io.github.classops.urouter.interceptor;

import androidx.annotation.Nullable;

import java.util.List;

import io.github.classops.urouter.Interceptor;
import io.github.classops.urouter.UriRequest;

public class RealInterceptorChain implements Interceptor.Chain {

    private final int index;
    private final List<Interceptor> interceptors;

    private final UriRequest request;

    public RealInterceptorChain(int index, List<Interceptor> interceptors, UriRequest request) {
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
        Interceptor.Chain next = getNext();
        Interceptor interceptor = this.interceptors.get(index);
        return interceptor.intercept(next);
    }

    private RealInterceptorChain getNext() {
        return new RealInterceptorChain(this.index + 1, this.interceptors, this.request);
    }

}
