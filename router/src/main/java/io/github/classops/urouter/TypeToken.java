package io.github.classops.urouter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * TypeToken，反射获取类型
 *
 * @author wangmingshuo
 * @since 2023/04/21 14:38
 */
public abstract class TypeToken<T> {

    private final Type type;

    protected TypeToken() {
        this.type = getParameterizedType();
    }

    private Type getParameterizedType() {
        Type superClass = getClass().getGenericSuperclass();
        assert superClass != null;
        return ((ParameterizedType) superClass).getActualTypeArguments()[0];
    }

    public Type getType() {
        return this.type;
    }
}
