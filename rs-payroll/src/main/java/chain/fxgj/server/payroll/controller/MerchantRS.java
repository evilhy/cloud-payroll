package chain.fxgj.server.payroll.controller;

import chain.css.exception.ParamsIllegalException;
import chain.css.log.annotation.TrackLog;
import chain.fxgj.core.common.constant.DictEnums.AppPartnerEnum;
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
import chain.utils.commons.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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
                                                @RequestBody MerchantDTO merchantDTO
    ) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        //取对接商户 信息
        MerchantsProperties.Merchant merchant = this.getMerchant(StringUtils.trimToEmpty(appid));
        String merchantAppid = StringUtils.trimToEmpty(merchant.getAppid());

        //
        if (merchant == null) {
            log.error("appid={}，不存在", appid);
            throw new ParamsIllegalException(ErrorConstant.MERCHANT_01.getErrorMsg());
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
                employeeWechat.setNickname(employeeWechatInfo.getNickname());
                employeeWechat.setHeadimgurl(employeeWechatInfo.getHeadimgurl());
                merchantService.saveMerchant(employeeWechat);
            } else {
                log.info("用户信息不存在！");
                merchantService.saveMerchant(employeeWechatInfo);
            }

            Integer accessToken = (int) (Math.random() * 100000);

            String redisKey = FxgjDBConstant.PREFIX + ":merchant:" + merchantAppid + ":" + employeeWechatInfo.getIdNumber();
            redisTemplate.opsForValue().set(redisKey, accessToken, PayrollConstants.MERCHANT_EXPIRESIN, TimeUnit.SECONDS);

            //5、生成签名信息

            MerchantAccessDTO merchantAccessDTO = MerchantAccessDTO.builder()
                    .accessToken(accessToken.toString())
                    .expiresIn(PayrollConstants.MERCHANT_EXPIRESIN)
                    .accessUrl(merchant.getAccessUrl())
                    .build();
            MerchantAccessDTO merchantAccess = MerchantAccessDTO.encryption(merchantAccessDTO, merchant.getRsaPublicKey());


            String retureSignature = MerchantAccessDTO.signature(merchantAccess, merchantHeadDTO);
            //String result = java.net.URLDecoder.decode(en ,"UTF-8");

            log.info("返回签名：{}", retureSignature);
            //response.getHeaders().set("signature",retureSignature);
            return merchantAccess;
        }).subscribeOn(Schedulers.elastic());

    }


}



