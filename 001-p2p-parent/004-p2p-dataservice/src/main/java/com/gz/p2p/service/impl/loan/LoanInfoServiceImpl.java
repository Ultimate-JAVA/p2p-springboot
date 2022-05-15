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
import com.gz.p2p.service.loan.LoanInfoService;
import com.gz.p2p.vo.PaginationVo;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service(interfaceClass = LoanInfoService.class, version = "1.0.0", timeout = 15000)
@Component
public class LoanInfoServiceImpl implements LoanInfoService {

    @Resource
    private LoanInfoMapper loanInfoMapper;

    @Resource
    private FinanceAccountMapper financeAccountMapper;

    @Resource
    private BidInfoMapper bidInfoMapper;

    @Resource
    private RedisTemplate<Object, Object> redisTemplate;

    @Override
    public Double queryHistoryAvgRate() {
        /*
         * 解决缓存穿透问题：
         * 方案一：在方法上加 synchronized
         *   但是有缺点，范围太大，效率太低
         * 方案二：双重验证+ synchronized同步锁
         *
         * 当其中一个线程走到（判断后同步锁）的上面，判断到redis中没有数据，然后他睡着了。
         * 其他线程也同样走到这里，就会等待其中一个线程醒来执行完，第一个执行完之后，从数据库查询到数据放到了redis中。
         * 其他线程会进入同步锁代码块，此时再去redis中get数据，就已经有数据了，因此41行if判断就不会再执行了，不会再走数据库查询了。
         * */
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        Double historyAvgRate = (Double) redisTemplate.opsForValue().get(Constants.HISTORY_AVG_RATE);
        if (ObjectUtils.allNull(historyAvgRate)) {
            synchronized (this) {
                historyAvgRate = (Double) redisTemplate.opsForValue().get(Constants.HISTORY_AVG_RATE);
                if (ObjectUtils.allNull(historyAvgRate)) {
//                    System.out.println("==从数据库中查询==");
                    historyAvgRate = loanInfoMapper.selectHistoryAvgRate();
                    redisTemplate.opsForValue().set(Constants.HISTORY_AVG_RATE, historyAvgRate,1, TimeUnit.DAYS);

                } else {
//                    System.out.println("==从redis中查询==");
                }
            }
        } else {
//            System.out.println("==从redis中查询==");
        }
        return historyAvgRate;
        /*
         * 当高并发时，有的线程可能只走到了数据库查询就睡着了，线程的执行权限被别的线程抢到了，当其他线程开始代码块时，还是从redis中找不到数据
         * 因此会去数据库再次查询，当这个线程又睡着了，被别的线程抢到了，这里的缓存就用不到，加大服务器的压力。
         * 因此会有缓存穿透的问题。
         * */
        /*Double historyAvgRate = (Double) redisTemplate.opsForValue().get(Constants.HISTORY_AVG_RATE);
        if (ObjectUtils.allNull(historyAvgRate)) {
            System.out.println("==从数据库中查询==");
            historyAvgRate = loanInfoMapper.selectHistoryAvgRate();
            redisTemplate.opsForValue().set(Constants.HISTORY_AVG_RATE,historyAvgRate);
        }else {
            System.out.println("==从redis中查询==");
        }*/
    }

    @Override
    public List<LoanInfo> queryLoanInfoByProductType(Map<String, Integer> param) {
        return loanInfoMapper.selectLoanInfoByProductType(param);
    }


    @Override
    public PaginationVo<LoanInfo> queryLoanInfoListByPage(Map<String, Integer> param) {

        Integer totalSize = loanInfoMapper.selectTotalSize(param);
        List<LoanInfo> loanInfoList = loanInfoMapper.selectLoanInfoByProductType(param);
        PaginationVo<LoanInfo> paginationVo = new PaginationVo<>();

        paginationVo.setDates(loanInfoList);
        paginationVo.setTotalSize(totalSize);
        return paginationVo;
    }

    @Override
    public LoanInfo queryLoanInfoById(Integer id) {

        return loanInfoMapper.selectByPrimaryKey(id);
    }

    @Transactional
    @Override
    public void invest(Map<String, Object> param) throws Exception {

        String a = (String) param.get("loanId");
        Integer uid = (Integer) param.get("uid");
        String phone = (String) param.get("phone");
        String d = (String) param.get("bidMoney");
        Integer loanId = Integer.valueOf(a);
        Double bidMoney = Double.valueOf(d);

//        获得版本号，使用乐观锁，防止超卖
        LoanInfo loanInfo = loanInfoMapper.selectByPrimaryKey(loanId);
        Integer version = loanInfo.getVersion();
        param.put("version",version);
//        更新剩余可投金额
        int lpmRows = loanInfoMapper.updateLeftProductMoney(param);
        if (lpmRows == 0) {
            throw new Exception("投资产品失败，更新产品剩余可投金额错误");
        }
//        更新账户余额
        int bmRows = financeAccountMapper.updateBidMoney(param);
        if (bmRows == 0) {
            throw new Exception("投资产品失败，更新账户余额错误");
        }
        BidInfo bidInfo = new BidInfo();
        bidInfo.setBidMoney(bidMoney);
        bidInfo.setBidTime(new Date());
        bidInfo.setLoanId(loanId);
        bidInfo.setUid(uid);
        bidInfo.setBidStatus(1);
        int biRows = bidInfoMapper.insert(bidInfo);
        if (biRows == 0) {
            throw new Exception("投资产品失败，添加投资记录错误");
        }
//        修改产品信息表的产品状态
        LoanInfo info = loanInfoMapper.selectByPrimaryKey(loanId);
        if (info.getLeftProductMoney()== 0) {
//            已经满标
            info.setProductStatus(1);
            int psRows = loanInfoMapper.updateByPrimaryKeySelective(info);
            if (psRows == 0) {
                throw new Exception("投资产品失败，修改投标状态错误");
            }
        }
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.opsForZSet().incrementScore(Constants.INVEST_TOP, phone, bidMoney);
    }
}
