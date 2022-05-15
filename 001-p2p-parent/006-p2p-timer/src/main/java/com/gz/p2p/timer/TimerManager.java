package com.gz.p2p.timer;

import com.alibaba.dubbo.config.annotation.Reference;
import com.gz.p2p.model.loan.IncomeRecord;
import com.gz.p2p.service.loan.IncomeRecordService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @Auther: 翟文海
 * @Date: 2022/5/15/015 17:35
 * @Description:
 */
@Component
public class TimerManager {

    @Reference(interfaceClass = IncomeRecordService.class,timeout = 15000,version = "1.0.0")
    private IncomeRecordService incomeRecordService;


    @Scheduled(cron = "0/10 * * * * *")
    public void generateIncomePlan() {
        System.out.println("生成收益计划开始===");

        try {
            incomeRecordService.generateIncomePlan();
        } catch (Exception e) {
            e.printStackTrace();
        }


        System.out.println("生成收益计划结束===");
    }

    /**
     *返还收益
     */
    @Scheduled(cron = "0/5 * * * * *")
    public void generateIncomeBack() {

        System.out.println("生成收益计划开始===");
        try {
            incomeRecordService.generateIncomeBack();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("生成收益计划结束===");
    }

}
