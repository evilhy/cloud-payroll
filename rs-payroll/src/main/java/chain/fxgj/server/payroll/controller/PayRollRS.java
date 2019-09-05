package chain.fxgj.server.payroll.controller;

import chain.css.exception.ParamsIllegalException;
import chain.css.log.annotation.TrackLog;
import chain.fxgj.core.common.constant.DictEnums.IsStatusEnum;
import chain.fxgj.core.common.constant.ErrorConstant;
import chain.fxgj.core.common.service.EmployeeEncrytorService;
import chain.fxgj.core.common.service.WageWechatService;
import chain.fxgj.core.common.service.WechatBindService;
import chain.fxgj.core.common.util.TransUtil;
import chain.fxgj.server.payroll.dto.response.*;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.fxgj.server.payroll.web.WebContext;
import chain.payroll.client.feign.PayrollFeignController;
import chain.payroll.dto.response.PayrollUserPrincipalDTO;
import chain.payroll.dto.response.PayrollWageDetailDTO;
import chain.payroll.dto.response.PayrollWageDetailReqDTO;
import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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

/**
 * 工资条
 */
@RestController
@Validated
@RequestMapping(value = "/roll")
@Slf4j
@SuppressWarnings("unchecked")
public class PayRollRS {

    @Inject
    WageWechatService wageWechatService;
    @Inject
    WechatBindService wechatBindService;
    @Inject
    EmployeeEncrytorService employeeEncrytorService;

    @Resource
    RedisTemplate redisTemplate;
    @Autowired
    PayrollFeignController payrollFeignController;

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

            NewestWageLogDTO bean = wageWechatService.newGroupPushInfo(idNumber, principal);

            IndexDTO indexDTO = new IndexDTO();
            indexDTO.setBean(bean);
            //查询用户是否银行卡号变更有最新未读消息
            Integer isNew = wechatBindService.getCardUpdIsNew(idNumber);
            log.info("isNew:{}", isNew);
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

            List<NewestWageLogDTO> list = wageWechatService.groupList(idNumber, principal);
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

            Res100701 res100701 = wechatBindService.getEntList(idNumber, principal);
            if (res100701.getBindStatus().equals("1")) {
                throw new ParamsIllegalException(ErrorConstant.WECHAR_002.getErrorMsg());
            }
            if (res100701.getEmployeeList() == null || res100701.getEmployeeList().size() <= 0) {
                throw new ParamsIllegalException(ErrorConstant.WECHAR_001.getErrorMsg());
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
        String idNumber = principal.getIdNumber();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            Res100703 res100703 = null;
            if (LocalDate.now().getYear() == Integer.parseInt(year)) {
                res100703 = wageWechatService.wageList(idNumber, groupId, year, type,principal);
            } else {
                res100703 = wageWechatService.wageHistroyList(idNumber, groupId, year, type,principal);
            }
            res100703.setYears(wageWechatService.years(res100703.getEmployeeSid(), type));

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
                List<PayrollWageDetailDTO> source = null;
                try {
                    source = payrollFeignController.wageDetail(payrollWageDetailReqDTO);
                    BeanUtils.copyProperties(source, list);
                } catch (Exception e) {
                    log.info("payrollFeignController.wageDetail(payrollWageDetailReqDTO) Exception: [{}]", e);
                }
                if (null == list || list.size() == 0) {
                    qryMySql = true;
                }
                log.info("source.size():[{}]",source.size());
            } catch (Exception e) {
                log.info("查询mongo异常，转查mysql,idNumber:[{}]",idNumber);
                qryMySql = true;
            }
            //查询mongo异常，转查mysql
            if (qryMySql) {
                list = wageWechatService.getWageDetail(principal.getIdNumber(), groupId, wageSheetId,principal);
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

            List<Res100708> res100708 = wechatBindService.empList(idNumber,principal);
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
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            List<GroupInvoiceDTO> list = wechatBindService.invoiceList(idNumber);
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
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            if (StringUtils.isEmpty(pwd)) {
                throw new ParamsIllegalException(ErrorConstant.WECHAR_007.getErrorMsg());
            }
            String openId = principal.getOpenId();
            String id = principal.getWechatId();
            //String queryPwd = wechatBindService.getQueryPwd(openId);
            String queryPwd = wechatBindService.getQueryPwdById(id);
            if (!queryPwd.equals(employeeEncrytorService.encryptPwd(pwd))) {
                throw new ParamsIllegalException(ErrorConstant.WECHAR_007.getErrorMsg());
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

            int is = wechatBindService.checkCardNo(idNumber, cardNo);
            if (is == IsStatusEnum.NO.getCode()) {
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
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            EmpInfoDTO empInfoDTO = EmpInfoDTO.builder()
                    .headimgurl(userPrincipal.getHeadimgurl())
                    .idNumber(userPrincipal.getIdNumber())
                    .name(userPrincipal.getName())
                    .phone(userPrincipal.getPhone())
                    .phoneStar(TransUtil.phoneStar(userPrincipal.getPhone()))
                    .idNumberStar(TransUtil.idNumberStar(userPrincipal.getIdNumber()))
                    .build();

            //查询用户是否银行卡号变更有最新未读消息
            Integer isNew = wechatBindService.getCardUpdIsNew(userPrincipal.getIdNumber());
            empInfoDTO.setIsNew(isNew);
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
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            List<EmpEntDTO> list = wechatBindService.empEntList(userPrincipal.getIdNumber(),userPrincipal);
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
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            List<EmpEntDTO> list = wechatBindService.empEntList(userPrincipal.getIdNumber(), userPrincipal);
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

            //ids "|"分割
            List<EmpCardLogDTO> list = wechatBindService.empCardLog(ids.split("\\|"));
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
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            List<EmployeeListBean> list = wechatBindService.getEntPhone(userPrincipal.getIdNumber(), userPrincipal);
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

            List<EntUserDTO> list = wechatBindService.entUser(entId);
            return list;
        }).subscribeOn(Schedulers.elastic());
    }

}
