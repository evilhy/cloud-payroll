package chain.fxgj.server.payroll.controller;

import chain.css.exception.ParamsIllegalException;
import chain.css.log.annotation.TrackLog;
import chain.fxgj.feign.client.CustManagerFeignService;
import chain.fxgj.server.payroll.constant.ErrorConstant;
import chain.fxgj.server.payroll.service.FundService;
import chain.fxgj.server.payroll.service.WechatRedisService;
import chain.pub.common.dto.wechat.AccessTokenDTO;
import chain.pub.common.enums.WechatGroupEnum;
import chain.utils.commons.JacksonUtil;
import chain.utils.commons.StringUtils;
import chain.utils.commons.UUIDUtil;
import chain.wage.manager.core.dto.custmanager.WageManagerInfoDTO;
import chain.wisales.core.dto.promise.FundAppointmentInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

/**
 * 基金销售
 */
@RestController
@Validated
@RequestMapping(value = "/fund")
@Slf4j
@SuppressWarnings("unchecked")
public class FundController {

    @Autowired
    WechatRedisService wechatRedisService;
    @Autowired
    FundService fundService;
    @Autowired
    CustManagerFeignService custManagerFeignService;


    /**
     * 查询基金预约信息-工资条菜单使用
     * @return
     */
    @GetMapping("/appointmentInfo")
    @TrackLog
    public Mono<FundAppointmentInfoDTO> appointmentInfo(@RequestParam("code") String code,
                                                         @RequestHeader(value = "encry-salt", required = false) String salt,
                                                         @RequestHeader(value = "encry-passwd", required = false) String passwd) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            String jsessionId = UUIDUtil.createUUID32();

            //【一】根据code获取 openId、accessToken
            WechatGroupEnum wechatGroup = WechatGroupEnum.FXGJ;
            log.info("wechatGroup:[{}][{}], code:[{}]", wechatGroup.getId(), wechatGroup.getDesc(), code);
            AccessTokenDTO accessTokenDTO = wechatRedisService.oauth2AccessToken(wechatGroup, code);
            log.info("accessTokenDTO:[{}]", JacksonUtil.objectToJson(accessTokenDTO));
            String openId = accessTokenDTO.getOpenid();
            if (StringUtils.isEmpty(openId)) {
                throw new ParamsIllegalException(ErrorConstant.AUTH_ERR.getErrorMsg());
            }

            //【二】根据 openId 查询预约信息
            FundAppointmentInfoDTO fundAppointmentInfoDTO = fundService.qryFundAppointmentInfo(jsessionId, openId);
            // todo 加密
            fundAppointmentInfoDTO.setSalt(salt);
            fundAppointmentInfoDTO.setPasswd(passwd);
            log.info("fundAppointmentInfoDTO:[{}]", JacksonUtil.objectToJson(fundAppointmentInfoDTO));
            return fundAppointmentInfoDTO;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 基金预约入库-公众号页面调用
     * @return
     */
    @PostMapping("/appointmentSave")
    @TrackLog
    public Mono<FundAppointmentInfoDTO> appointmentSave(@RequestBody FundAppointmentInfoDTO fundSaveDTO) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            log.info("appointmentSave.fundSaveDTO[{}]", JacksonUtil.objectToJson(fundSaveDTO));
            String jsessionId = fundSaveDTO.getJsessionId();

            //根据 jsessionId 查询Redis预约信息
            FundAppointmentInfoDTO fundAppointmentInfoDTO = fundService.qryFunInfo(jsessionId);
            if (null == fundAppointmentInfoDTO) {
                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("页面失效，请刷新进入"));
            }
            log.info("appointmentSave.fundSaveDTO.qry[{}]", JacksonUtil.objectToJson(fundAppointmentInfoDTO));
            //1.已预约，抛异常
            if (fundAppointmentInfoDTO.getAppointmentType().equals("1")) {
                throw new ParamsIllegalException(ErrorConstant.FINANCE_001.getErrorMsg());
            }

            //2.未预约，调用唯销入库，入库完成之后，再更新缓存
            fundAppointmentInfoDTO.setName(fundSaveDTO.getName());
            fundAppointmentInfoDTO.setPhone(fundSaveDTO.getPhone());
            fundAppointmentInfoDTO.setCity(fundSaveDTO.getCity());
            fundAppointmentInfoDTO.setMoney(fundSaveDTO.getMoney());
            FundAppointmentInfoDTO fundAppointmentInfoDTORes = fundService.fundAppointmentInfoSave(jsessionId, fundSaveDTO);

            return fundAppointmentInfoDTORes;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 基金预约入库-其他页面调用
     * @return
     */
    @PostMapping("/appointmentSaveByManagerId")
    @TrackLog
    public Mono<FundAppointmentInfoDTO> appointmentSaveByManagerId(@RequestBody FundAppointmentInfoDTO fundSaveDTO) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            log.info("appointmentSave.fundSaveDTO[{}]", JacksonUtil.objectToJson(fundSaveDTO));

            String managerId = fundSaveDTO.getManagerId();
            //根据 managerId 查询经理信息
            WageManagerInfoDTO wageManagerInfoDTO = custManagerFeignService.managerInfoById(managerId);
            if (null == wageManagerInfoDTO) {
                throw new ParamsIllegalException(ErrorConstant.MANAGERINFOERR.getErrorMsg());
            }

            FundAppointmentInfoDTO fundAppointmentInfoDTO = new FundAppointmentInfoDTO();
            BeanUtils.copyProperties(fundSaveDTO, fundAppointmentInfoDTO);
            fundAppointmentInfoDTO.setBranchNo(wageManagerInfoDTO.getBranchOrgNo());
            fundAppointmentInfoDTO.setBranchName(wageManagerInfoDTO.getBranchOrgName());
            fundAppointmentInfoDTO.setSubBranchNo(wageManagerInfoDTO.getSubBranchOrgNo());
            fundAppointmentInfoDTO.setSubBranchName(wageManagerInfoDTO.getSubBranchOrgName());
            fundAppointmentInfoDTO.setManagerId(wageManagerInfoDTO.getId());
            fundAppointmentInfoDTO.setManagerName(wageManagerInfoDTO.getManagerName());
            fundAppointmentInfoDTO.setManagerPhone(wageManagerInfoDTO.getMobile());
            String jsessionId = UUIDUtil.createUUID32();
            log.info("appointmentSaveByManagerId.jsessionId:[{}]", jsessionId);
            FundAppointmentInfoDTO fundAppointmentInfoDTORes = fundService.fundAppointmentInfoSave(jsessionId, fundAppointmentInfoDTO);

            return fundAppointmentInfoDTORes;
        }).subscribeOn(Schedulers.elastic());
    }
}
