package chain.fxgj.server.payroll.service.impl;

import chain.css.exception.ParamsIllegalException;
import chain.fxgj.server.payroll.constant.ErrorConstant;
import chain.fxgj.server.payroll.dto.handpassword.HandPasswordDTO;
import chain.fxgj.server.payroll.service.AdminService;
import chain.payroll.client.feign.EmployeeWechatDeignController;
import chain.payroll.dto.request.wechat.EmployeeWechatSaveReq;
import chain.payroll.dto.response.wechat.EmployeeWechatDTO;
import chain.utils.commons.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * @Description:
 * @Author: du
 * @Date: 2020/8/11 20:48
 */
@Service
@Slf4j
public class AdminServiceImpl implements AdminService {

    @Autowired
    EmployeeWechatDeignController employeeWechatDeignController;

    @Override
    public HandPasswordDTO queryHandPassword(String wechatId) {
        Optional.ofNullable(wechatId).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("‘用户微信绑定ID’参数不能为空")));

        EmployeeWechatDTO dto = employeeWechatDeignController.findById(wechatId);

        if (null == dto) {
            log.info("=====> wechatId:{} 用户微信绑定信息不存在!", wechatId);
            throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("用户微信绑定信息不存在"));
        }
        HandPasswordDTO handPasswordDTO = HandPasswordDTO.builder()
                .status(0)
                .statusVal("未开启")
                .build();
        if (StringUtils.isNotBlank(dto.getHeadimgurl())) {
            handPasswordDTO.setStatus(1);
            handPasswordDTO.setStatusVal("已开启");
        }
        return handPasswordDTO;
    }

    @Override
    public EmployeeWechatDTO checkPassword(String wechatId, String password, String type) {
        Optional.ofNullable(password).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("‘数字密码’参数不能为空")));
        Optional.ofNullable(wechatId).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("‘用户微信绑定ID’参数不能为空")));
        Optional.ofNullable(type).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("‘密码类型’参数不能为空")));

        EmployeeWechatDTO dto = employeeWechatDeignController.findById(wechatId);
        if (null == dto) {
            log.info("=====> wechatId:{} 用户微信绑定信息不存在!", wechatId);
            throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("用户微信绑定信息不存在"));
        }

        if ("0".equals(type)) {
            //数字密码校验
            if (!password.equals(dto.getQueryPwd())) {
                log.info("=====> wechatId:{} 用户数字密码校验失败：queryPwd：{}，password：{}", wechatId, password);
                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("数字密码校验失败"));
            }
        } else if ("1".equals(type)) {
            //手势密码校验
            if (!password.equals(dto.getHandPassword())) {
                log.info("=====> wechatId:{} 用户手密码校验失败：queryPwd：{}，password：{}", wechatId, password);
                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("手势密码校验失败"));
            }
        } else {
            log.info("=====> 校验失败，密码类型异常(0：数字密码  1：手势密码) type = {}", type);
            throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("校验失败，密码类型异常"));
        }
        return dto;
    }

    @Override
    public EmployeeWechatDTO savePassword(String wechatId, String password, String type) {
        Optional.ofNullable(wechatId).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("‘用户微信绑定ID’参数不能为空")));
        Optional.ofNullable(password).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("‘确认密码’参数不能为空")));

        EmployeeWechatDTO dto = employeeWechatDeignController.findById(wechatId);
        if (null == dto) {
            log.info("=====> wechatId:{} 用户微信绑定信息不存在!", wechatId);
            throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("用户微信绑定信息不存在"));
        }

        EmployeeWechatSaveReq saveReq = EmployeeWechatSaveReq.builder()
                .updDateTime(LocalDateTime.now())
                .wechatId(dto.getWechatId())
                .build();

        if ("0".equals(type)) {
            //数字密码校验
            saveReq.setQueryPwd(password);
        } else if ("1".equals(type)) {
            //手势密码校验
            saveReq.setHandPassword(password);
        } else {
            log.info("=====> 校验失败，密码类型异常(0：数字密码  1：手势密码) type = {}", type);
            throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("校验失败，密码类型异常"));
        }
        return employeeWechatDeignController.save(saveReq);
    }

    @Override
    public EmployeeWechatDTO closeHandPassword(String wechatId) {
        Optional.ofNullable(wechatId).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("‘用户微信绑定ID’参数不能为空")));

        EmployeeWechatDTO dto = employeeWechatDeignController.findById(wechatId);
        if (null == dto) {
            log.info("=====> wechatId:{} 用户微信绑定信息不存在!", wechatId);
            throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("用户微信绑定信息不存在"));
        }

        EmployeeWechatSaveReq saveReq = EmployeeWechatSaveReq.builder()
                .updDateTime(LocalDateTime.now())
                .wechatId(dto.getWechatId())
                .handPassword("")
                .build();
        return employeeWechatDeignController.save(saveReq);
    }
}
