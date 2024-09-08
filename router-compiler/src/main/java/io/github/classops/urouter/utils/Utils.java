package io.github.classops.urouter.utils;

public class Utils {

    public static boolean isJavaIdentifierStart(char c) {
        return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || Character.isJavaIdentifierStart(c);
    }

    public static boolean isJavaIdentifierPart(char c) {
        return c >= '0' && c <= '9' || isJavaIdentifierStart(c);
    }

    public static boolean isJavaIdentifier(String text) {
        int len = text.length();
        if (len == 0) return false;
        if (!isJavaIdentifierStart(text.charAt(0))) return false;
        for (int i = 1; i < len; i++) {
            if (!isJavaIdentifierPart(text.charAt(i))) return false;
        }
        return true;
    }

    // 正则 替换 特殊字符
    public static String getValidTypeName(String name) {
        if (isJavaIdentifier(name)) {
            return name;
        } else {
            return name.replaceAll("\\W", "_");
        }
    }

}
