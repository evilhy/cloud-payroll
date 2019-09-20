package chain.fxgj.server.payroll.service.impl;

import chain.fxgj.feign.dto.web.WageUserPrincipal;
import chain.fxgj.server.payroll.service.EmpWechatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmpWechatServiceImpl implements EmpWechatService {

    @Override
    public WageUserPrincipal getWechatInfoDetail(String jsessionId) {
        return null;
    }
}
