package com.gz.p2p.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.gz.p2p.cons.Constants;
import com.gz.p2p.model.loan.LoanInfo;
import com.gz.p2p.service.loan.BidInfoService;
import com.gz.p2p.service.loan.LoanInfoService;
import com.gz.p2p.service.user.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class IndexController {
    @Reference(interfaceClass = LoanInfoService.class,version = "1.0.0",timeout = 50000)
    private LoanInfoService loanInfoService;

    @Reference(interfaceClass = UserService.class,version = "1.0.0",timeout = 50000)
    private UserService userService;

    @Reference(interfaceClass = BidInfoService.class,version = "1.0.0",timeout = 50000)
    private BidInfoService bidInfoService;
    @RequestMapping("/")
    public String toIndex() {
        return "toIndex";
    }


    @RequestMapping("/index")
    public String index(Model model) {

        /*模拟高并发环境
        ExecutorService executorService = Executors.newFixedThreadPool(1000);
        for (int i = 0; i < 1000; i++) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    Double historyAvgRate = loanInfoService.queryHistoryAvgRate();
                    model.addAttribute(Constants.HISTORY_AVG_RATE,historyAvgRate);
                }
            });
        }
        executorService.shutdown();*/

//        历史年化收益率
        Double historyAvgRate = loanInfoService.queryHistoryAvgRate();
        model.addAttribute(Constants.HISTORY_AVG_RATE, historyAvgRate);

//        平台用户总数量
        Long allUserCount = userService.queryAllUserCount();
        model.addAttribute(Constants.ALL_USER_COUNT,allUserCount);

//        平台累计成交额
        Double allBidMoney = bidInfoService.queryAllBidMoney();
        model.addAttribute(Constants.All_BID_MONEY, allBidMoney);

//        查询新手宝信息
        Map<String, Integer> param = new HashMap<>();
        param.put("productType", Constants.LOAN_INFO_X);
        param.put("currentPage", 0);
        param.put("pageSize",1);

        List<LoanInfo> loanInfoListX = loanInfoService.queryLoanInfoByProductType(param);
        model.addAttribute(Constants.LOAN_INFO_LIST_X, loanInfoListX);

        param.put("productType", Constants.LOAN_INFO_Y);
        param.put("pageSize",4);
//        查询优选产品
        List<LoanInfo> loanInfoListY = loanInfoService.queryLoanInfoByProductType(param);
        model.addAttribute(Constants.LOAN_INFO_LIST_Y, loanInfoListY);

        param.put("productType", Constants.LOAN_INFO_S);
        param.put("pageSize",8);
//        查询散标产品
        List<LoanInfo> loanInfoListS = loanInfoService.queryLoanInfoByProductType(param);
        model.addAttribute(Constants.LOAN_INFO_LIST_S, loanInfoListS);

        return "index";
    }
}
