package chain.fxgj.server.payroll.service.impl;

import chain.fxgj.server.payroll.service.EmpWechatService;
import core.dto.wechat.CacheUserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmpWechatServiceImpl implements EmpWechatService {

    @Override
    public CacheUserPrincipal getWechatInfoDetail(String jsessionId) {
        return null;
    }
}
