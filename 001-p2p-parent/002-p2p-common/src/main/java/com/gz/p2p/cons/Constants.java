package com.gz.p2p.cons;

public class Constants {
    public final static String HISTORY_AVG_RATE = "historyAvgRate";
    public static final String ALL_USER_COUNT = "allUserCount";
    public static final String All_BID_MONEY = "allBidMoney";
    public static final String LOAN_INFO_LIST_X = "loanInfoListX";
    public static final String LOAN_INFO_LIST_Y = "loanInfoListY";
    public static final String LOAN_INFO_LIST_S = "loanInfoListS";
    //新手宝
    public static final int LOAN_INFO_X = 0;
    //优选
    public static final int LOAN_INFO_Y = 1;
    //散标
    public static final int LOAN_INFO_S = 2;
    public static final String SESSION_USER = "sessionUser";
    public static final String VERIFY_CODE = "verifyCode";
    public static final String AVAILABLE_MONEY = "availableMoney";
    public static final String FINANCE_ACCOUNT = "financeAccount";
    public static final String INVEST_TOP = "investTop";
    public static final String BID_USER_LISTS = "bidUserLists";

    //产品未投满标
    public static final int PRODUCT_STATUS_NOT_FULL = 0;
    //产品已满标
    public static final int PRODUCT_STATUS_FULL = 1;
    //产品已满标并且生成了收益记录
    public static final int PRODUCT_STATUS_FULL_PLAN = 2;
    //收益未返还
    public static final Integer INCOME_STATUS_NOT_FULL = 0;
    //收益已返还
    public static final Integer INCOME_STATUS_FULL = 1;


    public Constants() {
    }
}
