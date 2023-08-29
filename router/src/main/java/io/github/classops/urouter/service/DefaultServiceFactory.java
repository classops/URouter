package io.github.classops.urouter.service;

import android.content.Context;

import androidx.annotation.NonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import io.github.classops.urouter.Router;

public class DefaultServiceFactory implements ServiceFactory {

    @NonNull
    @Override
    public Object create(Class<?> clazz) {
        try {
            // context constructor
            Constructor<?> constructor = getConstructor(clazz, Context.class);
            if (constructor != null) {
                return constructor.newInstance(Router.get().getContext());
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
