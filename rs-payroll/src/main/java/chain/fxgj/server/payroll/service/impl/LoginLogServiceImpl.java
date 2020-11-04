package chain.fxgj.server.payroll.service.impl;

import chain.fxgj.server.payroll.service.LoginLogService;
import chain.payroll.client.feign.LoginLogFeignController;
import core.dto.request.loginlog.LoginLogDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@SuppressWarnings("unchecked")
public class LoginLogServiceImpl implements LoginLogService {

    @Autowired
    LoginLogFeignController loginLogFeignController;

    @Override
    @Async
    public void saveLoginLog(String openId, String methodName) {
        try {
            log.info("saveLoginLog.openId:[{}],methodName:[{}], start:[{}]", openId, methodName, LocalDateTime.now());
            LoginLogDTO loginLogDTO = LoginLogDTO.builder()
                    .openId(openId)
                    .methodName(methodName)
                    .build();
            loginLogFeignController.save(loginLogDTO);
            log.info("saveLoginLog.end:[{}]", LocalDateTime.now());
        } catch (Exception e) {
            log.error("登录日志入库异常");
        }
    }
}
