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

    /**
     * Uri参数添加
     *
     * @param request Uri路由请求
     * @param routeInfo 路由信息
     * @return 页面传递参数
     */
    Bundle query(UriRequest request, RouteInfo routeInfo);

    /**
     * 将Activity/Fragment参数注入到 object 的 @Param 字段里
     *
     * @param object Activity/Fragment
     */
    void inject(Object object);

}
