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

    @Override
    public CacheUserPrincipal upWechatInfoDetail(String jsessionId, String entId, CacheUserPrincipal sourcePrincipal) {
        CacheUserPrincipal cacheUserPrincipal = sourcePrincipal;
        cacheUserPrincipal.setEntId(entId);
        log.info("缓存jsessionId:[{}]中，entId已更新为:[{}]", jsessionId, entId);
        return cacheUserPrincipal;
    }
}
