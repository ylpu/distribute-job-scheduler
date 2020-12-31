package com.ylpu.thales.scheduler.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.ylpu.thales.scheduler.response.SchedulerResponse;
import com.ylpu.thales.scheduler.service.exception.ThalesRuntimeException;

@ControllerAdvice
@RestController
public class SchedulerControllerAdvice {

    private static final Log log = LogFactory.getLog(SchedulerControllerAdvice.class);

    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public SchedulerResponse<?> errorHandler(Exception e) {
        log.error(e);
        if (e instanceof ThalesRuntimeException) {
            ThalesRuntimeException exception = (ThalesRuntimeException) e;
            return new SchedulerResponse<>(501, exception.getMessage());
        }
        return new SchedulerResponse<>(500, "system internal error");
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public <T> SchedulerResponse<T> notValidExceptionHandler(MethodArgumentNotValidException e) throws Exception {
        SchedulerResponse<T> response = new SchedulerResponse<T>(999);
        BindingResult bindingResult = e.getBindingResult();

        if (bindingResult != null && !CollectionUtils.isEmpty(bindingResult.getAllErrors())) {
            response.setErrorMsg(e.getBindingResult().getAllErrors().get(0).getDefaultMessage());
        } else {
            response.setErrorMsg(e.getMessage());
        }
        return response;
    }
}
