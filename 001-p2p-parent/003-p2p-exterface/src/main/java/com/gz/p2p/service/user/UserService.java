package com.gz.p2p.service.user;

import com.gz.p2p.model.user.User;

import java.util.Map;

public interface UserService {
    Long queryAllUserCount();

    User queryUserByPhone(String phone);

    User register(String phone, String loginPassword) throws Exception;


    int modifyUserById(User user);

    User queryUserByPhoneAndPassword(Map<String, Object> param);

}
