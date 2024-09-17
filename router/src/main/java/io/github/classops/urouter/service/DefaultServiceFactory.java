package io.github.classops.urouter.service;

import android.content.Context;

import androidx.annotation.NonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class DefaultServiceFactory implements ServiceFactory {

    @NonNull
    private final Context context;

    public DefaultServiceFactory(@NonNull Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public Object create(Class<?> clazz) {
        try {
            // context constructor
            Constructor<?> constructor = getConstructor(clazz, Context.class);
            if (constructor != null) {
                return constructor.newInstance(context);
            }
            Constructor<?> constructor2 = getConstructor(clazz);
            if (constructor2 != null) {
                return constructor2.newInstance();
            }

            throw new RuntimeException("constructor is not found!");
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    private Constructor<?> getConstructor(Class<?> clazz, Class<?>... parameterTypes) {
        try {
            return clazz.getConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

}
