package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解：用于标识需要进行自动填充处理的数据库操作方法
 * <p>
 * 该注解通常配合AOP切面使用，根据操作类型（INSERT/UPDATE）自动填充
 * 创建时间、更新时间、创建人、更新人等公共字段
 * </p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {

    /**
     * 指定数据库操作类型
     * <p>
     * 由于方法名为 value()，使用时可以省略属性名，例如：
     * <pre>{@code @AutoFill(OperationType.INSERT)}</pre>
     * 而不需要写成：
     * <pre>{@code @AutoFill(value = OperationType.INSERT)}</pre>
     * </p>
     *
     * @return 操作类型（INSERT 或 UPDATE）
     */
    OperationType value();
}
