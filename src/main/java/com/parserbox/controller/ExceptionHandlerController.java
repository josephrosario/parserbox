package com.parserbox.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.json.simple.JSONObject;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
@RestController
public class ExceptionHandlerController extends ResponseEntityExceptionHandler{


    private Log log = LogFactory.getLog(this.getClass());

    @ExceptionHandler(MultipartException.class)
    @ResponseBody
    JSONObject handleFileException(HttpServletRequest request, Throwable ex) {
        log.info(ex);
        JSONObject json = new JSONObject();
        json.put("success", false);
        Throwable e = ex;

        for (int i = 0; i < 5; i++) {
            if (e.getCause() == null) break;
            else e = e.getCause();
        }
        json.put("error",  e.getMessage());
        return json;
    }

}
