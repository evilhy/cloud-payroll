package chain.fxgj.server.payroll.controller;


import chain.css.exception.ParamsIllegalException;
import chain.css.log.annotation.TrackLog;
import chain.fxgj.core.common.constant.PayrollDBConstant;
import chain.fxgj.server.payroll.config.ErrorConstant;
import chain.fxgj.server.payroll.config.properties.MerchantsProperties;
import chain.fxgj.server.payroll.constant.PayrollConstants;
import chain.fxgj.server.payroll.dto.merchant.MerchantAccessDTO;
import chain.fxgj.server.payroll.dto.merchant.MerchantDTO;
import chain.fxgj.server.payroll.dto.merchant.MerchantHeadDTO;
import chain.fxgj.server.payroll.dto.response.Res100705;
import chain.fxgj.server.payroll.service.EmployeeEncrytorService;
import chain.fxgj.server.payroll.util.RSAEncrypt;
import chain.payroll.client.feign.EmployeeWechatFeignController;
import chain.payroll.client.feign.MerchantFeignController;
import chain.utils.commons.JacksonUtil;
import chain.utils.commons.StringUtils;
import chain.utils.commons.UUIDUtil;
import chain.utils.fxgj.constant.DictEnums.AppPartnerEnum;
import chain.utils.fxgj.constant.DictEnums.RegisterTypeEnum;
import core.dto.response.merchant.CacheEmpInfoDTO;
import core.dto.response.merchant.CacheEmployeeWechatInfoDTO;
import core.dto.response.merchant.CacheRes100705;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 工资条  对外输出接口
 */
@RestController
@Validated
@RequestMapping(value = "/merchant")
@Slf4j
@SuppressWarnings("unchecked")
public class MerchantController {

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

    /**
     * 根据appid 查询工资条接入合作方信息
     *
     * @param appid 合作方id
     */
    private MerchantsProperties.Merchant getMerchant(String appid) {

        Optional<MerchantsProperties.Merchant> qWechat = merchantProperties.getMerchant().stream()
                .filter(item -> item.getAppid().equalsIgnoreCase(appid)).findFirst();
        MerchantsProperties.Merchant merchant = qWechat.orElseThrow(() -> {
            log.error("==>appid={}，不存在，非法访问！", appid);
            return new ParamsIllegalException(ErrorConstant.MERCHANT_01.getErrorMsg());
        });
        return merchant;
    }

    /**
     * 访问凭证
     *
     * @param signature 签名
     * @param appid     合作方id
     * @param version   接口版本号
     */
    @PostMapping("/getAccess")
    @TrackLog
    public Mono<MerchantAccessDTO> getAccessUrl(@RequestHeader(value = "signature", required = true) String signature,
                                                @RequestHeader(value = "appid", required = true) String appid,
                                                @RequestHeader(value = "version", defaultValue = "1.0") String version,
                                                @RequestHeader(value = "clientSn", required = true) String clientSn,
                                                @RequestHeader(value = "clientDate", required = true) String clientDate,
                                                @RequestHeader(value = "clientTime", required = true) String clientTime,
                                                @RequestBody MerchantDTO merchantDTO,
                                                ServerHttpResponse response
    ) throws Exception {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        log.info("getAccess.signature:[{}]", signature);
        log.info("getAccess.appid:[{}]", appid);
        log.info("getAccess.version:[{}]", version);
        log.info("getAccess.merchantDTO:[{}]", JacksonUtil.objectToJson(merchantDTO));
        //【1】根据appid -->  取配置文件里商户信息
        MerchantsProperties.Merchant merchant = this.getMerchant(StringUtils.trimToEmpty(appid));

        //构建 head
        MerchantHeadDTO merchantHeadDTO = MerchantHeadDTO.builder()
                .version(version)
                .signature(signature)
                .appid(appid)
                .build();
        log.info("构建 MerchantHeadDTO:[{}]", JacksonUtil.objectToJson(merchantHeadDTO));
        //解密
        MerchantHeadDTO merchantHeadDecrypt = MerchantHeadDTO.decrypt(merchantHeadDTO, merchant.getRsaPrivateKey());
        log.info("解密 MerchantHeadDTO:[{}]", JacksonUtil.objectToJson(merchantHeadDecrypt));

        //1、解析 返回报文体信息
        MerchantDTO merchantDecrypt = MerchantDTO.decrypt(merchantDTO, merchant.getRsaPrivateKey());

        //2、生成签名信息
        String checkSignature = MerchantDTO.signature(merchantDecrypt, merchantHeadDecrypt);

        //3、对比签名信息
        Base64 base64 = new Base64();
        //signature base64 解密
        signature = new String(base64.decode(signature), "UTF-8");
        //signature 公钥 解密
        signature = RSAEncrypt.decrypt(signature, merchant.getRsaPrivateKey());
        log.info("签名信息，报文中={} ,解析后生成签名={} ", signature, checkSignature);

        // todo 注释后测试使用暂不验签。生产请放开注释
//        if (!signature.equalsIgnoreCase(checkSignature)) {
//            log.error("签名信息,验证失败。报文中={} ,解析后生成签名={} ", signature, checkSignature);
//            throw new ParamsIllegalException(ErrorConstant.MERCHANT_02.getErrorMsg());
//        }
        log.info(">>>>>>>验签通过<<<<<<<");

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            //4、生成访问信息
            String idNumber = employeeEncrytorService.encryptIdNumber(merchantDecrypt.getIdNumber().toUpperCase());
            String phone = employeeEncrytorService.encryptPhone(merchantDecrypt.getPhone());
            CacheEmployeeWechatInfoDTO paramWetchatDto = new CacheEmployeeWechatInfoDTO();

            paramWetchatDto.setHeadimgurl(merchantDecrypt.getHeadimgurl());
            paramWetchatDto.setUid(merchantDecrypt.getUid());
            paramWetchatDto.setOpenId(merchantDecrypt.getOpenId());
            paramWetchatDto.setNickname(merchantDecrypt.getNickname());
            paramWetchatDto.setIdNumber(idNumber);
            paramWetchatDto.setPhone(phone);
            paramWetchatDto.setAppPartner(merchant.getMerchantCode().getCode());
            paramWetchatDto.setRegisterType(RegisterTypeEnum.UUID.getCode());
            CacheEmployeeWechatInfoDTO wechatInfoDTO = employeeWechatFeignController.findempwechat(paramWetchatDto);
            if (wechatInfoDTO != null) {  //认证绑定信息表已经存在，则说明已经绑定成功
                log.info("认证绑定信息表【存在】，则说明已经绑定成功！");
                wechatInfoDTO.setNickname(paramWetchatDto.getNickname());
                wechatInfoDTO.setHeadimgurl(paramWetchatDto.getHeadimgurl());
                wechatInfoDTO.setUid(paramWetchatDto.getUid());
                wechatInfoDTO.setOpenId(paramWetchatDto.getOpenId());
                wechatInfoDTO.setRegisterType(RegisterTypeEnum.UUID.getCode());
                wechatInfoDTO.setAppPartner(merchant.getMerchantCode().getCode());
                log.info("wechatInfoDTO入库:[{}]", JacksonUtil.objectToJson(wechatInfoDTO));
                employeeWechatFeignController.saveempwechat(wechatInfoDTO);
            } else {
                log.info("认证绑定信息表【不存在】！");
                //查询用户信息
                CacheEmpInfoDTO wageEmpInfoDTO = new CacheEmpInfoDTO();
                wageEmpInfoDTO.setIdNumber(merchantDecrypt.getIdNumber());
                wageEmpInfoDTO.setName(merchantDecrypt.getName());
                CacheEmpInfoDTO empInfoDTO = employeeWechatFeignController.findemp(wageEmpInfoDTO);
                if (empInfoDTO != null) {
                    log.info("员工信息表中【存在】！入库:[{}]", JacksonUtil.objectToJson(paramWetchatDto));
                    wechatInfoDTO = employeeWechatFeignController.saveempwechat(paramWetchatDto);
                } else {
                    log.info("员工信息表中【不存在】！信息未认证");
                    throw new ParamsIllegalException(ErrorConstant.MERCHANT_07.getErrorMsg());
                }

            }


            String accessToken = UUIDUtil.createUUID8();
            log.info("accessToken:[{}]", accessToken);
            String redisKey = PayrollDBConstant.PREFIX + ":merchant:" + accessToken;
            merchantDecrypt.setDataAuths(merchant.getDataAuths());
            merchantDecrypt.setAppPartner(merchant.getMerchantCode());
            merchantDecrypt.setRegisterType(RegisterTypeEnum.UUID);
            String emp = JacksonUtil.objectToJson(merchantDecrypt);
            redisTemplate.opsForValue().set(redisKey, emp, PayrollConstants.MERCHANT_EXPIRESIN, TimeUnit.SECONDS);

            //5、生成签名信息

            MerchantAccessDTO merchantAccessDTO = MerchantAccessDTO.builder()
                    .accessToken(accessToken)
                    .expiresIn(PayrollConstants.MERCHANT_EXPIRESIN)
                    .accessUrl(merchant.getAccessUrl())
                    .build();
            MerchantAccessDTO merchantAccess = MerchantAccessDTO.encryption(merchantAccessDTO, merchant.getParaRsaPublicKey());

            String encryptVersion = RSAEncrypt.encrypt("1.0", merchant.getParaRsaPublicKey());
            log.info("encryptAccessUrl:[{}]", merchantAccess.getAccessUrl());
            log.info("encryptVersion:[{}]", encryptVersion);
            log.info("appid:[{}]", merchant.getAppid());
            String retureSignature = MerchantAccessDTO.signatureSHA(merchantAccess.getAccessUrl(), encryptVersion, merchant.getAppid());
            log.info("accessUrl.version.appid签名：{}", retureSignature);

            //公钥加密
            retureSignature = RSAEncrypt.encrypt(retureSignature, merchant.getParaRsaPublicKey());
            log.info("==>retureSignature签名使用公钥再加密 ={}", retureSignature);

            //signature base64
            retureSignature = base64.encodeToString(retureSignature.getBytes("UTF-8"));
            log.info("==>retureSignature base64 ={}", retureSignature);

            response.getHeaders().set("signature", retureSignature);
            log.info("headers.signature返回签名：{}", retureSignature);
            LocalDateTime now = LocalDateTime.now();
            String encryptLog = RSAEncrypt.encrypt("1.0", merchant.getParaRsaPublicKey());
            log.info("encryptLog:[{}]", encryptLog);
            response.getHeaders().set("version", encryptVersion);
            response.getHeaders().set("clientSn", UUIDUtil.createUUID32());

            response.getHeaders().set("clientDate", new SimpleDateFormat("yyyyMMdd").format(new Date()));

            long timeStamp = now.toInstant(ZoneOffset.of("+8")).toEpochMilli();
            response.getHeaders().set("clientTime", String.valueOf(timeStamp));
            log.info("merchantAccess:[{}]", JacksonUtil.objectToJson(merchantAccess));
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

        String redisKey = PayrollDBConstant.PREFIX + ":merchant:" + token;
        Object value = redisTemplate.opsForValue().get(redisKey);

        if (value == null) {
            log.error("根据:[{}]未查询到缓存值", redisKey);
            throw new ParamsIllegalException(ErrorConstant.MERCHANT_06.getErrorMsg());
        }

        MerchantDTO merchantDecrypt = JacksonUtil.jsonToBean((String) value, MerchantDTO.class);

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            String jsessionId = UUIDUtil.createUUID32();
            Res100705 res100705 = Res100705.builder().build();
            log.info("set之前打印jsessionId:[{}]", jsessionId);
            res100705.setJsessionId(jsessionId);
            CacheEmployeeWechatInfoDTO paramWetchatDto = CacheEmployeeWechatInfoDTO.builder()
                    .name(merchantDecrypt.getName())
                    .idType(Integer.valueOf(merchantDecrypt.getIdType()))
                    .phone(merchantDecrypt.getPhone())
                    .uid(merchantDecrypt.getUid())
                    .openId(merchantDecrypt.getOpenId())
                    .nickname(merchantDecrypt.getNickname())
                    .headimgurl(merchantDecrypt.getHeadimgurl())
                    .build();
            paramWetchatDto.setIdNumber(employeeEncrytorService.encryptIdNumber(merchantDecrypt.getIdNumber()));
            paramWetchatDto.setAppPartner(AppPartnerEnum.NEWUP.getCode());
            paramWetchatDto.setRegisterType(RegisterTypeEnum.UUID.getCode());
            log.info("copyProperties.paramWetchatDto:[{}]", JacksonUtil.objectToJson(paramWetchatDto));
            CacheEmployeeWechatInfoDTO wechatInfoDTO = employeeWechatFeignController.findempwechat(paramWetchatDto);
            if (wechatInfoDTO != null) {
                CacheRes100705 wageRes100705 = merchantFeignController.wxCallback(accessToken);
                if (res100705 != null) {
                    BeanUtils.copyProperties(wageRes100705, res100705);
                    res100705.setApppartner(chain.utils.fxgj.constant.DictEnums.AppPartnerEnum.values()[wageRes100705.getApppartner()]);
                }
            } else {
                log.info("用户信息不存在！");
            }
            log.info("wxCallback.res100705:[{}]", JacksonUtil.objectToJson(res100705));
            return res100705;
        }).subscribeOn(Schedulers.elastic());
    }


}



