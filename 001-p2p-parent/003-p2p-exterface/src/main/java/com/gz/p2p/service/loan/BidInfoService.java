package com.gz.p2p.service.loan;

import com.gz.p2p.model.loan.BidInfo;
import com.gz.p2p.model.user.BidUserVO;

import java.util.List;
import java.util.Map;

public interface BidInfoService {
    Double queryAllBidMoney();

    List<BidInfo> queryRecentlyBidInfoByLoanId(Map<String, Object> param);

    List<BidUserVO> queryBidUserTOP();
}
