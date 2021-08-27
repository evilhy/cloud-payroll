package chain.fxgj.server.payroll.service.impl;

import chain.css.exception.ParamsIllegalException;
import chain.fxgj.server.payroll.constant.ErrorConstant;
import chain.fxgj.server.payroll.service.EmployeeWechatService;
import chain.payroll.client.feign.WechatFeignController;
import chain.utils.commons.StringUtils;
import core.dto.wechat.EmployeeWechatDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description:
 * @Author: du
 * @Date: 2021/8/23 18:47
 */
@Slf4j
@Service
public class EmployeeWechatServiceImpl implements EmployeeWechatService {

    @Autowired
    WechatFeignController wechatFeignController;

    /**
     * 获取登陆人信息
     *
     * @param jsessionId
     * @return
     */
    @Override
    public EmployeeWechatDTO findByJsessionId(String jsessionId, String userName) {
        log.info("=====> 根据JsessionId查询用户信息 jsessionId:{}", jsessionId);
        EmployeeWechatDTO dto = wechatFeignController.findByJsessionId(jsessionId);
        if (null == dto) {
            throw new ParamsIllegalException(ErrorConstant.Error0001.format("登录人"));
        }
        if (StringUtils.isBlank(dto.getName())) {
            dto.setName(userName);
        }
        return dto;

    }
}
