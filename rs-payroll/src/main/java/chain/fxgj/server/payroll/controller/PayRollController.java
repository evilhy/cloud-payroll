package chain.fxgj.server.payroll.controller;

import chain.css.exception.ErrorMsg;
import chain.css.exception.ParamsIllegalException;
import chain.css.log.annotation.TrackLog;
import chain.feign.hxinside.ent.service.EmployeeInfoServiceFeign;
import chain.fxgj.core.common.config.properties.PayrollProperties;
import chain.fxgj.core.common.constant.PayrollDBConstant;
import chain.fxgj.ent.core.dto.request.EmployeeQueryRequest;
import chain.fxgj.ent.core.dto.response.EmployeeInfoRes;
import chain.fxgj.feign.client.EnterpriseFeignService;
import chain.fxgj.feign.client.GroupInfoFeignService;
import chain.fxgj.feign.client.PayRollFeignService;
import chain.fxgj.server.payroll.constant.ErrorConstant;
import chain.fxgj.server.payroll.dto.payroll.CheckPwdDTO;
import chain.fxgj.server.payroll.dto.payroll.ContentDTO;
import chain.fxgj.server.payroll.dto.payroll.EntEmpDTO;
import chain.fxgj.server.payroll.dto.payroll.SignedReceiptPdfDTO;
import chain.fxgj.server.payroll.dto.request.SignedSaveReq;
import chain.fxgj.server.payroll.dto.response.EmployeeListBean;
import chain.fxgj.server.payroll.dto.response.EntUserDTO;
import chain.fxgj.server.payroll.dto.response.GroupInvoiceDTO;
import chain.fxgj.server.payroll.dto.response.Res100701;
import chain.fxgj.server.payroll.dto.response.*;
import chain.fxgj.server.payroll.service.EmpWechatService;
import chain.fxgj.server.payroll.service.EmployeeEncrytorService;
import chain.fxgj.server.payroll.service.PaswordService;
import chain.fxgj.server.payroll.util.*;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.fxgj.server.payroll.web.WebContext;
import chain.payroll.client.feign.*;
import chain.utils.commons.JacksonUtil;
import chain.utils.commons.JsonUtil;
import chain.utils.commons.UUIDUtil;
import chain.utils.fxgj.constant.DictEnums.AttestStatusEnum;
import chain.utils.fxgj.constant.DictEnums.DelStatusEnum;
import chain.utils.fxgj.constant.DictEnums.IsStatusEnum;
import chain.wage.manager.core.dto.response.WageEntUserDTO;
import chain.wage.manager.core.dto.response.WageRes100708;
import chain.wage.manager.core.dto.response.enterprise.EntErpriseInfoDTO;
import chain.wage.manager.core.dto.response.group.GroupInfoDTO;
import chain.wage.manager.core.dto.web.WageUserPrincipal;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import core.dto.request.CacheCheckCardDTO;
import core.dto.request.CacheEmployeeInfoReq;
import core.dto.request.empCard.EmployeeCardLogQueryReq;
import core.dto.request.empCard.EmployeeCardQueryReq;
import core.dto.request.employeeTaxAttest.EmployeeTaxAttestQueryReq;
import core.dto.request.employeeTaxSigning.EmployeeTaxSigningQueryReq;
import core.dto.response.*;
import core.dto.response.cardbin.CardbinQueRes;
import core.dto.response.empCard.EmployeeCardDTO;
import core.dto.response.empCard.EmployeeCardLogDTO;
import core.dto.response.employeeTaxAttest.EmployeeTaxAttestDTO;
import core.dto.response.employeeTaxSigning.EmployeeTaxSigningDTO;
import core.dto.response.signedreceipt.SignedReceiptSaveReq;
import core.dto.response.wagesheet.WageSheetDTO;
import core.dto.wechat.CacheUserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import java.io.File;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * ?????????
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
    PayRollFeignService wageMangerFeignService;
    @Autowired
    PayrollProperties payrollProperties;

    @Autowired
    PaswordService paswordService;
    @Autowired
    EmpWechatService empWechatService;
    @Autowired
    WageSheetFeignController wageSheetFeignController;
    @Autowired
    WageDetailYearFeignController wageDetailYearFeignController;
    @Autowired
    SignedReceiptFeignController signedReceiptFeignController;
    @Autowired
    EmployeeCardFeignService employeeCardFeignService;
    @Autowired
    EmployeeInfoServiceFeign employeeInfoServiceFeign;
    @Autowired
    EmployeeCardLogFeignService employeeCardLogFeignService;
    @Autowired
    GroupInfoFeignService groupInfoFeignService;
    @Autowired
    EnterpriseFeignService enterpriseFeignService;
    @Autowired
    CardBinFeignService cardBinFeignService;
    @Autowired
    EmployeeTaxAttestFeignService employeeTaxAttestFeignService;
    @Autowired
    EmployeeTaxSigningFeignService employeeTaxSigningFeignService;

    /**
     * ??????????????????
     *
     * @return
     */
    @GetMapping("/sdt")
    @TrackLog
    @PermitAll
    public Mono<Long> serverDateTime() {
        return Mono.fromCallable(
                () -> LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        ).subscribeOn(Schedulers.boundedElastic());
    }


    /**
     * ??????????????????(????????????-????????????)
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
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * ?????????????????????????????????????????????
     *
     * @return
     */
    @PostMapping("/entEmp")
    @TrackLog
    public Mono<Res100701> entEmp(@RequestBody EntEmpDTO entEmpDTO, @RequestHeader(value = "encry-salt", required = false) String salt,
                                  @RequestHeader(value = "encry-passwd", required = false) String passwd) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal principal = WebContext.getCurrentUser();
        //?????????????????????
        String idNumber = entEmpDTO.getIdNumber().toUpperCase();
        if (StringUtils.isBlank(idNumber)) {
            throw new ParamsIllegalException(new ErrorMsg("9999", "??????????????????!"));
        }
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            CacheUserPrincipal wageUserPrincipal = TransferUtil.userPrincipalToWageUserPrincipal(principal);
            Res100701 res100701 = null;
            log.info("??????wageMangerFeignService.entEmp(idNumber,wageUserPrincipal)??????");
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
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * ?????????????????? ???????????????????????????
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
            log.info("??????wageMangerFeignService.empInfo(wageUserPrincipal)??????");
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
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * ???????????????????????????@???
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
//            List<WageGroupInvoiceDTO> wageGroupInvoiceDTOList = wageMangerFeignService.invoice(idNumber); //??????mongo
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
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * ????????????
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
            log.info("??????wageMangerFeignService.checkPwd(pwd,wageUserPrincipal)??????");
            boolean bool = payrollFeignController.checkPwd(pwd, cacheUserPrincipal);
            if (!bool) {
                throw new ParamsIllegalException(ErrorConstant.WECHAR_007.getErrorMsg());
            }
            try {
                //??????????????????????????????????????????????????????????????????????????????????????????????????????key???sessionId
                String redisKey = PayrollDBConstant.PREFIX + ":checkFreePassword:" + idNumberEncrytor;
                log.info("checkPwd.redisKey:[{}]", redisKey);
                redisTemplate.opsForValue().set(redisKey, true, 5, TimeUnit.MINUTES);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("?????????????????????:[{}]", e.getMessage());
            }
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * ????????????????????????
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
            log.info("??????wageMangerFeignService.checkCard(idNumber,cardNo)??????");
            String cardNo = cacheCheckCardDTO.getCardNo();
            //???????????????????????????
            String password = paswordService.checkNumberPassword(cardNo, wechatId);
            CacheCheckCardDTO checkCardDTOReq = new CacheCheckCardDTO();
            BeanUtils.copyProperties(cacheCheckCardDTO, checkCardDTOReq);
            checkCardDTOReq.setCardNo(password);

            boolean bool = payrollFeignController.checkCard(checkCardDTOReq);
            if (!bool) {
                throw new ParamsIllegalException(ErrorConstant.WECHAR_006.getErrorMsg());
            }
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }


    /**
     * ?????????????????????@???
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
            log.info("??????PayrollFeignController.emp(wageUserPrincipal)??????");
            PayrollEmpInfoDTO emp = payrollFeignController.emp(build);
            if (emp != null) {
                empInfoDTO = new EmpInfoDTO();
                BeanUtils.copyProperties(emp, empInfoDTO);
                //???????????????????????????
                empInfoDTO.setIdNumber(empInfoDTO.getIdNumberStar());
                empInfoDTO.setPhone(empInfoDTO.getPhoneStar());
            }
            log.info("??????????????????emp.empInfoDTO.ret:[{}]", JacksonUtil.objectToJson(empInfoDTO));

            empInfoDTO.setSignStatus(IsStatusEnum.NO.getCode());
            empInfoDTO.setSignStatusVal(IsStatusEnum.NO.getDesc());
            empInfoDTO.setAttestStatus(AttestStatusEnum.NOT.getCode());
            empInfoDTO.setAttestStatusVal(AttestStatusEnum.NOT.getDesc());
            //??????????????????
            EmployeeTaxAttestQueryReq attestQueryReq = EmployeeTaxAttestQueryReq.builder()
                    .idNumber(userPrincipal.getIdNumber())
                    .userName(empInfoDTO.getName())
                    .phone(userPrincipal.getPhone())
                    .delStatusEnums(Arrays.asList(DelStatusEnum.normal))
                    .build();
            List<EmployeeTaxAttestDTO> attestDTOList = employeeTaxAttestFeignService.list(attestQueryReq);
            if (null != attestDTOList && attestDTOList.size() > 0) {
                EmployeeTaxAttestDTO employeeTaxAttestDTO = attestDTOList.get(0);
                empInfoDTO.setAttestStatus(null == employeeTaxAttestDTO.getAttestStatus() ? AttestStatusEnum.NOT.getCode() : employeeTaxAttestDTO.getAttestStatus().getCode());
                empInfoDTO.setAttestStatusVal(null == employeeTaxAttestDTO.getAttestStatus() ? AttestStatusEnum.NOT.getDesc() : employeeTaxAttestDTO.getAttestStatus().getDesc());

                //??????????????????
                EmployeeTaxSigningQueryReq signingQueryReq = EmployeeTaxSigningQueryReq.builder()
                        .entId(userPrincipal.getEntId())
                        .empTaxAttestId(employeeTaxAttestDTO.getId())
                        .signStatus(IsStatusEnum.YES)
                        .build();
                List<EmployeeTaxSigningDTO> signingDTOS = employeeTaxSigningFeignService.list(signingQueryReq);
                if (null != signingDTOS && signingDTOS.size() > 0) {
                    EmployeeTaxSigningDTO signing = signingDTOS.get(0);
                    empInfoDTO.setTaxSignId(signing.getId());
                    empInfoDTO.setSignStatus(signing.getSignStatus().getCode());
                    empInfoDTO.setSignStatusVal(signing.getSignStatus().getDesc());
                }
            }
            return empInfoDTO;
        }).subscribeOn(Schedulers.boundedElastic());

    }

    /**
     * ???????????????@???
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
                log.error("??????id???????????????????????????");
                return new EmpEntDTO();
            }

            List<EmpEntDTO> list = null;
            log.info("??????wageMangerFeignService.empEnt(wageUserPrincipal)??????");
            List<CacheEmpEntDTO> wageEmpEntDTOList = payrollFeignController.empEnt(wageUserPrincipal);
            log.info("empEnt---->{}", wageEmpEntDTOList);
            if (!CollectionUtils.isEmpty(wageEmpEntDTOList)) {
                list = new ArrayList<>();
                for (CacheEmpEntDTO wageEmpEntDTO : wageEmpEntDTOList) {
                    EmpEntDTO empEntDTO = new EmpEntDTO();
//                   BeanUtils.copyProperties(wageEmpEntDTO,empEntDTO);
                    //????????????
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
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * ??????????????????@???
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
            log.info("??????wageMangerFeignService.empCard(wageUserPrincipal)??????idNumber:[{}], entId[{}]", idNumber1, entId1);
            List<PayrollBankCardDTO> payrollBankCardDTOS = payrollFeignController.empCard(cacheEmployeeInfoReq);
            //todo ????????????
            if (!CollectionUtils.isEmpty(payrollBankCardDTOS)) {
                //???????????????????????????
                for (PayrollBankCardDTO item : payrollBankCardDTOS) {
                    String cardNo = item.getCardNo();
                    item.setCardNo(EncrytorUtils.encryptField(item.getCardNo(), salt, passwd));
                    item.setOldCardNo(EncrytorUtils.encryptField(item.getOldCardNo(), salt, passwd));
                    item.setSalt(salt);
                    item.setPasswd(passwd);
                    item.setCardType(0);
                    item.setCardTypeVal("?????????");

                    //????????????
                    EmployeeQueryRequest employeeQueryRequest = EmployeeQueryRequest.builder()
                            .entId(entId1)
                            .idNumber(idNumber1)
                            .build();
                    List<EmployeeInfoRes> employeeInfoRes = employeeInfoServiceFeign.empInfoList(employeeQueryRequest);
                    if (null == employeeInfoRes || employeeInfoRes.size() <= 0) {
                        continue;
                    }
                    List<String> employeeIds = new ArrayList<>();
                    Map<String, EmployeeInfoRes> employeeInfoResMap = new HashMap<>();
                    for (EmployeeInfoRes emp : employeeInfoRes
                    ) {
                        employeeIds.add(emp.getEmployeeId());
                        employeeInfoResMap.put(emp.getEmployeeId(), emp);
                    }

                    //????????????????????????
                    LocalDateTime logCrtDateTime = null;
                    EmployeeCardLogDTO dto = null;
                    EmployeeCardLogQueryReq logQueryReq = EmployeeCardLogQueryReq.builder()
                            .cardNo(cardNo)
                            .delStatus(Arrays.asList(DelStatusEnum.normal))
                            .build();
                    List<EmployeeCardLogDTO> logDTOS = employeeCardLogFeignService.query(logQueryReq);
                    if (null != logDTOS && logDTOS.size() > 0) {
                        int index = logDTOS.size() - 1;
                        dto = logDTOS.get(index);
                        logCrtDateTime = dto.getCrtDateTime();
                    }

                    //???????????????
                    EmployeeCardQueryReq employeeCardQueryReq = EmployeeCardQueryReq.builder()
                            .employeeIds(employeeIds)
                            .cardNo(cardNo)
                            .build();
                    List<EmployeeCardDTO> cardDTOS = employeeCardFeignService.query(employeeCardQueryReq);
                    EmployeeCardDTO employeeCardDTO = null;
                    LocalDateTime crtDateTime = null;
                    if (null != cardDTOS && cardDTOS.size() > 0) {
                        employeeCardDTO = cardDTOS.get(0);
                        employeeCardDTO.getCrtDateTime();

                    }

                    if ((null != logCrtDateTime && null != crtDateTime && logCrtDateTime.isBefore(crtDateTime))
                            || (null != logCrtDateTime && null == crtDateTime)) {
                        String bankCardId = dto.getBankCardId();
                        employeeCardDTO = employeeCardFeignService.findById(bankCardId);
                    }

                    if (null != employeeCardDTO) {
                        //?????????
                        EmployeeInfoRes employee = employeeInfoResMap.get(employeeCardDTO.getEmployeeId());
                        if (null != employee) {
                            item.setUserName(employee.getEmployeeName());

                            //???????????????????????????
                            GroupInfoDTO groupInfoDTO = groupInfoFeignService.findById(employee.getGroupId());
                            item.setGroupName(null == groupInfoDTO ? null : groupInfoDTO.getGroupName());

                            //???????????????????????????
                            EntErpriseInfoDTO erpriseInfoDTO = enterpriseFeignService.findById(employee.getEntId());
                            item.setEntName(null == erpriseInfoDTO ? null : erpriseInfoDTO.getEntName());
                        }
                    }
                }
            }
            log.info("????????????empCard.payrollBankCardDTOS:[{}]", JacksonUtil.objectToJson(payrollBankCardDTOS));
            return payrollBankCardDTOS;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * ???????????????????????????
     *
     * @param ids ??????
     * @return
     */
    @GetMapping("/empCardLog")
    @TrackLog
    public Mono<List<EmpCardLogDTO>> empCardLog(@RequestParam("ids") String ids) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            List<EmpCardLogDTO> list = null;
            log.info("??????wageMangerFeignService.empCardLog(ids)??????");
            List<PayrollEmpCardLogDTO> wageEmpCardLogList = payrollFeignController.empCardLog(ids);
            log.info("empCardLog--->{}", wageEmpCardLogList);
            if (!CollectionUtils.isEmpty(wageEmpCardLogList)) {
                list = new ArrayList<>();
                for (PayrollEmpCardLogDTO wageEmpCardLogDTO : wageEmpCardLogList) {
                    EmpCardLogDTO logDTO = new EmpCardLogDTO();
                    BeanUtils.copyProperties(wageEmpCardLogDTO, logDTO);
                    //????????????
                    logDTO.setCardNo(SensitiveInfoUtils.bankCard(logDTO.getCardNo()));
                    logDTO.setCardNoOld(SensitiveInfoUtils.bankCard(logDTO.getCardNoOld()));
                    list.add(logDTO);
                }
            }
            log.info("????????????empCardLog.list:[{}]", JacksonUtil.objectToJson(list));
            return list;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * ????????????????????????
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
            log.info("??????wageMangerFeignService.entPhone(cacheUserPrincipal)??????");
            List<core.dto.response.EmployeeListBean> wageEmployeeListBeanList = payrollFeignController.entPhone(cacheUserPrincipal);
            log.info("entPhone-->{}", wageEmployeeListBeanList);
            if (!CollectionUtils.isEmpty(wageEmployeeListBeanList)) {
                list = new ArrayList<>();
                for (core.dto.response.EmployeeListBean welb : wageEmployeeListBeanList) {
                    EmployeeListBean eb = new EmployeeListBean();
                    BeanUtils.copyProperties(welb, eb);
                    //?????????????????????????????????
                    String idNumberEncrypt = EncrytorUtils.encryptField(eb.getIdNumber(), salt, passwd);
                    String mobileEncrypt = EncrytorUtils.encryptField(eb.getPhone(), salt, passwd);
                    eb.setIdNumber(idNumberEncrypt);
                    eb.setPhone(mobileEncrypt);
                    eb.setSalt(salt);
                    eb.setPasswd(passwd);
                    list.add(eb);
                }
            }
            log.info("??????????????????entPhone.list:[{}]", JacksonUtil.objectToJson(list));
            return list;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * ????????????
     *
     * @param entId ????????????
     * @return
     */
    @GetMapping("/entUser")
    @TrackLog
    public Mono<List<EntUserDTO>> entUser(@RequestParam("entId") String entId) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            List<EntUserDTO> list = null;
            log.info("??????wageMangerFeignService.entUser(entId)??????");
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
        }).subscribeOn(Schedulers.boundedElastic());
    }


    /**
     * ???????????????????????????
     *
     * @return
     */
    private List<EmpEntDTO> transfalWageEmpEntDto(List<PayrollEmpEntDTO> wageEmpEntDTOList, String salt, String passwd) {
        List<EmpEntDTO> empEntDTOList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(wageEmpEntDTOList)) {
            empEntDTOList = new ArrayList<>();
            for (PayrollEmpEntDTO wageEmpEntDTO : wageEmpEntDTOList) {
                EmpEntDTO empEntDTO = new EmpEntDTO();
                //????????????
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
     * ??????????????????
     *
     * @return true ?????????false ??????????????????
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
                log.info("idNumberEncrytor??????????????????false?????????????????????");
                return false;
            }
            String redisKey = PayrollDBConstant.PREFIX + ":checkFreePassword:" + idNumberEncrytor;
            Object value = redisTemplate.opsForValue().get(redisKey);
            if (value == null) {
                log.info("??????idNumberEncrytor:[{}]????????????????????????", idNumberEncrytor);
                return false;
            }
            log.info("??????idNumberEncrytor:[{}]?????????????????????,value:[{}]", idNumberEncrytor, value);
            return true;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * ??????????????????
     *
     * @param groupId ??????Id
     * @param year    ??????
     * @param type    ?????? 0???????????? 1??????
     * @return
     */
    @GetMapping("/wageList")
    @TrackLog
    public Mono<Res100703> wageList(
//            @RequestHeader(value = "ent-id") String entId,
            @RequestParam("groupId") String groupId,
            @RequestParam("year") String year,
            @RequestParam("type") String type) {
        log.info("??????wageList????????????::[{}]", LocalDateTime.now());
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
                log.info("????????????wageList??????:[{}]", LocalDateTime.now());
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
                    log.info("??????mongo??????????????????");
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("wageList??????mongo??????:[{}]", e);
            }
            log.info("??????wageList????????????::[{}]", LocalDateTime.now());
            return res100703;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * ?????????????????????
     *
     * @param wageSheetId ??????id
     * @param groupId     ??????id
     * @return
     */
    @GetMapping("/wageDetail")
    @TrackLog
    public Mono<List<WageDetailDTO>> wageDetail(@RequestHeader(value = "jsession-id", required = false) String jsessionId,
                                                @RequestParam("wageSheetId") String wageSheetId,
                                                @RequestParam("groupId") String groupId) {
        log.info("??????wageDetail????????????::[{}]", LocalDateTime.now());
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
                log.info("groupId:[{}]???idNumber:[{}]???wageSheetId:[{}]", groupId, idNumber, wageSheetId);
                log.info("??????payrollFeignController.wageDetail(payrollWageDetailReqDTO)????????????????????????:[{}]", LocalDateTime.now());
                List<PayrollWageDetailDTO> source = payrollFeignController.wageDetail(payrollWageDetailReqDTO);
                log.info("??????payrollFeignController.wageDetail(payrollWageDetailReqDTO)????????????????????????:[{}]", LocalDateTime.now());
                log.info("source.size():[{}]", source.size());
                if (null != source && source.size() > 0) {
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
                        wageDetailDTO.setSkinUrl(payrollWageDetailDTO.getSkinUrl());
                        wageDetailDTO.setReceiptStautus(payrollWageDetailDTO.getReceiptStautus());
                        wageDetailDTO.setDifferRealAmt(payrollWageDetailDTO.getDifferRealAmt());
                        wageDetailDTO.setPayStatus(payrollWageDetailDTO.getPayStatus());
                        wageDetailDTO.setSign(payrollWageDetailDTO.getSign());
                        list.add(wageDetailDTO);
                    }
                } else {
                    log.info("mongo.wageDetail??????????????????,idNumber:[{}]", idNumber);
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.info("??????mongo??????:[{}]", e);
            }

            //???????????????????????????????????????????????????entId
            if (StringUtils.isNotBlank(jsessionId)) {
                CacheUserPrincipal wechatInfoDetail = empWechatService.getWechatInfoDetail(jsessionId);
                WageSheetDTO wageSheet = wageSheetFeignController.findById(wageSheetId);
                empWechatService.upWechatInfoDetail(jsessionId, wageSheet.getEntId(), wechatInfoDetail);
            } else {
                log.info("wageDetail?????????????????????entId");
            }

            log.info("web.list:[{}]", JacksonUtil.objectToJson(list));
            return list;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * ??????????????????????????????
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

            Optional.ofNullable(req.getWageSheetId()).orElseThrow(() -> new ParamsIllegalException(chain.wage.core.constant.ErrorConstant.SYS_ERROR.format("????????????ID????????????")));
            Optional.ofNullable(req.getWageDetailId()).orElseThrow(() -> new ParamsIllegalException(chain.wage.core.constant.ErrorConstant.SYS_ERROR.format("???????????????ID????????????")));
            Optional.ofNullable(req.getSign()).orElseThrow(() -> new ParamsIllegalException(chain.wage.core.constant.ErrorConstant.SYS_ERROR.format("??????????????????")));

            log.info("=====> ?????????????????????????????? req:{}", JsonUtil.objectToJson(req));

            //????????????
            WageSheetDTO wageSheet = wageSheetFeignController.findById(req.getWageSheetId());
            if (null == wageSheet) {
                throw new ParamsIllegalException(ErrorConstant.Error0001.format("????????????"));
            }

            //????????????
            int year = wageSheet.getCrtDateTime().getYear();
            core.dto.response.wageDetail.WageDetailDTO wageDetail = wageDetailYearFeignController.findById(req.getWageDetailId(), year);
            if (null == wageDetail) {
                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("?????????????????????"));
            }

            String url = payrollProperties.getSignPdfPath() + DateTimeUtils.getDate() + "/";
            File file1 = new File(url);
            if (!file1.exists()) {//????????????????????????
                file1.mkdir();//???????????????
            }

            //??????????????????
            String imgStr = req.getSign().replace("data:image/png;base64,", "");
            String signImg = url + UUIDUtil.createUUID32() + ".jpg";
            boolean b = ImageBase64Utils.base64ToImageFile(imgStr, signImg);
            if (!b) {
                log.info("====> ???????????????????????????wageSheetId:{}, wageDetailId:{}, signImg:{}", wageSheet.getWageSheetId(), wageDetail.getDetailId(), signImg);
                throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("????????????????????????"));
            }

            //?????????PDF????????????
            String pdfUrl = url + wageSheet.getWageSheetId() + "/";
            File file2 = new File(pdfUrl);
            if (!file2.exists()) {//????????????????????????
                file2.mkdir();//???????????????
            }

            //??????PDF
            String pdfPath = createPDF(signImg, pdfUrl, wageSheet, wageDetail);

            //??????????????????
            boolean delete = new File(signImg).delete();
            if (!delete) {
                log.info("====> ???????????????????????????wageSheetId:{}, wageDetailId:{}, signImg:{}", wageSheet.getWageSheetId(), wageDetail.getDetailId(), signImg);
            }

            //????????????
            log.info("=====> ??????????????????{}", pdfPath);
            log.info("=====> ????????????????????????{}", payrollProperties.getSignReplacePath());
            String replace = pdfPath.replace(payrollProperties.getSignReplacePath(), "");
            log.info("=====> ??????????????????{}", replace);

            //????????????
            SignedReceiptSaveReq saveReq = SignedReceiptSaveReq.builder()
                    .crtDateTime(LocalDateTime.now())
                    .employeeSid(wageDetail.getEmployeeSid())
                    .entId(wageSheet.getEntId())
                    .groupId(wageSheet.getGroupId())
                    .idNumber(wageDetail.getIdNumber())
                    .receiptPath(replace)
//                    .signedReceiptId(UUIDUtil.createUUID32())
                    .signImg(req.getSign())
                    .updDateTime(LocalDateTime.now())
                    .wageDetailId(wageDetail.getDetailId())
                    .wageSheetId(wageSheet.getWageSheetId())
                    .build();
            signedReceiptFeignController.save(saveReq);

            //????????????
//            receipt(wageDetail.getId());
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * ??????????????????
     *
     * @param signUrl    ??????????????????
     * @param pdfPath    ?????????PDF??????????????????
     * @param wageSheet  ????????????
     * @param wageDetail ????????????
     * @return
     */
    public String createPDF(String signUrl, String pdfPath, WageSheetDTO wageSheet, core.dto.response.wageDetail.WageDetailDTO wageDetail) {

        //???????????????????????????????????????
        if (StringUtils.isBlank(wageSheet.getAccount()) || StringUtils.isBlank(wageSheet.getAccountName())
                || StringUtils.isBlank(wageSheet.getFundTypeDesc()) || StringUtils.isBlank(wageSheet.getGroupName())) {
            throw new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("??????????????????"));
        }

//        SignedReceiptDTO signedReceiptDTO = signedReceiptFeignController.findByWageDetailId(wageDetail.getId());
        if (wageDetail.getPayStatus() != chain.utils.fxgj.constant.DictEnums.PayStatusEnum.SUCCESS) {
            return null;
        }

        //??????????????????
        List<ContentDTO> contentDTOS = new ArrayList<>();
        Map<String, String> map = wageDetailYearFeignController.contentById(wageDetail.getDetailId(), wageSheet.getCrtDateTime().getYear());
        for (String key : map.keySet()
        ) {
            ContentDTO dto = ContentDTO.builder()
                    .name(key)
                    .value(map.get(key))
                    .build();
            contentDTOS.add(dto);
        }

        SignedReceiptPdfDTO dto = SignedReceiptPdfDTO.builder()
                .account(wageSheet.getAccount())
                .accountName(wageSheet.getAccountName())
                .applyDateTime(null == wageSheet.getApplyDateTime() ? null : wageSheet.getApplyDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .bankCard(wageDetail.getBankCard())
                .crDateTime(null == wageSheet.getCrtDateTime() ? null : wageSheet.getCrtDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .custName(wageDetail.getCustName())
                .fundDate(null == wageSheet.getFundDate() ? null : wageSheet.getFundDate().getDesc())
                .fundType(wageSheet.getFundTypeDesc())
                .groupName(wageSheet.getGroupName())
                .idNumber(wageDetail.getIdNumber())
                .remark(StringUtils.isBlank(wageDetail.getRemark4()) ? "" : wageDetail.getRemark4())
                .signUrl(signUrl)
                .wageName(wageSheet.getWageName())
                .detailId(wageDetail.getDetailId())
                .amt(null == wageDetail.getRealTotalAmt() ? BigDecimal.ZERO : wageDetail.getRealTotalAmt())
                .build();
        log.info("=====> PDF ???????????? pdfReq:{}", JacksonUtil.objectToJson(dto));
        log.info("=====> PDF ???????????? Content:{}", JacksonUtil.objectToJson(contentDTOS));
        String receiptPdf = creartSignedReceiptPdf(pdfPath, dto, contentDTOS);
        log.info("=====> ??????pdf?????????????????????????????? PATH:{}", receiptPdf);
        return receiptPdf;
    }

    /**
     * ??????PDF
     *
     * @param path    ?????????PDF??????????????????
     * @param dto     ??????????????????
     * @param content ????????????
     * @return
     */
    public String creartSignedReceiptPdf(String path, SignedReceiptPdfDTO dto, List<ContentDTO> content) {
        String detailId = dto.getDetailId();
        String bankCard = dto.getBankCard();
        String fundDate = dto.getFundDate();
        String fundType = dto.getFundType();
        String idNumber = dto.getIdNumber();
        String custName = dto.getCustName();
        String remark = dto.getRemark();
        String groupName = dto.getGroupName();
        String wageName = dto.getWageName();
        Long crDateTime = dto.getCrDateTime();
        String accountName = dto.getAccountName();
        Long applyDateTime = dto.getApplyDateTime();
        String account = dto.getAccount();
        String signUrl = dto.getSignUrl();
        BigDecimal amt = dto.getAmt();
        String successfullyReceived = "successfully_received.png";
        try {
            String title = null;

            //?????????pdf?????????
            String fileName = custName + idNumber + "-" + detailId;
            String filePathName = path + fileName + ".pdf";

            PDFUtil pdfUtil = new PDFUtil();
            Font chapterFont = PDFUtil.createCHineseFont(18, Font.BOLD, BaseColor.BLACK);//??????????????????
            Font sectionFont = PDFUtil.createCHineseFont(12, Font.BOLD, BaseColor.BLACK);//??????????????????
            Font apiFont = PDFUtil.createCHineseFont(8, Font.BOLD, new BaseColor(66, 66, 66));
            Font redapiFont = PDFUtil.createCHineseFont(8, Font.BOLD, BaseColor.RED);

            pdfUtil.createDocument(filePathName, null);

            Chapter chapter = PDFUtil.createChapter(title, 1, 1, 0, chapterFont);
            Section section1 = PDFUtil.createSection(chapter, "", sectionFont, 0);

            float[] widths = {0.20f, 0.28f, 0.20f, 0.28f};
            PdfPTable table1 = new PdfPTable(widths);
            pdfUtil.decorateTable(table1);

            PdfPCell pdfPCel = pdfUtil.createCell("?????????????????????", chapterFont);
            pdfPCel.setColspan(4);
            table1.addCell(pdfPCel);

            table1.addCell(pdfUtil.createHeaderCell("?????????", apiFont));
            table1.addCell(pdfUtil.createCell(custName, apiFont));
//        table1.addCell(pdfUtil.createHeaderCell("??????????????????", apiFont));
//        table1.addCell(pdfUtil.createCell(idNumber, apiFont));
            table1.addCell(pdfUtil.createHeaderCell("???????????????", apiFont));
            table1.addCell(pdfUtil.createCell(bankCard, apiFont));


            table1.addCell(pdfUtil.createHeaderCell("????????????", apiFont));
            table1.addCell(pdfUtil.createCell(groupName, apiFont));
            table1.addCell(pdfUtil.createHeaderCell("????????????", apiFont));
            table1.addCell(pdfUtil.createCell(wageName, apiFont));

            table1.addCell(pdfUtil.createHeaderCell("????????????", apiFont));
            table1.addCell(pdfUtil.createCell(fundType, apiFont));
            table1.addCell(pdfUtil.createHeaderCell("????????????", apiFont));
            table1.addCell(pdfUtil.createCell(fundDate, apiFont));

            table1.addCell(pdfUtil.createHeaderCell("????????????", apiFont));
            table1.addCell(pdfUtil.createCell(crDateTime + "", apiFont));
            table1.addCell(pdfUtil.createHeaderCell("???????????????", apiFont));
            table1.addCell(pdfUtil.createCell(account, apiFont));

            table1.addCell(pdfUtil.createHeaderCell("??????????????????", apiFont));
            table1.addCell(pdfUtil.createCell(accountName, apiFont));
            table1.addCell(pdfUtil.createHeaderCell("????????????", apiFont));
            LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(applyDateTime), ZoneId.systemDefault());
            table1.addCell(pdfUtil.createCell(DateTimeUtils.formatLocalDateTimeSss(dateTime), apiFont));

            table1.addCell(pdfUtil.createHeaderCell("????????????", apiFont));
            BigDecimal decimal = amt.setScale(2, BigDecimal.ROUND_HALF_UP);
            table1.addCell(pdfUtil.createCell(decimal + "", apiFont));
            table1.addCell(pdfUtil.createHeaderCell("????????????", apiFont));
            table1.addCell(pdfUtil.createCell("????????????", apiFont));

            table1.addCell(pdfUtil.createHeaderCell("??????", apiFont));
            PdfPCell pdfPCell = pdfUtil.createCell(remark, apiFont);
            pdfPCell.setColspan(3);
            table1.addCell(pdfPCell);

            table1.addCell(pdfUtil.createHeaderCell("?????????????????????", apiFont));
            Image image1 = Image.getInstance(signUrl);
            PdfPCell pdfCel1 = new PdfPCell(image1, false);
            pdfCel1.setColspan(2);
            pdfCel1.setFixedHeight(60);//?????????????????????
            pdfCel1.disableBorderSide(8);//???????????????
            table1.addCell(pdfCel1);
            Image image2 = Image.getInstance(PayRollController.class.getClassLoader().getResource(successfullyReceived).getPath());
            PdfPCell pdfCel2 = new PdfPCell(image2, false);
            pdfCel2.setFixedHeight(60);//?????????????????????
            pdfCel2.disableBorderSide(4);//???????????????
            table1.addCell(pdfCel2);
            section1.add(table1);

            PdfPTable table2 = new PdfPTable(widths);
            pdfUtil.decorateTable(table2);
            PdfPCell pdfPCe2 = pdfUtil.createCell("?????????????????????", chapterFont);
            pdfPCe2.setColspan(4);
            table2.addCell(pdfPCe2);
            if (null != content && content.size() > 0) {
                for (int i = 0; i < content.size(); i++) {
                    ContentDTO contentDTO = content.get(i);
                    table2.addCell(pdfUtil.createHeaderCell(contentDTO.getName(), apiFont));
                    //?????????????????????,???????????????3???
                    if (i == content.size() - 1 && content.size() / 2 > 0) {
                        PdfPCell cell = pdfUtil.createCell(contentDTO.getValue(), apiFont);
                        cell.setColspan(3);
                        table2.addCell(cell);
                        continue;
                    }
                    table2.addCell(pdfUtil.createCell(contentDTO.getValue(), apiFont));
                }
            }
            section1.add(table2);

            pdfUtil.writeChapterToDoc(chapter);
            pdfUtil.closeDocument();

            log.debug(filePathName);

            return filePathName;
        } catch (Exception e) {
            log.error("??????pdf????????????????????????????????????", e);
            throw new ParamsIllegalException(chain.wage.core.constant.ErrorConstant.WZWAGE_013.getErrorMsg());
        }
    }


    /**
     * ??????????????????bin??????
     *
     * @param cardNo
     * @return
     */
    @GetMapping("/checkCardBin/{cardNo}")
    @TrackLog
    @PermitAll
    public Mono<CheckCardBinRes> checkCardBin(@PathVariable("cardNo") String cardNo) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            Optional.ofNullable(cardNo).orElseThrow(() -> new ParamsIllegalException(ErrorConstant.SYS_ERROR.format("????????????????????????")));
            log.info("=====> /roll/checkCardBin ??????????????????bin?????? cardNo:{}", cardNo);

            CardbinQueRes cardbinQueRes = cardBinFeignService.cardBinByBankCard(cardNo);

            return CheckCardBinRes.builder()
                    .binNum(cardbinQueRes.getBinNum())
                    .cardName(cardbinQueRes.getCardName())
                    .cardNoLen(cardbinQueRes.getCardNoLen())
                    .issuerCode(cardbinQueRes.getIssuerCode())
                    .issuerFullName(cardbinQueRes.getIssuerFullName())
                    .issuerName(cardbinQueRes.getIssuerName())
                    .build();
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
