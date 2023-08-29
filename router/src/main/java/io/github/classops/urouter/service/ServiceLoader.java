package io.github.classops.urouter.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceLoader {

    // service factory 注解获取实例， interfaces 匹配
    // class 创建
    private final ServiceFactory serviceFactory = new DefaultServiceFactory();
    private final Map<Class<?>, Object> serviceMap = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> clazz) {
        Object obj = serviceMap.get(clazz);
        if (obj != null) {
            return (T) obj;
        }
        synchronized (serviceMap) {
            obj = serviceFactory.create(clazz);
            serviceMap.put(clazz, obj);
        }
        return (T) obj;
    }

}
