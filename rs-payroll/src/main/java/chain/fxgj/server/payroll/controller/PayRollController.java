package chain.fxgj.server.payroll.controller;

import chain.css.exception.ParamsIllegalException;
import chain.css.log.annotation.TrackLog;
import chain.fxgj.core.common.constant.ErrorConstant;
import chain.fxgj.feign.client.PayRollFeignService;
import chain.fxgj.feign.client.SynTimerFeignService;
import chain.fxgj.feign.dto.response.*;
import chain.fxgj.feign.dto.web.WageUserPrincipal;
import chain.fxgj.server.payroll.dto.response.*;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

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
            WageUserPrincipal wageUserPrincipal=new WageUserPrincipal();
            BeanUtils.copyProperties(principal,wageUserPrincipal);
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
            return list;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 根据身份账号返回手机和公司列表
     *
     * @param idNumber 身份证号
     * @return
     */
    @GetMapping("/entEmp")
    @TrackLog
    public Mono<Res100701> entEmp(@RequestParam("idNumber") String idNumber) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal principal = WebContext.getCurrentUser();

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            WageUserPrincipal wageUserPrincipal=new WageUserPrincipal();
            BeanUtils.copyProperties(principal,wageUserPrincipal);
            Res100701 res100701=null;
            WageRes100701 wageRes100701=wageMangerFeignService.entEmp(idNumber,wageUserPrincipal);
            log.info("wageRes100701-->{}",wageRes100701);
            if (wageRes100701!=null){
                res100701=new Res100701();
                BeanUtils.copyProperties(wageRes100701,res100701);
            }
            return res100701;
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
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal principal = WebContext.getCurrentUser();
        PayrollUserPrincipalDTO payrollUserPrincipalDTO = new PayrollUserPrincipalDTO();
        BeanUtils.copyProperties(principal, payrollUserPrincipalDTO);
        String idNumber = principal.getIdNumber();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            Res100703 res100703 = new Res100703();
            boolean qryMySql = false;
            try {
                PayrollRes100703ReqDTO payrollRes100703ReqDTO = new PayrollRes100703ReqDTO();
                payrollRes100703ReqDTO.setGroupId(groupId);
                payrollRes100703ReqDTO.setYear(year);
                payrollRes100703ReqDTO.setType(type);
                payrollRes100703ReqDTO.setIdNumber(idNumber);
                payrollRes100703ReqDTO.setPayrollUserPrincipalDTO(payrollUserPrincipalDTO);
                log.info("groupId:[{}]，year:[{}]，type:[{}]，idNumber:[{}]",groupId, year, type, idNumber);
                PayrollRes100703DTO source = payrollFeignController.wageList(payrollRes100703ReqDTO);
                res100703.setShouldTotalAmt(source.getShouldTotalAmt());
                res100703.setDeductTotalAmt(source.getDeductTotalAmt());
                res100703.setEmployeeSid(source.getEmployeeSid());
                res100703.setRealTotalAmt(source.getRealTotalAmt());
                List<Integer> years = source.getYears();
                res100703.setYears(years);

                List<PayrollPlanListDTO> planListSource = source.getPlanList();
                List<PlanListBean> planListBeans = new ArrayList<>();
                if (null != planListSource && planListSource.size() > 0) {
                    for (PayrollPlanListDTO payrollPlanListDTO : planListSource) {
                        PlanListBean planListBean = new PlanListBean();
                        BeanUtils.copyProperties(payrollPlanListDTO, planListBean);
                        planListBeans.add(planListBean);
                    }
                    res100703.setPlanList(planListBeans);
                    log.info("res100703:[{}]", JacksonUtil.objectToJson(res100703));

                    //判断是否需要数据同步(比较 mongo 和 mysql 中最新的sheetId 是否相同，不相同则数据同步)
                    PayrollPlanListDTO payrollPlanListDTO = planListSource.get(0);
                    String mongoNewestWageSheetId = payrollPlanListDTO.getWageSheetId();
                    boolean retBoolean =wageMangerFeignService.compareSheetCrtDataTime(idNumber, groupId, mongoNewestWageSheetId) ;//wageWechatService.compareSheetCrtDataTime(idNumber, groupId, mongoNewestWageSheetId);
                    if (!retBoolean) {
                        log.info("mongo库中，wageSheetInfo最新sheetId 与 Mysql中 最新sheetId 不相等，则需要同步数据");
                        mysqlDataSynToMongo(idNumber,groupId,year,type,principal);
                    }
                }
                if (null == planListSource || planListSource.size() == 0) {
                    log.info("wageList查询mongo数据为空，转查mysql,idNumber:[{}]", idNumber);
                    qryMySql = true;
                }
            } catch (Exception e) {
                qryMySql = true;
                log.info("wageList查询mongo异常，转查mysql,idNumber:[{}]", idNumber);
            }
            if (qryMySql) {
                WageUserPrincipal wageUserPrincipal=new WageUserPrincipal();
                BeanUtils.copyProperties(principal,wageUserPrincipal);
                WageRes100703 wageRes100703=wageMangerFeignService.wageList(groupId,year,type,wageUserPrincipal);
                log.info("wageRes100703-->{}",wageRes100703);
                if (wageRes100703!=null){
                    if (res100703==null){
                        res100703=new Res100703();
                    }
                    BeanUtils.copyProperties(wageRes100703,res100703);
                }
                log.info("数据同步");
                mysqlDataSynToMongo(idNumber,groupId,year,type,principal);
            }
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
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        UserPrincipal principal = WebContext.getCurrentUser();
        String idNumber = principal.getIdNumber();
        PayrollUserPrincipalDTO payrollUserPrincipalDTO = new PayrollUserPrincipalDTO();
        BeanUtils.copyProperties(principal, payrollUserPrincipalDTO);
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            boolean qryMySql = false;
            List<WageDetailDTO> list = new ArrayList<>();
            try {
                PayrollWageDetailReqDTO payrollWageDetailReqDTO = new PayrollWageDetailReqDTO();
                payrollWageDetailReqDTO.setIdNumber(idNumber);
                payrollWageDetailReqDTO.setGroupId(groupId);
                payrollWageDetailReqDTO.setWageSheetId(wageSheetId);
                payrollWageDetailReqDTO.setPayrollUserPrincipalDTO(payrollUserPrincipalDTO);
                log.info("groupId:[{}]，idNumber:[{}]，wageSheetId:[{}]",groupId, idNumber,wageSheetId);
                List<PayrollWageDetailDTO> source = new ArrayList<>();
                source = payrollFeignController.wageDetail(payrollWageDetailReqDTO);
                log.info("source.size():[{}]",source.size());
                for (PayrollWageDetailDTO payrollWageDetailDTO : source) {
                    WageDetailDTO wageDetailDTO = new WageDetailDTO();
                    BeanUtils.copyProperties(payrollWageDetailDTO, wageDetailDTO);
                    list.add(wageDetailDTO);
                }
                if (null == list || list.size() == 0) {
                    log.info("wageDetail查询mongo数据为空，转查mysql,idNumber:[{}]", idNumber);
                    qryMySql = true;
                }
            } catch (Exception e) {
                log.info("wageDetail查询mongo异常，转查mysql,idNumber:[{}]",idNumber);
                log.info("查询mongo异常:[{}]",e);
                qryMySql = true;
            }
            //查询mongo异常，转查mysql
            log.info("qryMySql:[{}]",qryMySql);
            if (qryMySql) {
                WageUserPrincipal wageUserPrincipal=new WageUserPrincipal();
                BeanUtils.copyProperties(principal,wageUserPrincipal);
                List<WageDetailInfoDTO> wageDetailInfoDTOList=wageMangerFeignService.wageDetail(wageSheetId,groupId,wageUserPrincipal);
                log.info("wageDetailInfoDTOList--->{}",wageDetailInfoDTOList);
                if(!CollectionUtils.isEmpty(wageDetailInfoDTOList)){
                   if (list==null){
                       list=new ArrayList<>();
                   }
                   for (WageDetailInfoDTO wageDetailInfoDTO:wageDetailInfoDTOList){
                       WageDetailDTO detailDTO=new WageDetailDTO();
                       BeanUtils.copyProperties(wageDetailInfoDTO,detailDTO);
                       list.add(detailDTO);
                   }
                }
            }
            return list;
        }).subscribeOn(Schedulers.elastic());
    }

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

    /**
     * 验证密码
     *
     * @param pwd 查询密码
     * @return
     */
    @GetMapping("/checkPwd")
    @TrackLog
    public Mono<Void> checkPwd(@RequestParam("pwd") String pwd) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        UserPrincipal principal = WebContext.getCurrentUser();
        WageUserPrincipal wageUserPrincipal=new WageUserPrincipal();
        BeanUtils.copyProperties(principal,wageUserPrincipal);
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            if (StringUtils.isEmpty(pwd)) {
                throw new ParamsIllegalException(ErrorConstant.WECHAR_007.getErrorMsg());
            }
            boolean bool=wageMangerFeignService.checkPwd(pwd,wageUserPrincipal);
            if (!bool){
                throw new ParamsIllegalException(ErrorConstant.WECHAR_006.getErrorMsg());
            }
            return null;
        }).subscribeOn(Schedulers.elastic()).then();
    }

    /**
     * 验证银行卡后六位【@】
     *
     * @param idNumber 身份证号
     * @param cardNo   银行卡后6位
     * @return
     */
    @GetMapping("/checkCard")
    @TrackLog
    public Mono<Void> checkCard(@RequestParam("idNumber") String idNumber,
                                @RequestParam("cardNo") String cardNo) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            boolean bool=wageMangerFeignService.checkCard(idNumber,cardNo);
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
            WageEmpInfoDTO wageEmpInfoDTO=wageMangerFeignService.emp(wageUserPrincipal);
            if (wageEmpInfoDTO!=null){
                empInfoDTO=new EmpInfoDTO();
                BeanUtils.copyProperties(wageEmpInfoDTO,empInfoDTO);
            }
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
            List<WageEmpEntDTO> wageEmpEntDTOList=wageMangerFeignService.empEnt(wageUserPrincipal);
            log.info("empEnt---->{}",wageEmpEntDTOList);
            if (!CollectionUtils.isEmpty(wageEmpEntDTOList)){
                list=new ArrayList<>();
               for (WageEmpEntDTO wageEmpEntDTO:wageEmpEntDTOList){
                   EmpEntDTO empEntDTO=new EmpEntDTO();
                   BeanUtils.copyProperties(wageEmpEntDTO,empEntDTO);
                   list.add(empEntDTO);
               }
            }
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
    public Mono<List<EmpEntDTO>> empCard() {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        UserPrincipal userPrincipal = WebContext.getCurrentUser();
        WageUserPrincipal wageUserPrincipal=new WageUserPrincipal();
        BeanUtils.copyProperties(userPrincipal,wageUserPrincipal);
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            List<EmpEntDTO> list=null;
            List<WageEmpEntDTO> wageEmpEntDTOList=wageMangerFeignService.empCard(wageUserPrincipal);
            if (!CollectionUtils.isEmpty(wageEmpEntDTOList)){
                list=new ArrayList<>();
                for (WageEmpEntDTO wageEmpEntDTO:wageEmpEntDTOList){
                    EmpEntDTO entDTO=new EmpEntDTO();
                    BeanUtils.copyProperties(wageEmpEntDTO,entDTO);
                    list.add(entDTO);
                }
            }
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
            List<WageEmpCardLogDTO> wageEmpCardLogList=wageMangerFeignService.empCardLog(ids);
            log.info("empCardLog--->{}",wageEmpCardLogList);
            if (!CollectionUtils.isEmpty(wageEmpCardLogList)){
                list=new ArrayList<>();
                for (WageEmpCardLogDTO wageEmpCardLogDTO:wageEmpCardLogList){
                    EmpCardLogDTO logDTO=new EmpCardLogDTO();
                    BeanUtils.copyProperties(wageEmpCardLogDTO,logDTO);
                    list.add(logDTO);
                }
            }
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
    public Mono<List<EmployeeListBean>> entPhone() {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        UserPrincipal userPrincipal = WebContext.getCurrentUser();
        WageUserPrincipal wageUserPrincipal=new WageUserPrincipal();
        BeanUtils.copyProperties(userPrincipal,wageUserPrincipal);
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            List<EmployeeListBean> list=null;
            List<WageEmployeeListBean> wageEmployeeListBeanList=wageMangerFeignService.entPhone(wageUserPrincipal);
            log.info("entPhone-->{}",wageEmployeeListBeanList);
            if (!CollectionUtils.isEmpty(wageEmployeeListBeanList)){
                list=new ArrayList<>();
                for (WageEmployeeListBean welb:wageEmployeeListBeanList){
                    EmployeeListBean eb=new EmployeeListBean();
                    BeanUtils.copyProperties(welb,eb);
                    list.add(eb);
                }
            }
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
                    log.info("开始处理同步相应数据信息。。。。");
                    WageUserPrincipal wageUserPrincipal=new WageUserPrincipal();
                    BeanUtils.copyProperties(principal,wageUserPrincipal);
                    wageSynFeignService.pushSyncDataToCache(idNumber,groupId,year,type,wageUserPrincipal);
                    //pushSyncDataService.pushSyncDataToCache(idNumber,groupId,year,type,principal);
                } catch (Exception e) {
                    log.error("WageList同步相应数据信息", e);
                }
            }
        };
        executor.execute(syncData);
    }
}
