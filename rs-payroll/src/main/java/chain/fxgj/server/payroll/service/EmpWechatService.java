package chain.fxgj.server.payroll.service;

import core.dto.wechat.CacheUserPrincipal;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface EmpWechatService {
    /**
     * 获取微信绑定信息 WageUserPrincipal 接收
     *
     * @return
     */
    @Cacheable(cacheNames = "wechat", key = "'jsession:'.concat(#jsessionId)")
    CacheUserPrincipal getWechatInfoDetail(String jsessionId);


}
