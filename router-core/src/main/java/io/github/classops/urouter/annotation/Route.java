package io.github.classops.urouter.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Route {

    String path() default "";

    String[] alias() default {};

    int flag() default 0;

}
