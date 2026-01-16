package com.wiley.luo.springcloudclientdemo.exception;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BlockException.class)
    public ResponseEntity<String> handleBlockException(BlockException e) {
        return ResponseEntity.status(429)
                .body("服务限流/降级中，请稍后重试");
    }
}
