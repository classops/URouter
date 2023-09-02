package io.github.classops.urouter.inject;

import android.os.Bundle;

import io.github.classops.urouter.UriRequest;
import io.github.classops.urouter.route.RouteInfo;

/**
 * 参数注入接口
 *
 * @author wangmingshuo
 * @since 2022/12/13 16:51
 */
public interface Injector {

    Bundle query(UriRequest request, RouteInfo routeInfo);

    void inject(Object object);

}
