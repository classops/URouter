package io.github.classops.urouter.annotation;

import java.lang.annotation.*;

/**
 * 文件名：Param <br/>
 * 描述：注解字段参数
 *
 * @author wangmingshuo
 * @since 2022/11/18 16:46
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Param {

    /**
     * 参数名
     */
    String name() default "";

    boolean required() default false;

    /**
     * 参数描述
     */
    String desc() default "";

}
