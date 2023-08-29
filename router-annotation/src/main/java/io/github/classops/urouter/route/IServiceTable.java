package io.github.classops.urouter.route;

import java.util.Map;

/**
 * Service表
 */
public interface IServiceTable {

    void load(Map<String, RouteInfo> table);

}
