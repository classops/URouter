package io.github.classops.urouter.route;

import java.util.Map;

/**
 * 路由表加载类
 */
public interface IRouteTable {

    void load(Map<String, RouteInfo> table);

}
