package com.gz.p2p.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSONObject;
import com.gz.p2p.cons.Constants;
import com.gz.p2p.model.user.FinanceAccount;
import com.gz.p2p.model.user.User;
import com.gz.p2p.service.loan.BidInfoService;
import com.gz.p2p.service.loan.LoanInfoService;
import com.gz.p2p.service.user.FinanceAccountService;
import com.gz.p2p.service.user.RedisService;
import com.gz.p2p.service.user.UserService;
import com.gz.p2p.utils.Result;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @Auther: 翟文海
 * @Date: 2022/5/12/012 19:28
 * @Description:
 */
@Controller
public class UserController {

    @Reference(interfaceClass = UserService.class,version = "1.0.0",timeout = 15000)
    private UserService userService;

    @Reference(interfaceClass = RedisService.class,version = "1.0.0",timeout = 15000)
    private RedisService redisService;

    @Reference(interfaceClass = FinanceAccountService.class,version = "1.0.0",timeout = 15000)
    private FinanceAccountService financeAccountService;

    @Reference(interfaceClass = LoanInfoService.class,version = "1.0.0",timeout = 15000)
    private LoanInfoService loanInfoService;

    @Reference(interfaceClass = BidInfoService.class,version = "1.0.0",timeout = 15000)
    private BidInfoService bidInfoService;


    @RequestMapping("/loan/page/register")
    public String toRegister(){
        return "register";
    }

    @RequestMapping("/loan/checkPhone")
    @ResponseBody
    public Result checkPhone(@RequestParam("phone")String phone){
        User user = userService.queryUserByPhone(phone);
        if (ObjectUtils.allNotNull(user)) {
            return Result.resultFail("手机号已被注册过了");
        } else {
            return Result.resultSuccess();
        }
    }

    @RequestMapping("/loan/register")
    @ResponseBody
    public Result register(HttpServletRequest request,
                           @RequestParam("phone")String phone,
                           @RequestParam("loginPassword")String loginPassword,
                           @RequestParam("messageCode")String messageCode){
        try {
            String redisCode = (String) redisService.get(phone);
            if (!StringUtils.equals(redisCode, messageCode)) {
                return Result.resultCodeFail("请输入正确的验证码");
            }
//            如果Session不存在就创建一个新的
            User user = userService.register(phone, loginPassword);
            request.getSession(true).setAttribute(Constants.SESSION_USER, user);
            return Result.resultSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.resultFail("系统忙，请稍后...");
        }
    }

    @RequestMapping("/user/messageCode")
    @ResponseBody
    public Result sendMessageCode(@RequestParam("phone")String phone) {
        String url = "https://way.jd.com/kaixintong/kaixintong";
        String randNum = getRandNum(6);
        Map<String,Object> param = new HashMap<>();
        param.put("appkey","cd78f38a0c05e5ea52cc19d3eee04554");
        param.put("mobile",phone);
        param.put("content", "【凯信通】您的验证码是：" + randNum);
        try {
            //String json = HttpClientUtils.doGet(url, param);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.resultFail("系统忙，请稍后...");
        }
        String json =
                "{\n" +
                        "    \"code\": \"10000\",\n" +
                        "    \"charge\": false,\n" +
                        "    \"remain\": 0,\n" +
                        "    \"msg\": \"查询成功\",\n" +
                        "    \"result\": \"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" ?><returnsms>\\n <returnstatus>Success</returnstatus>\\n <message>ok</message>\\n <remainpoint>-1111611</remainpoint>\\n <taskID>101609164</taskID>\\n <successCounts>1</successCounts></returnsms>\"\n" +
                        "}";
        JSONObject jsonObject =JSONObject.parseObject(json);
        String code = jsonObject.getString("code");

        if (!StringUtils.equals(code, "10000")) {
            return Result.resultCodeFail("验证码发送失败");
        }
        String resultXml = jsonObject.getString("result");
        try {
            Document document = DocumentHelper.parseText(resultXml);
            Node node = document.selectSingleNode("/returnsms/returnstatus[1]");
            String returnstatus = node.getText();
            if (!StringUtils.equals(returnstatus, "Success")) {
                return Result.resultCodeFail("验证码发送失败");
            }
        } catch (DocumentException e) {
            e.printStackTrace();
            return Result.resultCodeFail("验证码发送错误");
        }
        //保存到redis里面
        redisService.put(phone,randNum);
        return Result.resultSuccess(randNum);

    }

    /**
     * 跳转到实名认证页面
     * /代表根路径也就是p2p
     * @return
     */
    @RequestMapping("/loan/page/realName")
    public String toRealName(){
        return "realName";
    }


    @RequestMapping("/loan/realName")
    @ResponseBody
    public Result realName(HttpServletRequest request,@RequestParam("phone") String phone,
                            @RequestParam("realName") String realName,
                           @RequestParam("idCard") String idCard,
                           @RequestParam("messageCode") String messageCode) {
        String url = "https://way.jd.com/youhuoBeijing/test";
        Map<String, String> param = new HashMap<>();
        param.put("appkey", "cd78f38a0c05e5ea52cc19d3eee04554");
        param.put("cardNo", idCard);
        param.put("realName", realName);

        try {
            //查询验证码是否正确
            String redisCode = (String) redisService.get(phone);
            if (!StringUtils.equals(redisCode, messageCode)) {
                return Result.resultCodeFail("验证码错误");
            }
//            String json = HttpClientUtils.doGet(url, param);
            String json = "{\n" +
                    "    \"code\": \"10000\",\n" +
                    "    \"charge\": false,\n" +
                    "    \"remain\": 1305,\n" +
                    "    \"msg\": \"查询成功\",\n" +
                    "    \"result\": {\n" +
                    "        \"error_code\": 0,\n" +
                    "        \"reason\": \"成功\",\n" +
                    "        \"result\": {\n" +
                    "            \"realname\": \"乐天磊\",\n" +
                    "            \"idcard\": \"350721197702134399\",\n" +
                    "            \"isok\": true\n" +
                    "        }\n" +
                    "    }\n" +
                    "}";
            JSONObject jsonObject = JSONObject.parseObject(json);
            String code = jsonObject.getString("code");
            if (!StringUtils.equals(code, "10000")) {
                return Result.resultFail("姓名或者身份证错误");
            }
            //                查询成功
            String outResult = jsonObject.getString("result");
            JSONObject resultObject = JSONObject.parseObject(outResult);
            String inResult = resultObject.getString("result");
            JSONObject object = JSONObject.parseObject(inResult);
            Boolean isok = object.getBoolean("isok");
            if (!isok) {
                return Result.resultFail("姓名或者身份证错误");
            }
//            修改用户的名字和身份证
            HttpSession session = request.getSession(false);
            if (session != null) {
                User user = (User) session.getAttribute(Constants.SESSION_USER);
                user.setName(realName);
                user.setIdCard(idCard);
                int rows = userService.modifyUserById(user);
                if (rows == 0) {
                    throw new Exception("系统忙，请稍后...");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.resultFail("系统忙，请稍后...");
        }

        return Result.resultSuccess();
    }

    @RequestMapping("/loan/myFinanceAccount")
    @ResponseBody
    public Double myFinanceAccount(HttpServletRequest request) {
        User user = (User) request.getSession()
                .getAttribute(Constants.SESSION_USER);
        Double availableMoney = null;
        if (user != null) {
            FinanceAccount  financeAccount= financeAccountService.queryFinanceAccountByUid(user.getId());
            availableMoney = financeAccount.getAvailableMoney();
        }
        return availableMoney;
    }
    @RequestMapping("/loan/page/login")
    public String toLogin(Model model){
        Double historyAvgRate = loanInfoService.queryHistoryAvgRate();
        Long allUserCount = userService.queryAllUserCount();
        Double allBidMoney = bidInfoService.queryAllBidMoney();

        model.addAttribute(Constants.HISTORY_AVG_RATE,historyAvgRate);
        model.addAttribute(Constants.ALL_USER_COUNT,allUserCount);
        model.addAttribute(Constants.All_BID_MONEY,allBidMoney);
        return "login";
    }

    @RequestMapping("/loan/login")
    @ResponseBody
    public Result login(HttpServletRequest request,String phone, String loginPassword, String messageCode) {
        User user = null;
        try {
            String verifyCode = (String) redisService.get(Constants.VERIFY_CODE);
            if (!StringUtils.equals(verifyCode, messageCode)) {
                return Result.resultCodeFail("验证码错误");
            }
            Map<String, Object> param = new HashMap<>();
            param.put("phone", phone);
            param.put("loginPassword", loginPassword);
            user = userService.queryUserByPhoneAndPassword(param);
            if (ObjectUtils.allNull(user)) {
                return Result.resultFail("手机号或者密码错误");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.resultFail("系统繁忙，请稍后...");
        }
        request.getSession(true).setAttribute(Constants.SESSION_USER,user);
        return Result.resultSuccess();
    }

    @RequestMapping("/loan/logout")
    public String logout(HttpServletRequest request) {
        request.getSession(true).removeAttribute(Constants.SESSION_USER);
        return "redirect:/index";
    }

    @RequestMapping("/loan/myCenter")
    public String toMyCenter(HttpServletRequest request,Model model) {
        User user = (User) request.getSession().getAttribute(Constants.SESSION_USER);
        if (ObjectUtils.allNotNull(user)) {
            Integer id = user.getId();
            FinanceAccount financeAccount = financeAccountService.queryFinanceAccountByUid(id);
            model.addAttribute(Constants.FINANCE_ACCOUNT,financeAccount);
        }

        return "myCenter";
    }

    private String getRandNum(int n) {
        Random messageCode = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < n; i++) {
            int randNum = messageCode.nextInt(9);
            sb.append(randNum);
        }
        return sb.toString();
    }

}
