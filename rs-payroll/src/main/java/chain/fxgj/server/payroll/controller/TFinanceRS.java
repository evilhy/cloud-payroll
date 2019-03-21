package chain.fxgj.server.payroll.controller;

import chain.css.exception.ParamsIllegalException;
import chain.css.log.annotation.TrackLog;
import chain.fxgj.core.common.config.properties.PayrollProperties;
import chain.fxgj.core.common.constant.DictEnums.IntentStatusEnum;
import chain.fxgj.core.common.constant.DictEnums.IsStatusEnum;
import chain.fxgj.core.common.constant.ErrorConstant;
import chain.fxgj.core.common.constant.FxgjDBConstant;
import chain.fxgj.core.common.service.FinanceService;
import chain.fxgj.core.common.util.TransUtil;
import chain.fxgj.core.jpa.model.EmployeeInfo;
import chain.fxgj.server.payroll.dto.PageResponseDTO;
import chain.fxgj.server.payroll.dto.tfinance.*;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.fxgj.server.payroll.web.WebContext;
import chain.outside.common.dto.wechat.EventDTO;
import chain.outside.common.dto.wechat.WeixinAuthorizeUrlDTO;
import chain.utils.commons.JacksonUtil;
import chain.wechat.client.feign.IwechatFeignService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;


/**
 * 同事理财团
 */
@CrossOrigin
@RestController
@Validated
@RequestMapping("/tfinance")
@Slf4j
public class TFinanceRS {
    @Autowired
    PayrollProperties payrollProperties;
    @Inject
    FinanceService financeService;
    @Autowired
    IwechatFeignService iwechatFeignService;

    /**
     * 活动产品列表
     */
    @GetMapping("/list")
    @TrackLog
    public Mono<List<ProductDTO>> list() {
        UserPrincipal userPrincipal = WebContext.getCurrentUser();
        return Mono.fromCallable(() -> {
            ProductDTO productDTO = new ProductDTO();
            String productId = financeService.getBankProductInfo();
            productDTO.setProductId(productId);
            if (StringUtils.isNotBlank(userPrincipal.getIdNumber())) {
                //判断用户是否预约
                String yEntId = financeService.getIdNumberIntent(productDTO.getProductId(), userPrincipal.getIdNumber());
                if (StringUtils.isEmpty(yEntId))
                    productDTO.setEntId(userPrincipal.getEntId());
                else
                    productDTO.setEntId(yEntId);
            }
            List<ProductDTO> list = new ArrayList();
            list.add(productDTO);
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
        UserPrincipal userPrincipal = WebContext.getCurrentUser();
        return Mono.fromCallable(() -> {
            //查询理财产品基本信息
            ProductInfoDTO productInfoDTO = financeService.getProductInfo(productId, payrollProperties.getImgUrl());
            productInfoDTO.setNowDate(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            productInfoDTO.setMinIntentAmt(10000);

            //是否已绑定
            if (StringUtils.isNotBlank(userPrincipal.getIdNumber())) {
                productInfoDTO.setWechatId(userPrincipal.getWechatId());
                productInfoDTO.setBindStatus(IsStatusEnum.YES.getCode());

                //是否已预约
                String yEntId = financeService.getIdNumberIntent(productId, userPrincipal.getIdNumber());
                if (StringUtils.isNotBlank(yEntId)) {
                    productInfoDTO.setIntentStatus(IsStatusEnum.YES.getCode());
                    productInfoDTO.setEntId(yEntId);
                } else {
                    productInfoDTO.setEntId(userPrincipal.getEntId());
                    if (!entId.equals(userPrincipal.getEntId())) {
                        //判断用户是否在该企业
                        EmployeeInfo employeeInfo = financeService.getEmpByIdNumberEnt(userPrincipal.getIdNumber(), entId);
                        if (employeeInfo != null) {
                            productInfoDTO.setEntId(entId);
                        }
                    }
                }
                String entId_new = productInfoDTO.getEntId();
//                entId = productInfoDTO.getEntId();

                Long intentNumber = financeService.getIntentNum(productId, entId_new);
                productInfoDTO.setIntentNum(Math.toIntExact(intentNumber));

                if (intentNumber > 1 || (intentNumber == 1 && StringUtils.isEmpty(yEntId))) {
                    productInfoDTO.setShow("1");
                }

                //当前档位
                for (int i = 0; i < productInfoDTO.getMarkList().size(); i++) {
                    if (intentNumber >= productInfoDTO.getMarkList().get(i).getMinPeople() && intentNumber <= productInfoDTO.getMarkList().get(i).getMaxPeople()) {
                        productInfoDTO.setNowMark(productInfoDTO.getMarkList().get(i).getMarkLevel());
                        productInfoDTO.getMarkList().get(i).setNowMark(IsStatusEnum.YES.getCode());
                    }
                    if (intentNumber >= productInfoDTO.getMarkList().get(i).getMinPeople()) {
                        productInfoDTO.getMarkList().get(i).setSucess(IsStatusEnum.YES.getCode());
                    }
                }

                //浏览产品 预约后不添加
                if (StringUtils.isEmpty(yEntId)) {
                    try {
                        BrowseRequestDTO browseRequestDTO = new BrowseRequestDTO();
                        browseRequestDTO.setEntId(entId_new);
                        browseRequestDTO.setProductId(productId);
                        browseRequestDTO.setOpenId(userPrincipal.getOpenId());
                        browseRequestDTO.setChannel(channel);
                        browseRequestDTO.setFxId(fxId);
                        Client client = ClientBuilder.newClient();
                        financeService.addBrowse(browseRequestDTO);
                    } catch (Exception e) {
                    }
                }
            } else {
                //判断是否关注
                if (!financeService.isFollowWechat(userPrincipal.getOpenId())) {
                    productInfoDTO.setFollowStatus(IsStatusEnum.NO.getCode());

                    if (StringUtils.isNotBlank(channel) && (channel.equals("0") || channel.equals("1"))) {
                        productInfoDTO.setFollowStatus(IsStatusEnum.YES.getCode());
                        //0 菜单 1 关注条banner
                        //添加关注记录
                        try {
                            EventDTO eventDTO = new EventDTO();
                            eventDTO.setOpenId(userPrincipal.getOpenId());
                            eventDTO.setEvent("subscribe");
                            //请求关注/取关
                            iwechatFeignService.addWechatFollow(eventDTO);
                            Client client = ClientBuilder.newClient();
                        } catch (Exception e) {
                        }
                    }
                }
            }
            log.info("productInfoDTO:[{}]", JacksonUtil.objectToJson(productInfoDTO));
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
        return Mono.fromCallable(() -> {
            IntentListDTO intentListDTO = financeService.getIntentList(productId);
            intentListDTO.setNowDate(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
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
        UserPrincipal userPrincipal = WebContext.getCurrentUser();
        return Mono.fromCallable(() -> {

            Page<OperateDTO> page = financeService.getOperate(productId, entId, operate, PageRequest.of(pageNum - 1, size), userPrincipal.getOpenId());

            PageResponseDTO<OperateDTO> response = new PageResponseDTO(page.getContent(), page.getTotalPages(), page.getTotalElements(), pageNum, size);

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
        UserPrincipal userPrincipal = WebContext.getCurrentUser();
        return Mono.fromCallable(() -> {
            IntentInfoDTO intentInfoDTO = financeService.getIntetByIdNumber(productId, userPrincipal.getIdNumber());
            if (intentInfoDTO != null) {
                //产品信息
                ProductInfoDTO productInfoDTO = financeService.getProductInfo(productId, payrollProperties.getImgUrl());
                productInfoDTO.setNowDate(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                productInfoDTO.setWechatId(userPrincipal.getWechatId());
                productInfoDTO.setMinIntentAmt(10000);

                //企业预约人数
                Long intentNumber = financeService.getIntentNum(productId, intentInfoDTO.getEntId());
                productInfoDTO.setIntentNum(Math.toIntExact(intentNumber));

                //当前档位
                ProductInfoDTO.ProductMarkDTO mark = new ProductInfoDTO.ProductMarkDTO();
                for (int i = 0; i < productInfoDTO.getMarkList().size(); i++) {
                    if (productInfoDTO.getMarkList().get(i).getMarkLevel() == 1) {
                        mark = productInfoDTO.getMarkList().get(i);
                    }

                    if (intentNumber >= productInfoDTO.getMarkList().get(i).getMinPeople() && intentNumber <= productInfoDTO.getMarkList().get(i).getMaxPeople()) {
                        productInfoDTO.setNowMark(productInfoDTO.getMarkList().get(i).getMarkLevel());
                        productInfoDTO.getMarkList().get(i).setNowMark(IsStatusEnum.YES.getCode());
                        mark = productInfoDTO.getMarkList().get(i);
                    }
                    if (intentNumber >= productInfoDTO.getMarkList().get(i).getMinPeople()) {
                        productInfoDTO.getMarkList().get(i).setSucess(IsStatusEnum.YES.getCode());
                    }
                }

                //计算收益=金额*利率(0.05)*天数/365
                double amt = intentInfoDTO.getIntentAmount().doubleValue() * mark.getLevelRate() * 0.01 * productInfoDTO.getProductTerm() / 365;
                if (intentInfoDTO.getStatus().equals(IntentStatusEnum.SUBCRIBE_SUCESS.getCode())) {
                    amt = intentInfoDTO.getSubcribeAmount().doubleValue() * intentInfoDTO.getSubcribeRate() * 0.01 * productInfoDTO.getProductTerm() / 365;
                }
                intentInfoDTO.setProfit(new BigDecimal(amt).setScale(2, BigDecimal.ROUND_HALF_UP));

                intentInfoDTO.setProductInfoDTO(productInfoDTO);
                LocalDateTime dealDate = productInfoDTO.getSubscribeEndDate1().plusDays(Long.parseLong(productInfoDTO.getProductTerm().toString()));
                intentInfoDTO.setDealDateTime(dealDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                intentInfoDTO.setDealDay(Duration.between(LocalDateTime.now(), dealDate).toDays());
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
        UserPrincipal userPrincipal = WebContext.getCurrentUser();
        return Mono.fromCallable(() -> {
            if (StringUtils.isEmpty(userPrincipal.getIdNumber())) {
                throw new ParamsIllegalException(ErrorConstant.WECHAR_003.getErrorMsg());
            }

            String idNumber = userPrincipal.getIdNumber();
            String phone = userPrincipal.getPhone();

            UserInfoDTO userInfoDTO = financeService.getUserInfo(idNumber, entId);

            userInfoDTO.setIdNumber(idNumber);
            userInfoDTO.setIdNumberStar(TransUtil.idNumberStar(idNumber));
            userInfoDTO.setClientPhone(phone);
            userInfoDTO.setClientPhoneStar(TransUtil.phoneStar(phone));
            userInfoDTO.setNowDate(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());

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
        UserPrincipal userPrincipal = WebContext.getCurrentUser();
        return Mono.fromCallable(() -> {
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

            //产品是否可预约
            Long now = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            ProductInfoDTO productInfoDTO = financeService.getProductInfo(intentRequestDTO.getProductId(), payrollProperties.getImgUrl());
            if (productInfoDTO.getIntentFlag() == IsStatusEnum.NO.getCode()
                    || productInfoDTO.getIntentStartDate() > now || productInfoDTO.getIntentEndDate() < now) {
                throw new ParamsIllegalException(ErrorConstant.FINANCE_002.getErrorMsg());
            }

            //判断是否已预约
            IntentInfoDTO intentInfoDTO = financeService.getIntetByIdNumber(intentRequestDTO.getProductId(), userPrincipal.getIdNumber());
            if (intentInfoDTO != null) {
                throw new ParamsIllegalException(ErrorConstant.FINANCE_001.getErrorMsg());
            }
            intentRequestDTO.setOpenId(userPrincipal.getOpenId());

            financeService.addIntent(intentRequestDTO);
            log.info("预约完成！");
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
        return Mono.fromCallable(() -> {
            WeixinAuthorizeUrlDTO weixinAuthorizeUrlDTO = new WeixinAuthorizeUrlDTO();
            weixinAuthorizeUrlDTO.setUrl(redirectUrl);
            weixinAuthorizeUrlDTO.setState("STATE");
            String url = iwechatFeignService.getOAuthUrl(payrollProperties.getId(), weixinAuthorizeUrlDTO).getAuthorizeurl();
            return url;
        }).subscribeOn(Schedulers.elastic());
    }
}
