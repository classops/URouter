package io.github.classops.urouter.utils;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.Serializable;

public class StateUtils {

    public static boolean getBoolean(Activity activity, String name, boolean defVal) {
        return activity.getIntent().getBooleanExtra(name, defVal);
    }

    public static byte getByte(Activity activity, String name, byte defVal) {
        return activity.getIntent().getByteExtra(name, defVal);
    }

    public static short getShort(Activity activity, String name, short defVal) {
        return activity.getIntent().getShortExtra(name, defVal);
    }

    public static int getInt(Activity activity, String name, int defVal) {
        return activity.getIntent().getIntExtra(name, defVal);
    }

    public static long getLong(Activity activity, String name, long defVal) {
        return activity.getIntent().getLongExtra(name, defVal);
    }

    public static char getChar(Activity activity, String name, char defVal) {
        return activity.getIntent().getCharExtra(name, defVal);
    }

    public static float getFloat(Activity activity, String name, float defVal) {
        return activity.getIntent().getFloatExtra(name, defVal);
    }

    public static double getDouble(Activity activity, String name, double defVal) {
        return activity.getIntent().getDoubleExtra(name, defVal);
    }

    public static CharSequence getCharSequence(Activity activity, String name, CharSequence defVal) {
        CharSequence val =  activity.getIntent().getCharSequenceExtra(name);
        return val != null ? val : defVal;
    }

    public static String getString(Activity activity, String name, String defVal) {
        String val = activity.getIntent().getStringExtra(name);
        return val != null ? val : defVal;
    }

    @Nullable
    public static String getString(Activity activity, String name) {
        return activity.getIntent().getStringExtra(name);
    }

    public static <T extends Parcelable> T getParcelable(Activity activity, String name) {
        return activity.getIntent().getParcelableExtra(name);
    }

    public static Serializable getSerializable(Activity activity, String name) {
        return activity.getIntent().getSerializableExtra(name);
    }

    //-------------------------------------------------------------

    public static boolean getBoolean(Fragment fragment, String name, boolean defVal) {
        Bundle args = fragment.getArguments();
        return args != null ? args.getBoolean(name, defVal) : defVal;
    }

    public static byte getByte(Fragment fragment, String name, byte defVal) {
        Bundle args = fragment.getArguments();
        return args != null ? args.getByte(name, defVal) : defVal;
    }

    public static short getShort(Fragment fragment, String name, short defVal) {
        Bundle args = fragment.getArguments();
        return args != null ? args.getShort(name, defVal) : defVal;
    }

    public static int getInt(Fragment fragment, String name, int defVal) {
        Bundle args = fragment.getArguments();
        return args != null ? args.getInt(name, defVal) : defVal;
    }

    public static long getLong(Fragment fragment, String name, long defVal) {
        Bundle args = fragment.getArguments();
        return args != null ? args.getLong(name, defVal) : defVal;
    }

    public static char getChar(Fragment fragment, String name, char defVal) {
        Bundle args = fragment.getArguments();
        return args != null ? args.getChar(name, defVal) : defVal;
    }

    public static float getFloat(Fragment fragment, String name, float defVal) {
        Bundle args = fragment.getArguments();
        return args != null ? args.getFloat(name, defVal) : defVal;
    }

    public static double getDouble(Fragment fragment, String name, double defVal) {
        Bundle args = fragment.getArguments();
        return args != null ? args.getDouble(name, defVal) : defVal;
    }

    public static CharSequence getCharSequence(Fragment fragment, String name, CharSequence defVal) {
        Bundle args = fragment.getArguments();
        return args != null ? args.getCharSequence(name, defVal) : defVal;
    }

    public static String getString(Fragment fragment, String name, String defVal) {
        String val = getString(fragment, name);
        return val != null ? val : defVal;
    }

    @Nullable
    public static String getString(Fragment fragment, String name) {
        Bundle args = fragment.getArguments();
        return args != null ? args.getString(name) : null;
    }

    @Nullable
    public static <T extends Parcelable> T getParcelable(Fragment fragment, String name) {
        Bundle args = fragment.getArguments();
        if (args != null) {
            return args.getParcelable(name);
        }
        return null;
    }

    @Nullable
    public static Serializable getSerializable(Fragment fragment, String name) {
        Bundle args = fragment.getArguments();
        return args != null ? args.getSerializable(name) : null;
    }

}
