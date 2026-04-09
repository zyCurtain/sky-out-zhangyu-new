package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Component
@Aspect
@Slf4j
public class AutoFillAspect {
    /**
     * 定义切入点：只要添加了@AutoFill注解的就交给切面类管理
     */
    @Pointcut("@annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {
    }

    /**
     * 第二步：定义【通知】（Advice）
     * 使用 @Before 表示在真正的 Mapper 方法执行“之前”运行这段逻辑。
     * 因为我们必须在 SQL 发送给数据库之前，把时间、操作人填好。
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("--- AOP 开始自动填充公共字段 ---");
        // 1. 获取当前被拦截方法上的【注解信息】
        // 目的：知道当前是 INSERT 还是 UPDATE
        MethodSignature signature = (MethodSignature) joinPoint.getSignature(); // 获取方法签名
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class); // 获取方法上的注解对象
        OperationType operationType = autoFill.value(); // 获取注解里的操作类型（枚举值）

        // 2. 获取当前被拦截方法的【参数值】
        // 目的：拿到你要存入数据库的那个对象（比如 Dept 对象或 Emp 对象）
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) return; // 如果方法没传参数，直接结束

        // 约定：我们要操作的实体类（如 Dept）必须是方法的第一个参数
        Object entity = args[0];

        // 3. 准备要填充的数据内容
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        // 4. 利用【反射】（Reflection）为对象的属性赋值
        // 为什么用反射？因为 AOP 不知道你传入的是 Dept 还是 Emp，
        // 反射可以动态地在运行时去调用对象身上的 set方法。
        try {
            if (operationType == OperationType.INSERT) {
                // 如果是新增操作，需要填充 4 个字段
                // 获取 set 方法的对象（方法名，参数类型）
                Method setCreateTime = entity.getClass().getDeclaredMethod("setCreateTime", LocalDateTime.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod("setCreateUser", Long.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod("setUpdateUser", Long.class);

                // 通过 invoke 执行方法：相当于执行了 entity.setCreateTime(now)
                setCreateTime.invoke(entity, now);
                setUpdateTime.invoke(entity, now);
                setCreateUser.invoke(entity, currentId);
                setUpdateUser.invoke(entity, currentId);

                log.info("已自动填充：createTime, updateTime, createUser, updateUser");

            } else if (operationType == OperationType.UPDATE) {
                // 如果是修改操作，只需要填充 2 个字段（更新时间和更新人）
                Method setUpdateTime = entity.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod("setUpdateUser", Long.class);

                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);

                log.info("已自动填充：updateTime, updateUser");
            }
        } catch (Exception e) {
            log.error("公共字段填充发生异常: {}", e.getMessage());
        }
    }
}
