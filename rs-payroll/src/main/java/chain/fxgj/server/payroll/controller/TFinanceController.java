package chain.fxgj.server.payroll.controller;

import chain.css.exception.ParamsIllegalException;
import chain.css.log.annotation.TrackLog;
import chain.fxgj.feign.client.TFinanceFeignService;
import chain.fxgj.server.payroll.config.ErrorConstant;
import chain.fxgj.server.payroll.constant.PayrollConstants;
import chain.fxgj.server.payroll.dto.PageResponseDTO;
import chain.fxgj.server.payroll.dto.tfinance.*;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.fxgj.server.payroll.web.WebContext;
import chain.pub.client.feign.WechatFeignClient;
import chain.pub.common.dto.wechat.WechatConfigDTO;
import chain.pub.common.enums.WechatGroupEnum;
import chain.utils.commons.JacksonUtil;
import chain.utils.fxgj.constant.DictEnums.IsStatusEnum;
import chain.wage.manager.core.dto.WagePageResponseDTO;
import chain.wage.manager.core.dto.tfinance.*;
import chain.wage.manager.core.dto.web.WageUserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.security.PermitAll;
import javax.ws.rs.DefaultValue;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 同事理财团
 */
@RestController
@Validated
@RequestMapping("/tfinance")
@Slf4j
public class TFinanceController {
    @Autowired
    private TFinanceFeignService financeFeignService;
    @Autowired
    WechatFeignClient wechatFeignClient;

    /**
     * 活动产品列表
     */
    @GetMapping("/list")
    @TrackLog
    public Mono<List<ProductDTO>> list() {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        UserPrincipal userPrincipal = WebContext.getCurrentUser();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            WageUserPrincipal wageUserPrincipal=new WageUserPrincipal();
            BeanUtils.copyProperties(userPrincipal,wageUserPrincipal);
            List<WageProductDTO> wageProductDTOList=financeFeignService.list(wageUserPrincipal);
            log.info("list---->{}",wageProductDTOList);
            List<ProductDTO> list=null;
            if(wageProductDTOList!=null){
                list=new ArrayList<>();
                for(WageProductDTO dto:wageProductDTOList){
                    ProductDTO productDTO=new ProductDTO();
                    BeanUtils.copyProperties(dto,productDTO);
                    list.add(productDTO);
                }
            }
            log.info("ret.list:[{}]", JacksonUtil.objectToJson(list));
            return list;
        }).subscribeOn(Schedulers.boundedElastic());

    }

    /**
     * 同事团理财产品
     *
     * @param productId 产品Id
     * @param entId     企业Id
     * @param channel   渠道(0公众号菜单 1banner 2分享)
     * @param fxId      分享人id
     * @return
     */
    @GetMapping("/product")
    @TrackLog
    public Mono<ProductInfoDTO> productInfo(@RequestParam("productId") String productId,
                                            @RequestParam("entId") String entId,
                                            @RequestParam("channel") String channel,
                                            @RequestParam(value = "fxId", required = false) String fxId) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        UserPrincipal userPrincipal = WebContext.getCurrentUser();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            ProductInfoDTO productInfoDTO=null;
            WageUserPrincipal wageUserPrincipal=new WageUserPrincipal();
            BeanUtils.copyProperties(userPrincipal,wageUserPrincipal);
            WageProductInfoDTO wageProductInfoDTO=financeFeignService.productInfo(productId,entId,channel,fxId,wageUserPrincipal);
            log.info("productInfo-->{}",wageProductInfoDTO);
            if(wageProductInfoDTO!=null){
                productInfoDTO=new ProductInfoDTO();
                BeanUtils.copyProperties(wageProductInfoDTO,productInfoDTO);
            }
            log.info("ret.productInfoDTO:[{}]", JacksonUtil.objectToJson(productInfoDTO));
            return productInfoDTO;
        }).subscribeOn(Schedulers.boundedElastic());

    }

    /**
     * 平台产品预约列表
     *
     * @param productId 产品Id
     * @return
     */
    @GetMapping("/intentionList")
    @TrackLog
    @PermitAll
    public Mono<IntentListDTO> intentionList(@RequestParam("productId") String productId) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            IntentListDTO intentListDTO=null;
            WageIntentListDTO wageIntentListDTO=financeFeignService.intentionList(productId);
            log.info("intentionList-->{}",wageIntentListDTO);
            if (wageIntentListDTO!=null){
                intentListDTO=new IntentListDTO();
                BeanUtils.copyProperties(wageIntentListDTO,intentListDTO);
            }
            return intentListDTO;
        }).subscribeOn(Schedulers.boundedElastic());

    }

    /**
     * 操作列表
     *
     * @param pageNum   当前页数
     * @param size      每页显示条数
     * @param productId 产品Id
     * @param entId     企业Id
     * @param operate   操作类型 0 浏览 1预约
     * @return
     */
    @GetMapping("/operateList")
    @TrackLog
    public Mono<PageResponseDTO<OperateDTO>> operateList(
            @RequestHeader("page") @DefaultValue("1") int pageNum,
            @RequestHeader("size") @DefaultValue("10") int size,
            @RequestParam("productId") String productId,
            @RequestParam("entId") String entId,
            @RequestParam("operate") Integer operate) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        UserPrincipal userPrincipal = WebContext.getCurrentUser();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            WageUserPrincipal wageUserPrincipal=new WageUserPrincipal();
            BeanUtils.copyProperties(userPrincipal,wageUserPrincipal);
            WagePageResponseDTO<WageOperateDTO> responseDTO=financeFeignService.operateList(pageNum,size,productId,entId,operate,wageUserPrincipal);
            log.info("operateList-->{}",responseDTO);
            PageResponseDTO<OperateDTO> response =null;
            if (responseDTO!=null){
                response=new PageResponseDTO<>();
                BeanUtils.copyProperties(responseDTO,response);
            }
            return response;
        }).subscribeOn(Schedulers.boundedElastic());

    }

    /**
     * 预约明细
     *
     * @param productId 产品Id
     * @return
     */
    @GetMapping("/intentInfo")
    @TrackLog
    public Mono<IntentInfoDTO> intentInfo(@RequestParam("productId") String productId) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        UserPrincipal userPrincipal = WebContext.getCurrentUser();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            WageUserPrincipal wageUserPrincipal=new WageUserPrincipal();
            BeanUtils.copyProperties(userPrincipal,wageUserPrincipal);
            IntentInfoDTO intentInfoDTO=null;
            WageIntentInfoDTO wageIntentInfoDTO=financeFeignService.intentInfo(productId,wageUserPrincipal);
            log.info("intentInfo-->{}",wageIntentInfoDTO);
            if(wageIntentInfoDTO!=null){
                intentInfoDTO=new IntentInfoDTO();
                BeanUtils.copyProperties(wageIntentInfoDTO,intentInfoDTO);
            }
            return intentInfoDTO;
        }).subscribeOn(Schedulers.boundedElastic());

    }

    /**
     * 预约人信息
     *
     * @param entId 企业Id
     * @return
     */
    @GetMapping("/userInfo")
    @TrackLog
    public Mono<UserInfoDTO> userInfo(@RequestParam("entId") String entId) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        UserPrincipal userPrincipal = WebContext.getCurrentUser();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            WageUserPrincipal wageUserPrincipal=new WageUserPrincipal();
            BeanUtils.copyProperties(userPrincipal,wageUserPrincipal);
            UserInfoDTO userInfoDTO=null;
            WageUserInfoDTO wageUserInfoDTO=financeFeignService.userInfo(entId,wageUserPrincipal);
            log.info("userInfo-->{}",wageUserInfoDTO);
            if (wageUserInfoDTO!=null){
                userInfoDTO=new UserInfoDTO();
                BeanUtils.copyProperties(wageUserInfoDTO,userInfoDTO);
            }
            return userInfoDTO;
        }).subscribeOn(Schedulers.boundedElastic());

    }

    /**
     * 预约产品
     *
     * @param intentRequestDTO
     * @return
     */
    @PostMapping("/intent")
    @TrackLog
    public Mono<Void> addIntent(@RequestBody IntentRequestDTO intentRequestDTO) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        UserPrincipal userPrincipal = WebContext.getCurrentUser();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            if (StringUtils.isEmpty(userPrincipal.getIdNumber())) {
                throw new ParamsIllegalException(ErrorConstant.WECHAR_003.getErrorMsg());
            }
            if (intentRequestDTO.getProtocol() == IsStatusEnum.NO.getCode()) {
                throw new ParamsIllegalException(ErrorConstant.FINANCE_003.getErrorMsg());
            }

            if (StringUtils.isEmpty(intentRequestDTO.getProductId()) || StringUtils.isEmpty(intentRequestDTO.getClientName())
                    || StringUtils.isEmpty(intentRequestDTO.getIdNumber()) || StringUtils.isEmpty(intentRequestDTO.getClientPhone())) {
                throw new ParamsIllegalException(ErrorConstant.FINANCE_004.getErrorMsg());
            }

            if(intentRequestDTO!=null){
                WageIntentRequestDTO wageIntentRequestDTO=new WageIntentRequestDTO();
                BeanUtils.copyProperties(intentRequestDTO,wageIntentRequestDTO);
                WageUserPrincipal wageUserPrincipal=new WageUserPrincipal();
                BeanUtils.copyProperties(userPrincipal,wageUserPrincipal);
                WageIntentDTO intentDTO=new WageIntentDTO();
                intentDTO.setIntentRequestDTO(wageIntentRequestDTO);
                intentDTO.setUserPrincipal(wageUserPrincipal);
                financeFeignService.addIntent(intentDTO);
            }
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();

    }

    /**
     * 获取Code的url
     *
     * @param redirectUrl 跳转url
     * @return
     */
    @GetMapping("/codeUrl")
    @TrackLog
    @PermitAll
    public Mono<String> getCodeUrl(@RequestParam("redirectUrl") String redirectUrl) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            //inside停用，所以注释
//            String str=financeFeignService.getCodeUrl(redirectUrl);

            //改为调用put-client获取微信配置，构造网络授权连接
            log.info("getCodeUrl start:[{}]", WechatGroupEnum.FXGJ);
            WechatConfigDTO wechatConfigDTO = wechatFeignClient.getConfig(WechatGroupEnum.FXGJ);
            log.info("getCodeUrl wechatConfigDTO:[{}]", wechatConfigDTO);
            String appId = wechatConfigDTO.getAppId();
            String oauthUrl = wechatConfigDTO.getOauthUrl();
            String authorizeurl = PayrollConstants.OAUTH_AUTHORIZE_URL;
            authorizeurl = authorizeurl.replace("APPID", appId);
            authorizeurl = authorizeurl.replace("REDIRECT_URI", URLEncoder.encode(oauthUrl, "UTF-8"));
            authorizeurl = authorizeurl.replace("STATE", "STATE");
            authorizeurl = authorizeurl.replace("SCOPE", PayrollConstants.SNSAPI_USERINFO);
            log.info("authorizeurl:[{}]",authorizeurl);
            return authorizeurl;
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
