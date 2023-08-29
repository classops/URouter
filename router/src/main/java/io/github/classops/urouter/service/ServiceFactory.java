package io.github.classops.urouter.service;

import androidx.annotation.NonNull;

/**
 * 服务对象工厂接口
 */
public interface ServiceFactory {

    @NonNull
    Object create(Class<?> clazz);

}
