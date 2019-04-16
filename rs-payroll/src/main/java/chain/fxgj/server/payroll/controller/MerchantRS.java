package chain.fxgj.server.payroll.controller;


import chain.css.log.annotation.TrackLog;
import chain.fxgj.server.payroll.config.properties.MerchantsProperties;
import chain.fxgj.server.payroll.dto.merchant.MerchantAccessDTO;
import chain.fxgj.server.payroll.dto.merchant.MerchantDTO;
import chain.fxgj.server.payroll.dto.merchant.MerchantHeadDTO;
import chain.fxgj.server.payroll.dto.request.DistributeDTO;
import chain.fxgj.server.payroll.util.RSAEncrypt;
import chain.utils.commons.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.security.DigestException;
import java.util.Map;
import java.util.Optional;

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
    //@TrackLog
    public Mono<MerchantAccessDTO> getAccessUrl(@RequestHeader(value = "signature", required = true) String signature,
                                                @RequestHeader(value = "appid", required = true) String appid,
                                                @RequestHeader(value = "version", defaultValue = "1.0") String version,
                                                @RequestBody MerchantDTO merchantDTO
    ) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        MerchantsProperties.Merchant merchant = this.getMerchant(StringUtils.trimToEmpty(appid));

        if (merchant == null) {
            //todo merchantCode信息不存在
        }

        MerchantHeadDTO merchantHeadDTO = MerchantHeadDTO.builder()
                .version(version)
                .signature(signature)
                .appid(appid)
                .build();


        MerchantHeadDTO merchantHeadDecrypt = MerchantHeadDTO.decrypt(merchantHeadDTO,merchant.getRsaPrivateKey());


        //1、解析 返回报文信息
        MerchantDTO merchantDecrypt = MerchantDTO.decrypt(merchantDTO, merchant.getRsaPrivateKey());

        try {
            //2、生成签名信息
            String checkSignature = MerchantDTO.signature(merchantDecrypt, merchantHeadDecrypt);

            //3、对比签名信息
            if (!signature.equalsIgnoreCase(checkSignature)){
                //todo
            }

        } catch (DigestException e) {
            e.printStackTrace();
        }


        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            //4、生成访问信息

            //5、生成签名信息
            MerchantAccessDTO merchantAccessDTO = MerchantAccessDTO.builder()
                    .accessToken("12345")
                    .expiresIn(7200)
                    .accessUrl("http://www.baidu.com")
                    .build();
            MerchantAccessDTO merchantAccess = MerchantAccessDTO.encryption(merchantAccessDTO, merchant.getRsaPublicKey());



            String retureSignature = MerchantAccessDTO.signature(merchantAccess, merchantHeadDTO);
            //String result = java.net.URLDecoder.decode(en ,"UTF-8");

            log.info("返回签名：{}", retureSignature);
            return merchantAccess;
        }).subscribeOn(Schedulers.elastic());


    }


}



