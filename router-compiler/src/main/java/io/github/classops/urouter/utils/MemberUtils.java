package io.github.classops.urouter.utils;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

public class MemberUtils {

    public static boolean isStatic(Element element) {
        return element.getModifiers().contains(Modifier.STATIC);
    }

    public static boolean isFinal(Element element) {
        return element.getModifiers().contains(Modifier.FINAL);
    }

    public static boolean isPublic(Element element) {
        return element.getModifiers().contains(Modifier.PUBLIC);
    }

    public static boolean isPrivate(Element element) {
        return element.getModifiers().contains(Modifier.PRIVATE);
    }

}
