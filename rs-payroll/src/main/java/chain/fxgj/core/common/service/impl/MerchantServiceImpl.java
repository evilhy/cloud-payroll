package chain.fxgj.core.common.service.impl;

import chain.fxgj.core.common.constant.DictEnums.DelStatusEnum;
import chain.fxgj.core.common.constant.DictEnums.FundLiquidationEnum;
import chain.fxgj.core.common.service.EmployeeEncrytorService;
import chain.fxgj.core.common.service.MerchantService;
import chain.fxgj.core.common.service.PayRollAsyncService;
import chain.fxgj.core.jpa.dao.EmployeeInfoDao;
import chain.fxgj.core.jpa.dao.EmployeeWechatInfoDao;
import chain.fxgj.core.jpa.model.EmployeeInfo;
import chain.fxgj.core.jpa.model.EmployeeWechatInfo;
import chain.fxgj.core.jpa.model.QEmployeeInfo;
import chain.fxgj.core.jpa.model.QEmployeeWechatInfo;
import chain.fxgj.server.payroll.dto.ent.EntInfoDTO;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.utils.commons.StringUtils;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class MerchantServiceImpl implements MerchantService {

    @Autowired
    EmployeeWechatInfoDao employeeWechatInfoDao;
    @Autowired
    EmployeeInfoDao employeeInfoDao;
    @Autowired
    EmployeeEncrytorService employeeEncrytorService;
    @Autowired
    PayRollAsyncService payRollAsyncService;


    /**
     * 保存工资条绑定信息
     *
     * @param employeeWechatInfo 员工微信信息
     * @return
     */
    @Override
    public EmployeeWechatInfo saveMerchant(EmployeeWechatInfo employeeWechatInfo) {
        EmployeeWechatInfo employeeWechat = employeeWechatInfoDao.save(employeeWechatInfo);
        return employeeWechat;
    }


    /**
     * 查询员工信息
     *
     * @param employeeInfo 员工信息
     * @return
     */
    @Override
    public EmployeeInfo findEmployeeInfo(EmployeeInfo employeeInfo) {

        QEmployeeInfo qEmployeeInfo = QEmployeeInfo.employeeInfo;

        Predicate predicate = qEmployeeInfo.delStatusEnum.eq(DelStatusEnum.normal);
        if (employeeInfo.getIdNumber() != null) {
            predicate = ExpressionUtils.and(predicate, qEmployeeInfo.idNumber.eq(employeeInfo.getIdNumber()));
        }
        if (employeeInfo.getEmployeeName() != null) {
            predicate = ExpressionUtils.and(predicate, qEmployeeInfo.employeeName.eq(employeeInfo.getEmployeeName()));
        }

        EmployeeInfo employee = employeeInfoDao.selectFrom(qEmployeeInfo)
                .where(predicate)
                .fetchFirst();

        return employee;
    }

    /**
     * 工资条绑定信息
     *
     * @param employeeWechatInfo 员工微信信息
     * @return
     */
    @Override
    public EmployeeWechatInfo findMerchant(EmployeeWechatInfo employeeWechatInfo) {
        QEmployeeWechatInfo qEmployeeWechatInfo = QEmployeeWechatInfo.employeeWechatInfo;

        Predicate predicate = qEmployeeWechatInfo.delStatusEnum.eq(DelStatusEnum.normal);
        if (StringUtils.isNotBlank(employeeWechatInfo.getIdNumber())) {
            predicate = ExpressionUtils.and(predicate, qEmployeeWechatInfo.idNumber.eq(employeeWechatInfo.getIdNumber()));
        }
        if (employeeWechatInfo.getAppPartner() != null) {
            predicate = ExpressionUtils.and(predicate, qEmployeeWechatInfo.appPartner.eq(employeeWechatInfo.getAppPartner()));
        }

        EmployeeWechatInfo employeeWechat = employeeWechatInfoDao.selectFrom(qEmployeeWechatInfo)
                .where(predicate)
                .fetchFirst();

        return employeeWechat;
    }


    @Override
    public UserPrincipal setWechatInfo(String jsessionId, EmployeeWechatInfo employeeWechatInfo, List<FundLiquidationEnum> dataAuths) throws Exception {

        String idNumber = employeeWechatInfo.getIdNumber();

        UserPrincipal userPrincipal = UserPrincipal.builder()
                .sessionId(jsessionId)
                .sessionTimeOut(LocalDateTime.now().plusHours(8))
                .nickname(employeeWechatInfo.getNickname())
                .headimgurl(employeeWechatInfo.getHeadimgurl())
                .idNumber(employeeWechatInfo.getIdNumber())
                .appPartner(employeeWechatInfo.getAppPartner())
                .dataAuths(dataAuths)
                .build();

        EmployeeWechatInfo employeeWechat = this.findMerchant(employeeWechatInfo);

        if (employeeWechat != null) {
            log.info("员工信息不为空+++++++++++++++++++++");
            String idNumberEncrypt = employeeWechatInfo.getIdNumber();
            userPrincipal.setIdNumberEncrytor(idNumberEncrypt);
            idNumber = employeeEncrytorService.decryptIdNumber(idNumberEncrypt);
            userPrincipal.setIdNumber(idNumber);

            String phone = employeeEncrytorService.decryptPhone(employeeWechatInfo.getPhone());
            log.info("手机号phone:{}", phone);
            userPrincipal.setPhone(phone);
            userPrincipal.setWechatId(employeeWechatInfo.getId());
            userPrincipal.setQueryPwd(employeeWechatInfo.getQueryPwd());
            userPrincipal.setAppPartner(employeeWechat.getAppPartner());
            userPrincipal.setOpenId(employeeWechat.getOpenId());
            //修改绑定信息
            employeeWechat.setJsessionId(jsessionId);

            this.saveMerchant(employeeWechat);
        }

        //用户机构
        List<EntInfoDTO> entInfoDTOS = payRollAsyncService.getGroups(idNumber, userPrincipal).get();
        userPrincipal.setEntInfoDTOS(entInfoDTOS);
        if (entInfoDTOS != null && entInfoDTOS.size() > 0
                && entInfoDTOS.get(0).getGroupInfoList() != null && entInfoDTOS.get(0).getGroupInfoList().size() > 0) {
            userPrincipal.setName(entInfoDTOS.get(0).getGroupInfoList().get(0).getEmployeeInfoList().get(0).getEmployeeName());
            userPrincipal.setEntId(entInfoDTOS.get(0).getEntId());
            userPrincipal.setEntName(entInfoDTOS.get(0).getEntName());
        }
        return userPrincipal;
    }


}
