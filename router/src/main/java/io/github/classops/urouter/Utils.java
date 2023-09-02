package io.github.classops.urouter;

import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 格式化工具类
 *
 * @author wangmingshuo
 * @since 2023/04/28 16:15
 */
public class Utils {

    public static final class MapBuilder {

        private final Map<String, Integer> map = new ArrayMap<>();

        public MapBuilder add(String name, Integer type) {
            this.map.put(name, type);
            return this;
        }

        public Map<String, Integer> build() {
            return this.map;
        }

    }

    public static MapBuilder mapBuilder() {
        return new MapBuilder();
    }

    public static Map<String, List<String>> getQueryParameters(@NonNull Uri uri) {
        String query = uri.getEncodedQuery();
        if (query == null) {
            return Collections.emptyMap();
        }

        Map<String, List<String>> paramMap = new ArrayMap<>();
        int start = 0;
        do {
            int next = query.indexOf("&", start);
            int end = next == -1 ? query.length() : next;

            int sep = query.indexOf("=", start);
            if (sep > end || sep == -1) {
                sep = end;
            }
            String name = query.substring(start, sep);
            if (!TextUtils.isEmpty(name)) {
                String key = Uri.decode(name);
                String value = sep == end ? "" : query.substring(sep + 1, end);
                List<String> values = paramMap.get(key);
                if (values == null) {
                    values = new ArrayList<>();
                    paramMap.put(key, values);
                }
                values.add(Uri.decode(value));
            }
            start = end + 1;
        } while (start < query.length());

        return Collections.unmodifiableMap(paramMap);
    }

    public static String getParam(Map<String, List<String>> paramMap, String key) {
        List<String> values = paramMap.get(key);
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.get(0);
    }

    public static boolean[] toBooleanArray(@Nullable final List<String> list, boolean defaultValue) {
        if (list == null || list.isEmpty()) {
            return null;
        }

        boolean[] result = new boolean[list.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = toBoolean(list.get(i), defaultValue);
        }
        return result;
    }

    public static boolean toBoolean(@Nullable final String str) {
        return toBoolean(str, false);
    }

    public static boolean toBoolean(@Nullable final String str, final boolean defaultValue) {
        if (str == null) return defaultValue;

        try {
            return Boolean.parseBoolean(str);
        } catch (Exception e) {
            return defaultValue;
        }
    }


    public static byte toByte(@Nullable final String str) {
        return toByte(str, (byte) 0);
    }

    public static byte toByte(@Nullable final String str, final byte defaultValue) {
        if (str == null) return defaultValue;

        try {
            return Byte.parseByte(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static short toShort(@Nullable final String str) {
        return toShort(str, (short) 0);
    }

    public static short toShort(@Nullable final String str, final short defaultValue) {
        if (str == null) return defaultValue;

        try {
            return Short.parseShort(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static int toInt(@Nullable final String str) {
        return toInt(str, 0);
    }

    public static int toInt(@Nullable final String str, final int defaultValue) {
        if (str == null) return defaultValue;

        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static long toLong(@Nullable final String str) {
        return toLong(str, 0L);
    }

    public static long toLong(@Nullable final String str, final long defaultValue) {
        if (str == null) return defaultValue;

        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static float toFloat(@Nullable final String str) {
        return toFloat(str, 0f);
    }

    public static float toFloat(@Nullable final String str, final float defaultValue) {
        if (str == null) return defaultValue;

        try {
            return Float.parseFloat(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static double toDouble(@Nullable final String str) {
        return toDouble(str, 0.0);
    }

    public static double toDouble(@Nullable final String str, final double defaultValue) {
        if (str == null) return defaultValue;

        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static char toChar(@Nullable final String str) {
        return toChar(str, Character.MIN_VALUE);
    }

    public static char toChar(@Nullable final String str, final char defaultValue) {
        if (str == null || str.isEmpty()) return defaultValue;

        return str.charAt(0);
    }

}
