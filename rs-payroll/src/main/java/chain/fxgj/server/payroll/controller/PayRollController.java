package chain.fxgj.server.payroll.controller;

import chain.css.exception.ErrorMsg;
import chain.css.exception.ParamsIllegalException;
import chain.css.log.annotation.TrackLog;
import chain.fxgj.core.common.constant.DictEnums.FundLiquidationEnum;
import chain.fxgj.core.common.constant.ErrorConstant;
import chain.fxgj.core.common.constant.FxgjDBConstant;
import chain.fxgj.feign.client.PayRollFeignService;
import chain.fxgj.feign.client.SynTimerFeignService;
import chain.fxgj.feign.dto.CheckCardDTO;
import chain.fxgj.feign.dto.response.*;
import chain.fxgj.feign.dto.web.WageUserPrincipal;
import chain.fxgj.server.payroll.dto.payroll.CheckPwdDTO;
import chain.fxgj.server.payroll.dto.payroll.EntEmpDTO;
import chain.fxgj.server.payroll.dto.request.ReqPhone;
import chain.fxgj.server.payroll.dto.response.*;
import chain.fxgj.server.payroll.dto.response.BankCard;
import chain.fxgj.server.payroll.service.WechatRedisService;
import chain.fxgj.server.payroll.util.EncrytorUtils;
import chain.fxgj.server.payroll.util.SensitiveInfoUtils;
import chain.fxgj.server.payroll.util.TransferUtil;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.fxgj.server.payroll.web.WebContext;
import chain.payroll.client.feign.PayrollFeignController;
import chain.payroll.dto.response.*;
import chain.utils.commons.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * 工资条
 */
@RestController
@Validated
@RequestMapping(value = "/roll")
@Slf4j
@SuppressWarnings("unchecked")
public class PayRollController {
    @Resource
    RedisTemplate redisTemplate;
    @Autowired
    PayrollFeignController payrollFeignController;
    @Autowired
    @Qualifier("applicationTaskExecutor")
    Executor executor;
    @Autowired
    private PayRollFeignService wageMangerFeignService;
    @Autowired
    private SynTimerFeignService wageSynFeignService;
    @Autowired
    private WechatRedisService wechatRedisService;

    /**
     * 服务当前时间
     *
     * @return
     */
    @GetMapping("/sdt")
    @TrackLog
    @PermitAll
    public Mono<Long> serverDateTime() {
        return Mono.fromCallable(
                () -> LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        ).subscribeOn(Schedulers.elastic());
    }


    /**
     * 首页
     */
    @GetMapping("/index")
    @TrackLog
    public Mono<IndexDTO> index() {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        UserPrincipal principal = WebContext.getCurrentUser();
        String idNumber = principal.getIdNumber();

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            WageUserPrincipal wageUserPrincipal = TransferUtil.userPrincipalToWageUserPrincipal(principal);
            log.info("wageMangerFeignService.index(wageUserPrincipal)开始");
            WageIndexDTO wageIndexDTO = wageMangerFeignService.index(wageUserPrincipal);
            NewestWageLogDTO newestWageLogDTO = new NewestWageLogDTO();
            Integer isNew = 0;
            if (null != wageIndexDTO) {
                WageNewestWageLogDTO wageNewestWageLogDTO = wageIndexDTO.getBean();
                BeanUtils.copyProperties(wageNewestWageLogDTO,newestWageLogDTO);
                isNew = wageIndexDTO.getIsNew();
            }
            IndexDTO indexDTO = new IndexDTO();
            indexDTO.setBean(newestWageLogDTO);
            indexDTO.setIsNew(isNew);
            return indexDTO;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 企业机构列表
     *
     * @return
     */
    @GetMapping("/groupList")
    @TrackLog
    public Mono<List<NewestWageLogDTO>> groupList() {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        UserPrincipal principal = WebContext.getCurrentUser();
        String idNumber = principal.getIdNumber();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
//            WageUserPrincipal wageUserPrincipal=new WageUserPrincipal();
//            BeanUtils.copyProperties(principal,wageUserPrincipal);
            WageUserPrincipal wageUserPrincipal=TransferUtil.userPrincipalToWageUserPrincipal(principal);

            log.info("调用wageMangerFeignService.groupList(wageUserPrincipal)开始");
            List<WageNewestWageLogDTO> wageNewestWageLogDTOS=wageMangerFeignService.groupList(wageUserPrincipal);
            log.info("groupList--->{}",wageNewestWageLogDTOS.size());
            List<NewestWageLogDTO> list=null;
            if (!CollectionUtils.isEmpty(wageNewestWageLogDTOS)){
                list=new ArrayList<>();
                for (WageNewestWageLogDTO wageLogDTO:wageNewestWageLogDTOS){
                    NewestWageLogDTO dto=new NewestWageLogDTO();
                    BeanUtils.copyProperties(wageLogDTO,dto);
                    list.add(dto);
                }
            }
            log.info("NewestWageLogDTOList:[{}]", JacksonUtil.objectToJson(list));
            return list;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 根据身份账号返回手机和公司列表
     *
     * @param idNumber 身份证号
     * @return
     */
//    @GetMapping("/entEmp")
//    @TrackLog
//    public Mono<Res100701> entEmp(@RequestParam("idNumber") String idNumber) {
//        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
//        UserPrincipal principal = WebContext.getCurrentUser();
//
//        return Mono.fromCallable(() -> {
//            MDC.setContextMap(mdcContext);
//            WageUserPrincipal wageUserPrincipal = TransferUtil.userPrincipalToWageUserPrincipal(principal);
//            Res100701 res100701=null;
//            log.info("调用wageMangerFeignService.entEmp(idNumber,wageUserPrincipal)开始");
//            WageRes100701 wageRes100701=wageMangerFeignService.entEmp(idNumber,wageUserPrincipal);
//            log.info("wageRes100701:[{}]",JacksonUtil.objectToJson(wageRes100701));
//            if (wageRes100701!=null){
//                res100701=new Res100701();
//                BeanUtils.copyProperties(wageRes100701,res100701);
//            }
//            return res100701;
//        }).subscribeOn(Schedulers.elastic());
//    }

    /**
     * 根据身份账号返回手机和公司列表
     *
     * @return
     */
    @PostMapping("/entEmp")
    @TrackLog
    public Mono<Res100701> entEmp(@RequestBody EntEmpDTO entEmpDTO, @RequestHeader(value = "encry-salt", required = false) String salt,
        @RequestHeader(value = "encry-passwd", required = false) String passwd) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal principal = WebContext.getCurrentUser();
        String idNumber = entEmpDTO.getIdNumber();
        if (StringUtils.isBlank(idNumber)) {
            throw new ParamsIllegalException(new ErrorMsg("9999", "请输入身份证!"));
        }
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            WageUserPrincipal wageUserPrincipal = TransferUtil.userPrincipalToWageUserPrincipal(principal);
            Res100701 res100701=null;
            log.info("调用wageMangerFeignService.entEmp(idNumber,wageUserPrincipal)开始");
            WageRes100701 wageRes100701=wageMangerFeignService.entEmp(idNumber,wageUserPrincipal);
            log.info("wageRes100701:[{}]",JacksonUtil.objectToJson(wageRes100701));
            if (wageRes100701!=null){
                res100701=new Res100701();
                BeanUtils.copyProperties(wageRes100701,res100701);
                List<EmployeeListBean> employeeListBeanList = new ArrayList<>();
                List<WageEmployeeListBean> wageEmployeeListBeanList = wageRes100701.getEmployeeList();
                if (null != wageEmployeeListBeanList && wageEmployeeListBeanList.size() > 0) {
                    for (WageEmployeeListBean wageEmployeeListBean : wageEmployeeListBeanList) {

                        EmployeeListBean employeeListBean = new EmployeeListBean();
                        employeeListBean.setEmployeeName(EncrytorUtils.encryptField(wageEmployeeListBean.getEmployeeName(), salt, passwd));
                        employeeListBean.setEntId(EncrytorUtils.encryptField(wageEmployeeListBean.getEntId(), salt, passwd));
                        employeeListBean.setEntName(EncrytorUtils.encryptField(wageEmployeeListBean.getEntName(), salt, passwd));
                        employeeListBean.setIdNumber(EncrytorUtils.encryptField(wageEmployeeListBean.getIdNumber(), salt, passwd));
                        employeeListBean.setPhone(EncrytorUtils.encryptField(wageEmployeeListBean.getPhone(), salt, passwd));

                        employeeListBean.setSalt(salt);
                        employeeListBean.setPasswd(passwd);
                        employeeListBean.setPhoneStar(wageEmployeeListBean.getPhoneStar());
                        employeeListBean.setSex(wageEmployeeListBean.getSex());

                        employeeListBeanList.add(employeeListBean);
                    }
                }
                res100701.setEmployeeList(employeeListBeanList);

            }
            log.info("entEmp.res100701:[{}]", JacksonUtil.objectToJson(res100701));
            return res100701;
        }).subscribeOn(Schedulers.elastic());
    }
//
//    /**
//     * 个人薪资列表
//     *
//     * @param groupId 机构Id
//     * @param year    年份
//     * @param type    类型 0资金到账 1合计
//     * @return
//     */
//    @GetMapping("/wageList")
//    @TrackLog
//    public Mono<Res100703> wageList(@RequestParam("groupId") String groupId,
//                                    @RequestParam("year") String year,
//                                    @RequestParam("type") String type) {
//        log.info("调用wageList开始时间::[{}]",LocalDateTime.now());
//        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
//        UserPrincipal principal = WebContext.getCurrentUser();
//        PayrollUserPrincipalDTO payrollUserPrincipalDTO = new PayrollUserPrincipalDTO();
//        BeanUtils.copyProperties(principal, payrollUserPrincipalDTO);
//        String idNumber = principal.getIdNumber();
//        return Mono.fromCallable(() -> {
//            MDC.setContextMap(mdcContext);
//            Res100703 res100703 = new Res100703();
//            boolean qryMySql = false;
//            try {
//                PayrollRes100703ReqDTO payrollRes100703ReqDTO = new PayrollRes100703ReqDTO();
//                payrollRes100703ReqDTO.setGroupId(groupId);
//                payrollRes100703ReqDTO.setYear(year);
//                payrollRes100703ReqDTO.setType(type);
//                payrollRes100703ReqDTO.setIdNumber(idNumber);
//                payrollRes100703ReqDTO.setPayrollUserPrincipalDTO(payrollUserPrincipalDTO);
//                log.info("groupId:[{}]，year:[{}]，type:[{}]，idNumber:[{}]",groupId, year, type, idNumber);
////                //先查redis
////                PayrollRes100703DTO source = wechatRedisService.wageListByMongo(idNumber, groupId, year, type);
////                if (null == source) {
////                    log.info("redis未查询到wageListMongo数据！");
////                    source = payrollFeignController.wageList(payrollRes100703ReqDTO);
////                } else {
////                    log.info("redis有wageListMongo缓存有数据！");
////                }
//                log.info("调用payrollFeignController.wageList(payrollRes100703ReqDTO)开始时间:[{}]", LocalDateTime.now());
//                PayrollRes100703DTO source = payrollFeignController.wageList(payrollRes100703ReqDTO);
//                log.info("调用payrollFeignController.wageList(payrollRes100703ReqDTO)返回时间:[{}]", LocalDateTime.now());
//                res100703.setShouldTotalAmt(source.getShouldTotalAmt());
//                res100703.setDeductTotalAmt(source.getDeductTotalAmt());
//                res100703.setEmployeeSid(source.getEmployeeSid());
//                res100703.setRealTotalAmt(source.getRealTotalAmt());
//                List<Integer> years = source.getYears();
//                res100703.setYears(years);
//
//                List<PayrollPlanListDTO> planListSource = source.getPlanList();
//                List<PlanListBean> planListBeans = new ArrayList<>();
//                if (null != planListSource && planListSource.size() > 0) {
//                    for (PayrollPlanListDTO payrollPlanListDTO : planListSource) {
//                        PlanListBean planListBean = new PlanListBean();
//                        BeanUtils.copyProperties(payrollPlanListDTO, planListBean);
//                        planListBeans.add(planListBean);
//                    }
//                    res100703.setPlanList(planListBeans);
//                    log.info("res100703:[{}]", JacksonUtil.objectToJson(res100703));
//
//                    //判断是否需要数据同步(比较 mongo 和 mysql 中最新的sheetId 是否相同，不相同则数据同步)
//                    PayrollPlanListDTO payrollPlanListDTO = planListSource.get(0);
//                    String mongoNewestWageSheetId = payrollPlanListDTO.getWageSheetId();
//                    boolean retBoolean =wageMangerFeignService.compareSheetCrtDataTime(idNumber, groupId, mongoNewestWageSheetId);
//                    if (!retBoolean) {
//                        log.info("mongo库中，wageSheetInfo最新sheetId 与 Mysql中 最新sheetId 不相等，则需要同步数据");
//                        mysqlDataSynToMongo(idNumber,groupId,year,type,principal);
//                    }
//                }
//                if (null == planListSource || planListSource.size() == 0) {
//                    log.info("wageList查询mongo数据为空，转查mysql,idNumber:[{}]", idNumber);
//                    qryMySql = true;
//                }
//            } catch (Exception e) {
//                qryMySql = true;
//                log.info("wageList查询mongo异常，转查mysql,idNumber:[{}]", idNumber);
//            }
//            if (qryMySql) {
//                WageUserPrincipal wageUserPrincipal=new WageUserPrincipal();
//                BeanUtils.copyProperties(principal,wageUserPrincipal);
////                //先查redis
////                WageRes100703 wageRes100703 = wechatRedisService.wageListByMysql(idNumber, groupId, year, type);
////                if (null == wageRes100703) {
////                    log.info("redis中未查到wageListMysql数据！");
////                    wageRes100703 = wageMangerFeignService.wageList(groupId, year, type, wageUserPrincipal);
////                } else {
////                    log.info("redis中有wageListMysql数据！");
////                }
//                log.info("调用wageMangerFeignService.wageList(groupId, year, type, wageUserPrincipal)开始时间:[{}]", LocalDateTime.now());
//                WageRes100703 wageRes100703 = wageMangerFeignService.wageList(groupId, year, type, wageUserPrincipal);
//                log.info("调用wageMangerFeignService.wageList(groupId, year, type, wageUserPrincipal)结束时间:[{}]", LocalDateTime.now());
//                log.info("wage wageRes100703:[{}]",JacksonUtil.objectToJson(wageRes100703));
//                if (wageRes100703!=null){
//                    res100703.setShouldTotalAmt(wageRes100703.getShouldTotalAmt());
//                    res100703.setDeductTotalAmt(wageRes100703.getDeductTotalAmt());
//                    res100703.setEmployeeSid(wageRes100703.getEmployeeSid());
//                    res100703.setRealTotalAmt(wageRes100703.getRealTotalAmt());
//                    List<Integer> years = wageRes100703.getYears();
//                    res100703.setYears(years);
//                    List<WagePlanListBean> planList = wageRes100703.getPlanList();
//                    List<PlanListBean> planListBeans = new ArrayList<>();
//                    if (null != planList && planList.size() > 0) {
//                        for (WagePlanListBean wagePlanListBean : planList) {
//                            PlanListBean planListBean = new PlanListBean();
//                            BeanUtils.copyProperties(wagePlanListBean, planListBean);
//                            planListBeans.add(planListBean);
//                        }
//                        res100703.setPlanList(planListBeans);
//                    }
//                log.info("转换后 res100703:[{}]", JacksonUtil.objectToJson(res100703));
//                }
//                log.info("数据同步");
//                mysqlDataSynToMongo(idNumber,groupId,year,type,principal);
//            }
//            log.info("调用wageList返回时间::[{}]",LocalDateTime.now());
//            return res100703;
//        }).subscribeOn(Schedulers.elastic());
//    }
//
//    /**
//     * 查看工资条详情
//     *
//     * @param wageSheetId 方案id
//     * @param groupId     机构id
//     * @return
//     */
//    @GetMapping("/wageDetail")
//    @TrackLog
//    public Mono<List<WageDetailDTO>> wageDetail(@RequestParam("wageSheetId") String wageSheetId,
//                                                @RequestParam("groupId") String groupId) {
//        log.info("调用wageDetail开始时间::[{}]",LocalDateTime.now());
//        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
//        UserPrincipal principal = WebContext.getCurrentUser();
//        String idNumber = principal.getIdNumber();
//        PayrollUserPrincipalDTO payrollUserPrincipalDTO = new PayrollUserPrincipalDTO();
//        BeanUtils.copyProperties(principal, payrollUserPrincipalDTO);
//        return Mono.fromCallable(() -> {
//            MDC.setContextMap(mdcContext);
//            boolean qryMySql = false;
//            List<WageDetailDTO> list = new ArrayList<>();
//            try {
//                PayrollWageDetailReqDTO payrollWageDetailReqDTO = new PayrollWageDetailReqDTO();
//                payrollWageDetailReqDTO.setIdNumber(idNumber);
//                payrollWageDetailReqDTO.setGroupId(groupId);
//                payrollWageDetailReqDTO.setWageSheetId(wageSheetId);
//                payrollWageDetailReqDTO.setPayrollUserPrincipalDTO(payrollUserPrincipalDTO);
//                log.info("groupId:[{}]，idNumber:[{}]，wageSheetId:[{}]",groupId, idNumber,wageSheetId);
////                //先查redis从缓存中获取数据，缓存中没数据时，再调服务查询
////                List<PayrollWageDetailDTO> source = wechatRedisService.getWageDetailByMongo(idNumber, groupId, wageSheetId);
////                if (null == source || source.size() == 0) {
////                    log.info("redis中未查询到wageDetailMongo数据");
////                    source = payrollFeignController.wageDetail(payrollWageDetailReqDTO);
////                }else {
////                    log.info("redis有wageDetailMongo数据");
////                }
//                log.info("调用payrollFeignController.wageDetail(payrollWageDetailReqDTO)，查明细开始时间:[{}]", LocalDateTime.now());
//                List<PayrollWageDetailDTO> source = payrollFeignController.wageDetail(payrollWageDetailReqDTO);
//                log.info("调用payrollFeignController.wageDetail(payrollWageDetailReqDTO)，查明细结束时间:[{}]", LocalDateTime.now());
//                log.info("source.size():[{}]",source.size());
//                for (PayrollWageDetailDTO payrollWageDetailDTO : source) {
//                    WageDetailDTO wageDetailDTO = new WageDetailDTO();
//
//                    PayrollWageHeadDTO payrollWageHeadDTO = payrollWageDetailDTO.getWageHeadDTO();
//                    WageHeadDTO wageHeadDTO = new WageHeadDTO();
//                    wageHeadDTO.setDoubleRow(payrollWageHeadDTO.isDoubleRow());
//                    wageHeadDTO.setHeadIndex(payrollWageHeadDTO.getHeadIndex());
//                    List<PayrollWageHeadDTO.Cell> heads = payrollWageHeadDTO.getHeads();
//                    List<WageHeadDTO.Cell> headsList = new ArrayList<>();
//                    for (PayrollWageHeadDTO.Cell head : heads) {
//                        WageHeadDTO.Cell cell = new WageHeadDTO.Cell();
//                        cell.setColName(head.getColName());
//                        cell.setColNum(head.getColNum());
//                        cell.setHidden(head.isHidden());
//                        PayrollWageHeadDTO.Type type = head.getType();
//                        WageHeadDTO.Type type1 = WageHeadDTO.Type.valueOf(type.name());
//                        cell.setType(type1);
//                        headsList.add(cell);
//                    }
//                    wageHeadDTO.setHeads(headsList);
//                    wageDetailDTO.setWageHeadDTO(wageHeadDTO);
//
//                    PayrollWageDetailDTO.PayrollWageShowDTO payrollWageShowDTO = payrollWageDetailDTO.getWageShowDTO();
//                    WageDetailDTO.WageShowDTO wageShowDTO = new WageDetailDTO.WageShowDTO();
//                    BeanUtils.copyProperties(payrollWageShowDTO, wageShowDTO);
//                    wageDetailDTO.setWageShowDTO(wageShowDTO);
//
//                    List<PayrollWageDetailDTO.Content> payrollContent = payrollWageDetailDTO.getContent();
//                    List<WageDetailDTO.Content> contentList = new ArrayList<>();
//                    for (PayrollWageDetailDTO.Content content : payrollContent) {
//                        WageDetailDTO.Content content1 = new WageDetailDTO.Content();
//                        BeanUtils.copyProperties(content, content1);
//                        contentList.add(content1);
//                    }
//                    wageDetailDTO.setContent(contentList);
//
//                    wageDetailDTO.setWageDetailId(payrollWageDetailDTO.getWageDetailId());
//                    wageDetailDTO.setBankName(payrollWageDetailDTO.getBankName());
//                    wageDetailDTO.setCardNo(payrollWageDetailDTO.getCardNo());
//                    wageDetailDTO.setWageName(payrollWageDetailDTO.getWageName());
//                    wageDetailDTO.setRealAmt(payrollWageDetailDTO.getRealAmt());
//                    wageDetailDTO.setEntName(payrollWageDetailDTO.getEntName());
//                    wageDetailDTO.setGroupName(payrollWageDetailDTO.getGroupName());
//                    wageDetailDTO.setGroupId(payrollWageDetailDTO.getGroupId());
//                    wageDetailDTO.setPushDateTime(payrollWageDetailDTO.getPushDateTime());
//                    wageDetailDTO.setReceiptStautus(payrollWageDetailDTO.getReceiptStautus());
//                    wageDetailDTO.setDifferRealAmt(payrollWageDetailDTO.getDifferRealAmt());
//                    wageDetailDTO.setPayStatus(payrollWageDetailDTO.getPayStatus());
//                    list.add(wageDetailDTO);
//                }
//                if (null == list || list.size() == 0) {
//                    log.info("wageDetail查询mongo数据为空，转查mysql,idNumber:[{}]", idNumber);
//                    qryMySql = true;
//                }
//            } catch (Exception e) {
//                log.info("wageDetail查询mongo异常，转查mysql,idNumber:[{}]",idNumber);
//                log.info("查询mongo异常:[{}]",e);
//                qryMySql = true;
//            }
//            //查询mongo异常，转查mysql
//            log.info("qryMySql:[{}]",qryMySql);
//            if (qryMySql) {
//                WageUserPrincipal wageUserPrincipal=new WageUserPrincipal();
//                BeanUtils.copyProperties(principal,wageUserPrincipal);
////                //先查redis
////                List<WageDetailInfoDTO> wageDetailInfoDTOList = wechatRedisService.getWageDetailByMysql(idNumber, groupId, wageSheetId);
////                if (null == wageDetailInfoDTOList || wageDetailInfoDTOList.size() == 0) {
////                    log.info("redis中未查询到wageDetailMysql数据！");
////                    wageDetailInfoDTOList = wageMangerFeignService.wageDetail(wageSheetId, groupId, wageUserPrincipal);
////                } else {
////                    log.info("redis中有wageDetailMysql数据！");
////                }
//                log.info("调用wageMangerFeignService.wageDetail(wageSheetId, groupId, wageUserPrincipal)查明细开始时间:[{}]", LocalDateTime.now());
//                List<WageDetailInfoDTO> wageDetailInfoDTOList =wageMangerFeignService.wageDetail(wageSheetId, groupId, wageUserPrincipal);
//                log.info("调用wageMangerFeignService.wageDetail(wageSheetId, groupId, wageUserPrincipal)查明细结束时间:[{}]", LocalDateTime.now());
//                log.info("wageDetailInfoDTOList--->{}",wageDetailInfoDTOList);
//                if(!CollectionUtils.isEmpty(wageDetailInfoDTOList)){
//                    if (list==null){
//                        list=new ArrayList<>();
//                    }
//                    for (WageDetailInfoDTO wageDetailInfoDTO:wageDetailInfoDTOList){
//                        WageDetailDTO wageDetailDTO = new WageDetailDTO();
//
//                        WageWageHeadDTO wageHeadDTO1 = wageDetailInfoDTO.getWageHeadDTO();
//                        WageHeadDTO wageHeadDTO = new WageHeadDTO();
//                        wageHeadDTO.setDoubleRow(wageHeadDTO1.isDoubleRow());
//                        wageHeadDTO.setHeadIndex(wageHeadDTO1.getHeadIndex());
//                        List<WageWageHeadDTO.Cell> heads = wageHeadDTO1.getHeads();
//                        List<WageHeadDTO.Cell> headsList = new ArrayList<>();
//                        for (WageWageHeadDTO.Cell head : heads) {
//                            WageHeadDTO.Cell cell = new WageHeadDTO.Cell();
//                            cell.setColName(head.getColName());
//                            cell.setColNum(head.getColNum());
//                            cell.setHidden(head.isHidden());
//                            WageWageHeadDTO.Type type = head.getType();
//                            WageHeadDTO.Type type1 = WageHeadDTO.Type.valueOf(type.name());
//                            cell.setType(type1);
//                            headsList.add(cell);
//                        }
//                        wageHeadDTO.setHeads(headsList);
//                        wageDetailDTO.setWageHeadDTO(wageHeadDTO);
//
//                        WageDetailInfoDTO.WageShowDTO wageShowDTO1 = wageDetailInfoDTO.getWageShowDTO();
//                        WageDetailDTO.WageShowDTO wageShowDTO = new WageDetailDTO.WageShowDTO();
//                        BeanUtils.copyProperties(wageShowDTO1, wageShowDTO);
//                        wageDetailDTO.setWageShowDTO(wageShowDTO);
//
//                        List<WageDetailInfoDTO.Content> payrollContent = wageDetailInfoDTO.getContent();
//                        List<WageDetailDTO.Content> contentList = new ArrayList<>();
//                        for (WageDetailInfoDTO.Content content : payrollContent) {
//                            WageDetailDTO.Content content1 = new WageDetailDTO.Content();
//                            BeanUtils.copyProperties(content, content1);
//                            contentList.add(content1);
//                        }
//                        wageDetailDTO.setContent(contentList);
//
//                        wageDetailDTO.setWageDetailId(wageDetailInfoDTO.getWageDetailId());
//                        wageDetailDTO.setBankName(wageDetailInfoDTO.getBankName());
//                        wageDetailDTO.setCardNo(wageDetailInfoDTO.getCardNo());
//                        wageDetailDTO.setWageName(wageDetailInfoDTO.getWageName());
//                        wageDetailDTO.setRealAmt(wageDetailInfoDTO.getRealAmt());
//                        wageDetailDTO.setEntName(wageDetailInfoDTO.getEntName());
//                        wageDetailDTO.setGroupName(wageDetailInfoDTO.getGroupName());
//                        wageDetailDTO.setGroupId(wageDetailInfoDTO.getGroupId());
//                        wageDetailDTO.setPushDateTime(wageDetailInfoDTO.getPushDateTime());
//                        wageDetailDTO.setReceiptStautus(wageDetailInfoDTO.getReceiptStautus());
//                        wageDetailDTO.setDifferRealAmt(wageDetailInfoDTO.getDifferRealAmt());
//                        wageDetailDTO.setPayStatus(wageDetailInfoDTO.getPayStatus());
//                        list.add(wageDetailDTO);
//                    }
//                }
//                try {
//                    WageDetailDTO wageDetailDTO = list.get(0);
//                    log.info("wageDetail.get(0):[{}]", JacksonUtil.objectToJson(wageDetailDTO));
//                    Long pushDateTime = wageDetailDTO.getPushDateTime();
//                    log.info("pushDetailTime:[{}]", pushDateTime);
//                    LocalDateTime pushDateTimeLocal = LocalDateTime.ofInstant(Instant.ofEpochMilli(pushDateTime), ZoneId.systemDefault());
//                    log.info("pushDateTimeLocal:[{}]",pushDateTimeLocal);
//                    int year = pushDateTimeLocal.getYear();
//                    mysqlDataSynToMongo(idNumber,groupId,String.valueOf(year),null,principal);
//                } catch (Exception e) {
//                    log.info("wageDetail 同步数据异常！:[{}]", e);
//                }
//            }
//            log.info("web.list:[{}]",JacksonUtil.objectToJson(list));
//            log.info("调用wageDetail返回时间::[{}]",LocalDateTime.now());
//            return list;
//        }).subscribeOn(Schedulers.elastic());
//    }

    /**
     * 员工个人信息
     *
     * @return
     */
    @GetMapping("/empInfo")
    @TrackLog
    public Mono<List<Res100708>> empInfo() {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal principal = WebContext.getCurrentUser();
        String idNumber = principal.getIdNumber();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            WageUserPrincipal wageUserPrincipal=new WageUserPrincipal();
            BeanUtils.copyProperties(principal,wageUserPrincipal);
            log.info("调用wageMangerFeignService.empInfo(wageUserPrincipal)开始");
            List<WageRes100708> wageRes100708List=wageMangerFeignService.empInfo(wageUserPrincipal);
            log.info("wageRes100708List-->{}",wageRes100708List.size());
            List<Res100708> res100708=null;
            if (!CollectionUtils.isEmpty(wageRes100708List)){
                res100708=new ArrayList<>();
                for (WageRes100708 res1007081:wageRes100708List){
                    Res100708 res=new Res100708();
                    BeanUtils.copyProperties(res1007081,res);
                    res100708.add(res);
                }
            }
            return res100708;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 查询发票信息列表【@】
     *
     * @return
     */
    @GetMapping("/invoice")
    @TrackLog
    public Mono<List<GroupInvoiceDTO>> invoice() {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        String idNumber = WebContext.getCurrentUser().getIdNumber();
        UserPrincipal principal = WebContext.getCurrentUser();
        WageUserPrincipal wageUserPrincipal=new WageUserPrincipal();
        BeanUtils.copyProperties(principal,wageUserPrincipal);
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            List<GroupInvoiceDTO> list=null;
            log.info("调用wageMangerFeignService.invoice(wageUserPrincipal)开始");
            List<WageGroupInvoiceDTO> wageGroupInvoiceDTOList=wageMangerFeignService.invoice(wageUserPrincipal);
           log.info("invoice-->{}",wageGroupInvoiceDTOList);
            if (!CollectionUtils.isEmpty(wageGroupInvoiceDTOList)){
                list=new ArrayList<>();
                for (WageGroupInvoiceDTO wageGroupInvoiceDTO:wageGroupInvoiceDTOList){
                    GroupInvoiceDTO groupInvoiceDTO=new GroupInvoiceDTO();
                    BeanUtils.copyProperties(wageGroupInvoiceDTO,groupInvoiceDTO);
                    list.add(groupInvoiceDTO);
                }
            }
            return list;
        }).subscribeOn(Schedulers.elastic());
    }

//    /**
//     * 验证密码 (Get方式修改成Post方式请求，下个版本删除此方法
//     *
//     * @param pwd 查询密码
//     * @return
//     */
//    @GetMapping("/checkPwd")
//    @TrackLog
//    @Deprecated
//    public Mono<Void> checkPwd(@RequestParam("pwd") String pwd) {
//        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
//
//        UserPrincipal principal = WebContext.getCurrentUser();
//        WageUserPrincipal wageUserPrincipal = TransferUtil.userPrincipalToWageUserPrincipal(principal);
//        return Mono.fromCallable(() -> {
//            MDC.setContextMap(mdcContext);
//            if (StringUtils.isEmpty(pwd)) {
//                throw new ParamsIllegalException(ErrorConstant.WECHAR_007.getErrorMsg());
//            }
//            log.info("调用wageMangerFeignService.checkPwd(pwd,wageUserPrincipal)开始");
//            boolean bool = wageMangerFeignService.checkPwd(pwd,wageUserPrincipal);
//            if (!bool){
//                throw new ParamsIllegalException(ErrorConstant.WECHAR_007.getErrorMsg());
//            }
//            return null;
//        }).subscribeOn(Schedulers.elastic()).then();
//    }

    /**
     * 验证密码(Get方式修改成Post方式请求
     *
     * @return
     */
    @PostMapping("/checkPwd")
    @TrackLog
    public Mono<Void> checkPwd(@RequestBody CheckPwdDTO checkPwdDTO) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        String pwd = checkPwdDTO.getPwd();
        UserPrincipal principal = WebContext.getCurrentUser();
        String sessionId = principal.getSessionId();
        String idNumberEncrytor = principal.getIdNumberEncrytor();
        WageUserPrincipal wageUserPrincipal = TransferUtil.userPrincipalToWageUserPrincipal(principal);
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            if (StringUtils.isEmpty(pwd)) {
                throw new ParamsIllegalException(ErrorConstant.WECHAR_007.getErrorMsg());
            }
            log.info("调用wageMangerFeignService.checkPwd(pwd,wageUserPrincipal)开始");
            boolean bool = wageMangerFeignService.checkPwd(pwd,wageUserPrincipal);
            if (!bool){
                throw new ParamsIllegalException(ErrorConstant.WECHAR_007.getErrorMsg());
            }
            try {
                //密码校验通过之后，缓存中登记一条记录，之后的几分钟只能不再输入密码，key：sessionId
                String redisKey = FxgjDBConstant.PREFIX + ":checkFreePassword:" + idNumberEncrytor;
                log.info("checkPwd.redisKey:[{}]", redisKey);
                redisTemplate.opsForValue().set(redisKey, true, 5, TimeUnit.MINUTES);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("免密入缓存失败:[{}]", e.getMessage());
            }
            return null;
        }).subscribeOn(Schedulers.elastic()).then();
    }

//    /**
//     * 验证银行卡后六位【@】(Get方式修改成Post方式请求，下个版本删除此方法
//     *
//     * @param idNumber 身份证号
//     * @param cardNo   银行卡后6位
//     * @return
//     */
//    @GetMapping("/checkCard")
//    @TrackLog
//    @Deprecated
//    public Mono<Void> checkCard(@RequestParam("idNumber") String idNumber,
//                                @RequestParam("cardNo") String cardNo) {
//        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
//        return Mono.fromCallable(() -> {
//            MDC.setContextMap(mdcContext);
//            log.info("调用wageMangerFeignService.checkCard(idNumber,cardNo)开始");
//            CheckCardDTO checkCardDTO = new CheckCardDTO();
//            checkCardDTO.setIdNumber(idNumber);
//            checkCardDTO.setCardNo(cardNo);
//            boolean bool=wageMangerFeignService.checkCard(checkCardDTO);
//            if (!bool){
//                throw new ParamsIllegalException(ErrorConstant.WECHAR_006.getErrorMsg());
//            }
//            return null;
//        }).subscribeOn(Schedulers.elastic()).then();
//    }
    /**
     * 验证银行卡后六位【@】 (Get方式修改成Post方式请求
     *
     * @return
     */
    @PostMapping("/checkCard")
    @TrackLog
    public Mono<Void> checkCard(@RequestBody CheckCardDTO checkCardDTO) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            log.info("调用wageMangerFeignService.checkCard(idNumber,cardNo)开始");
            boolean bool=wageMangerFeignService.checkCard(checkCardDTO);
            if (!bool){
                throw new ParamsIllegalException(ErrorConstant.WECHAR_006.getErrorMsg());
            }
            return null;
        }).subscribeOn(Schedulers.elastic()).then();
    }


    /**
     * 员工个人信息【@】
     *
     * @return
     */
    @GetMapping("/emp")
    @TrackLog
    public Mono<EmpInfoDTO> emp() {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        UserPrincipal userPrincipal = WebContext.getCurrentUser();
        WageUserPrincipal wageUserPrincipal=new WageUserPrincipal();
        BeanUtils.copyProperties(userPrincipal,wageUserPrincipal);
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            EmpInfoDTO empInfoDTO=null;
            log.info("调用wageMangerFeignService.emp(wageUserPrincipal)开始");
            WageEmpInfoDTO wageEmpInfoDTO=wageMangerFeignService.emp(wageUserPrincipal);
            if (wageEmpInfoDTO!=null){
                empInfoDTO=new EmpInfoDTO();
                BeanUtils.copyProperties(wageEmpInfoDTO,empInfoDTO);
                //身份证、手机号脱敏
                empInfoDTO.setIdNumber(empInfoDTO.getIdNumberStar());
                empInfoDTO.setPhone(empInfoDTO.getPhoneStar());
            }
            log.info("返回脱敏数据emp.empInfoDTO.ret:[{}]", JacksonUtil.objectToJson(empInfoDTO));
            return empInfoDTO;
        }).subscribeOn(Schedulers.elastic());

    }

    /**
     * 员工企业【@】
     *
     * @return
     */
    @GetMapping("/empEnt")
    @TrackLog
    public Mono<List<EmpEntDTO>> empEnt() {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        UserPrincipal userPrincipal = WebContext.getCurrentUser();
        WageUserPrincipal wageUserPrincipal=new WageUserPrincipal();
        BeanUtils.copyProperties(userPrincipal,wageUserPrincipal);
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            List<EmpEntDTO> list=null;
            log.info("调用wageMangerFeignService.empEnt(wageUserPrincipal)开始");
            List<WageEmpEntDTO> wageEmpEntDTOList=wageMangerFeignService.empEnt(wageUserPrincipal);
            log.info("empEnt---->{}",wageEmpEntDTOList);
            if (!CollectionUtils.isEmpty(wageEmpEntDTOList)){
                list=new ArrayList<>();
               for (WageEmpEntDTO wageEmpEntDTO:wageEmpEntDTOList){
                   EmpEntDTO empEntDTO=new EmpEntDTO();
//                   BeanUtils.copyProperties(wageEmpEntDTO,empEntDTO);
                   //脱敏处理
                   empEntDTO.setEntName(wageEmpEntDTO.getEntName());
                   empEntDTO.setShortEntName(wageEmpEntDTO.getShortEntName());

                   List<chain.fxgj.feign.dto.response.BankCard> cardList = wageEmpEntDTO.getCards();
                   List<BankCard> cardListNew = new ArrayList<>();
                   if (null != cardList && cardList.size() > 0) {
                       for (chain.fxgj.feign.dto.response.BankCard bankCard : cardList) {
                           BankCard bankCard1 = new BankCard();
                           List<WageBankCardGroup> bankCardGroupList = bankCard.getBankCardGroups();
                           List<BankCardGroup> bankCardGroupList1 = new ArrayList<>();
                           if (null != bankCardGroupList && bankCardGroupList.size() > 0) {
                               for (WageBankCardGroup bankCardGroup : bankCardGroupList) {
                                   BankCardGroup bankCardGroup1 = new BankCardGroup();
                                   bankCardGroup1.setGroupId(bankCardGroup.getGroupId());
                                   bankCardGroup1.setId(bankCardGroup.getId());
                                   bankCardGroup1.setShortGroupName(bankCardGroup.getShortGroupName());
                                   bankCardGroupList1.add(bankCardGroup1);
                               }
                           }
                           bankCard1.setBankCardGroups(bankCardGroupList1);
                           bankCard1.setCardNo(SensitiveInfoUtils.bankCard(bankCard.getCardNo()));
                           bankCard1.setCardUpdStatus(bankCard.getCardUpdStatus());
                           bankCard1.setCardUpdStatusVal(bankCard.getCardUpdStatusVal());
                           bankCard1.setIsNew(bankCard.getIsNew());
                           bankCard1.setIssuerName(bankCard.getIssuerName());
                           bankCard1.setOldCardNo(SensitiveInfoUtils.bankCard(bankCard.getOldCardNo()));
                           bankCard1.setUpdDesc(bankCard.getUpdDesc());
                           cardListNew.add(bankCard1);
                       }
                       empEntDTO.setCards(cardListNew);
                   }

                   List<WageRes100708> itemList = wageEmpEntDTO.getItems();
                   List<Res100708> itemListNew = new ArrayList<>();
                   if (null != itemList && itemList.size() > 0) {
                       for (WageRes100708 wageRes100708 : itemList) {
                           Res100708 res100708 = new Res100708();
                           List<WageRes100708.BankCardListBean> bankCardList = wageRes100708.getBankCardList();
                           List<Res100708.BankCardListBean> bankCardListBeans = new ArrayList<>();
                           if (null != bankCardList && bankCardList.size() > 0) {
                               for (WageRes100708.BankCardListBean bankCardListBean : bankCardList) {
                                   Res100708.BankCardListBean bankCardListBean1 = new Res100708.BankCardListBean();
                                   bankCardListBean1.setBankCard(SensitiveInfoUtils.bankCard(bankCardListBean.getBankCard()));
                                   bankCardListBean1.setBankName(bankCardListBean.getBankName());
                                   bankCardListBeans.add(bankCardListBean1);
                               }
                           }
                           res100708.setBankCardList(bankCardListBeans);
                           res100708.setEmployeeId(wageRes100708.getEmployeeId());
                           res100708.setEmployeeName(wageRes100708.getEmployeeName());
                           res100708.setEmployeeNo(wageRes100708.getEmployeeNo());
                           res100708.setEntryDate(wageRes100708.getEntryDate());
                           res100708.setGroupName(wageRes100708.getGroupName());
                           res100708.setIdNumberStar(SensitiveInfoUtils.idCardNumDefinedPrefix(wageRes100708.getIdNumberStar(), 3));
                           res100708.setInServiceStatus(wageRes100708.getInServiceStatus());
                           res100708.setInServiceStatusVal(wageRes100708.getInServiceStatusVal());
                           res100708.setPhoneStar(SensitiveInfoUtils.mobilePhonePrefix(wageRes100708.getPhoneStar()));
                           res100708.setPosition(wageRes100708.getPosition());
                           itemListNew.add(res100708);
                       }
                       empEntDTO.setItems(itemListNew);
                   }

                   list.add(empEntDTO);
               }
            }
            log.info("empEnt.list:[{}]", JacksonUtil.objectToJson(list));
            return list;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 员工银行卡【@】
     *
     * @return
     */
    @GetMapping("/empCard")
    @TrackLog
    public Mono<List<EmpEntDTO>> empCard(@RequestHeader(value = "encry-salt", required = false) String salt,
                                         @RequestHeader(value = "encry-passwd", required = false) String passwd) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal userPrincipal = WebContext.getCurrentUser();
        WageUserPrincipal wageUserPrincipal = TransferUtil.userPrincipalToWageUserPrincipal(userPrincipal);
        try {
            List<FundLiquidationEnum> userPrincipalDataAuths = userPrincipal.getDataAuths();
            List<FundLiquidationEnum> wageUserPrincipalDataAuths = wageUserPrincipal.getDataAuths();
            log.info("userPrincipalDataAuths.size():[{}]",userPrincipalDataAuths.size());
            log.info("wageUserPrincipalDataAuths.size():[{}]",wageUserPrincipalDataAuths.size());
            for (FundLiquidationEnum userPrincipalDataAuth : userPrincipalDataAuths) {
                log.info("userPrincipalDataAuth:[{}]",userPrincipalDataAuth.getDesc());
            }
        } catch (Exception e) {
            log.info("日志打印报错");
        }
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            List<EmpEntDTO> list = new ArrayList<>();
            log.info("调用wageMangerFeignService.empCard(wageUserPrincipal)开始");
            List<WageEmpEntDTO> wageEmpEntDTOList=wageMangerFeignService.empCard(wageUserPrincipal);
            if (!CollectionUtils.isEmpty(wageEmpEntDTOList)){
//                for (WageEmpEntDTO wageEmpEntDTO:wageEmpEntDTOList){
//                    EmpEntDTO entDTO=new EmpEntDTO();
//                    BeanUtils.copyProperties(wageEmpEntDTO,entDTO);
//                    list.add(entDTO);
//                }
                //数据转换及加密处理
                list = transfalWageEmpEntDto(wageEmpEntDTOList, salt, passwd);
            }
            log.info("加密返回empCard.list:[{}]",JacksonUtil.objectToJson(list));
            return list;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 员工银行卡修改记录
     *
     * @param ids 编号
     * @return
     */
    @GetMapping("/empCardLog")
    @TrackLog
    public Mono<List<EmpCardLogDTO>> empCardLog(@RequestParam("ids") String ids) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            List<EmpCardLogDTO> list=null;
            log.info("调用wageMangerFeignService.empCardLog(ids)开始");
            List<WageEmpCardLogDTO> wageEmpCardLogList=wageMangerFeignService.empCardLog(ids);
            log.info("empCardLog--->{}",wageEmpCardLogList);
            if (!CollectionUtils.isEmpty(wageEmpCardLogList)){
                list=new ArrayList<>();
                for (WageEmpCardLogDTO wageEmpCardLogDTO:wageEmpCardLogList){
                    EmpCardLogDTO logDTO=new EmpCardLogDTO();
                    BeanUtils.copyProperties(wageEmpCardLogDTO,logDTO);
                    //脱敏处理
                    logDTO.setCardNo(SensitiveInfoUtils.bankCard(logDTO.getCardNo()));
                    logDTO.setCardNoOld(SensitiveInfoUtils.bankCard(logDTO.getCardNoOld()));
                    list.add(logDTO);
                }
            }
            log.info("脱敏数据empCardLog.list:[{}]", JacksonUtil.objectToJson(list));
            return list;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 手机号和公司列表
     *
     * @return
     */
    @GetMapping("/entPhone")
    @TrackLog
    public Mono<List<EmployeeListBean>> entPhone(@RequestHeader(value = "encry-salt", required = false) String salt,
        @RequestHeader(value = "encry-passwd", required = false) String passwd) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        UserPrincipal userPrincipal = WebContext.getCurrentUser();
        WageUserPrincipal wageUserPrincipal=new WageUserPrincipal();
        BeanUtils.copyProperties(userPrincipal,wageUserPrincipal);
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            List<EmployeeListBean> list=null;
            log.info("调用wageMangerFeignService.entPhone(wageUserPrincipal)开始");
            List<WageEmployeeListBean> wageEmployeeListBeanList=wageMangerFeignService.entPhone(wageUserPrincipal);
            log.info("entPhone-->{}",wageEmployeeListBeanList);
            if (!CollectionUtils.isEmpty(wageEmployeeListBeanList)){
                list=new ArrayList<>();
                for (WageEmployeeListBean welb:wageEmployeeListBeanList){
                    EmployeeListBean eb=new EmployeeListBean();
                    BeanUtils.copyProperties(welb,eb);
                    //身份证、手机号加密处理
                    String idNumberEncrypt = EncrytorUtils.encryptField(eb.getIdNumber(), salt, passwd);
                    String mobileEncrypt = EncrytorUtils.encryptField(eb.getPhone(), salt, passwd);
                    eb.setIdNumber(idNumberEncrypt);
                    eb.setPhone(mobileEncrypt);
                    eb.setSalt(salt);
                    eb.setPasswd(passwd);
                    list.add(eb);
                }
            }
            log.info("返回脱敏数据entPhone.list:[{}]",JacksonUtil.objectToJson(list));
            return list;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 企业超管
     *
     * @param entId 企业编号
     * @return
     */
    @GetMapping("/entUser")
    @TrackLog
    public Mono<List<EntUserDTO>> entUser(@RequestParam("entId") String entId) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            List<EntUserDTO> list=null;
            log.info("调用wageMangerFeignService.entUser(entId)开始");
            List<WageEntUserDTO> wageEntUserDTOList=wageMangerFeignService.entUser(entId);
            if (!CollectionUtils.isEmpty(wageEntUserDTOList)){
                list=new ArrayList<>();
                for (WageEntUserDTO wageEntUserDTO:wageEntUserDTOList){
                    EntUserDTO entUserDTO=new EntUserDTO();
                    BeanUtils.copyProperties(wageEntUserDTO,entUserDTO);
                    list.add(entUserDTO);
                }
            }
            return list;
        }).subscribeOn(Schedulers.elastic());
    }

    public void mysqlDataSynToMongo(String idNumber, String groupId, String year, String type, UserPrincipal principal){
        Runnable syncData = new Runnable() {
            @Override
            public void run() {
                try {
                    log.info("开始处理同步相应数据信息:[{}]", LocalDateTime.now());
                    WageUserPrincipal wageUserPrincipal=new WageUserPrincipal();
                    BeanUtils.copyProperties(principal,wageUserPrincipal);
                    wageSynFeignService.pushSyncDataToCache(idNumber,groupId,year,type,wageUserPrincipal);
                    //pushSyncDataService.pushSyncDataToCache(idNumber,groupId,year,type,principal);
                    log.info("同步相应数据信息完成:[{}]", LocalDateTime.now());
                } catch (Exception e) {
                    log.error("WageList同步相应数据信息", e);
                }
            }
        };
        executor.execute(syncData);
    }

    /**
     * 数据转换及加密处理
     * @return
     */
    private List<EmpEntDTO> transfalWageEmpEntDto(List<WageEmpEntDTO> wageEmpEntDTOList, String salt, String passwd){
        List<EmpEntDTO> empEntDTOList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(wageEmpEntDTOList)){
            empEntDTOList = new ArrayList<>();
            for (WageEmpEntDTO wageEmpEntDTO:wageEmpEntDTOList){
                EmpEntDTO empEntDTO=new EmpEntDTO();
                //脱敏处理
                empEntDTO.setEntName(wageEmpEntDTO.getEntName());
                empEntDTO.setShortEntName(wageEmpEntDTO.getShortEntName());

                List<chain.fxgj.feign.dto.response.BankCard> cardList = wageEmpEntDTO.getCards();
                List<BankCard> cardListNew = new ArrayList<>();
                if (null != cardList && cardList.size() > 0) {
                    for (chain.fxgj.feign.dto.response.BankCard bankCard : cardList) {
                        BankCard bankCard1 = new BankCard();
                        List<WageBankCardGroup> bankCardGroupList = bankCard.getBankCardGroups();
                        List<BankCardGroup> bankCardGroupList1 = new ArrayList<>();
                        if (null != bankCardGroupList && bankCardGroupList.size() > 0) {
                            for (WageBankCardGroup bankCardGroup : bankCardGroupList) {
                                BankCardGroup bankCardGroup1 = new BankCardGroup();
                                bankCardGroup1.setGroupId(bankCardGroup.getGroupId());
                                bankCardGroup1.setId(bankCardGroup.getId());
                                bankCardGroup1.setShortGroupName(bankCardGroup.getShortGroupName());
                                bankCardGroupList1.add(bankCardGroup1);
                            }
                        }
                        bankCard1.setBankCardGroups(bankCardGroupList1);
                        bankCard1.setCardNo(EncrytorUtils.encryptField(bankCard.getCardNo(), salt, passwd));
                        bankCard1.setCardUpdStatus(bankCard.getCardUpdStatus());
                        bankCard1.setCardUpdStatusVal(bankCard.getCardUpdStatusVal());
                        bankCard1.setIsNew(bankCard.getIsNew());
                        bankCard1.setIssuerName(bankCard.getIssuerName());
                        bankCard1.setOldCardNo(EncrytorUtils.encryptField(bankCard.getOldCardNo(), salt, passwd));
                        bankCard1.setUpdDesc(bankCard.getUpdDesc());
                        cardListNew.add(bankCard1);
                    }
                    empEntDTO.setCards(cardListNew);
                }

                List<WageRes100708> itemList = wageEmpEntDTO.getItems();
                List<Res100708> itemListNew = new ArrayList<>();
                if (null != itemList && itemList.size() > 0) {
                    for (WageRes100708 wageRes100708 : itemList) {
                        Res100708 res100708 = new Res100708();
                        List<WageRes100708.BankCardListBean> bankCardList = wageRes100708.getBankCardList();
                        List<Res100708.BankCardListBean> bankCardListBeans = new ArrayList<>();
                        if (null != bankCardList && bankCardList.size() > 0) {
                            for (WageRes100708.BankCardListBean bankCardListBean : bankCardList) {
                                Res100708.BankCardListBean bankCardListBean1 = new Res100708.BankCardListBean();
                                bankCardListBean1.setBankCard(EncrytorUtils.encryptField(bankCardListBean.getBankCard(), salt, passwd));
                                bankCardListBean1.setBankName(bankCardListBean.getBankName());
                                bankCardListBeans.add(bankCardListBean1);
                            }
                        }
                        res100708.setBankCardList(bankCardListBeans);
                        res100708.setEmployeeId(wageRes100708.getEmployeeId());
                        res100708.setEmployeeName(wageRes100708.getEmployeeName());
                        res100708.setEmployeeNo(wageRes100708.getEmployeeNo());
                        res100708.setEntryDate(wageRes100708.getEntryDate());
                        res100708.setGroupName(wageRes100708.getGroupName());
                        res100708.setIdNumberStar(EncrytorUtils.encryptField(wageRes100708.getIdNumberStar(), salt, passwd));
                        res100708.setInServiceStatus(wageRes100708.getInServiceStatus());
                        res100708.setInServiceStatusVal(wageRes100708.getInServiceStatusVal());
                        res100708.setPhoneStar(EncrytorUtils.encryptField(wageRes100708.getPhoneStar(), salt, passwd));
                        res100708.setPosition(wageRes100708.getPosition());
                        itemListNew.add(res100708);
                    }
                    empEntDTO.setItems(itemListNew);
                    empEntDTO.setSalt(salt);
                    empEntDTO.setPasswd(passwd);
                }
                empEntDTOList.add(empEntDTO);
            }
        }
        log.info("transfalWageEmpEntDto.empEntDTOList:[{}]", JacksonUtil.objectToJson(empEntDTOList));
        return empEntDTOList;
    }


    /**
     * 校验是否免密
     *
     * @return true 免密，false 需要输入密码
     */
    @GetMapping("/checkFreePassword")
    @TrackLog
    public Mono<Boolean> checkFreePassword() {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal principal = WebContext.getCurrentUser();
        String sessionId = principal.getSessionId();
        String idNumberEncrytor = principal.getIdNumberEncrytor();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            if (StringUtils.isBlank(idNumberEncrytor)) {
                log.info("idNumberEncrytor空，直接返回false，需要输入密码");
                return false;
            }
            String redisKey = FxgjDBConstant.PREFIX + ":checkFreePassword:" + idNumberEncrytor;
            Object value = redisTemplate.opsForValue().get(redisKey);
            if (value == null) {
                log.info("根据idNumberEncrytor:[{}]未查询到登录记录", idNumberEncrytor);
                return false;
            }
            log.info("根据idNumberEncrytor:[{}]查询到登录记录,value:[{}]", idNumberEncrytor, value);
            return true;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 个人薪资列表
     *
     * @param groupId 机构Id
     * @param year    年份
     * @param type    类型 0资金到账 1合计
     * @return
     */
    @GetMapping("/wageList")
    @TrackLog
    public Mono<Res100703> wageList(@RequestParam("groupId") String groupId,
                                    @RequestParam("year") String year,
                                    @RequestParam("type") String type) {
        log.info("调用wageList开始时间::[{}]",LocalDateTime.now());
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal principal = WebContext.getCurrentUser();
        PayrollUserPrincipalDTO payrollUserPrincipalDTO = new PayrollUserPrincipalDTO();
        BeanUtils.copyProperties(principal, payrollUserPrincipalDTO);
        String idNumber = principal.getIdNumber();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            Res100703 res100703 = new Res100703();
            try {
                PayrollRes100703ReqDTO payrollRes100703ReqDTO = new PayrollRes100703ReqDTO();
                payrollRes100703ReqDTO.setGroupId(groupId);
                payrollRes100703ReqDTO.setYear(year);
                payrollRes100703ReqDTO.setType(type);
                payrollRes100703ReqDTO.setIdNumber(idNumber);
                payrollRes100703ReqDTO.setPayrollUserPrincipalDTO(payrollUserPrincipalDTO);
                log.info("开始调用wageList时间:[{}]",LocalDateTime.now());
                log.info("groupId:[{}]，year:[{}]，type:[{}]，idNumber:[{}]",groupId, year, type, idNumber);
                PayrollRes100703DTO source = payrollFeignController.wageList(payrollRes100703ReqDTO);
                if (null != source) {
                    res100703.setShouldTotalAmt(source.getShouldTotalAmt());
                    res100703.setDeductTotalAmt(source.getDeductTotalAmt());
                    res100703.setEmployeeSid(source.getEmployeeSid());
                    res100703.setRealTotalAmt(source.getRealTotalAmt());
                    List<Integer> years = source.getYears();
                    res100703.setYears(years);
                    log.info("mongo.wageList:[{}]", JacksonUtil.objectToJson(res100703));
                } else {
                    log.info("查询mongo未查询到数据");
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("wageList查询mongo异常:[{}]", e);
            }
            log.info("调用wageList返回时间::[{}]",LocalDateTime.now());
            return res100703;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 查看工资条详情
     *
     * @param wageSheetId 方案id
     * @param groupId     机构id
     * @return
     */
    @GetMapping("/wageDetail")
    @TrackLog
    public Mono<List<WageDetailDTO>> wageDetail(@RequestParam("wageSheetId") String wageSheetId,
                                                @RequestParam("groupId") String groupId) {
        log.info("调用wageDetail开始时间::[{}]",LocalDateTime.now());
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal principal = WebContext.getCurrentUser();
        String idNumber = principal.getIdNumber();
        PayrollUserPrincipalDTO payrollUserPrincipalDTO = new PayrollUserPrincipalDTO();
        BeanUtils.copyProperties(principal, payrollUserPrincipalDTO);
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            List<WageDetailDTO> list = new ArrayList<>();
            try {
                PayrollWageDetailReqDTO payrollWageDetailReqDTO = new PayrollWageDetailReqDTO();
                payrollWageDetailReqDTO.setIdNumber(idNumber);
                payrollWageDetailReqDTO.setGroupId(groupId);
                payrollWageDetailReqDTO.setWageSheetId(wageSheetId);
                payrollWageDetailReqDTO.setPayrollUserPrincipalDTO(payrollUserPrincipalDTO);
                log.info("groupId:[{}]，idNumber:[{}]，wageSheetId:[{}]",groupId, idNumber,wageSheetId);
                log.info("调用payrollFeignController.wageDetail(payrollWageDetailReqDTO)，查明细开始时间:[{}]", LocalDateTime.now());
                List<PayrollWageDetailDTO> source = payrollFeignController.wageDetail(payrollWageDetailReqDTO);
                log.info("调用payrollFeignController.wageDetail(payrollWageDetailReqDTO)，查明细结束时间:[{}]", LocalDateTime.now());
                log.info("source.size():[{}]",source.size());
                if (null != list && list.size() > 0) {
                    for (PayrollWageDetailDTO payrollWageDetailDTO : source) {
                        WageDetailDTO wageDetailDTO = new WageDetailDTO();

                        PayrollWageHeadDTO payrollWageHeadDTO = payrollWageDetailDTO.getWageHeadDTO();
                        WageHeadDTO wageHeadDTO = new WageHeadDTO();
                        wageHeadDTO.setDoubleRow(payrollWageHeadDTO.isDoubleRow());
                        wageHeadDTO.setHeadIndex(payrollWageHeadDTO.getHeadIndex());
                        List<PayrollWageHeadDTO.Cell> heads = payrollWageHeadDTO.getHeads();
                        List<WageHeadDTO.Cell> headsList = new ArrayList<>();
                        for (PayrollWageHeadDTO.Cell head : heads) {
                            WageHeadDTO.Cell cell = new WageHeadDTO.Cell();
                            cell.setColName(head.getColName());
                            cell.setColNum(head.getColNum());
                            cell.setHidden(head.isHidden());
                            PayrollWageHeadDTO.Type type = head.getType();
                            WageHeadDTO.Type type1 = WageHeadDTO.Type.valueOf(type.name());
                            cell.setType(type1);
                            headsList.add(cell);
                        }
                        wageHeadDTO.setHeads(headsList);
                        wageDetailDTO.setWageHeadDTO(wageHeadDTO);

                        PayrollWageDetailDTO.PayrollWageShowDTO payrollWageShowDTO = payrollWageDetailDTO.getWageShowDTO();
                        WageDetailDTO.WageShowDTO wageShowDTO = new WageDetailDTO.WageShowDTO();
                        BeanUtils.copyProperties(payrollWageShowDTO, wageShowDTO);
                        wageDetailDTO.setWageShowDTO(wageShowDTO);

                        List<PayrollWageDetailDTO.Content> payrollContent = payrollWageDetailDTO.getContent();
                        List<WageDetailDTO.Content> contentList = new ArrayList<>();
                        for (PayrollWageDetailDTO.Content content : payrollContent) {
                            WageDetailDTO.Content content1 = new WageDetailDTO.Content();
                            BeanUtils.copyProperties(content, content1);
                            contentList.add(content1);
                        }
                        wageDetailDTO.setContent(contentList);

                        wageDetailDTO.setWageDetailId(payrollWageDetailDTO.getWageDetailId());
                        wageDetailDTO.setBankName(payrollWageDetailDTO.getBankName());
                        wageDetailDTO.setCardNo(payrollWageDetailDTO.getCardNo());
                        wageDetailDTO.setWageName(payrollWageDetailDTO.getWageName());
                        wageDetailDTO.setRealAmt(payrollWageDetailDTO.getRealAmt());
                        wageDetailDTO.setEntName(payrollWageDetailDTO.getEntName());
                        wageDetailDTO.setGroupName(payrollWageDetailDTO.getGroupName());
                        wageDetailDTO.setGroupId(payrollWageDetailDTO.getGroupId());
                        wageDetailDTO.setPushDateTime(payrollWageDetailDTO.getPushDateTime());
                        wageDetailDTO.setReceiptStautus(payrollWageDetailDTO.getReceiptStautus());
                        wageDetailDTO.setDifferRealAmt(payrollWageDetailDTO.getDifferRealAmt());
                        wageDetailDTO.setPayStatus(payrollWageDetailDTO.getPayStatus());
                        list.add(wageDetailDTO);
                    }
                }else {
                    log.info("mongo.wageDetail查询数据为空,idNumber:[{}]", idNumber);
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.info("查询mongo异常:[{}]",e);
            }
            log.info("web.list:[{}]",JacksonUtil.objectToJson(list));
            return list;
        }).subscribeOn(Schedulers.elastic());
    }
}
