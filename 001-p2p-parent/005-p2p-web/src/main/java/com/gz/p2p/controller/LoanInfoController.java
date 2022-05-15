package com.gz.p2p.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.gz.p2p.cons.Constants;
import com.gz.p2p.model.loan.BidInfo;
import com.gz.p2p.model.loan.LoanInfo;
import com.gz.p2p.model.user.BidUserVO;
import com.gz.p2p.model.user.FinanceAccount;
import com.gz.p2p.model.user.User;
import com.gz.p2p.service.loan.BidInfoService;
import com.gz.p2p.service.loan.LoanInfoService;
import com.gz.p2p.service.user.FinanceAccountService;
import com.gz.p2p.utils.Result;
import com.gz.p2p.vo.PaginationVo;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auther: 翟文海
 * @Date: 2022/5/11/011 22:12
 * @Description: 贷款相关
 */
@Controller
public class LoanInfoController {

    @Reference(interfaceClass = LoanInfoService.class, version = "1.0.0", timeout = 15000)
    private LoanInfoService loanInfoService;

    @Reference(interfaceClass = BidInfoService.class, version = "1.0.0", timeout = 15000)
    private BidInfoService bidInfoService;

    @Reference(interfaceClass = FinanceAccountService.class, version = "1.0.0", timeout = 15000)
    private FinanceAccountService financeAccountService;

    /**
     * 功能描述: 跳转到分页查询当中
     *
     * @param: currentPage 当前页数，当前的条目数量
     * @return:
     * @auther: Dell
     * @date: 2022/5/11/011 22:27
     */
    @RequestMapping("/loan/loan")
    public String loan(HttpServletRequest request,
                       Model model,
                       @RequestParam(value = "ptype", required = false) Integer ptype,
                       @RequestParam(value = "currentPage", defaultValue = "1") Integer currentPage) {
        //每一页的展示条数
        int pageSize = 9;

        Map<String, Integer> param = new HashMap<>();
        param.put("productType", ptype);
        param.put("currentPage", (currentPage - 1) * pageSize);
        param.put("pageSize", pageSize);

        PaginationVo<LoanInfo> paginationVo = loanInfoService.queryLoanInfoListByPage(param);
        int totalPage = paginationVo.getTotalSize() / pageSize;
        int mod = paginationVo.getTotalSize() % pageSize;
        if (mod > 0) {
            totalPage = totalPage + 1;
        }
        model.addAttribute("totalPage", totalPage);//总页数
        model.addAttribute("currentPage", currentPage);//当前页
        model.addAttribute("loanInfoList", paginationVo.getDates());
        model.addAttribute("totalSize", paginationVo.getTotalSize());

        if (ObjectUtils.allNotNull(ptype)) {
            model.addAttribute("ptype", ptype);
        }
        //投资排行榜
        List<BidUserVO> bidUserLists = bidInfoService.queryBidUserTOP();
        model.addAttribute(Constants.BID_USER_LISTS,bidUserLists);

        return "loan";
    }

    @RequestMapping("/loan/loanInfo")
    public String loanInfo(HttpServletRequest request, Model model,
                           @RequestParam("id") Integer id) {
        //查询详情
        LoanInfo loanInfo = loanInfoService.queryLoanInfoById(id);
        //查询投资记录
        Map<String, Object> param = new HashMap<>();
        param.put("loanId", id);
        param.put("currentPage", 0);
        param.put("pageSize", 10);
        List<BidInfo> bidInfoList = bidInfoService.queryRecentlyBidInfoByLoanId(param);
        model.addAttribute("loanInfo", loanInfo);
        model.addAttribute("bidInfoList", bidInfoList);

//        在商品详情页里面获取自己的资金余额
        /*不存在就返回null
        * getSession(false)
        * */

        User user = (User) request.getSession().getAttribute(Constants.SESSION_USER);
        if (user != null) {
            FinanceAccount financeAccount = financeAccountService.queryFinanceAccountByUid(user.getId());
            model.addAttribute(Constants.AVAILABLE_MONEY, financeAccount.getAvailableMoney());
        }


        return "loanInfo";
    }

    @RequestMapping("/loan/invest")
    @ResponseBody
    public Result invest(HttpServletRequest request,@RequestParam("loanId") String loanId,
                         @RequestParam("bidMoney") String bidMoney) {
        try {
            Map<String,Object> param = new HashMap<>();
            param.put("loanId",loanId);
            param.put("bidMoney", bidMoney);
            User user = (User) request.getSession().getAttribute(Constants.SESSION_USER);
            Integer uid = user.getId();
            param.put("uid",uid);
            param.put("phone",user.getPhone());
//            投资
            loanInfoService.invest(param);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.resultFail("投资人数过多，请稍后重试");
        }

        return Result.resultSuccess();
    }
}
