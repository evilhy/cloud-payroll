package chain.fxgj.server.payroll.controller;

import chain.css.log.annotation.TrackLog;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.fxgj.server.payroll.web.WebContext;
import chain.utils.commons.JacksonUtil;
import chain.wisales.client.feign.WelfareActivityFeignService;
import chain.wisales.core.constant.dictEnum.AreaEnum;
import chain.wisales.core.constant.dictEnum.IsStatusEnum;
import chain.wisales.core.dto.PageDTO;
import chain.wisales.core.dto.fxgj.welfare.*;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 工资条-唯销-员工福利
 */
@RestController
@Validated
@RequestMapping(value = "/wisales")
@Slf4j
public class WisalesController {

    @Autowired
    WelfareActivityFeignService welfareActivityFeignService;

    /**
     * 历史活动记录
     * 员工福利活动列表
     * @param pageNum
     * @param limit
     * @param idNumber
     * @return
     */
    @GetMapping("/welfareActivity/listByPayRoll")
    public Mono<PageDTO<WelfareActivityPayRollInfoDTO>> listByPayRoll(@RequestHeader(value = "pageNum",
            defaultValue = "1") int pageNum,
                                                               @RequestHeader(value = "limit",defaultValue = "10") int limit,
                                                               @RequestParam(required = false) String idNumber,
                                                               @RequestParam(required = false) String custPhoneNo){
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal principal = WebContext.getCurrentUser();
        String idNum = principal.getIdNumber();
        String phone = principal.getPhone();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            PageDTO<WelfareActivityPayRollInfoDTO> welfareActivityPayRollInfoDTOPageDTO = welfareActivityFeignService.listByPayRoll(pageNum, limit, idNum, phone);
            return welfareActivityPayRollInfoDTOPageDTO;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 图片预览
     * @param id
     * @return
     */
    @GetMapping("/h5/unAuth/img/{id}")
    public Mono<byte[]> lookImg(@NotNull @PathVariable String id,
                   ServerHttpResponse response){
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            byte[] bytes = welfareActivityFeignService.lookImg(id);
            return bytes;
        }).subscribeOn(Schedulers.elastic());
    }
    /**
     * 福利领取活动详情
     * @param idNumber
     * @param activityId
     * @return
     */
    @GetMapping("/welfareActivity/detailByPayRoll")
    public Mono<WelfareActivityPayRollDetailDTO> detailByPayRoll(@RequestParam(required = false) String idNumber,
                                                                 @RequestParam String activityId){
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal principal = WebContext.getCurrentUser();
        String idNum = principal.getIdNumber();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            WelfareActivityPayRollDetailDTO welfareActivityPayRollDetailDTO = welfareActivityFeignService.detailByPayRoll(idNum, activityId);
            return welfareActivityPayRollDetailDTO;
        }).subscribeOn(Schedulers.elastic());
    }
    /**
     * 福利活动商品列表
     * @param pageNum
     * @param limit
     * @param activityId
     * @param fItemCatId
     * @param sItemCatId
     * @param tItemCatId
     * @param minAmt
     * @param maxAmt
     * @return
     */
    @GetMapping("/welfareGoods/list")
    public Mono<WelfareGoodsInfoAllResDTO> baseList(@RequestHeader(value = "page-num",defaultValue = "1")int pageNum,
                                             @RequestHeader(value = "limit",defaultValue = "10") int limit,
                                             @Nullable @RequestParam String goodsNo,
                                             @Nullable @RequestParam String goodsName,
                                             @Nullable @RequestParam String activityId,
                                             @Nullable @RequestParam String fItemCatId,
                                             @Nullable @RequestParam String sItemCatId,
                                             @Nullable @RequestParam String tItemCatId,
                                             @Nullable @RequestParam BigDecimal minAmt,
                                             @Nullable @RequestParam BigDecimal maxAmt,
                                             @Nullable @RequestParam Boolean pickFlag){
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            //todo 待解决
//            WelfareGoodsInfoAllResDTO welfareGoodsInfoAllResDTO = new WelfareGoodsInfoAllResDTO();
            WelfareGoodsInfoAllResDTO welfareGoodsInfoAllResDTO = welfareActivityFeignService.baseList(pageNum, limit, goodsNo, goodsName, activityId, fItemCatId, sItemCatId, tItemCatId, minAmt, maxAmt, pickFlag, IsStatusEnum.YES.name());
            return welfareGoodsInfoAllResDTO;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 商品详情
     * @param idNumber 身份证
     * @param activityId 活动id
     * @param goodsInfoId 商品id
     * @return
     */
    @GetMapping("/welfareGoods/detail")
    public Mono<WelfareGoodsDetailDTO> detail(@RequestParam(required = false) String idNumber,
                                       @RequestParam String activityId,
                                       @RequestParam String goodsInfoId){
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal principal = WebContext.getCurrentUser();
        String idNum = principal.getIdNumber();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            WelfareGoodsDetailDTO detail = welfareActivityFeignService.detail(idNum, activityId, new ObjectId(goodsInfoId));
            return detail;
        }).subscribeOn(Schedulers.elastic());
    }

    // 兑换
    /**
     * 福利货柜活动商品兑换(实物兑换)
     * @param addCustTransOrderInfoDTO
     * @return
     */
    @PostMapping("welfareCustOrder/welfareExchangeGoods")
    @TrackLog
    public Mono<AddCustTransOrderInfoDTO> welfareExchangeGoods(@RequestBody AddCustTransOrderInfoDTO addCustTransOrderInfoDTO){
        try {
            log.info("addCustTransOrderInfoDTO:[{}]", JacksonUtil.objectToJson(addCustTransOrderInfoDTO));
        } catch (Exception e) {
            log.info("日志打印错误！");
        }
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal principal = WebContext.getCurrentUser();
        String idNum = principal.getIdNumber();
        addCustTransOrderInfoDTO.setIdNumber(idNum);
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            AddCustTransOrderInfoDTO addCustTransOrderInfoDTO1 = welfareActivityFeignService.welfareExchangeGoods(addCustTransOrderInfoDTO);
            return addCustTransOrderInfoDTO1;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 福利货柜活动商品兑换(虚拟卡券影视月卡电影票星巴克)
     *
     * @param addCustTransOrderInfoDTO
     * @return
     */
    @PostMapping("welfareCustOrder/welfareExchange")
    public Mono<AddCustTransOrderInfoDTO> welfareExchange(@RequestBody AddCustTransOrderInfoDTO addCustTransOrderInfoDTO){
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal principal = WebContext.getCurrentUser();
        String idNum = principal.getIdNumber();
        addCustTransOrderInfoDTO.setIdNumber(idNum);
        log.info("custTransOrderInfoDTO:[{}]", JacksonUtil.objectToJson(addCustTransOrderInfoDTO));
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            AddCustTransOrderInfoDTO addCustTransOrderInfoDTO1 = welfareActivityFeignService.welfareExchange(addCustTransOrderInfoDTO);
            return addCustTransOrderInfoDTO1;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 福利货柜活动商品兑换(话费流量兑换)
     * @param addCustTransOrderInfoDTO
     * @return
     */
    @PostMapping("welfareCustOrder/welfareExchangePhone")
    @TrackLog
    public Mono<AddCustTransOrderInfoDTO> welfareExchangePhone(@RequestBody AddCustTransOrderInfoDTO addCustTransOrderInfoDTO){
        log.info("addCustTransOrderInfoDTO:[{}]", JacksonUtil.objectToJson(addCustTransOrderInfoDTO));
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal principal = WebContext.getCurrentUser();
        String idNum = principal.getIdNumber();
        addCustTransOrderInfoDTO.setIdNumber(idNum);
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            AddCustTransOrderInfoDTO addCustTransOrderInfoDTO1 = welfareActivityFeignService.welfareExchangePhone(addCustTransOrderInfoDTO);
            return addCustTransOrderInfoDTO1;
        }).subscribeOn(Schedulers.elastic());
    }


    //收获地址
    /**
     * 查询客户收货地址-列表
     * @param idNumber
     * @return
     */
    @GetMapping("welfareCust/address/get")
    public Mono<PageDTO<WelfareCustAddressInfoDTO>> getCustAddress(@RequestParam(required = false) String idNumber){
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal principal = WebContext.getCurrentUser();
        String idNum = principal.getIdNumber();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            PageDTO<WelfareCustAddressInfoDTO> custAddress = welfareActivityFeignService.getCustAddress(idNum);
            return custAddress;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 根据收货地址ID 查询 客户地址 信息-地址详情
     * @param idNumber
     * @param addressId
     * @return
     */
    @GetMapping("welfareCust/address/getById")
    public Mono<WelfareCustAddressInfoDTO> getCustAddressById(@RequestParam(required = false) String idNumber,
                                                              @RequestParam String addressId){
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal principal = WebContext.getCurrentUser();
        String idNum = principal.getIdNumber();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            WelfareCustAddressInfoDTO custAddressById = welfareActivityFeignService.getCustAddressById(idNum, addressId);
            return custAddressById;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 新增、修改收货地址
     *
     * @param welfareCustAddressInfoDTO
     * @return
     */
    @PostMapping("welfareCust/address/save")
    public Mono<Void> addressSave(@RequestBody WelfareCustAddressInfoDTO welfareCustAddressInfoDTO){
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal principal = WebContext.getCurrentUser();
        String idNum = principal.getIdNumber();
        String phone = principal.getPhone();
        welfareCustAddressInfoDTO.setPhoneNo(phone);
        welfareCustAddressInfoDTO.setIdNumber(idNum);
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            welfareActivityFeignService.addressSave(welfareCustAddressInfoDTO);
            return null;
        }).subscribeOn(Schedulers.elastic()).then();
    }

    /**
     * 删除收货地址
     *
     * @param welfareCustAddressInfoDTO
     * @return
     */
    @PostMapping("welfareCust/address/delete")
    @TrackLog
    public Mono<Void> addressDelete(@RequestBody WelfareCustAddressInfoDTO welfareCustAddressInfoDTO){
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal principal = WebContext.getCurrentUser();
        String idNum = principal.getIdNumber();
        String phone = principal.getPhone();
        welfareCustAddressInfoDTO.setPhoneNo(phone);
        welfareCustAddressInfoDTO.setIdNumber(idNum);
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            Void aVoid = welfareActivityFeignService.addressDelete(welfareCustAddressInfoDTO);
            return null;
        }).subscribeOn(Schedulers.elastic()).then();
    }

    /**
     * 省市区收货地址区域查询
     * @param areaType
     * @param code
     * @return
     */
    @GetMapping("welfareCust/area/baseQuery")
    public Mono<List<WelfareAreaInfoDTO>> findAreaaseQuery(@RequestParam(required = false) AreaEnum areaType,
                                                    @RequestParam(required = false) String code){
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            List<WelfareAreaInfoDTO> areaaseQuery = welfareActivityFeignService.findAreaaseQuery(areaType, code);
            return areaaseQuery;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 悠彩乡镇街道收货地址区域查询
     * @param code
     * @return
     */
    @GetMapping("welfareCust/area/townQuery")
    public Mono<List<WelfareAreaInfoDTO>> findAreaQuery(@RequestParam(required = false) String code){
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            List<WelfareAreaInfoDTO> areaQuery = welfareActivityFeignService.findAreaQuery(code);
            return areaQuery;
        }).subscribeOn(Schedulers.elastic());
    }

    //兑换记录
    /**
     * 查询客户活动兑换记录列表
     *
     * @param activityId
     * @return
     */
    @GetMapping("welfareCust/custOrderList")
    public Mono<PageDTO<CustTransOrderInfoDTO>> findAllByPage(@RequestParam String activityId){
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal principal = WebContext.getCurrentUser();
        String idNumber = principal.getIdNumber();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            PageDTO<CustTransOrderInfoDTO> allByPage = welfareActivityFeignService.findAllByPage(idNumber, activityId);
            return allByPage;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 客户活动兑换明细查询
     *
     * @param transOrderId
     * @return
     */
    @GetMapping("welfareCust/custOrderDetail")
    public Mono<CustTransOrderInfoDetailDTO> findTransDtailById(@RequestParam String transOrderId){
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            CustTransOrderInfoDetailDTO transDtailById = welfareActivityFeignService.findTransDtailById(transOrderId);
            return transDtailById;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 根据系统下单订单号查询物流信息
     * @param transOrderId
     * @return
     */
    @GetMapping("welfareCust/orderTrack")
    public Mono<CustExchangeTransTrackResultDataDTO> findOrderTrack(@RequestParam("transOrderId") String transOrderId){
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            CustExchangeTransTrackResultDataDTO orderTrack = welfareActivityFeignService.findOrderTrack(transOrderId);
            return orderTrack;
        }).subscribeOn(Schedulers.elastic());
    }
}
