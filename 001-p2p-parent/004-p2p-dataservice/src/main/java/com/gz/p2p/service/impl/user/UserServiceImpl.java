package com.gz.p2p.service.impl.user;

import com.alibaba.dubbo.config.annotation.Service;
import com.gz.p2p.cons.Constants;
import com.gz.p2p.mapper.user.FinanceAccountMapper;
import com.gz.p2p.mapper.user.UserMapper;
import com.gz.p2p.model.user.FinanceAccount;
import com.gz.p2p.model.user.User;
import com.gz.p2p.service.user.UserService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@Service(interfaceClass = UserService.class,version = "1.0.0",timeout = 50000)
public class UserServiceImpl implements UserService {
    @Resource
    private RedisTemplate<Object,Object> redisTemplate;

    @Resource
    private FinanceAccountMapper financeAccountMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    public Long queryAllUserCount() {

        Long allUserCount = (Long)redisTemplate.opsForValue().get(Constants.ALL_USER_COUNT);
        if (ObjectUtils.allNull(allUserCount)) {
            synchronized (this){
                allUserCount = (Long)redisTemplate.opsForValue().get(Constants.ALL_USER_COUNT);
                if (ObjectUtils.allNull(allUserCount)) {
                    allUserCount = userMapper.selectAllUserCount();
                    redisTemplate.opsForValue().set(Constants.ALL_USER_COUNT, allUserCount,2, TimeUnit.MINUTES);
                }
            }
        }
        return allUserCount;
    }

    @Override
    public User queryUserByPhone(String phone) {
        return userMapper.selectUserByPhone(phone);
    }

    @Transactional
    @Override
    public User register(String phone, String loginPassword) throws Exception {
        User user = new User();
        user.setAddTime(new Date());
        user.setPhone(phone);
        user.setLoginPassword(loginPassword);
        user.setLastLoginTime(new Date());
        int userRows = userMapper.insertSelective(user);
        if (userRows <= 0) {
            throw new Exception("新增账户失败");
        }
        FinanceAccount financeAccount = new FinanceAccount();
        financeAccount.setUid(user.getId());
        financeAccount.setAvailableMoney(888.0);
        int faRows = financeAccountMapper.insert(financeAccount);
        if (faRows <= 0) {
            throw new Exception("开设账户失败");
        }
        return user;
    }

    @Override
    public int modifyUserById(User user) {
        return userMapper.updateByPrimaryKeySelective(user);
    }

    @Override
    public User queryUserByPhoneAndPassword(Map<String, Object> param) {
        User user  = userMapper.selectUserByPhoneAndPassword(param);
        return user;
    }
}
