package chain.fxgj.server.payroll.controller;

import chain.css.exception.ParamsIllegalException;
import chain.css.log.annotation.TrackLog;
import chain.fxgj.core.common.constant.DictEnums.AppPartnerEnum;
import chain.fxgj.core.common.constant.DictEnums.IsStatusEnum;
import chain.fxgj.core.common.constant.FxgjDBConstant;
import chain.fxgj.core.common.service.EmployeeEncrytorService;
import chain.fxgj.core.common.service.MerchantService;
import chain.fxgj.core.jpa.model.EmployeeWechatInfo;
import chain.fxgj.server.payroll.config.ErrorConstant;
import chain.fxgj.server.payroll.config.properties.MerchantsProperties;
import chain.fxgj.server.payroll.constant.PayrollConstants;
import chain.fxgj.server.payroll.dto.merchant.MerchantAccessDTO;
import chain.fxgj.server.payroll.dto.merchant.MerchantDTO;
import chain.fxgj.server.payroll.dto.merchant.MerchantHeadDTO;
import chain.fxgj.server.payroll.dto.response.Res100705;
import chain.fxgj.server.payroll.util.RSAEncrypt;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.utils.commons.JacksonUtil;
import chain.utils.commons.StringUtils;
import chain.utils.commons.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 工资条  对外输出接口
 */
@CrossOrigin
@RestController
@Validated
@RequestMapping(value = "/merchant")
@Slf4j
@SuppressWarnings("unchecked")
public class MerchantRS {

    @Autowired
    MerchantsProperties merchantProperties;
    @Autowired
    MerchantService merchantService;
    @Autowired
    EmployeeEncrytorService employeeEncrytorService;
    @Resource
    RedisTemplate redisTemplate;


    private MerchantsProperties.Merchant getMerchant(String id) {
        Optional<MerchantsProperties.Merchant> qWechat = merchantProperties.getMerchant().stream()
                .filter(item -> item.getAppid().equalsIgnoreCase(id)).findFirst();
        MerchantsProperties.Merchant merchant = qWechat.orElse(null);
        return merchant;
    }

    /**
     * 访问凭证
     */
    @PostMapping("/getAccess")
    @TrackLog
    public Mono<MerchantAccessDTO> getAccessUrl(@RequestHeader(value = "signature", required = true) String signature,
                                                @RequestHeader(value = "appid", required = true) String appid,
                                                @RequestHeader(value = "version", defaultValue = "1.0") String version,
                                                @RequestBody MerchantDTO merchantDTO,
                                                ServerHttpResponse response
    ) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        //【1】根据appid -->  取配置文件里商户信息
        MerchantsProperties.Merchant merchant = this.getMerchant(StringUtils.trimToEmpty(appid));
        String merchantAppid = StringUtils.trimToEmpty(merchant.getAppid());

        //
        if (merchant == null) {
            log.error("appid={}，不存在", appid);
            throw new ParamsIllegalException(ErrorConstant.MERCHANT_01.getErrorMsg());
        }


        //String ss ="usDVZ9ACNgGgRolN35m52K6Js-sdz3q9QJOZY3Fk5bydFblij9urJ1a0On5Dp2P494Gry77rV6MTB2ltNDr0lZ6fIyT7l3VFD0vIG6ixNqHi7WVOkd0IWp-D_sRciVva_lPevmmlGbfzUQXPSIluQLnKgU77WhhVJSAcg09kgeaG2vhdTemCT0IQTHa_qpq0j0scnoJseBi5AQbKfa_lTaWtNCKregHmO4yTxLJ3R81p8zxMDP6N--UjMHR2vyMPfEwedjRw3ikHKfOVAFjCIHk8i9ZG80dWb3_YP66W6HMe4nitgTEQfjz5xS4_nB4I98lorMm4vFAnkMs7KllGpw";
        String ss = "UYKneVmmXb3K1KQ1Z9SAcNdTbB4XufY9VmUZMoJkWesr9P80R_zfeu4V_aAlk6zr-ZaAs3HjRuikSRw3obGrP_n2VzW6qqt3uPI4Z54BmCaT3FJ9O0Dr-_5jwoiq-VltgaVaRiAgVow1Dc1fG4vnwInH_tDbuhmF3JUGxyd09RM";
        try {
            ss = RSAEncrypt.decrypt(ss, merchant.getRsaPrivateKey());
        } catch (Exception e) {
            e.printStackTrace();
        }


        //构建 head
        MerchantHeadDTO merchantHeadDTO = MerchantHeadDTO.builder()
                .version(version)
                .signature(signature)
                .appid(appid)
                .build();

        //解密
        MerchantHeadDTO merchantHeadDecrypt = MerchantHeadDTO.decrypt(merchantHeadDTO, merchant.getRsaPrivateKey());


        //1、解析 返回报文体信息
        MerchantDTO merchantDecrypt = MerchantDTO.decrypt(merchantDTO, merchant.getRsaPrivateKey());

        //2、生成签名信息
        String checkSignature = MerchantDTO.signature(merchantDecrypt, merchantHeadDecrypt);

        //3、对比签名信息
        if (!signature.equalsIgnoreCase(checkSignature)) {
            log.error("签名信息,验证失败。报文中={} ,解析后生成签名={} ", signature, checkSignature);
            throw new ParamsIllegalException(ErrorConstant.MERCHANT_02.getErrorMsg());
        }


        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            //4、生成访问信息
            EmployeeWechatInfo employeeWechatInfo = merchantDecrypt.conver();

            String idNumber = employeeEncrytorService.encryptIdNumber(employeeWechatInfo.getIdNumber());

            String phone = employeeEncrytorService.encryptPhone(employeeWechatInfo.getPhone());
            log.info("手机号phone:{}", phone);

            employeeWechatInfo.setIdNumber(idNumber);
            employeeWechatInfo.setPhone(phone);

            employeeWechatInfo.setAppPartner(AppPartnerEnum.values()[Integer.valueOf(merchant.getMerchantCode())]);

            EmployeeWechatInfo employeeWechat = merchantService.findMerchant(employeeWechatInfo);
            if (employeeWechat != null) {
                log.info("用户信息存在！");
                //todo 微信昵称 问题
                employeeWechat.setNickname(employeeWechatInfo.getNickname());
                employeeWechat.setHeadimgurl(employeeWechatInfo.getHeadimgurl());
                merchantService.saveMerchant(employeeWechat);
            } else {
                log.info("用户信息不存在！");
                employeeWechatInfo = merchantService.saveMerchant(employeeWechatInfo);
            }


            String accessToken = UUIDUtil.createUUID8();

            String redisKey = FxgjDBConstant.PREFIX + ":merchant:" + accessToken;
            String emp = JacksonUtil.objectToJson(merchantDecrypt);
            redisTemplate.opsForValue().set(redisKey, emp, PayrollConstants.MERCHANT_EXPIRESIN, TimeUnit.SECONDS);

            //5、生成签名信息

            MerchantAccessDTO merchantAccessDTO = MerchantAccessDTO.builder()
                    .accessToken(accessToken)
                    .expiresIn(PayrollConstants.MERCHANT_EXPIRESIN)
                    .accessUrl(merchant.getAccessUrl())
                    .build();
            MerchantAccessDTO merchantAccess = MerchantAccessDTO.encryption(merchantAccessDTO, merchant.getParaRsaPublicKey());


            String retureSignature = MerchantAccessDTO.signature(merchantAccess, merchantHeadDTO);
            //String result = java.net.URLDecoder.decode(en ,"UTF-8");

            log.info("返回签名：{}", retureSignature);
            response.getHeaders().set("signature", retureSignature);

            return merchantAccess;
        }).subscribeOn(Schedulers.elastic());

    }


    /**
     * 访问凭证
     */
    @GetMapping("/callback")
    @TrackLog
    public Mono<Res100705> wxCallback(@RequestParam String accessToken) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        String token = StringUtils.trimToEmpty(accessToken);

        String redisKey = FxgjDBConstant.PREFIX + ":merchant:" + token;
        Object value = redisTemplate.opsForValue().get(redisKey);

        if (value == null) {
            throw new ParamsIllegalException(ErrorConstant.MERCHANT_06.getErrorMsg());
        }


        MerchantDTO merchantDecrypt = JacksonUtil.jsonToBean((String) value, MerchantDTO.class);

        EmployeeWechatInfo employeeWechatInfo = merchantDecrypt.conver();
        employeeWechatInfo.setIdNumber(employeeEncrytorService.encryptIdNumber(employeeWechatInfo.getIdNumber()));

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            String jsessionId = UUIDUtil.createUUID32();
            Res100705 res100705 = Res100705.builder().build();

            log.info("set之前打印jsessionId:[{}]", jsessionId);
            res100705.setJsessionId(jsessionId);

            EmployeeWechatInfo employeeWechat = merchantService.findMerchant(employeeWechatInfo);
            if (employeeWechat != null) {
                log.info("用户信息存在！");
                UserPrincipal userPrincipal = merchantService.setWechatInfo(jsessionId, employeeWechat);
                if (StringUtils.isNotBlank(userPrincipal.getIdNumber())) {
                    res100705.setBindStatus("1");
                    res100705.setIdNumber(userPrincipal.getIdNumberEncrytor());
                    res100705.setIfPwd(StringUtils.isEmpty(StringUtils.trimToEmpty(userPrincipal.getQueryPwd())) ? IsStatusEnum.NO.getCode() : IsStatusEnum.YES.getCode());
                    res100705.setName(userPrincipal.getName());
                    res100705.setPhone(userPrincipal.getPhone());
                    res100705.setHeadimgurl(employeeWechatInfo.getHeadimgurl());
                }
            } else {
                log.info("用户信息不存在！");
            }

            return res100705;
        }).subscribeOn(Schedulers.elastic());

    }


}



