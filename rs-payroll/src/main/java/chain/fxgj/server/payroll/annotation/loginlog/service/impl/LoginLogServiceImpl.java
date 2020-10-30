package chain.fxgj.server.payroll.annotation.loginlog.service.impl;

import chain.fxgj.server.payroll.annotation.loginlog.service.LoginLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@SuppressWarnings("unchecked")
public class LoginLogServiceImpl implements LoginLogService {


    @Override
    @Async
    public void saveLoginLog() {
        try {
            log.info("start:[{}]", LocalDateTime.now());
            Thread.sleep(1000*30);
            log.info("end:[{}]", LocalDateTime.now());
        } catch (InterruptedException e) {
            log.error("aop入库异常");
            e.printStackTrace();
        }
    }
}
