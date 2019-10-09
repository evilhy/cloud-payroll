package chain.fxgj.server.payroll.controller;

import chain.css.log.annotation.TrackLog;
import chain.fxgj.core.common.constant.DictEnums.AppPartnerEnum;
import chain.fxgj.core.common.constant.DictEnums.FundLiquidationEnum;
import chain.fxgj.feign.client.AdvertisingFeignService;
import chain.fxgj.feign.dto.advertising.WageAdvertisingRotationDTO;
import chain.fxgj.server.payroll.constant.PayrollConstants;
import chain.fxgj.server.payroll.dto.advertising.AdvertisingRotationDTO;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.fxgj.server.payroll.web.WebContext;
import chain.wisales.client.feign.WelfareActivityFeignService;
import chain.wisales.core.constant.dictEnum.AreaEnum;
import chain.wisales.core.constant.dictEnum.ItemCatEnum;
import chain.wisales.core.constant.dictEnum.WelfareActivityStatusEnum;
import chain.wisales.core.dto.PageDTO;
import chain.wisales.core.dto.fxgj.welfare.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.security.PermitAll;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
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
    Mono<PageDTO<WelfareActivityPayRollInfoDTO>> listByPayRoll(@RequestHeader(value = "pageNum",required = false) int pageNum,
                                                               @RequestHeader(value = "limit",required = false) int limit,
                                                               @RequestParam String idNumber,
                                                               @RequestParam String custPhoneNo){
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
    Mono<byte[]> lookImg(@NotNull @PathVariable String id,
                   ServerHttpResponse response){
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            byte[] bytes = welfareActivityFeignService.lookImg(id, response);
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
    Mono<WelfareActivityPayRollDetailDTO> detailByPayRoll(@RequestParam String idNumber, @RequestParam String activityId){
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
    Mono<WelfareGoodsInfoAllResDTO> baseList(@RequestHeader("pageNum") int pageNum,
                                             @RequestHeader("limit") int limit,
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
            WelfareGoodsInfoAllResDTO welfareGoodsInfoAllResDTO = welfareActivityFeignService.baseList(pageNum, limit, goodsNo, goodsName, activityId, fItemCatId, sItemCatId, tItemCatId, minAmt, maxAmt, pickFlag);
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
    Mono<WelfareGoodsDetailDTO> detail(@RequestParam String idNumber,
                                       @RequestParam String activityId,
                                       @RequestParam String goodsInfoId){
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal principal = WebContext.getCurrentUser();
        String idNum = principal.getIdNumber();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            WelfareGoodsDetailDTO detail = welfareActivityFeignService.detail(idNum, activityId, goodsInfoId);
            return detail;
        }).subscribeOn(Schedulers.elastic());
    }

    // 兑换
    /**
     * 福利货柜活动商品兑换(实物兑换)
     * @param custTransOrderInfoDTO
     * @return
     */
    @PostMapping("welfareCustOrder/welfareExchangeGoods")
    @TrackLog
    Mono<CustTransOrderInfoDTO> welfareExchangeGoods(@RequestBody CustTransOrderInfoDTO custTransOrderInfoDTO){
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal principal = WebContext.getCurrentUser();
        String idNum = principal.getIdNumber();
        String phone = principal.getPhone();
//        custTransOrderInfoDTO.setIdNumber(idNum);
//        custTransOrderInfoDTO.setPhoneNo(phone);
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            CustTransOrderInfoDTO custTransOrderInfoDTO1 = welfareActivityFeignService.welfareExchangeGoods(custTransOrderInfoDTO);
            return custTransOrderInfoDTO1;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 福利货柜活动商品兑换(虚拟卡券影视月卡电影票星巴克)
     *
     * @param custTransOrderInfoDTO
     * @return
     */
    @PostMapping("welfareCustOrder/welfareExchange")
    Mono<CustTransOrderInfoDTO> welfareExchange(@RequestBody CustTransOrderInfoDTO custTransOrderInfoDTO){
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal principal = WebContext.getCurrentUser();
        String idNum = principal.getIdNumber();
        String phone = principal.getPhone();
//        custTransOrderInfoDTO.setIdNumber(idNum);
//        custTransOrderInfoDTO.setPhoneNo(phone);
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            CustTransOrderInfoDTO custTransOrderInfoDTO1 = welfareActivityFeignService.welfareExchange(custTransOrderInfoDTO);
            return custTransOrderInfoDTO1;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 福利货柜活动商品兑换(话费流量兑换)
     * @param custTransOrderInfoDTO
     * @return
     */
    @PostMapping("welfareCustOrder/welfareExchangePhone")
    @TrackLog
    Mono<CustTransOrderInfoDTO> welfareExchangePhone(@RequestBody CustTransOrderInfoDTO custTransOrderInfoDTO){
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal principal = WebContext.getCurrentUser();
        String idNum = principal.getIdNumber();
        String phone = principal.getPhone();
//        custTransOrderInfoDTO.setIdNumber(idNum);
//        custTransOrderInfoDTO.setPhoneNo(phone);
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            CustTransOrderInfoDTO custTransOrderInfoDTO1 = welfareActivityFeignService.welfareExchangePhone(custTransOrderInfoDTO);
            return custTransOrderInfoDTO1;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 福利货柜活动商品兑换(油卡兑换)
     * @param custTransOrderInfoDTO
     * @return
     */
    @PostMapping("welfareCustOrder/welfareExchangeOilCard")
    Mono<CustTransOrderInfoDTO> welfareExchangeOilCard(@RequestBody CustTransOrderInfoDTO custTransOrderInfoDTO){
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal principal = WebContext.getCurrentUser();
        String idNum = principal.getIdNumber();
        String phone = principal.getPhone();
//        custTransOrderInfoDTO.setIdNumber(idNum);
//        custTransOrderInfoDTO.setPhoneNo(phone);
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            CustTransOrderInfoDTO custTransOrderInfoDTO1 = welfareActivityFeignService.welfareExchangeOilCard(custTransOrderInfoDTO);
            return custTransOrderInfoDTO1;
        }).subscribeOn(Schedulers.elastic());
    }

    //收获地址
    /**
     * 查询客户收货地址-列表
     * @param idNumber
     * @return
     */
    @GetMapping("welfareCust/address/get")
    Mono<PageDTO<WelfareCustAddressInfoDTO>> getCustAddress(@RequestParam String idNumber){
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
    Mono<WelfareCustAddressInfoDTO> getCustAddressById(@RequestParam String idNumber, @RequestParam String addressId){
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
    Mono<WelfareCustAddressInfoDTO> addressSave(@RequestBody WelfareCustAddressInfoDTO welfareCustAddressInfoDTO){
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal principal = WebContext.getCurrentUser();
        String idNum = principal.getIdNumber();
        String phone = principal.getPhone();
//        welfareCustAddressInfoDTO.setPhoneNo(phone);
//        welfareCustAddressInfoDTO.setIdNumber(idNum);
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            WelfareCustAddressInfoDTO welfareCustAddressInfoDTO1 = welfareActivityFeignService.addressSave(welfareCustAddressInfoDTO);
            return welfareCustAddressInfoDTO1;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 删除收货地址
     *
     * @param welfareCustAddressInfoDTO
     * @return
     */
    @PostMapping("welfareCust/address/delete")
    @TrackLog
    Mono<Void> addressDelete(@RequestBody WelfareCustAddressInfoDTO welfareCustAddressInfoDTO){
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal principal = WebContext.getCurrentUser();
        String idNum = principal.getIdNumber();
        String phone = principal.getPhone();
//        welfareCustAddressInfoDTO.setPhoneNo(phone);
//        welfareCustAddressInfoDTO.setIdNumber(idNum);
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
    Mono<List<WelfareAreaInfoDTO>> findAreaaseQuery(@RequestParam(required = false) AreaEnum areaType,
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
    Mono<List<WelfareAreaInfoDTO>> findAreaQuery(@RequestParam String code){
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
     * @param phoneNo
     * @param activityId
     * @return
     */
    @GetMapping("welfareCust/custOrderList")
    Mono<PageDTO<CustTransOrderInfoDTO>> findAllByPage(@RequestParam String phoneNo, @RequestParam String activityId){
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal principal = WebContext.getCurrentUser();
        String phone = principal.getPhone();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            PageDTO<CustTransOrderInfoDTO> allByPage = welfareActivityFeignService.findAllByPage(phone, activityId);
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
    Mono<CustTransOrderInfoDTO> findTransDtailById(@RequestParam String transOrderId){
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            CustTransOrderInfoDTO transDtailById = welfareActivityFeignService.findTransDtailById(transOrderId);
            return transDtailById;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 根据系统下单订单号查询物流信息
     * @param transLogId
     * @return
     */
    @GetMapping("welfareCust/orderTrack")
    Mono<CustExchangeTransTrackResultDataDTO> findOrderTrack(@RequestParam String transLogId){
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            CustExchangeTransTrackResultDataDTO orderTrack = welfareActivityFeignService.findOrderTrack(transLogId);
            return orderTrack;
        }).subscribeOn(Schedulers.elastic());
    }
}
