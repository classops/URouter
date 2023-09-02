package io.github.classops.urouter.route;

import java.util.Map;

/**
 * 路由目标信息
 */
public class RouteInfo {

    // 类型，Activity/Fragment/IService
    private byte type;
    private Class<?> clazz;
    private Map<String, Integer> paramsType;

    public RouteInfo(byte type, Class<?> clazz, Map<String, Integer> paramsType) {
        this.type = type;
        this.clazz = clazz;
        this.paramsType = paramsType;
    }

    public static RouteInfo build(byte type, Class<?> clazz, Map<String, Integer> paramsType) {
        return new RouteInfo(type, clazz, paramsType);
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public Map<String, Integer> getParamsType() {
        return paramsType;
    }

    public void setParamsType(Map<String, Integer> paramsType) {
        this.paramsType = paramsType;
    }
}
