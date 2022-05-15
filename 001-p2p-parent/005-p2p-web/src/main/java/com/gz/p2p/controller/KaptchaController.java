package com.gz.p2p.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.gz.p2p.cons.Constants;
import com.gz.p2p.service.user.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

/**
 * @Auther: 翟文海
 * @Date: 2022/5/14/014 12:24
 * @Description:
 */
@Controller
public class KaptchaController {

    @Autowired
    private DefaultKaptcha captchaProducer;

    @Reference(interfaceClass = RedisService.class,version = "1.0.0",timeout = 15000)
    private RedisService redisService;

    @GetMapping("/jcaptcha/captcha")
    public void defaultKaptcha(
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) throws Exception {

        byte[] captchaOutputStream;
        ByteArrayOutputStream imgOutputStream = new ByteArrayOutputStream();
        try {
            //生产验证码字符串并保存到redis中
            String verifyCode = captchaProducer.createText();
            redisService.put(Constants.VERIFY_CODE,verifyCode);
            BufferedImage challenge = captchaProducer.createImage(verifyCode);

            ImageIO.write(challenge, "jpg", imgOutputStream);
        } catch (IllegalArgumentException e) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        captchaOutputStream = imgOutputStream.toByteArray();
        httpServletResponse.setHeader("Cache-Control", "no-store");
        httpServletResponse.setHeader("Pragma", "no-cache");
        httpServletResponse.setDateHeader("Expires", 0);
        httpServletResponse.setContentType("image/jpeg");
        ServletOutputStream responseOutputStream = httpServletResponse.getOutputStream();
        responseOutputStream.write(captchaOutputStream);
        responseOutputStream.flush();
        responseOutputStream.close();
    }
}