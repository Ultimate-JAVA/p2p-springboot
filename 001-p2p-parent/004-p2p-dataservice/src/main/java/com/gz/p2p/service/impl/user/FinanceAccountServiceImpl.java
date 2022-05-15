package com.gz.p2p.service.impl.user;

import com.alibaba.dubbo.config.annotation.Service;
import com.gz.p2p.mapper.user.FinanceAccountMapper;
import com.gz.p2p.model.user.FinanceAccount;
import com.gz.p2p.service.user.FinanceAccountService;
import jdk.nashorn.internal.ir.annotations.Reference;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Auther: 翟文海
 * @Date: 2022/5/14/014 11:07
 * @Description:
 */
@Component
@Service(interfaceClass = FinanceAccountService.class, timeout = 1500, version = "1.0.0")
public class FinanceAccountServiceImpl implements FinanceAccountService {
    @Resource
    private FinanceAccountMapper financeAccountMapper;


    @Override
    public FinanceAccount queryFinanceAccountByUid(Integer uId) {
        return financeAccountMapper.selectByUid(uId);
    }
}
