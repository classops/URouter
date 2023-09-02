package io.github.classops.urouter.route;


/**
 * 支持的参数类型
 */
public final class ParamType {
    public static final int BOOLEAN = 0;
    public static final int BYTE = 1;
    public static final int SHORT = 2;
    public static final int INT = 3;
    public static final int LONG = 4;
    public static final int CHAR = 5;
    public static final int FLOAT = 6;
    public static final int DOUBLE = 7;
    public static final int STRING = 8;
    public static final int PARCELABLE = 9;
    public static final int SERIALIZABLE = 10;
    public static final int OBJECT = 11;
    public static final int CHARSEQUENCE = 12;

    public static boolean isPrimitiveType(int type) {
        return type >= BOOLEAN && type <= DOUBLE;
    }

}
