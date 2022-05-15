package com.gz.p2p.service.impl.loan;

import com.alibaba.dubbo.config.annotation.Service;
import com.gz.p2p.cons.Constants;
import com.gz.p2p.mapper.loan.BidInfoMapper;
import com.gz.p2p.model.loan.BidInfo;
import com.gz.p2p.model.user.BidUserVO;
import com.gz.p2p.service.loan.BidInfoService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
@Service(interfaceClass = BidInfoService.class,timeout = 15000,version = "1.0.0")
public class BidInfoServiceImpl implements BidInfoService {

    @Resource
    private RedisTemplate<Object, Object> redisTemplate;
    @Resource
    private BidInfoMapper bidInfoMapper;


    @Override
    public Double queryAllBidMoney() {

        Double allBidMoney = (Double) redisTemplate.opsForValue().get(Constants.All_BID_MONEY);
        if (ObjectUtils.allNull(allBidMoney)) {
            synchronized (this) {
                allBidMoney = (Double) redisTemplate.opsForValue().get(Constants.All_BID_MONEY);
                if (ObjectUtils.allNull(allBidMoney)) {
                    allBidMoney = bidInfoMapper.selectAllBidMoney();
                    redisTemplate.opsForValue().set(Constants.All_BID_MONEY, allBidMoney,1, TimeUnit.DAYS);
                }
            }
        }
        return allBidMoney;
    }

    @Override
    public List<BidInfo> queryRecentlyBidInfoByLoanId(Map<String, Object> param) {
        return bidInfoMapper.selectBidInfoById(param);
    }

    @Override
    public List<BidUserVO> queryBidUserTOP() {
        List<BidUserVO> bidUserVOS = new ArrayList<>();
        Set<ZSetOperations.TypedTuple<Object>> typedTuples = redisTemplate.opsForZSet().reverseRangeWithScores(Constants.INVEST_TOP, 0, 5);
        Iterator<ZSetOperations.TypedTuple<Object>> iterator = typedTuples.iterator();
        BidUserVO bidUserVO ;
        while (iterator.hasNext()){
            ZSetOperations.TypedTuple<Object> next = iterator.next();
            bidUserVO = new BidUserVO();
            String phone = (String) next.getValue();
            bidUserVO.setPhone(phone);
            Double score = next.getScore();
            bidUserVO.setBidMoney(score);
            bidUserVOS.add(bidUserVO);
        }
        return bidUserVOS;
    }
}
