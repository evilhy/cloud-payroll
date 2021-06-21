package chain.fxgj.server.payroll.controller;

import chain.css.exception.ErrorMsg;
import chain.css.exception.ParamsIllegalException;
import chain.css.log.annotation.TrackLog;
import chain.fxgj.core.common.constant.PayrollDBConstant;
import chain.fxgj.feign.client.PayRollFeignService;
import chain.fxgj.server.payroll.constant.ErrorConstant;
import chain.fxgj.server.payroll.dto.payroll.CheckPwdDTO;
import chain.fxgj.server.payroll.dto.payroll.EntEmpDTO;
import chain.fxgj.server.payroll.dto.request.SignedSaveReq;
import chain.fxgj.server.payroll.dto.response.EmployeeListBean;
import chain.fxgj.server.payroll.dto.response.EntUserDTO;
import chain.fxgj.server.payroll.dto.response.GroupInvoiceDTO;
import chain.fxgj.server.payroll.dto.response.Res100701;
import chain.fxgj.server.payroll.dto.response.*;
import chain.fxgj.server.payroll.service.EmpWechatService;
import chain.fxgj.server.payroll.service.PaswordService;
import chain.fxgj.server.payroll.util.EncrytorUtils;
import chain.fxgj.server.payroll.util.SensitiveInfoUtils;
import chain.fxgj.server.payroll.util.TransferUtil;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.fxgj.server.payroll.web.WebContext;
import chain.payroll.client.feign.PayrollFeignController;
import chain.payroll.client.feign.SignedReceiptFeignController;
import chain.payroll.client.feign.WageSheetFeignController;
import chain.utils.commons.JacksonUtil;
import chain.utils.commons.JsonUtil;
import chain.utils.commons.UUIDUtil;
import chain.wage.manager.core.dto.response.WageEntUserDTO;
import chain.wage.manager.core.dto.response.WageRes100708;
import chain.wage.manager.core.dto.web.WageUserPrincipal;
import chain.wage.service.WageFeignService;
import core.dto.request.CacheCheckCardDTO;
import core.dto.request.CacheEmployeeInfoReq;
import core.dto.response.*;
import core.dto.wechat.CacheUserPrincipal;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
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
    PayRollFeignService wageMangerFeignService;

    @Autowired
    PaswordService paswordService;
    @Autowired
    EmpWechatService empWechatService;
    @Autowired
    WageSheetFeignController wageSheetFeignController;
    @Autowired
    SignedReceiptFeignController signedReceiptFeignController;
    @Autowired
    WageFeignService wageFeignService;

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
     * 员工机构列表(我的收入-机构列表)
     *
     * @return
     */
    @GetMapping("/groupList")
    @TrackLog
    public Mono<List<core.dto.response.NewestWageLogDTO>> groupList() {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        UserPrincipal principal = WebContext.getCurrentUser();
        CacheUserPrincipal cacheUserPrincipal = TransferUtil.userPrincipalToWageUserPrincipal(principal);
        String entId = principal.getEntId();
        String idNumber = principal.getIdNumber();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            List<core.dto.response.NewestWageLogDTO> newestWageLogDTOS = payrollFeignController.empGroupList(cacheUserPrincipal);
            log.info("NewestWageLogDTOList:[{}]", JacksonUtil.objectToJson(newestWageLogDTOS));
            return newestWageLogDTOS;
        }).subscribeOn(Schedulers.elastic());
    }

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
        //强制转换成大写
        String idNumber = entEmpDTO.getIdNumber().toUpperCase();
        if (StringUtils.isBlank(idNumber)) {
            throw new ParamsIllegalException(new ErrorMsg("9999", "请输入身份证!"));
        }
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            CacheUserPrincipal wageUserPrincipal = TransferUtil.userPrincipalToWageUserPrincipal(principal);
            Res100701 res100701 = null;
            log.info("调用wageMangerFeignService.entEmp(idNumber,wageUserPrincipal)开始");
            core.dto.response.Res100701 wageRes100701 = payrollFeignController.entEmp(idNumber, wageUserPrincipal);
            log.info("wageRes100701:[{}]", JacksonUtil.objectToJson(wageRes100701));
            if (wageRes100701 != null) {
                res100701 = new Res100701();
                BeanUtils.copyProperties(wageRes100701, res100701);
                List<EmployeeListBean> employeeListBeanList = new ArrayList<>();
                List<core.dto.response.EmployeeListBean> wageEmployeeListBeanList = wageRes100701.getEmployeeList();
                if (null != wageEmployeeListBeanList && wageEmployeeListBeanList.size() > 0) {
                    for (core.dto.response.EmployeeListBean wageEmployeeListBean : wageEmployeeListBeanList) {

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

    /**
     * 员工个人信息 （确认前端未使用）
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
            WageUserPrincipal wageUserPrincipal = new WageUserPrincipal();
            BeanUtils.copyProperties(principal, wageUserPrincipal);
            log.info("调用wageMangerFeignService.empInfo(wageUserPrincipal)开始");
            List<WageRes100708> wageRes100708List = null;//wageMangerFeignService.empInfo(wageUserPrincipal);
            log.info("wageRes100708List-->{}", wageRes100708List.size());
            List<Res100708> res100708 = null;
            if (!CollectionUtils.isEmpty(wageRes100708List)) {
                res100708 = new ArrayList<>();
                for (WageRes100708 res1007081 : wageRes100708List) {
                    Res100708 res = new Res100708();
                    BeanUtils.copyProperties(res1007081, res);
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
        WageUserPrincipal wageUserPrincipal = new WageUserPrincipal();
        BeanUtils.copyProperties(principal, wageUserPrincipal);
        wageUserPrincipal.setLoginDateTime(null);
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            List<GroupInvoiceDTO> list = null;
            log.info("invoice.wageUserPrincipal:[{}]", JacksonUtil.objectToJson(wageUserPrincipal));
//            List<WageGroupInvoiceDTO> wageGroupInvoiceDTOList = wageMangerFeignService.invoice(idNumber); //改查mongo
            List<core.dto.response.GroupInvoiceDTO> wageGroupInvoiceDTOList = payrollFeignController.invoice(idNumber, principal.getEntId());
            log.info("invoice-->{}", wageGroupInvoiceDTOList);
            if (!CollectionUtils.isEmpty(wageGroupInvoiceDTOList)) {
                list = new ArrayList<>();
                for (core.dto.response.GroupInvoiceDTO wageGroupInvoiceDTO : wageGroupInvoiceDTOList) {
                    GroupInvoiceDTO groupInvoiceDTO = new GroupInvoiceDTO();
                    BeanUtils.copyProperties(wageGroupInvoiceDTO, groupInvoiceDTO);
                    list.add(groupInvoiceDTO);
                }
            }
            return list;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 验证密码
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
        CacheUserPrincipal cacheUserPrincipal = TransferUtil.userPrincipalToWageUserPrincipal(principal);
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            if (StringUtils.isEmpty(pwd)) {
                throw new ParamsIllegalException(ErrorConstant.WECHAR_007.getErrorMsg());
            }
            log.info("调用wageMangerFeignService.checkPwd(pwd,wageUserPrincipal)开始");
            boolean bool = payrollFeignController.checkPwd(pwd, cacheUserPrincipal);
            if (!bool) {
                throw new ParamsIllegalException(ErrorConstant.WECHAR_007.getErrorMsg());
            }
            try {
                //密码校验通过之后，缓存中登记一条记录，之后的几分钟只能不再输入密码，key：sessionId
                String redisKey = PayrollDBConstant.PREFIX + ":checkFreePassword:" + idNumberEncrytor;
                log.info("checkPwd.redisKey:[{}]", redisKey);
                redisTemplate.opsForValue().set(redisKey, true, 5, TimeUnit.MINUTES);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("免密入缓存失败:[{}]", e.getMessage());
            }
            return null;
        }).subscribeOn(Schedulers.elastic()).then();
    }

    /**
     * 验证银行卡后六位
     *
     * @return
     */
    @PostMapping("/checkCard")
    @TrackLog
    public Mono<Void> checkCard(@RequestBody CacheCheckCardDTO cacheCheckCardDTO) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        String wechatId = String.valueOf(WebContext.getCurrentUser().getWechatId());

        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            log.info("调用wageMangerFeignService.checkCard(idNumber,cardNo)开始");
            String cardNo = cacheCheckCardDTO.getCardNo();
            //数字键盘密码，解密
            String password = paswordService.checkNumberPassword(cardNo, wechatId);
            CacheCheckCardDTO checkCardDTOReq = new CacheCheckCardDTO();
            BeanUtils.copyProperties(cacheCheckCardDTO, checkCardDTOReq);
            checkCardDTOReq.setCardNo(password);

            boolean bool = payrollFeignController.checkCard(checkCardDTOReq);
            if (!bool) {
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
        CacheUserPrincipal build = TransferUtil.userPrincipalToWageUserPrincipal(userPrincipal);
//        BeanUtils.copyProperties(userPrincipal, build);
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            EmpInfoDTO empInfoDTO = null;
            log.info("调用PayrollFeignController.emp(wageUserPrincipal)开始");
            PayrollEmpInfoDTO emp = payrollFeignController.emp(build);
            if (emp != null) {
                empInfoDTO = new EmpInfoDTO();
                BeanUtils.copyProperties(emp, empInfoDTO);
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
    @Deprecated
    public Mono<EmpEntDTO> empEnt(@RequestHeader(value = "ent-id", required = false) String entId) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        UserPrincipal userPrincipal = WebContext.getCurrentUser();
        CacheUserPrincipal wageUserPrincipal = new CacheUserPrincipal();
        BeanUtils.copyProperties(userPrincipal, wageUserPrincipal);
        wageUserPrincipal.setEntId(entId);
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            if (StringUtils.isBlank(entId)) {
                log.error("企业id为空，请检查请求头");
                return new EmpEntDTO();
            }

            List<EmpEntDTO> list = null;
            log.info("调用wageMangerFeignService.empEnt(wageUserPrincipal)开始");
            List<CacheEmpEntDTO> wageEmpEntDTOList = payrollFeignController.empEnt(wageUserPrincipal);
            log.info("empEnt---->{}", wageEmpEntDTOList);
            if (!CollectionUtils.isEmpty(wageEmpEntDTOList)) {
                list = new ArrayList<>();
                for (CacheEmpEntDTO wageEmpEntDTO : wageEmpEntDTOList) {
                    EmpEntDTO empEntDTO = new EmpEntDTO();
//                   BeanUtils.copyProperties(wageEmpEntDTO,empEntDTO);
                    //脱敏处理
                    empEntDTO.setEntName(wageEmpEntDTO.getEntName());
                    empEntDTO.setShortEntName(wageEmpEntDTO.getShortEntName());
                    List<CacheBankCard> cardList = wageEmpEntDTO.getCards();
                    List<BankCard> cardListNew = new ArrayList<>();
                    if (null != cardList && cardList.size() > 0) {
                        for (CacheBankCard bankCard : cardList) {
                            BankCard bankCard1 = new BankCard();
                            List<core.dto.request.BankCardGroup> bankCardGroupList = bankCard.getBankCardGroups();
                            List<BankCardGroup> bankCardGroupList1 = new ArrayList<>();
                            if (null != bankCardGroupList && bankCardGroupList.size() > 0) {
                                for (core.dto.request.BankCardGroup bankCardGroup : bankCardGroupList) {
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
                    List<CacheRes100708> itemList = wageEmpEntDTO.getItems();
                    List<chain.fxgj.server.payroll.dto.response.Res100708> itemListNew = new ArrayList<>();
                    if (null != itemList && itemList.size() > 0) {
                        for (CacheRes100708 wageRes100708 : itemList) {
                            chain.fxgj.server.payroll.dto.response.Res100708 res100708 = new chain.fxgj.server.payroll.dto.response.Res100708();
                            List<CacheRes100708.BankCardListBean> bankCardList = wageRes100708.getBankCardList();
                            List<chain.fxgj.server.payroll.dto.response.Res100708.BankCardListBean> bankCardListBeans = new ArrayList<>();
                            if (null != bankCardList && bankCardList.size() > 0) {
                                for (CacheRes100708.BankCardListBean bankCardListBean : bankCardList) {
                                    chain.fxgj.server.payroll.dto.response.Res100708.BankCardListBean bankCardListBean1 = new chain.fxgj.server.payroll.dto.response.Res100708.BankCardListBean();
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
            return list.get(0);
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 员工银行卡【@】
     *
     * @return
     */
    @GetMapping("/empCard")
    @TrackLog
    public Mono<List<PayrollBankCardDTO>> empCard(
            @RequestHeader(value = "entId", required = false) String entId,
            @RequestHeader(value = "encry-salt", required = false) String salt,
            @RequestHeader(value = "encry-passwd", required = false) String passwd) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal userPrincipal = WebContext.getCurrentUser();
        String idNumber1 = userPrincipal.getIdNumber();
        String entId1 = userPrincipal.getEntId();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            List<EmpEntDTO> list = new ArrayList<>();
            CacheEmployeeInfoReq cacheEmployeeInfoReq = new CacheEmployeeInfoReq();
            cacheEmployeeInfoReq.setIdNumber(idNumber1);
            cacheEmployeeInfoReq.setEntId(entId1);
            log.info("调用wageMangerFeignService.empCard(wageUserPrincipal)开始idNumber:[{}], entId[{}]", idNumber1, entId1);
            List<PayrollBankCardDTO> payrollBankCardDTOS = payrollFeignController.empCard(cacheEmployeeInfoReq);
            //todo 加密处理
            if (!CollectionUtils.isEmpty(payrollBankCardDTOS)) {
                //数据转换及加密处理
                for (PayrollBankCardDTO item : payrollBankCardDTOS) {
                    item.setCardNo(EncrytorUtils.encryptField(item.getCardNo(), salt, passwd));
                    item.setOldCardNo(EncrytorUtils.encryptField(item.getOldCardNo(), salt, passwd));
                    item.setSalt(salt);
                    item.setPasswd(passwd);
                }
            }
            log.info("加密返回empCard.payrollBankCardDTOS:[{}]", JacksonUtil.objectToJson(payrollBankCardDTOS));
            return payrollBankCardDTOS;
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
            List<EmpCardLogDTO> list = null;
            log.info("调用wageMangerFeignService.empCardLog(ids)开始");
            List<PayrollEmpCardLogDTO> wageEmpCardLogList = payrollFeignController.empCardLog(ids);
            log.info("empCardLog--->{}", wageEmpCardLogList);
            if (!CollectionUtils.isEmpty(wageEmpCardLogList)) {
                list = new ArrayList<>();
                for (PayrollEmpCardLogDTO wageEmpCardLogDTO : wageEmpCardLogList) {
                    EmpCardLogDTO logDTO = new EmpCardLogDTO();
                    BeanUtils.copyProperties(wageEmpCardLogDTO, logDTO);
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
        CacheUserPrincipal cacheUserPrincipal = new CacheUserPrincipal();
        BeanUtils.copyProperties(userPrincipal, cacheUserPrincipal);
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            List<EmployeeListBean> list = null;
            log.info("调用wageMangerFeignService.entPhone(cacheUserPrincipal)开始");
            List<core.dto.response.EmployeeListBean> wageEmployeeListBeanList = payrollFeignController.entPhone(cacheUserPrincipal);
            log.info("entPhone-->{}", wageEmployeeListBeanList);
            if (!CollectionUtils.isEmpty(wageEmployeeListBeanList)) {
                list = new ArrayList<>();
                for (core.dto.response.EmployeeListBean welb : wageEmployeeListBeanList) {
                    EmployeeListBean eb = new EmployeeListBean();
                    BeanUtils.copyProperties(welb, eb);
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
            log.info("返回脱敏数据entPhone.list:[{}]", JacksonUtil.objectToJson(list));
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
            List<EntUserDTO> list = null;
            log.info("调用wageMangerFeignService.entUser(entId)开始");
            List<WageEntUserDTO> wageEntUserDTOList = wageMangerFeignService.entUser(entId);
            if (!CollectionUtils.isEmpty(wageEntUserDTOList)) {
                list = new ArrayList<>();
                for (WageEntUserDTO wageEntUserDTO : wageEntUserDTOList) {
                    EntUserDTO entUserDTO = new EntUserDTO();
                    BeanUtils.copyProperties(wageEntUserDTO, entUserDTO);
                    list.add(entUserDTO);
                }
            }
            return list;
        }).subscribeOn(Schedulers.elastic());
    }


    /**
     * 数据转换及加密处理
     *
     * @return
     */
    private List<EmpEntDTO> transfalWageEmpEntDto(List<PayrollEmpEntDTO> wageEmpEntDTOList, String salt, String passwd) {
        List<EmpEntDTO> empEntDTOList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(wageEmpEntDTOList)) {
            empEntDTOList = new ArrayList<>();
            for (PayrollEmpEntDTO wageEmpEntDTO : wageEmpEntDTOList) {
                EmpEntDTO empEntDTO = new EmpEntDTO();
                //脱敏处理
                empEntDTO.setEntName(wageEmpEntDTO.getEntName());
                empEntDTO.setShortEntName(wageEmpEntDTO.getShortEntName());
                List<PayrollBankCardDTO> cardList = wageEmpEntDTO.getCards();
                List<BankCard> cardListNew = new ArrayList<>();
                if (null != cardList && cardList.size() > 0) {
                    for (PayrollBankCardDTO bankCard : cardList) {
                        BankCard bankCard1 = new BankCard();
                        List<PayrollBankCardGroupDTO> bankCardGroupList = bankCard.getBankCardGroups();
                        List<BankCardGroup> bankCardGroupList1 = new ArrayList<>();
                        if (null != bankCardGroupList && bankCardGroupList.size() > 0) {
                            for (PayrollBankCardGroupDTO bankCardGroup : bankCardGroupList) {
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

                List<PayrollRes100708DTO> itemList = wageEmpEntDTO.getItems();
                List<Res100708> itemListNew = new ArrayList<>();
                if (null != itemList && itemList.size() > 0) {
                    for (PayrollRes100708DTO wageRes100708 : itemList) {
                        Res100708 res100708 = new Res100708();
                        List<PayrollRes100708DTO.BankCardListBean> bankCardList = wageRes100708.getBankCardList();
                        List<Res100708.BankCardListBean> bankCardListBeans = new ArrayList<>();
                        if (null != bankCardList && bankCardList.size() > 0) {
                            for (PayrollRes100708DTO.BankCardListBean bankCardListBean : bankCardList) {
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
//                    empEntDTO.setItems(itemListNew);
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
            String redisKey = PayrollDBConstant.PREFIX + ":checkFreePassword:" + idNumberEncrytor;
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
    public Mono<Res100703> wageList(
//            @RequestHeader(value = "ent-id") String entId,
            @RequestParam("groupId") String groupId,
            @RequestParam("year") String year,
            @RequestParam("type") String type) {
        log.info("调用wageList开始时间::[{}]", LocalDateTime.now());
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal principal = WebContext.getCurrentUser();
        String entId = principal.getEntId();
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
                payrollRes100703ReqDTO.setEntId(entId);
                log.info("开始调用wageList时间:[{}]", LocalDateTime.now());
                log.info("wageList.payrollRes100703ReqDTO:[{}]", JacksonUtil.objectToJson(payrollRes100703ReqDTO));
                PayrollRes100703DTO source = payrollFeignController.wageList(payrollRes100703ReqDTO);
                if (null != source) {
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

                    }
                    log.info("mongo.wageList:[{}]", JacksonUtil.objectToJson(res100703));
                } else {
                    log.info("查询mongo未查询到数据");
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("wageList查询mongo异常:[{}]", e);
            }
            log.info("调用wageList返回时间::[{}]", LocalDateTime.now());
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
    public Mono<List<WageDetailDTO>> wageDetail(@RequestHeader(value = "jsession-id", required = false) String jsessionId,
                                                @RequestParam("wageSheetId") String wageSheetId,
                                                @RequestParam("groupId") String groupId) {
        log.info("调用wageDetail开始时间::[{}]", LocalDateTime.now());
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            List<WageDetailDTO> list = new ArrayList<>();
            List<WageDetailDTO.Content> content = new ArrayList<>();
            content.add(WageDetailDTO.Content.builder().colNum(0).value("白浅浅").build());
            content.add(WageDetailDTO.Content.builder().colNum(1).value("367866199309052077").build());
            content.add(WageDetailDTO.Content.builder().colNum(2).value("6230202013350302").build());
            content.add(WageDetailDTO.Content.builder().colNum(3).value("30").build());
            List<WageHeadDTO.Cell> heads = new ArrayList<>();
            heads.add(WageHeadDTO.Cell.builder().colName("姓名").hidden(true).colNum(Arrays.asList(0)).build());
            heads.add(WageHeadDTO.Cell.builder().colName("身份证号").hidden(true).colNum(Arrays.asList(1)).build());
            heads.add(WageHeadDTO.Cell.builder().colName("银行卡号").hidden(true).colNum(Arrays.asList(2)).build());
            heads.add(WageHeadDTO.Cell.builder().colName("实发金额").hidden(true).colNum(Arrays.asList(3)).build());
            WageHeadDTO wageHeadDTO = WageHeadDTO.builder()
                    .headIndex(0)
                    .heads(heads)
                    .isDoubleRow(true)
                    .build();
            WageDetailDTO.WageShowDTO wageShowDTO = WageDetailDTO.WageShowDTO.builder()
                    .grantName("上海市好麦多食品有限公司")
                    .isReceipt(1)
                    .isShow0(1)
                    .isSign(1)
                    .receiptDay(10)
                    .build();
            list.add(WageDetailDTO.builder()
                    .bankName("华夏银行")
                    .cardNo("6230202013350302")
                    .content(content)
                    .differRealAmt(BigDecimal.ZERO)
                    .entName("上海市好麦多食品有限公司")
                    .groupId(UUIDUtil.createUUID32())
                    .groupName("上海市好麦多食品有限公司")
                    .payStatus("1")
                    .pushDateTime(System.currentTimeMillis())
                    .realAmt(new BigDecimal("30"))
                    .receiptStautus(3)
                    .sign(null)
                    .skinUrl("https://wxp.cardpu.com/upload/image/20200910_red.png")
                    .wageDetailId(UUIDUtil.createUUID32())
                    .wageHeadDTO(wageHeadDTO)
                    .wageName("9月份工资代发")
                    .wageShowDTO(wageShowDTO)
                    .build());
//            try {
//                PayrollWageDetailReqDTO payrollWageDetailReqDTO = new PayrollWageDetailReqDTO();
//                payrollWageDetailReqDTO.setIdNumber(idNumber);
//                payrollWageDetailReqDTO.setGroupId(groupId);
//                payrollWageDetailReqDTO.setWageSheetId(wageSheetId);
//                payrollWageDetailReqDTO.setPayrollUserPrincipalDTO(payrollUserPrincipalDTO);
//                log.info("groupId:[{}]，idNumber:[{}]，wageSheetId:[{}]", groupId, idNumber, wageSheetId);
//                log.info("调用payrollFeignController.wageDetail(payrollWageDetailReqDTO)，查明细开始时间:[{}]", LocalDateTime.now());
//                List<PayrollWageDetailDTO> source = payrollFeignController.wageDetail(payrollWageDetailReqDTO);
//                log.info("调用payrollFeignController.wageDetail(payrollWageDetailReqDTO)，查明细结束时间:[{}]", LocalDateTime.now());
//                log.info("source.size():[{}]", source.size());
//                if (null != source && source.size() > 0) {
//                    for (PayrollWageDetailDTO payrollWageDetailDTO : source) {
//                        WageDetailDTO wageDetailDTO = new WageDetailDTO();
//
//                        PayrollWageHeadDTO payrollWageHeadDTO = payrollWageDetailDTO.getWageHeadDTO();
//                        WageHeadDTO wageHeadDTO = new WageHeadDTO();
//                        wageHeadDTO.setDoubleRow(payrollWageHeadDTO.isDoubleRow());
//                        wageHeadDTO.setHeadIndex(payrollWageHeadDTO.getHeadIndex());
//                        List<PayrollWageHeadDTO.Cell> heads = payrollWageHeadDTO.getHeads();
//                        List<WageHeadDTO.Cell> headsList = new ArrayList<>();
//                        for (PayrollWageHeadDTO.Cell head : heads) {
//                            WageHeadDTO.Cell cell = new WageHeadDTO.Cell();
//                            cell.setColName(head.getColName());
//                            cell.setColNum(head.getColNum());
//                            cell.setHidden(head.isHidden());
//                            PayrollWageHeadDTO.Type type = head.getType();
//                            WageHeadDTO.Type type1 = WageHeadDTO.Type.valueOf(type.name());
//                            cell.setType(type1);
//                            headsList.add(cell);
//                        }
//                        wageHeadDTO.setHeads(headsList);
//                        wageDetailDTO.setWageHeadDTO(wageHeadDTO);
//
//                        PayrollWageDetailDTO.PayrollWageShowDTO payrollWageShowDTO = payrollWageDetailDTO.getWageShowDTO();
//                        WageDetailDTO.WageShowDTO wageShowDTO = new WageDetailDTO.WageShowDTO();
//                        BeanUtils.copyProperties(payrollWageShowDTO, wageShowDTO);
//                        wageDetailDTO.setWageShowDTO(wageShowDTO);
//
//                        List<PayrollWageDetailDTO.Content> payrollContent = payrollWageDetailDTO.getContent();
//                        List<WageDetailDTO.Content> contentList = new ArrayList<>();
//                        for (PayrollWageDetailDTO.Content content : payrollContent) {
//                            WageDetailDTO.Content content1 = new WageDetailDTO.Content();
//                            BeanUtils.copyProperties(content, content1);
//                            contentList.add(content1);
//                        }
//                        wageDetailDTO.setContent(contentList);
//
//                        wageDetailDTO.setWageDetailId(payrollWageDetailDTO.getWageDetailId());
//                        wageDetailDTO.setBankName(payrollWageDetailDTO.getBankName());
//                        wageDetailDTO.setCardNo(payrollWageDetailDTO.getCardNo());
//                        wageDetailDTO.setWageName(payrollWageDetailDTO.getWageName());
//                        wageDetailDTO.setRealAmt(payrollWageDetailDTO.getRealAmt());
//                        wageDetailDTO.setEntName(payrollWageDetailDTO.getEntName());
//                        wageDetailDTO.setGroupName(payrollWageDetailDTO.getGroupName());
//                        wageDetailDTO.setGroupId(payrollWageDetailDTO.getGroupId());
//                        wageDetailDTO.setPushDateTime(payrollWageDetailDTO.getPushDateTime());
//                        wageDetailDTO.setSkinUrl(payrollWageDetailDTO.getSkinUrl());
//                        wageDetailDTO.setReceiptStautus(payrollWageDetailDTO.getReceiptStautus());
//                        wageDetailDTO.setDifferRealAmt(payrollWageDetailDTO.getDifferRealAmt());
//                        wageDetailDTO.setPayStatus(payrollWageDetailDTO.getPayStatus());
//                        wageDetailDTO.setSign(payrollWageDetailDTO.getSign());
//                        list.add(wageDetailDTO);
//                    }
//                } else {
//                    log.info("mongo.wageDetail查询数据为空,idNumber:[{}]", idNumber);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                log.info("查询mongo异常:[{}]", e);
//            }
//
//            //工资详情查询完成之后，更新缓存中的entId
//            if (StringUtils.isNotBlank(jsessionId)) {
//                CacheUserPrincipal wechatInfoDetail = empWechatService.getWechatInfoDetail(jsessionId);
//                WageSheetDTO wageSheet = wageSheetFeignController.findById(wageSheetId);
//                empWechatService.upWechatInfoDetail(jsessionId, wageSheet.getEntId(), wechatInfoDetail);
//            } else {
//                log.info("wageDetail未更新缓存中的entId");
//            }

            log.info("web.list:[{}]", JacksonUtil.objectToJson(list));
            return list;
        }).subscribeOn(Schedulers.elastic());
    }

    /**
     * 保存发工资条用户签名
     *
     * @param req
     * @return
     */
    @PostMapping("/saveSigned")
    @TrackLog
    public Mono<Void> saveSigned(@RequestBody SignedSaveReq req) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);

            Optional.ofNullable(req.getWageSheetId()).orElseThrow(() -> new ParamsIllegalException(chain.wage.core.constant.ErrorConstant.SYS_ERROR.format("代发方案ID不能为空")));
            Optional.ofNullable(req.getWageDetailId()).orElseThrow(() -> new ParamsIllegalException(chain.wage.core.constant.ErrorConstant.SYS_ERROR.format("企代发明细ID不能为空")));
            Optional.ofNullable(req.getSign()).orElseThrow(() -> new ParamsIllegalException(chain.wage.core.constant.ErrorConstant.SYS_ERROR.format("签名不能为空")));

            log.info("=====> 保存发工资条用户签名 req:{}", JsonUtil.objectToJson(req));
//
//            //获取方案
//            WageResultRespone wageSheet = wageFeignService.getWageSheet(req.getWageSheetId());
//            if (null == wageSheet) {
//                throw new ParamsIllegalException(ErrorConstant.Error0001.format("代发方案"));
//            }
//
//            //获取明细
//            WageDetailResult wageDetail = null;
//            Boolean b = false;
//            for (WageDetailResult detail : wageSheet.getDetailResults()
//            ) {
//                if (detail.getId().equals(req.getWageSheetId())) {
//                    wageDetail = detail;
//                    b = true;
//                    break;
//                }
//            }
//            if (!b) {
//                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("未找到方案明细"));
//            }
//
//            //保存签名
//            SignedReceiptSaveReq saveReq = SignedReceiptSaveReq.builder()
//                    .crtDateTime(LocalDateTime.now())
//                    .employeeSid(wageDetail.getEmployeeSid())
//                    .entId(wageSheet.getEntId())
//                    .groupId(wageSheet.getGroupId())
//                    .idNumber(wageDetail.getIdNumber())
////                    .receiptPath()
//                    .signedReceiptId(UUIDUtil.createUUID32())
//                    .signImg(req.getSign())
//                    .updDateTime(LocalDateTime.now())
//                    .wageDetailId(wageDetail.getId())
//                    .wageSheetId(wageSheet.getWageSheetId())
//                    .build();
//            signedReceiptFeignController.save(saveReq);
            return null;
        }).subscribeOn(Schedulers.elastic()).then();
    }
}
