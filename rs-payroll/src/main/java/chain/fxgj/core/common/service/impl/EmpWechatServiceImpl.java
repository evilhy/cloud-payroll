package chain.fxgj.core.common.service.impl;

import chain.css.exception.ParamsIllegalException;
import chain.fxgj.core.common.constant.DictEnums.AppPartnerEnum;
import chain.fxgj.core.common.constant.DictEnums.CardUpdStatusEnum;
import chain.fxgj.core.common.constant.DictEnums.DelStatusEnum;
import chain.fxgj.core.common.constant.DictEnums.FundLiquidationEnum;
import chain.fxgj.core.common.constant.ErrorConstant;
import chain.fxgj.core.common.service.EmpWechatService;
import chain.fxgj.core.common.service.EmployeeEncrytorService;
import chain.fxgj.core.common.service.InsideService;
import chain.fxgj.core.common.service.PayRollAsyncService;
import chain.fxgj.core.common.util.TransUtil;
import chain.fxgj.core.jpa.dao.*;
import chain.fxgj.core.jpa.model.*;
import chain.fxgj.server.payroll.config.properties.MerchantsProperties;
import chain.fxgj.server.payroll.dto.EmployeeDTO;
import chain.fxgj.server.payroll.dto.ent.EntInfoDTO;
import chain.fxgj.server.payroll.dto.request.UpdBankCardDTO;
import chain.fxgj.server.payroll.dto.request.WechatLoginDTO;
import chain.fxgj.server.payroll.service.EmployeeService;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.utils.commons.JacksonUtil;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.client.Client;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EmpWechatServiceImpl implements EmpWechatService {
    @Autowired
    EmployeeWechatInfoDao employeeWechatInfoDao;
    @Autowired
    EmployeeEncrytorService employeeEncrytorService;
    @Autowired
    EmployeeInfoDao employeeInfoDao;
    @Autowired
    EntErpriseInfoDao entErpriseInfoDao;
    @Autowired
    CardbinInfoDao cardbinInfoDao;
    @Autowired
    EmployeeCardInfoDao employeeCardInfoDao;
    @Autowired
    InsideService insideService;
    @Autowired
    Client wechatClient;
    @Autowired
    PayRollAsyncService payRollAsyncService;
    @Autowired
    EmployeeCardLogDao employeeCardLogDao;
    @Autowired
    EmployeeService employeeService;
    @Autowired
    MerchantsProperties merchantProperties;

    /**
     * 根据appid 查询工资条接入合作方信息
     *
     * @param appPartner 合作方id
     */
    private MerchantsProperties.Merchant getMerchant(AppPartnerEnum appPartner) {
        Optional<MerchantsProperties.Merchant> qWechat = merchantProperties.getMerchant().stream()
                .filter(item -> item.getMerchantCode().equals(appPartner)).findFirst();
        MerchantsProperties.Merchant merchant = qWechat.orElse(null);
        return merchant;
    }

    /**
     * 查询 用户绑定信息
     *
     * @param idNumber
     * @param appPartner
     * @return
     */
    @Override
    public EmployeeWechatInfo getEmployeeWechatInfo(String idNumber, AppPartnerEnum appPartner) {
        return this.getEmployeeWechatInfo(null, idNumber, appPartner);
    }


    /**
     * 查询 用户绑定信息
     *
     * @param openId
     * @param idNumber
     * @param appPartner
     * @return
     */
    @Override
    public EmployeeWechatInfo getEmployeeWechatInfo(String openId, String idNumber, AppPartnerEnum appPartner) {

        QEmployeeWechatInfo qEmployeeWechatInfo = QEmployeeWechatInfo.employeeWechatInfo;

        Predicate predicate = qEmployeeWechatInfo.delStatusEnum.eq(DelStatusEnum.normal);
        if (StringUtils.isNotEmpty(idNumber)) {
            //判断微信是否绑定
            idNumber = idNumber.toUpperCase();  //证件号码 转成大写
            String idNumberEncrytor = employeeEncrytorService.encryptIdNumber(idNumber);
            log.info("====>加密后的身份证：{}", idNumberEncrytor);
            predicate = ExpressionUtils.and(predicate, qEmployeeWechatInfo.idNumber.eq(idNumberEncrytor));
        }
        if (StringUtils.isNotEmpty(openId)) {
            predicate = ExpressionUtils.and(predicate, qEmployeeWechatInfo.openId.eq(openId));
        }
        if (appPartner != null) {
            predicate = ExpressionUtils.and(predicate, qEmployeeWechatInfo.appPartner.eq(appPartner));
        }

        EmployeeWechatInfo employeeWechatInfo = employeeWechatInfoDao.select(qEmployeeWechatInfo)
                .from(qEmployeeWechatInfo)
                .where(predicate)
                .fetchFirst();
        return employeeWechatInfo;
    }

    @Override
    public UserPrincipal getWechatInfo(String jsessionId) {
        return null;
    }

    @Override
    public UserPrincipal setWechatInfo(String jsessionId, String openId, String nickname, String headimgurl, String idNumber, AppPartnerEnum appPartner) throws Exception {
        UserPrincipal userPrincipal = UserPrincipal.builder()
                .sessionId(jsessionId)
                .openId(openId)
                .sessionTimeOut(LocalDateTime.now().plusHours(8))
                .nickname(nickname)
                .headimgurl(headimgurl)
                .idNumber(idNumber)
                .appPartner(appPartner)
                .build();

        //取可以访问的数据权限
        MerchantsProperties.Merchant merchant = this.getMerchant(appPartner);
        List<FundLiquidationEnum> dataAuths = merchant.getDataAuths();
        userPrincipal.setDataAuths(dataAuths);

        EmployeeWechatInfo employeeWechatInfo = this.getEmployeeWechatInfo(openId, null, appPartner);
        if (employeeWechatInfo != null) {
            log.info("=====>员工信息不为空,{}");
            String idNumberEncrypt = employeeWechatInfo.getIdNumber();
            userPrincipal.setIdNumberEncrytor(idNumberEncrypt);
            idNumber = employeeEncrytorService.decryptIdNumber(idNumberEncrypt);
            userPrincipal.setIdNumber(idNumber);

            String phone = employeeEncrytorService.decryptPhone(employeeWechatInfo.getPhone());
            log.info("====>手机号phone:{}", phone);
            userPrincipal.setPhone(phone);
            userPrincipal.setWechatId(employeeWechatInfo.getId());
            userPrincipal.setQueryPwd(employeeWechatInfo.getQueryPwd());
            if (employeeWechatInfo.getAppPartner() != null) {
                userPrincipal.setAppPartner(employeeWechatInfo.getAppPartner());
            }

            //修改绑定信息
            WechatLoginDTO wechatLoginDTO = new WechatLoginDTO();
            wechatLoginDTO.setOpenId(openId);
            wechatLoginDTO.setJsessionId(jsessionId);
            wechatLoginDTO.setNickname(nickname);
            wechatLoginDTO.setHeadimgurl(headimgurl);
            insideService.login(openId, jsessionId, nickname, headimgurl, employeeWechatInfo.getId());
        }

        //用户机构
        EmployeeInfo employeeInfo = employeeService.getEmployeeInfoOne(idNumber, dataAuths).get();
        if (employeeInfo != null) {
            userPrincipal.setName(employeeInfo.getEmployeeName());
            userPrincipal.setEntId(employeeInfo.getEntId());
        }

        return userPrincipal;
    }

    @Override
    public List<EmployeeDTO> getEmpList(String idNumber) {
        if (StringUtils.isEmpty(idNumber)) {
            throw new ParamsIllegalException(ErrorConstant.WECHAT_OUT.getErrorMsg());
        }

        List<EntInfoDTO> entInfoDTOS = new ArrayList<>();
        try {
            log.info("====>idNumber:[{}]", idNumber);
            entInfoDTOS = payRollAsyncService.getGroups(idNumber).get();
            log.info("====>go on,entInfoDTOS.size()[{}]", entInfoDTOS.size());
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.error("e.printStackTrace()->[{}]", e.getMessage());
        } catch (ExecutionException e) {
            e.printStackTrace();
            log.error("e.printStackTrace()-->[{}]", e.getMessage());
        }

        List<EmployeeDTO> list = new ArrayList<>();
        log.info("====>entInfoDTOS[{}]", JacksonUtil.objectToJson(entInfoDTOS));
        for (EntInfoDTO entInfoDTO : entInfoDTOS) {
            for (EntInfoDTO.GroupInfo groupInfo : entInfoDTO.getGroupInfoList()) {
                LinkedList<EntInfoDTO.GroupInfo.EmployeeInfo> empList = groupInfo.getEmployeeInfoList();
                for (int i = 0; i < empList.size(); i++) {
                    EntInfoDTO.GroupInfo.EmployeeInfo emp = empList.get(i);
                    //if (emp.getDelStatus() == DelStatusEnum.normal.getCode()) {
                    EmployeeDTO employeeDTO = new EmployeeDTO(emp);
                    employeeDTO.setGroupId(groupInfo.getGroupId());
                    employeeDTO.setGroupName(groupInfo.getGroupName());
                    employeeDTO.setGroupShortName(groupInfo.getGroupShortName());
                    employeeDTO.setEntId(entInfoDTO.getEntId());
                    employeeDTO.setEntName(entInfoDTO.getEntName());
                    employeeDTO.setIdNumberStar(TransUtil.idNumberStar(idNumber));
                    list.add(employeeDTO);
                    // }
                }
            }
        }
        log.info("====>list.size()[{}]", list.size());
        return list;
    }

    @Override
    public CardbinInfo checkCard(String idNumber, UpdBankCardDTO updBankCardDTO) {
        String cardNo = updBankCardDTO.getCardNo();
        List<String> ids = new ArrayList<>();
        List<String> groupIds = updBankCardDTO.getBankCardGroups()
                .stream()
                .map(tuple -> {
                    ids.add(tuple.getId());
                    return tuple.getGroupId();
                }).collect(Collectors.toList());
        List<EmployeeCardInfo> allById = employeeCardInfoDao.findAllById(ids);
        for (EmployeeCardInfo employeeCardInfo : allById) {
            if (cardNo.equals(employeeCardInfo.getCardNo())) {
                throw new ParamsIllegalException(ErrorConstant.WECHAR_012.getErrorMsg());
            }
        }


        //验证是否正在修改审核中
        QEmployeeCardLog qEmployeeCardLog = QEmployeeCardLog.employeeCardLog;
        long count = employeeCardLogDao.selectFrom(qEmployeeCardLog)
                .where(qEmployeeCardLog.cardNo.eq(cardNo)
                        .and(qEmployeeCardLog.groupId.in(groupIds))
                        .and(qEmployeeCardLog.updStatus.eq(CardUpdStatusEnum.UNKOWN)))
                .fetchCount();
        if (count > 0) {
            throw new ParamsIllegalException(ErrorConstant.WECHAR_011.getErrorMsg());
        }

        //验证是否已被使用
        List<EmployeeCardInfo> list = employeeCardInfoDao.findAllByCardNo(cardNo);
        for (EmployeeCardInfo employeeCardInfo : list) {
            if (employeeCardInfo.getDelStatusEnum().equals(DelStatusEnum.normal)) {
                if (!employeeCardInfo.getEmployeeInfo().getIdNumber().equals(idNumber) ||
                        groupIds.contains(employeeCardInfo.getEmployeeInfo().getGroupId())) {
                    throw new ParamsIllegalException(ErrorConstant.WECHAR_010.getErrorMsg());
                }
            }
        }
        //查询卡bin信息
        List<CardbinInfo> cardbinInfos = cardbinInfoDao.findFirstByCardNo(cardNo);
        if (cardbinInfos == null || cardbinInfos.size() <= 0) {
            throw new ParamsIllegalException(ErrorConstant.CARD_ERR.getErrorMsg());
        }

        return cardbinInfos.get(0);
    }

    @Override
    public String getWechatId(String openId) {
        QEmployeeWechatInfo qEmployeeWechatInfo = QEmployeeWechatInfo.employeeWechatInfo;
        return employeeWechatInfoDao.select(qEmployeeWechatInfo.id)
                .from(qEmployeeWechatInfo)
                .where(qEmployeeWechatInfo.openId.eq(openId))
                .fetchFirst();
    }

}
