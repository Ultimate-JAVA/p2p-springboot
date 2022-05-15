package com.gz.p2p.service.impl.loan;

import com.alibaba.dubbo.config.annotation.Service;
import com.gz.p2p.cons.Constants;
import com.gz.p2p.mapper.loan.BidInfoMapper;
import com.gz.p2p.mapper.loan.IncomeRecordMapper;
import com.gz.p2p.mapper.loan.LoanInfoMapper;
import com.gz.p2p.mapper.user.FinanceAccountMapper;
import com.gz.p2p.model.loan.BidInfo;
import com.gz.p2p.model.loan.IncomeRecord;
import com.gz.p2p.model.loan.LoanInfo;
import com.gz.p2p.model.user.FinanceAccount;
import com.gz.p2p.service.loan.IncomeRecordService;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auther: 翟文海
 * @Date: 2022/5/15/015 17:41
 * @Description:
 */
@Component
@Service(interfaceClass = IncomeRecordService.class, timeout = 15000, version = "1.0.0")
public class IncomeRecordServiceImpl implements IncomeRecordService {
    @Resource
    private LoanInfoMapper loanInfoMapper;

    @Resource
    private BidInfoMapper bidInfoMapper;

    @Resource
    private IncomeRecordMapper incomeRecordMapper;

    @Resource
    private FinanceAccountMapper financeAccountMapper;


    @Transactional
    @Override
    public void generateIncomePlan() throws Exception {
        //TODO
        //查出已经满标的产品信息 1
        List<LoanInfo> loanInfoList = loanInfoMapper.selectLoanInfoByProductStatus(Constants.PRODUCT_STATUS_FULL);
        if (loanInfoList != null) {
            for (LoanInfo loanInfo : loanInfoList) {
                //投资产品信息记录
                List<BidInfo> bidInfoList = bidInfoMapper.selectBidInfoByLoanInfoId(loanInfo.getId());
                if (bidInfoList!=null){
                    for (BidInfo bidInfo : bidInfoList) {
                        IncomeRecord incomeRecord = new IncomeRecord();
                        incomeRecord.setUid(bidInfo.getUid());
                        incomeRecord.setLoanId(bidInfo.getLoanId());
                        incomeRecord.setBidId(bidInfo.getId());
                        incomeRecord.setBidMoney(bidInfo.getBidMoney());
                        //收益时间
                        Date incomeDate = null;
                        //收益金额
                        Double incomeMoney = null;
                        if (loanInfo.getProductType() == 0) {
                            //新手宝 是按照天计算
                            incomeDate = DateUtils.addDays(loanInfo.getProductFullTime(), loanInfo.getCycle());
                            incomeMoney = bidInfo.getBidMoney() * (loanInfo.getRate() / 100 / 365) * loanInfo.getCycle();
                        }else {
                            //其他产品 按照月计算
                            incomeDate = DateUtils.addMonths(loanInfo.getProductFullTime(), loanInfo.getCycle());
                            incomeMoney = bidInfo.getBidMoney() * (loanInfo.getRate() / 100 / 365) * loanInfo.getCycle()*30;
                        }
                        incomeMoney = Double.valueOf(Math.round(incomeMoney*Math.pow(10,2)/Math.pow(10,2)));
                        incomeRecord.setIncomeDate(incomeDate);
                        incomeRecord.setIncomeMoney(incomeMoney);
                        //收益状态
                        incomeRecord.setIncomeStatus(Constants.INCOME_STATUS_NOT_FULL); //0
                        int irRows = incomeRecordMapper.insertSelective(incomeRecord);
                        if (irRows == 0) {
                            throw new Exception("生成收益记录失败");
                        }
                        //满标并且生成了收益计划
                        loanInfo.setProductStatus(Constants.PRODUCT_STATUS_FULL_PLAN);
                        int liRows = loanInfoMapper.updateByPrimaryKeySelective(loanInfo);
                        if (liRows == 0) {
                            throw new Exception("生成收益记录，修改状态失败");
                        }
                    }
                }
            }
        }
    }
    @Transactional
    @Override
    public void generateIncomeBack() throws Exception {
        //满标了没有未收到收益的用户
        List<IncomeRecord> incomeRecordList = incomeRecordMapper.selectIncomeRecordByIncomeStatusAndCruDate(Constants.INCOME_STATUS_NOT_FULL);
        if (incomeRecordList != null) {
            for (IncomeRecord incomeRecord : incomeRecordList) {
                Map<String,Object> param = new HashMap<>();
                param.put("uid",incomeRecord.getUid());
                param.put("bidMoney",incomeRecord.getBidMoney());
                param.put("incomeMoney", incomeRecord.getIncomeMoney());

                //返还金额=投资金额+收益金额
                int faRows = financeAccountMapper.updateByUid(param);
                if (faRows == 0) {
                    throw new Exception("返还收益失败，更新账户表错误");
                }
                incomeRecord.setIncomeStatus(Constants.INCOME_STATUS_FULL);
                int irRows = incomeRecordMapper.updateByPrimaryKeySelective(incomeRecord);
                if (irRows == 0) {
                    throw new Exception("返还收益失败，更新收益状态错误");
                }
            }
        }

    }
}
