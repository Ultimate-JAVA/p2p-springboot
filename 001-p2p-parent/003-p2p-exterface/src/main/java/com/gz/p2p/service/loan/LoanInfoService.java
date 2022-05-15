package com.gz.p2p.service.loan;

import com.gz.p2p.model.loan.LoanInfo;
import com.gz.p2p.vo.PaginationVo;

import java.util.List;
import java.util.Map;

public interface LoanInfoService {
    Double queryHistoryAvgRate();

    List<LoanInfo> queryLoanInfoByProductType(Map<String, Integer> param);

    PaginationVo<LoanInfo> queryLoanInfoListByPage(Map<String, Integer> param);

    LoanInfo queryLoanInfoById(Integer id);

    void invest(Map<String, Object> param) throws Exception;
}
