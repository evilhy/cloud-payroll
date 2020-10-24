package chain.fxgj.server.payroll.controller;

import chain.css.log.annotation.TrackLog;
import chain.fxgj.feign.client.CustManagerFeignService;
import chain.fxgj.server.payroll.dto.response.ManagerInfoDTO;
import chain.fxgj.server.payroll.dto.response.ManagerInformationDTO;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.fxgj.server.payroll.web.WebContext;
import chain.utils.commons.JacksonUtil;
import chain.wage.manager.core.dto.custmanager.WageManagerInfoDTO;
import chain.wage.manager.core.dto.dataquery.OpeningTipsDTO;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

/**
 * 客户经理
 */
@RestController
@Validated
@RequestMapping("/manager")
@Slf4j
public class CustManagerController {
    @Autowired
    CustManagerFeignService custManagerFeignService;

    /**
     * 查询客户经理信息
     *
     * @return
     */
    @GetMapping("/managerInfo")
    @TrackLog
    public Mono<ManagerInfoDTO> sendCode(@RequestHeader("ent-id") String entId) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        UserPrincipal currentUser = WebContext.getCurrentUser();
        String idNumber = currentUser.getIdNumber();
        String entIdUser = currentUser.getEntId();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            log.debug("==>身份证号:{}, entId:[{}]", idNumber, entIdUser);
            WageManagerInfoDTO wageManagerInfoDTO = custManagerFeignService.sendCode(idNumber, entIdUser);
            ManagerInfoDTO managerInfoDTO = new ManagerInfoDTO();
            if (null != wageManagerInfoDTO) {
                BeanUtils.copyProperties(wageManagerInfoDTO, managerInfoDTO);
            }
            return managerInfoDTO;
        }).subscribeOn(Schedulers.elastic());

    }

    /**
     * 通知企业分配客户经理
     */
    @PostMapping("/distribute")
    @TrackLog
    public Mono<Void> distribute() {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal currentUser = WebContext.getCurrentUser();
        String entId = currentUser.getEntId();
        log.info("entId:[{}]", entId);
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            custManagerFeignService.distribute(entId);
            return null;
        }).subscribeOn(Schedulers.elastic()).then();
    }

    /**
     *
     * 客户经理入口，弹窗提示
     *
     * 查询员工所属企业，
     * 1.是否有客户经理
     * 2.银行卡所属银行，本行、他行
     *
     * @return
     */
    @GetMapping("/openingTips")
    public OpeningTipsDTO openingTips(@RequestHeader("ent-id") String entId) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        UserPrincipal currentUser = WebContext.getCurrentUser();
        String idNumber = currentUser.getIdNumber();
//        String entId = currentUser.getEntId();
        log.info("openingTips.idNumber:[{}], entId:[{}]", idNumber, entId);
        OpeningTipsDTO openingTipsDTO = custManagerFeignService.openingTips(idNumber, entId);
        log.info("openingTips.openingTipsDTO:[{}]", JacksonUtil.objectToJson(openingTipsDTO));
        return openingTipsDTO;
    }


    /**
     * 根据managerId查询客户经理信息
     *
     * @return
     */
    @GetMapping("/qryManagerInfo")
    @TrackLog
    public Mono<ManagerInformationDTO> qryManagerInfo(@RequestParam("managerId") String managerId) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return Mono.fromCallable(() -> {
            MDC.setContextMap(mdcContext);
            log.debug("qryManagerInfo.managerId:[{}]", managerId);
            WageManagerInfoDTO wageManagerInfoDTO = custManagerFeignService.managerInfoById(managerId);
            ManagerInformationDTO managerInformationDTO = new ManagerInformationDTO();
            if (null != wageManagerInfoDTO) {
                BeanUtils.copyProperties(wageManagerInfoDTO, managerInformationDTO);
                managerInformationDTO.setManagerId(wageManagerInfoDTO.getId());
                managerInformationDTO.setManagerName(wageManagerInfoDTO.getManagerName());
                managerInformationDTO.setBranchName(wageManagerInfoDTO.getBranchOrgName());
                managerInformationDTO.setBranchNo(wageManagerInfoDTO.getBranchOrgNo());
                managerInformationDTO.setSubBranchName(wageManagerInfoDTO.getSubBranchOrgName());
                managerInformationDTO.setSubBranchNo(wageManagerInfoDTO.getSubBranchOrgNo());
                managerInformationDTO.setManagerPhone(wageManagerInfoDTO.getMobile());
            }
            log.info("qryManagerInfo.managerInfoDTO:[{}]", JacksonUtil.objectToJson(managerInformationDTO));
            return managerInformationDTO;
        }).subscribeOn(Schedulers.elastic());

    }
}
