package chain.fxgj.server.payroll.controller;

import chain.css.exception.ParamsIllegalException;
import chain.css.log.annotation.TrackLog;
import chain.fxgj.core.common.constant.DictEnums.IsStatusEnum;
import chain.fxgj.core.common.constant.ErrorConstant;
import chain.fxgj.feign.client.TFinanceFeignService;
import chain.fxgj.feign.dto.WagePageResponseDTO;
import chain.fxgj.feign.dto.tfinance.*;
import chain.fxgj.feign.dto.web.WageUserPrincipal;
import chain.fxgj.server.payroll.dto.PageResponseDTO;
import chain.fxgj.server.payroll.dto.tfinance.*;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.fxgj.server.payroll.web.WebContext;
import chain.utils.commons.JacksonUtil;
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
        }).subscribeOn(Schedulers.elastic());

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
        }).subscribeOn(Schedulers.elastic());

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
        }).subscribeOn(Schedulers.elastic());

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
        }).subscribeOn(Schedulers.elastic());

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
        }).subscribeOn(Schedulers.elastic());

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
        }).subscribeOn(Schedulers.elastic());

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
        }).subscribeOn(Schedulers.elastic()).then();

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
            String str=financeFeignService.getCodeUrl(redirectUrl);
            log.info("getCodeUrl-->{}",str);
            return str;
        }).subscribeOn(Schedulers.elastic());
    }
}