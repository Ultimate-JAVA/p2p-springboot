package com.gz.p2p.utils;

import com.sun.org.apache.regexp.internal.RE;

import java.util.HashMap;

/**
 * @Auther: 翟文海
 * @Date: 2022/5/12/012 20:08
 * @Description:
 */
public class Result extends HashMap<String, Object> {
    public static Result resultSuccess(){
        Result result = new Result();
        result.put("code", 1);
        result.put("message","");
        result.put("success", true);
        return result;

    }
    public static Result resultSuccess(String messageCode){
        Result result = new Result();
        result.put("code", 1);
        result.put("message","");
        result.put("messageCode",messageCode);
        result.put("success", true);
        return result;

    }
    public static Result resultFail(String message){
        Result result = new Result();
        result.put("code", -1);
        result.put("message",message);
        result.put("success", false);
        return result;

    }
    public static Result resultCodeFail(String message){
        Result result = new Result();
        result.put("code", -2);
        result.put("message",message);
        result.put("success", false);
        return result;

    }
}
