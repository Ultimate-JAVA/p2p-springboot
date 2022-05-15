package com.gz.p2p.service.user;

import com.gz.p2p.model.user.FinanceAccount;

/**
 * @Auther: 翟文海
 * @Date: 2022/5/14/014 11:06
 * @Description:
 */
public interface FinanceAccountService {

    FinanceAccount queryFinanceAccountByUid(Integer uId);

}
