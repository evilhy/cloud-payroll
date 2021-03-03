package chain.fxgj.server.payroll.controller;


import chain.css.log.annotation.TrackLog;
import chain.fxgj.server.payroll.config.properties.MerchantsProperties;
import chain.fxgj.server.payroll.dto.nj.NjReqDTO;
import chain.fxgj.server.payroll.dto.nj.NjRes100705;
import chain.fxgj.server.payroll.service.EmployeeEncrytorService;
import chain.fxgj.server.payroll.service.sm4.TokenSignService;
import chain.fxgj.server.payroll.util.nj.SM4Util;
import chain.payroll.client.feign.EmployeeWechatFeignController;
import chain.payroll.client.feign.MerchantFeignController;
import chain.payroll.client.feign.NjFeignController;
import chain.utils.commons.JacksonUtil;
import chain.utils.fxgj.constant.DictEnums.AppPartnerEnum;
import chain.utils.fxgj.constant.DictEnums.RegisterTypeEnum;
import core.dto.response.ent.EntIdGroupIdDTO;
import core.dto.response.ent.EntIdGroupIdReqDTO;
import core.dto.response.merchant.CacheEmployeeWechatInfoDTO;
import core.dto.response.merchant.CacheRes100705;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 南京工资条  对外输出接口
 */
@RestController
@Validated
@RequestMapping(value = "/nj")
@Slf4j
@SuppressWarnings("unchecked")
public class NjController {

    @Autowired
    MerchantsProperties merchantProperties;
    @Autowired
    EmployeeEncrytorService employeeEncrytorService;
    @Resource
    RedisTemplate redisTemplate;
    @Autowired
    MerchantFeignController merchantFeignController;
    @Autowired
    EmployeeWechatFeignController employeeWechatFeignController;
    @Autowired
    NjFeignController njFeignController;
    @Autowired
    TokenSignService tokenSignService;


    /**
     * 访问凭证 Get
     */
    @GetMapping("/callback")
    @TrackLog
    public Mono<NjRes100705> wxCallback(@RequestParam String accessToken) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            //根据jsessionId查询微信绑定数据
            String jsessionId = accessToken;
            //兼容性判断
            if (accessToken.length() > 32) {
                //解密
                jsessionId = tokenSignService.Sm4FxgjDecrypt(SM4Util.ALGORITHM_NAME_ECB_PADDING, accessToken);
            }
            log.info("nj.callback.accessToken:[{}]", accessToken);
            NjRes100705 res100705 = NjRes100705.builder().build();
            res100705.setJsessionId(jsessionId);
            CacheEmployeeWechatInfoDTO cacheEmployeeWechatInfoDTOReq = new CacheEmployeeWechatInfoDTO();
            cacheEmployeeWechatInfoDTOReq.setJsessionId(jsessionId);
            cacheEmployeeWechatInfoDTOReq.setAppPartner(AppPartnerEnum.NJCB.getCode());
            cacheEmployeeWechatInfoDTOReq.setRegisterType(RegisterTypeEnum.UUID.getCode());
            CacheEmployeeWechatInfoDTO wechatInfoDTO = employeeWechatFeignController.findEmpwechatInfo(cacheEmployeeWechatInfoDTOReq);
            log.info("nj.wechatInfoDTO:[{}]", JacksonUtil.objectToJson(wechatInfoDTO));

            if (wechatInfoDTO != null) {
                CacheRes100705 wageRes100705 = njFeignController.wxCallback(accessToken);
                if (res100705 != null) {
                    BeanUtils.copyProperties(wageRes100705, res100705);
                    res100705.setApppartner(chain.utils.fxgj.constant.DictEnums.AppPartnerEnum.values()[wageRes100705.getApppartner()]);
                    EntIdGroupIdReqDTO entIdGroupIdReqDTO = EntIdGroupIdReqDTO.builder()
                            .entCustNo(wechatInfoDTO.getEntCustNo())
                            .encryptIdNumber(wechatInfoDTO.getIdNumber())
                            .build();
                    List<EntIdGroupIdDTO> entIdGroupIdDTOList = njFeignController.findByEntCustNo(entIdGroupIdReqDTO);
                    res100705.setEntList(entIdGroupIdDTOList);
                }
            } else {
                log.error("用户信息不存在！");
            }
            log.info("nj.wxCallback.res100705:[{}]", JacksonUtil.objectToJson(res100705));
            return res100705;
        }).subscribeOn(Schedulers.elastic());
    }


    /**
     * 访问凭证 Post
     */
    @PostMapping("/callback")
    @TrackLog
    public Mono<NjRes100705> callback(@RequestBody NjReqDTO njReqDTO) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            //根据jsessionId查询微信绑定数据
            String accessToken = njReqDTO.getAccessToken();
            String jsessionId = accessToken;
            //兼容性判断
            if (accessToken.length() > 32) {
                //解密
                jsessionId = tokenSignService.Sm4FxgjDecrypt(SM4Util.ALGORITHM_NAME_ECB_PADDING, accessToken);
            }
            log.info("nj.callback.accessToken:[{}]", jsessionId);
            NjRes100705 res100705 = NjRes100705.builder().build();
            res100705.setJsessionId(jsessionId);
            CacheEmployeeWechatInfoDTO cacheEmployeeWechatInfoDTOReq = new CacheEmployeeWechatInfoDTO();
            cacheEmployeeWechatInfoDTOReq.setJsessionId(jsessionId);
            cacheEmployeeWechatInfoDTOReq.setAppPartner(AppPartnerEnum.NJCB.getCode());
            cacheEmployeeWechatInfoDTOReq.setRegisterType(RegisterTypeEnum.UUID.getCode());
            CacheEmployeeWechatInfoDTO wechatInfoDTO = employeeWechatFeignController.findEmpwechatInfo(cacheEmployeeWechatInfoDTOReq);
            log.info("nj.wechatInfoDTO:[{}]", JacksonUtil.objectToJson(wechatInfoDTO));

            if (wechatInfoDTO != null) {
                CacheRes100705 wageRes100705 = njFeignController.wxCallback(jsessionId);
                if (res100705 != null) {
                    BeanUtils.copyProperties(wageRes100705, res100705);
                    res100705.setApppartner(chain.utils.fxgj.constant.DictEnums.AppPartnerEnum.values()[wageRes100705.getApppartner()]);
                    EntIdGroupIdReqDTO entIdGroupIdReqDTO = EntIdGroupIdReqDTO.builder()
                            .entCustNo(wechatInfoDTO.getEntCustNo())
                            .encryptIdNumber(wechatInfoDTO.getIdNumber())
                            .build();
                    List<EntIdGroupIdDTO> entIdGroupIdDTOList = njFeignController.findByEntCustNo(entIdGroupIdReqDTO);
                    res100705.setEntList(entIdGroupIdDTOList);
                }
            } else {
                log.error("用户信息不存在！");
            }
            log.info("nj.wxCallback.res100705:[{}]", JacksonUtil.objectToJson(res100705));
            return res100705;
        }).subscribeOn(Schedulers.elastic());
    }
}



