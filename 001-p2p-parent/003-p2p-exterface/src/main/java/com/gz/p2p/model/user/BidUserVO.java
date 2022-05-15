package com.gz.p2p.model.user;

import java.io.Serializable;

/**
 * @Auther: 翟文海
 * @Date: 2022/5/15/015 12:15
 * @Description:
 */
public class BidUserVO implements Serializable {
    private static final long serialVersionUID = 2514415690900065946L;
    private String phone;
    private Double bidMoney;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Double getBidMoney() {
        return bidMoney;
    }

    public void setBidMoney(Double bidMoney) {
        this.bidMoney = bidMoney;
    }
}
