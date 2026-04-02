package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 新增用户因唯一值重复导致SQL无法执行导致的重复异常处理
     */
    @ExceptionHandler
    public Result DuplicateException(SQLIntegrityConstraintViolationException exception){
        log.error("SQL重复异常：{}",exception.getMessage());
        if(exception.getMessage().contains("Duplicate")){
            String[] s = exception.getMessage().split(" ");// 针对错误信息基于空格进行分割
            return Result.error(s[2]+MessageConstant.ALREADY_EXISTS); // 将信息当中的实际重复变量值提交给返回结果给前端进行告警信息提示用户
        }else {
            return Result.error(MessageConstant.UNKNOWN_ERROR);
        }

    }

}
