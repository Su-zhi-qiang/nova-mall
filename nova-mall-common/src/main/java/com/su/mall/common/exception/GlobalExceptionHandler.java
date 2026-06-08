package com.su.mall.common.exception;

import cn.hutool.core.util.StrUtil;
import com.su.mall.common.api.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.util.stream.Collectors;

/**
 * 全局异常处理类
 * @author Su
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(value = ApiException.class)
    public CommonResult<Void> handle(ApiException e) {
        if (e.getErrorCode() != null) {
            return CommonResult.failed(e.getErrorCode());
        }
        return CommonResult.failed(e.getMessage());
    }

    /**
     * 处理SQL语法错误异常
     */
    @ResponseBody
    @ExceptionHandler(value = SQLSyntaxErrorException.class)
    public CommonResult<Void> handleSQLSyntaxErrorException(SQLSyntaxErrorException e) {
        String message = e.getMessage();
        if (StrUtil.isNotEmpty(message) && message.contains("denied")) {
            message = "演示环境暂无修改权限，如需修改数据可本地搭建后台服务！";
        }
        return CommonResult.failed(message);
    }

    /**
     * 处理所有SQL异常（包括约束违反等）
     */
    @ResponseBody
    @ExceptionHandler(value = SQLException.class)
    public CommonResult<Void> handleSQLException(SQLException e) {
        log.error("SQL异常: {}", e.getMessage(), e);
        String message = StrUtil.emptyToDefault(e.getMessage(), "数据库操作异常");
        // 处理MySQL约束违反（如唯一键冲突）
        if (message.contains("Duplicate entry") || message.contains("foreign key constraint")) {
            message = "数据重复或关联约束冲突，请检查数据后重试";
        }
        return CommonResult.businessFailed(message);
    }

    /**
     * 处理空指针异常
     */
    @ResponseBody
    @ExceptionHandler(value = NullPointerException.class)
    public CommonResult<Void> handleNullPointerException(NullPointerException e) {
        String message = StrUtil.emptyToDefault(e.getMessage(), "空指针异常");
        log.error("空指针异常: {}", message, e);
        return CommonResult.nullPointerFailed(message);
    }

    /**
     * 处理运行时异常
     */
    @ResponseBody
    @ExceptionHandler(value = RuntimeException.class)
    public CommonResult<Void> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常: {}", e.getMessage(), e);
        return CommonResult.businessFailed(e.getMessage());
    }

    /**
     * 处理所有未捕获的异常（兜底处理）
     */
    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public CommonResult<Void> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return CommonResult.systemFailed("系统异常，请稍后重试");
    }

    /**
     * 优化参数校验异常处理，返回所有字段错误
     */
    @ResponseBody
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public CommonResult<Void> handleValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        String message = bindingResult.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return CommonResult.validateFailed(message);
    }

    /**
     * 优化参数绑定异常处理，返回所有字段错误
     */
    @ResponseBody
    @ExceptionHandler(value = BindException.class)
    public CommonResult<Void> handleBindException(BindException e) {
        BindingResult bindingResult = e.getBindingResult();
        String message = bindingResult.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return CommonResult.validateFailed(message);
    }
}