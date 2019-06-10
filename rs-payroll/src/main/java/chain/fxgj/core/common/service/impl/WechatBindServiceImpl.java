package chain.fxgj.core.common.service.impl;

import chain.css.exception.ParamsIllegalException;
import chain.fxgj.core.common.constant.DictEnums.*;
import chain.fxgj.core.common.constant.ErrorConstant;
import chain.fxgj.core.common.constant.PermissionConstant;
import chain.fxgj.core.common.service.EmpWechatService;
import chain.fxgj.core.common.service.EmployeeEncrytorService;
import chain.fxgj.core.common.service.WechatBindService;
import chain.fxgj.core.common.util.TransUtil;
import chain.fxgj.core.jpa.dao.*;
import chain.fxgj.core.jpa.model.*;
import chain.fxgj.server.payroll.dto.EmployeeDTO;
import chain.fxgj.server.payroll.dto.ent.EntInfoDTO;
import chain.fxgj.server.payroll.dto.response.*;
import chain.utils.commons.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WechatBindServiceImpl implements WechatBindService {

    @Autowired
    EmployeeWechatInfoDao employeeWechatInfoDao;  //员工微信信息表
    @Autowired
    EmployeeInfoDao employeeInfoDao; //员工表
    @Autowired
    EmployeeCardInfoDao employeeCardInfoDao;//员工卡号表
    @Autowired
    EntErpriseInfoDao entErpriseInfoDao;//企业初始化信息表
    @Autowired
    EntGroupInfoDao entGroupInfoDao;//机构表
    @Autowired
    EntGroupInvoiceInfoDao entGroupInvoiceInfoDao; //机构发票表
    @Autowired
    UserInfoDao userInfoDao;

    @Autowired
    EmployeeEncrytorService employeeEncrytorService;
    @Autowired
    EmpWechatService empWechatService;
    @Autowired
    EmployeeCardLogDao employeeCardLogDao;
    @Autowired
    WechatBindService wechatBindService;


    private static ObjectMapper mapper = new ObjectMapper();

    /**
     * 获取员工企业
     *
     * @param idNumber 身份证（明文）
     * @return
     */
    @Override
    public Res100701 getEntList(String idNumber) {
        Res100701 res100701 = Res100701.builder().build();

        String bindStatus = res100701.getBindStatus();

        //判断微信是否绑定
        idNumber = idNumber.toUpperCase() ;  //证件号码 转成大写
        log.info("====>加密后的身份证：{}", employeeEncrytorService.encryptIdNumber(idNumber));
        log.info("====>idNumber：{}", idNumber);

        QEmployeeWechatInfo qEmployeeWechatInfo = QEmployeeWechatInfo.employeeWechatInfo;
        Predicate predicate = qEmployeeWechatInfo.idNumber.eq(idNumber);
        predicate = ExpressionUtils.and(predicate, qEmployeeWechatInfo.delStatusEnum.eq(DelStatusEnum.normal));
        predicate = ExpressionUtils.and(predicate, qEmployeeWechatInfo.appPartner.eq(AppPartnerEnum.FXGJ));
        EmployeeWechatInfo employeeWechatInfo = employeeWechatInfoDao.select(qEmployeeWechatInfo)
                .from(qEmployeeWechatInfo)
                .where(predicate)
                .fetchFirst();
        if (employeeWechatInfo != null) {
            bindStatus = "1";
        } else if (bindStatus.equals("0")) {
            res100701.setEmployeeList(this.getEntPhone(idNumber));
        }
        res100701.setBindStatus(bindStatus);
        return res100701;
    }

    @Override
    public List<EmployeeListBean> getEntPhone(String idNumber) {
        //查询员工信息
        QEmployeeInfo qEmployeeInfo = QEmployeeInfo.employeeInfo;
        QEntErpriseInfo qEntErpriseInfo = QEntErpriseInfo.entErpriseInfo;
        List<Tuple> tuples = employeeInfoDao.select(qEmployeeInfo.employeeName, qEmployeeInfo.idNumber, qEmployeeInfo.phone, qEntErpriseInfo.id, qEntErpriseInfo.entName)
                .from(qEmployeeInfo)
                .leftJoin(qEntErpriseInfo).on(qEntErpriseInfo.id.eq(qEmployeeInfo.entId))
                .where(qEmployeeInfo.idNumber.equalsIgnoreCase(idNumber).and(qEmployeeInfo.delStatusEnum.eq(DelStatusEnum.normal)))
                .groupBy(qEmployeeInfo.employeeName, qEmployeeInfo.idNumber, qEmployeeInfo.phone, qEntErpriseInfo.id, qEntErpriseInfo.entName)
                .fetch();

        List<EmployeeListBean> employeeList = new ArrayList<>();
        for (Tuple tuple : tuples) {
            EmployeeListBean bean = new EmployeeListBean();
            bean.setEmployeeName(tuple.get(qEmployeeInfo.employeeName));
            bean.setIdNumber(employeeEncrytorService.encryptIdNumber(tuple.get(qEmployeeInfo.idNumber)));
            String phone = tuple.get(qEmployeeInfo.phone);
            bean.setPhone(phone);
            bean.setPhoneStar(TransUtil.phoneStar(phone));
            bean.setSex(tuple.get(qEmployeeInfo.idNumber).length() != 18 ? "3" : tuple.get(qEmployeeInfo.idNumber).substring(17, 18));
            bean.setEntName(tuple.get(qEntErpriseInfo.entName));
            bean.setEntId(tuple.get(qEntErpriseInfo.id));

            employeeList.add(bean);
        }

        return employeeList;
    }

    /**
     * 员工个人信息
     *
     * @param idNumber
     * @return
     */
    @Override
    public List<Res100708> empList(String idNumber) {

        List<EmployeeDTO> employeeDTOList = empWechatService.getEmpList(idNumber);

        List<Res100708> employeeList = new ArrayList<>();
        for (EmployeeDTO employeeDTO : employeeDTOList) {
            Res100708 bean = new Res100708(employeeDTO);

            //员工银行卡
            QEmployeeCardInfo qEmployeeCardInfo = QEmployeeCardInfo.employeeCardInfo;
            List<EmployeeCardInfo> employeeCardInfos = employeeCardInfoDao.selectFrom(qEmployeeCardInfo)
                    .where(qEmployeeCardInfo.employeeInfo.id.eq(employeeDTO.getEmployeeId())
                            .and(qEmployeeCardInfo.delStatusEnum.eq(DelStatusEnum.normal))).fetch();

            List<Res100708.BankCardListBean> bankCardList = new ArrayList<>();
            for (EmployeeCardInfo employeeCardInfo : employeeCardInfos) {
                bankCardList.add(new Res100708.BankCardListBean(employeeCardInfo));
            }

            bean.setBankCardList(bankCardList);

            employeeList.add(bean);
        }

        return employeeList;
    }

    /**
     * 机构发票
     *
     * @param idNumber
     * @return
     */
    @Override
    public List<GroupInvoiceDTO> invoiceList(String idNumber) {
        //员工机构
        QEmployeeInfo qEmployeeInfo = QEmployeeInfo.employeeInfo;
        QEntGroupInfo qEntGroupInfo = QEntGroupInfo.entGroupInfo;
        List<String> tuples = employeeInfoDao.select(qEmployeeInfo.groupId).from(qEmployeeInfo)
                .leftJoin(qEntGroupInfo).on(qEntGroupInfo.id.eq(qEmployeeInfo.groupId))
                .where(qEmployeeInfo.idNumber.equalsIgnoreCase(idNumber)
                        .and(qEmployeeInfo.delStatusEnum.eq(DelStatusEnum.normal))
                        .and(qEntGroupInfo.delStatusEnum.eq(DelStatusEnum.normal))).fetch();

        //查询机构发票
        QEntGroupInvoiceInfo qEntGroupInvoiceInfo = QEntGroupInvoiceInfo.entGroupInvoiceInfo;
        List<EntGroupInvoiceInfo> entGroupInvoiceInfos = entGroupInvoiceInfoDao.selectFrom(qEntGroupInvoiceInfo)
                .where(qEntGroupInvoiceInfo.groupId.in(tuples)).fetch();

        List<GroupInvoiceDTO> list = new ArrayList<>();
        for (EntGroupInvoiceInfo entGroupInvoiceInfo : entGroupInvoiceInfos) {
            GroupInvoiceDTO groupInvoiceDTO = new GroupInvoiceDTO(entGroupInvoiceInfo);
            if (StringUtils.isBlank(entGroupInvoiceInfo.getGroupName()) || StringUtils.isBlank(entGroupInvoiceInfo.getGroupTaxNo())) {
                groupInvoiceDTO.setFlag("0");
            } else {
                groupInvoiceDTO.setFlag("1");
            }

            try {
                //普票二维码
                groupInvoiceDTO.setEntCommonQr(TransUtil.commonQr(entGroupInvoiceInfo.getGroupName(), entGroupInvoiceInfo.getGroupTaxNo()));
                //专票二维码
                groupInvoiceDTO.setEntQrPath(TransUtil.pathQr(entGroupInvoiceInfo.getGroupName(), entGroupInvoiceInfo.getGroupTaxNo(),
                        entGroupInvoiceInfo.getGroupAddress(), entGroupInvoiceInfo.getGroupPhone(), entGroupInvoiceInfo.getOpenBankName(), entGroupInvoiceInfo.getOpenBankAccount()));
            } catch (Exception e) {
                e.printStackTrace();
            }

            list.add(groupInvoiceDTO);
        }


        return list;
    }

    /**
     * 通过身份证 ，获取（正常）企业列表
     *
     * @param idNumber
     * @return
     */
    @Override
    public List<EntInfoDTO> getEntInfos(String idNumber) {
        return this.getEntInfos(idNumber, null);
    }


    /**
     * 通过身份证 ，获取企业列表
     *
     * @param idNumber
     * @param employeeStatusEnum
     * @return
     */
    @Override
    public List<EntInfoDTO> getEntInfos(String idNumber, EmployeeStatusEnum[] employeeStatusEnum) {
        log.info("====>根据身份证{}，查询 企业列表", idNumber);
        String alldata_json = null; //map转json

        //查询员工信息
        QEmployeeInfo qEmployeeInfo = QEmployeeInfo.employeeInfo;
        //查询条件
        //(1) 身份证
        Predicate predicate = qEmployeeInfo.idNumber.equalsIgnoreCase(idNumber);
        //(2) 正常
        //predicate = ExpressionUtils.and(predicate, qEmployeeInfo.delStatusEnum.eq(DelStatusEnum.normal));

        //(3)根据用户状态信息取，用户信息
        if (employeeStatusEnum != null && employeeStatusEnum.length > 0) {
            if (employeeStatusEnum.length == 1) {
                predicate = ExpressionUtils.and(predicate, qEmployeeInfo.employeeStatusEnum.eq(employeeStatusEnum[0]));
            } else {
                predicate = ExpressionUtils.and(predicate, qEmployeeInfo.employeeStatusEnum.in(employeeStatusEnum));
            }
        }

        //创建日期升序
        OrderSpecifier orderSpecifier = qEmployeeInfo.crtDateTime.asc();
        log.info("====>员工信息查询start");
        List<EmployeeInfo> employeeInfoList = employeeInfoDao.selectFrom(qEmployeeInfo).where(predicate).orderBy(orderSpecifier).fetch();
        log.info("====>员工信息查询end");

        //员工信息 以 groupid 为key 存储 用户信息
        HashMap<String, List<EmployeeInfo>> convertGroupEmp = new HashMap<>();
        //相同entid,所有机构信息 放在一起
        LinkedHashMap<String, LinkedHashMap<String, List<EmployeeInfo>>> convertEntGroup = new LinkedHashMap<>();

        for (EmployeeInfo employeeInfo : employeeInfoList) {
            String entId = employeeInfo.getEntId();
            String groupId = employeeInfo.getGroupId();
            log.info("====>entId={},groupId={},employeeInfo={}", entId, groupId, employeeInfo.getId());

            //【1】机构id
            if (convertGroupEmp.containsKey(groupId)) {
                convertGroupEmp.get(groupId).add(employeeInfo);
            } else {
                LinkedList<EmployeeInfo> list = new LinkedList<>();
                list.add(employeeInfo);
                convertGroupEmp.put(groupId, list);
            }

            //【2】企业id
            if (convertEntGroup.containsKey(entId)) {
                LinkedHashMap<String, List<EmployeeInfo>> groupMap = convertEntGroup.get(entId);

                if (groupMap.get(groupId) == null) {
                    List<EmployeeInfo> list = new LinkedList<>();
                    list.add(employeeInfo);
                    groupMap.put(groupId, list);

                } else {
                    groupMap.get(groupId).add(employeeInfo);
                }

                convertEntGroup.put(entId, groupMap);

            } else {
                LinkedHashMap<String, List<EmployeeInfo>> firstGroupMap = new LinkedHashMap<>();
                firstGroupMap.put(groupId, convertGroupEmp.get(groupId));
                convertEntGroup.put(entId, firstGroupMap);
            }
        }

        log.info("根据身份证，查询 【员工】 数量：{}", employeeInfoList.size());
        log.info("根据身份证，查询 【机构】 数量：{}", convertGroupEmp.size());

        log.info("循环匹配");

        List<EntInfoDTO> entInfoDTOList = new LinkedList<>();

        for (Map.Entry<String, LinkedHashMap<String, List<EmployeeInfo>>> entryEnt : convertEntGroup.entrySet()) {
            //企业id
            String entId = entryEnt.getKey();
            //机构 -- 员工列表数据
            LinkedHashMap<String, List<EmployeeInfo>> groupMap = entryEnt.getValue();

            //查询企业信息
            EntErpriseInfo entErpriseInfo = entErpriseInfoDao.findById(entId).get();

            EntInfoDTO entInfoDTO = EntInfoDTO.builder()
                    .entId(entId)
                    .entName(entErpriseInfo.getEntName())
                    .shortEntName(entErpriseInfo.getShortEntName())
                    .build();

            LinkedList<EntInfoDTO.GroupInfo> groupInfoList = entInfoDTO.getGroupInfoList();

            for (Map.Entry<String, List<EmployeeInfo>> groupEntry : groupMap.entrySet()) {
                String groupId = groupEntry.getKey();

                EntGroupInfo entGroupInfo = entGroupInfoDao.findById(groupId).orElse(null);
                if (entGroupInfo != null) {
                    EntInfoDTO.GroupInfo groupInfo = EntInfoDTO.GroupInfo.builder()
                            .groupId(entGroupInfo.getId())
                            .groupName(entGroupInfo.getGroupName())
                            .groupShortName(entGroupInfo.getShortGroupName())
                            .build();

                    LinkedList<EntInfoDTO.GroupInfo.EmployeeInfo> empList = new LinkedList<>();
                    groupInfo.setEmployeeInfoList(empList);
                    List<EmployeeInfo> listEmp = groupEntry.getValue();
                    for (int i = 0; i < listEmp.size(); i++) {
                        EmployeeInfo iemployeeInfo = listEmp.get(i);
                        EntInfoDTO.GroupInfo.EmployeeInfo employeeInfo = new EntInfoDTO.GroupInfo.EmployeeInfo();
                        //员工id
                        employeeInfo.setEmployeeId(iemployeeInfo.getId());
                        //姓名
                        employeeInfo.setEmployeeName(iemployeeInfo.getEmployeeName());
                        Integer code = iemployeeInfo.getEmployeeStatusEnum().getCode();
                        //在职状态
                        employeeInfo.setEmployeeStatus(code);
                        //在职描述
                        employeeInfo.setEmployeeStatusDesc(iemployeeInfo.getEmployeeStatusEnum().getDesc());
                        //手机号
                        employeeInfo.setPhone(iemployeeInfo.getPhone());
                        //员工工号
                        employeeInfo.setEmployeeNo(iemployeeInfo.getEmployeeNo());
                        //职位
                        employeeInfo.setPosition(iemployeeInfo.getPosition());
                        //入职时间
                        employeeInfo.setEntryDate(iemployeeInfo.getEntryDate() == null ? null : iemployeeInfo.getEntryDate().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                        Integer delCode = iemployeeInfo.getDelStatusEnum().getCode();
                        //员工状态
                        employeeInfo.setDelStatus(delCode);
                        //员工状态描述
                        employeeInfo.setDelStatusDesc(iemployeeInfo.getDelStatusEnum().getDesc());
                        empList.add(employeeInfo);
                    }
                    groupInfoList.add(groupInfo);
                }
            }
            entInfoDTO.setGroupInfoList(groupInfoList);
            entInfoDTOList.add(entInfoDTO);
        }

        return entInfoDTOList;
    }


    @Override
    public int checkCardNo(String idNumber, String cardNo) {
        int isstatus = IsStatusEnum.NO.getCode();
        idNumber = employeeEncrytorService.decryptIdNumber(idNumber);
        List<EmployeeInfo> employeeInfos = employeeInfoDao.findByIdNumberAndAndDelStatusEnum(idNumber, DelStatusEnum.normal);

        if (employeeInfos.size() <= 0) {
            throw new ParamsIllegalException(ErrorConstant.WECHAR_001.getErrorMsg());
        }

        for (EmployeeInfo employeeInfo : employeeInfos) {
            List<EmployeeCardInfo> employeeCardInfos = employeeInfo.getEmpCardList();
            for (EmployeeCardInfo employeeCardInfo : employeeCardInfos) {
                if (employeeCardInfo.getDelStatusEnum().equals(DelStatusEnum.normal) &&
                        employeeCardInfo.getCardNo().substring(employeeCardInfo.getCardNo().length() - 6, employeeCardInfo.getCardNo().length()).equals(cardNo)) {
                    isstatus = IsStatusEnum.YES.getCode();
                    break;
                }
            }
        }

        return isstatus;
    }

    @Override
    public List<EmpEntDTO> empEntList(String idNumber) {

        List<EntInfoDTO> entInfoDTOS = wechatBindService.getEntInfos(idNumber);
        List<EmpEntDTO> list = new ArrayList<>();
        for (EntInfoDTO entInfoDTO : entInfoDTOS) {
            EmpEntDTO empEntDTO = new EmpEntDTO();
            empEntDTO.setEntName(entInfoDTO.getEntName());
            empEntDTO.setShortEntName(entInfoDTO.getShortEntName());
            List<Res100708> items = new ArrayList<>();
            List<BankCard> bankCards = new ArrayList<>();
            for (EntInfoDTO.GroupInfo groupInfo : entInfoDTO.getGroupInfoList()) {

                LinkedList<EntInfoDTO.GroupInfo.EmployeeInfo> empList = groupInfo.getEmployeeInfoList();

                for (int i = 0; i < empList.size(); i++) {
                    EntInfoDTO.GroupInfo.EmployeeInfo emp = empList.get(i);

                    if (emp.getDelStatus() == DelStatusEnum.normal.getCode()) {
                        EmployeeDTO employeeDTO = new EmployeeDTO(emp);
                        employeeDTO.setGroupId(groupInfo.getGroupId());
                        employeeDTO.setGroupName(groupInfo.getGroupName());
                        employeeDTO.setGroupShortName(groupInfo.getGroupShortName());
                        employeeDTO.setEntId(entInfoDTO.getEntId());
                        employeeDTO.setEntName(entInfoDTO.getEntName());
                        employeeDTO.setIdNumberStar(idNumber);

                        Res100708 bean = new Res100708(employeeDTO);
                        //员工银行卡
                        QEmployeeCardInfo qEmployeeCardInfo = QEmployeeCardInfo.employeeCardInfo;
                        List<EmployeeCardInfo> employeeCardInfos = employeeCardInfoDao.selectFrom(qEmployeeCardInfo)
                                .where(qEmployeeCardInfo.employeeInfo.id.eq(employeeDTO.getEmployeeId())
                                        .and(qEmployeeCardInfo.delStatusEnum.eq(DelStatusEnum.normal))).fetch();

                        List<Res100708.BankCardListBean> bankCardList = new ArrayList<>();
                        for (EmployeeCardInfo employeeCardInfo : employeeCardInfos) {
                            //添加银行卡修改信息
                            EmployeeCardLog employeeCardLog = null;
                            List<EmployeeCardLog> employeeCardLogs = getEmployeeCardLogs(employeeCardInfo.getId());
                            if (employeeCardLogs.size() > 0) {
                                employeeCardLog = employeeCardLogs.get(0);
                            }

                            bankCardList.add(new Res100708.BankCardListBean(employeeCardInfo));

                            boolean hasCard = false;
                            BankCard card = new BankCard();
                            List<BankCardGroup> bankCardGroups = new ArrayList<>();
                            for (BankCard bankCard : bankCards) {
                                if (employeeCardInfo.getCardNo().equals(bankCard.getOldCardNo())) {
                                    hasCard = true;
                                    card = bankCard;
                                    bankCardGroups = bankCard.getBankCardGroups();
                                    break;
                                }
                            }
                            if (!hasCard) {
                                card.setOldCardNo(employeeCardInfo.getCardNo());
                                card.setCardNo(employeeCardInfo.getCardNo());
                                card.setIssuerName(employeeCardInfo.getIssuerName());
                                if (employeeCardLog != null) {
                                    card.setCardUpdStatus(employeeCardLog.getUpdStatus().getCode());
                                    card.setCardUpdStatusVal(employeeCardLog.getUpdStatus().getDesc());
                                    card.setUpdDesc(employeeCardLog.getUpdDesc());
                                    card.setIsNew(employeeCardLog.getIsNew().getCode());
                                    if (CardUpdStatusEnum.UNKOWN.equals(employeeCardLog.getUpdStatus())) {
                                        card.setCardNo(employeeCardLog.getCardNo());
                                        card.setIssuerName(employeeCardLog.getIssuerName());
                                    }
                                }
                            }

                            BankCardGroup bankCardGroup = new BankCardGroup();
                            bankCardGroup.setId(employeeCardInfo.getId());
                            bankCardGroup.setGroupId(groupInfo.getGroupId());
                            bankCardGroup.setShortGroupName(groupInfo.getGroupShortName());
                            bankCardGroups.add(bankCardGroup);

                            card.setBankCardGroups(bankCardGroups);

                            if (!hasCard) {
                                bankCards.add(card);
                            }
                        }

                        bean.setBankCardList(bankCardList);

                        items.add(bean);
                    }
                }


            }
            empEntDTO.setItems(items);
            empEntDTO.setCards(bankCards);
            list.add(empEntDTO);
        }

        return list;
    }

    @Override
    public List<EmpCardLogDTO> empCardLog(String[] ids) {
        List<EmpCardLogDTO> list = new ArrayList<>();

        List<EmployeeCardInfo> employeeCardInfos = employeeCardInfoDao.findByIdIn(ids);
        for (EmployeeCardInfo employeeCardInfo : employeeCardInfos) {
            List<EmployeeCardLog> employeeCardLogs = getEmployeeCardLogs(employeeCardInfo.getId());
            String groupName = "";
            if (employeeCardLogs != null && employeeCardLogs.size() > 0) {
                EntGroupInfo entGroupInfo = entGroupInfoDao.findById(employeeCardLogs.get(0).getGroupId())
                        .orElseThrow(() -> new ParamsIllegalException(ErrorConstant.Error0001.format("机构")));
                groupName = entGroupInfo.getShortGroupName();
            }

            for (EmployeeCardLog employeeCardLog : employeeCardLogs) {
                EmpCardLogDTO empCardLogDTO = new EmpCardLogDTO();
                empCardLogDTO.setCardNo(employeeCardLog.getCardNo());
                empCardLogDTO.setIssuerName(employeeCardLog.getIssuerName());
                empCardLogDTO.setCardNoOld(employeeCardLog.getCardNoOld());
                empCardLogDTO.setIssuerNameOld(employeeCardLog.getIssuerNameOld());
                empCardLogDTO.setCrtDateTime(employeeCardLog.getCrtDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                empCardLogDTO.setShortGroupName(groupName);
                empCardLogDTO.setUpdStatus(employeeCardLog.getUpdStatus().getCode());
                empCardLogDTO.setUpdStatusVal(employeeCardLog.getUpdStatus().getDesc());
                empCardLogDTO.setUpdDesc(employeeCardLog.getUpdDesc());
                empCardLogDTO.setUpdDateTime(employeeCardLog.getUpdDateTime() == null ? null : employeeCardLog.getUpdDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                if (IsStatusEnum.YES.equals(employeeCardLog.getIsNew())) {
                    empCardLogDTO.setLogId(employeeCardLog.getId());
                }
                employeeCardLog.setIsNew(IsStatusEnum.NO);
                list.add(empCardLogDTO);
            }
        }

//        Collections.sort(list, new Comparator<EmpCardLogDTO>() {
//            @Override
//            public int compare(EmpCardLogDTO o1, EmpCardLogDTO o2) {
//                return o1.getCrtDateTime().compareTo(o2.getCrtDateTime());
//            }
//        });
        List<EmpCardLogDTO> collect = list.stream().sorted(Comparator.comparing(EmpCardLogDTO::getCrtDateTime).reversed()).collect(Collectors.toList());

        return collect;
    }

    @Override
    public List<EntUserDTO> entUser(String entId) {

        //企业超管
        List<EntUserDTO> userInfos = new ArrayList<>();
        QUserInfo qUserInfo = QUserInfo.userInfo;
        List<UserInfo> userInfoList = userInfoDao.selectFrom(qUserInfo)
                .where(qUserInfo.entId.eq(entId).and(qUserInfo.delStatusEnum.eq(DelStatusEnum.normal))).fetch();
        for (UserInfo userInfo : userInfoList) {
            boolean has = false;
            Collection<SystemAuthorityInfo> userAuthority = userInfo.getUserAuthority();
            for (SystemAuthorityInfo systemAuthorityInfo : userAuthority) {
                if (systemAuthorityInfo.getId().equals(PermissionConstant.UPD_INFO)) {
                    has = true;
                    break;
                }
            }
            if (has) {
                EntUserDTO user = new EntUserDTO();
                user.setName(userInfo.getUserName());
                user.setPhone(userInfo.getPhone());
                user.setPosition(userInfo.getPosition().getDesc());

                userInfos.add(user);
            }
        }
        return userInfos;
    }

    @Override
    public void checkPhone(String idNumber, String phone) {
        QEmployeeInfo qEmployeeInfo = QEmployeeInfo.employeeInfo;

        List<EmployeeInfo> list = employeeInfoDao.selectFrom(qEmployeeInfo)
                .where(qEmployeeInfo.phone.eq(phone).and(qEmployeeInfo.idNumber.notEqualsIgnoreCase(idNumber))
                        .and(qEmployeeInfo.delStatusEnum.eq(DelStatusEnum.normal))).fetch();

        if (list.size() > 0) {
            throw new ParamsIllegalException(ErrorConstant.WECHAR_009.getErrorMsg());
        }
    }

    @Override
    public Integer getCardUpdIsNew(String idNumber) {
        Integer isNew = 0;
        QEmployeeInfo qEmployeeInfo = QEmployeeInfo.employeeInfo;

        List<EmployeeInfo> list = employeeInfoDao.selectFrom(qEmployeeInfo)
                .where(qEmployeeInfo.idNumber.equalsIgnoreCase(idNumber)
                        .and(qEmployeeInfo.delStatusEnum.eq(DelStatusEnum.normal)
                                .and(qEmployeeInfo.empCardList.any().delStatusEnum.eq(DelStatusEnum.normal)))).fetch();
        for (EmployeeInfo employeeInfo : list) {
            for (EmployeeCardInfo employeeCardInfo : employeeInfo.getNormalEmpCardList()) {
                List<EmployeeCardLog> employeeCardLogs = getEmployeeCardLogs(employeeCardInfo.getId());
                for (EmployeeCardLog employeeCardLog : employeeCardLogs) {
                    if (IsStatusEnum.YES.equals(employeeCardLog.getIsNew())) {
                        isNew = 1;
                        break;
                    }
                }
            }
        }
        return isNew;
    }

    @Override
    public String getQueryPwd(String openId) {
        QEmployeeWechatInfo qEmployeeWechatInfo = QEmployeeWechatInfo.employeeWechatInfo;
        String queryPwd = employeeWechatInfoDao.select(qEmployeeWechatInfo.queryPwd)
                .from(qEmployeeWechatInfo)
                .where(qEmployeeWechatInfo.openId.eq(openId))
                .orderBy(qEmployeeWechatInfo.crtDateTime.desc())
                .fetchFirst();
        return queryPwd;
    }

    @Override
    public String getQueryPwdById(String id) {
        QEmployeeWechatInfo qEmployeeWechatInfo = QEmployeeWechatInfo.employeeWechatInfo;
        String queryPwd = employeeWechatInfoDao.select(qEmployeeWechatInfo.queryPwd)
                .from(qEmployeeWechatInfo)
                .where(qEmployeeWechatInfo.id.eq(id))
                .orderBy(qEmployeeWechatInfo.crtDateTime.desc())
                .fetchFirst();
        return queryPwd;
    }

    @Override
    public List<EmployeeCardLog> getEmployeeCardLogs(String bankCardId) {
        //添加银行卡修改信息
        QEmployeeCardLog qEmployeeCardLog = QEmployeeCardLog.employeeCardLog;
        return employeeCardLogDao.selectFrom(qEmployeeCardLog)
                .where(qEmployeeCardLog.bankCardId.eq(bankCardId)
                        .and(qEmployeeCardLog.delStatus.eq(DelStatusEnum.normal)))
                .orderBy(qEmployeeCardLog.crtDateTime.desc())
                .fetch();
    }
}
