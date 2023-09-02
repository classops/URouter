package io.github.classops.urouter.utils;


import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import java.io.Serializable;

public class BundleUtils {

    public static boolean getBoolean(@Nullable Bundle args, String name, boolean defVal) {
        return args != null ? args.getBoolean(name, defVal) : defVal;
    }

    public static byte getByte(@Nullable Bundle args, String name, byte defVal) {
        return args != null ? args.getByte(name, defVal) : defVal;
    }

    public static short getShort(@Nullable Bundle args, String name, short defVal) {
        return args != null ? args.getShort(name, defVal) : defVal;
    }

    public static int getInt(@Nullable Bundle args, String name, int defVal) {
        return args != null ? args.getInt(name, defVal) : defVal;
    }

    public static long getLong(@Nullable Bundle args, String name, long defVal) {
        return args != null ? args.getLong(name, defVal) : defVal;
    }

    public static char getChar(@Nullable Bundle args, String name, char defVal) {
        return args != null ? args.getChar(name, defVal) : defVal;
    }

    public static float getFloat(@Nullable Bundle args, String name, float defVal) {
        return args != null ? args.getFloat(name, defVal) : defVal;
    }

    public static double getDouble(@Nullable Bundle args, String name, double defVal) {
        return args != null ? args.getDouble(name, defVal) : defVal;
    }

    public static CharSequence getCharSequence(@Nullable Bundle args, String name, CharSequence defVal) {
        return args != null ? args.getCharSequence(name, defVal) : defVal;
    }

    @Nullable
    public static String getString(@Nullable Bundle args, String name) {
        return args != null ? args.getString(name) : null;
    }

    public static String getString(@Nullable Bundle args, String name, String defVal) {
        String val = getString(args, name);
        return val != null ? val : defVal;
    }

    @SuppressWarnings("deprecation")
    @Nullable
    public static <T extends Parcelable> T getParcelable(@Nullable Bundle args, String name, Class<T> clazz) {
        if (args == null) return null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return args.getParcelable(name, clazz);
        } else {
            return args.getParcelable(name);
        }
    }

    @SuppressWarnings({"deprecation", "unchecked"})
    @Nullable
    public static <T extends Serializable> T getSerializable(@Nullable Bundle args, String name,
                                                             Class<T> clazz) {
        if (args == null) return null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return args.getSerializable(name, clazz);
        } else {
            return (T) args.getSerializable(name);
        }
    }
    
}
