package io.github.classops.urouter.service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Type;

/**
 * 对象JSON序列化的Service
 *
 * @author wangmingshuo
 * @since 2023/04/07 10:06
 */
public interface SerializationService extends IService {

    <T> T parseObject(@Nullable String json, @NonNull Type type);

    @Nullable
    String toJson(@Nullable Object obj);

}
