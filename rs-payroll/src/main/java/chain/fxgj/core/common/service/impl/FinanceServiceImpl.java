package chain.fxgj.core.common.service.impl;

import chain.css.exception.ParamsIllegalException;
import chain.fxgj.core.common.constant.DictEnums.*;
import chain.fxgj.core.common.constant.ErrorConstant;
import chain.fxgj.core.common.service.EmployeeEncrytorService;
import chain.fxgj.core.common.service.FinanceService;
import chain.fxgj.core.jpa.dao.*;
import chain.fxgj.core.jpa.model.*;
import chain.fxgj.server.payroll.dto.tfinance.*;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class FinanceServiceImpl implements FinanceService {
    @Autowired
    BankProductInfoDao bankProductInfoDao;
    @Autowired
    BankProductIntentionDao bankProductIntentionDao;
    @Autowired
    BankProductOperateDao bankProductOperateDao;
    @Autowired
    EmployeeWechatInfoDao employeeWechatInfoDao;
    @Autowired
    EmployeeInfoDao employeeInfoDao;
    @Autowired
    EntErpriseInfoDao entErpriseInfoDao;
    @Autowired
    ManagerInfoDao managerInfoDao;
    @Autowired
    WechatFollowInfoDao wechatFollowInfoDao;
    @Autowired
    EmployeeEncrytorService employeeEncrytorService;


    @Override
    public ProductInfoDTO getProductInfo(String productId, String imgUrl) {
        BankProductInfo bankProductInfo = bankProductInfoDao.findById(productId)
                .orElseThrow(() -> new ParamsIllegalException(ErrorConstant.Error0001.format("理财产品")));

        ProductInfoDTO productInfoDTO = new ProductInfoDTO(bankProductInfo, imgUrl);

        List<ProductInfoDTO.ProductMarkDTO> markList = new ArrayList<>();
        for (BankProductMark productMark : bankProductInfo.getProductMarks()) {
            markList.add(new ProductInfoDTO.ProductMarkDTO(productMark, bankProductInfo.getProductTerm()));
        }
        productInfoDTO.setMarkList(markList);

        return productInfoDTO;
    }

    @Override
    public String getIdNumberIntent(String productId, String idNumber) {
        QBankProductIntention qBankProductIntention = QBankProductIntention.bankProductIntention;
        String entId = bankProductIntentionDao.select(qBankProductIntention.entId).from(qBankProductIntention)
                .where(qBankProductIntention.productId.eq(productId).and(qBankProductIntention.idNumber.eq(idNumber))).fetchFirst();
        return entId;
    }

    @Override
    public Long getIntentNum(String productId, String entId) {
        QBankProductIntention qBankProductIntention = QBankProductIntention.bankProductIntention;
        Long num = bankProductIntentionDao.selectFrom(qBankProductIntention)
                .where(qBankProductIntention.productId.eq(productId).and(qBankProductIntention.entId.eq(entId))).fetchCount();
        return num;
    }

    @Override
    public IntentInfoDTO getIntetByIdNumber(String productId, String idNumber) {
        QBankProductIntention qBankProductIntention = QBankProductIntention.bankProductIntention;
        QManagerInfo qManagerInfo = QManagerInfo.managerInfo;

        Tuple tuple = bankProductIntentionDao.select(qBankProductIntention, qManagerInfo.id, qManagerInfo.managerName, qManagerInfo.phone)
                .from(qBankProductIntention).leftJoin(qManagerInfo).on(qBankProductIntention.custManagerId.eq(qManagerInfo.id))
                .where(qBankProductIntention.productId.eq(productId).and(qBankProductIntention.idNumber.eq(idNumber))).fetchFirst();
        if (tuple != null) {
            BankProductIntention bankProductIntention = tuple.get(qBankProductIntention);
            IntentInfoDTO intentInfoDTO = new IntentInfoDTO(bankProductIntention);
            intentInfoDTO.setManagerName(tuple.get(qManagerInfo.managerName));
            intentInfoDTO.setManagerPhone(tuple.get(qManagerInfo.phone));

            //查询最开始预约产品的用户
            List<IntentInfoDTO.WechatUser> list = new ArrayList<>();
            List<String> idNumbers = bankProductOperateDao.select(qBankProductIntention.idNumber).from(qBankProductIntention)
                    .where(qBankProductIntention.productId.eq(productId).and(qBankProductIntention.entId.eq(bankProductIntention.getEntId()))
                            .and(qBankProductIntention.intentStatus.eq(bankProductIntention.getIntentStatus())))
                    .limit(3).orderBy(qBankProductIntention.crtDateTime.asc()).fetch();
            for (String number : idNumbers) {
                IntentInfoDTO.WechatUser wechatUser = new IntentInfoDTO.WechatUser();
                //获取微信头像
                EmployeeWechatInfo employeeWechatInfo = employeeWechatInfoDao.findFirstByIdNumberAndAndDelStatusEnum(employeeEncrytorService.encryptIdNumber(number), DelStatusEnum.normal);
                if (employeeWechatInfo != null) {
                    wechatUser.setNickname(StringUtils.isEmpty(employeeWechatInfo.getNickname()) ? "????" : employeeWechatInfo.getNickname());
                    wechatUser.setHeadimgurl(StringUtils.isEmpty(employeeWechatInfo.getHeadimgurl()) ? null : employeeWechatInfo.getHeadimgurl());
                }
                list.add(wechatUser);
            }
            intentInfoDTO.setList(list);

            return intentInfoDTO;
        }

        return null;
    }

    @Override
    public IntentListDTO getIntentList(String productId) {
        IntentListDTO intentListDTO = new IntentListDTO();
        BankProductInfo bankProductInfo = bankProductInfoDao.findById(productId)
                .orElseThrow(() -> new ParamsIllegalException(ErrorConstant.Error0001.format("理财产品")));
        //平台预约数
        QBankProductIntention qBankProductIntention = QBankProductIntention.bankProductIntention;
        Long num = bankProductIntentionDao.selectFrom(qBankProductIntention)
                .where(qBankProductIntention.productId.eq(productId)).fetchCount();
        intentListDTO.setIntentNum(Math.toIntExact(num));

        //最新10条预约信息
        List<BankProductIntention> bankProductIntentions = bankProductIntentionDao.selectFrom(qBankProductIntention)
                .where(qBankProductIntention.productId.eq(productId)).orderBy(qBankProductIntention.crtDateTime.desc())
                .limit(15).fetch();

        List<IntentListDTO.IntentRealDTO> list = new ArrayList<>();
        for (BankProductIntention bankProductIntention : bankProductIntentions) {
            IntentListDTO.IntentRealDTO dto = new IntentListDTO.IntentRealDTO();
            String clientName = StringUtils.trimToEmpty(bankProductIntention.getClientName());
            if (clientName.length() >= 1) {
                clientName = bankProductIntention.getClientName().substring(0, 1) + "**";
            } else {
                clientName = "**";
            }
            dto.setClientName(clientName);
            dto.setIntentAmount(bankProductIntention.getIntentAmount());
            dto.setCrtDateTime(bankProductIntention.getCrtDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());

            //企业预约人数
            Long eNum = this.getIntentNum(productId, bankProductIntention.getEntId());
            //当前企业收益
            BankProductMark mark = new BankProductMark();
            for (BankProductMark productMark : bankProductInfo.getProductMarks()) {
                if (productMark.getMarkLevel() == 1) {
                    mark = productMark;
                }
                if (eNum >= productMark.getMinPeople() && eNum <= productMark.getMaxPeople()) {
                    mark = productMark;
                    break;
                }
            }

            //计算收益=金额*利率(0.05)*天数/365
            double amt = bankProductIntention.getIntentAmount().doubleValue() * mark.getLevelRate() * 0.01 * bankProductInfo.getProductTerm() / 365;
            dto.setProfit(new BigDecimal(amt).setScale(2, BigDecimal.ROUND_HALF_UP));

            list.add(dto);
        }
        intentListDTO.setSize(15);
        intentListDTO.setList(list);
        return intentListDTO;
    }

    @Override
    public Page<OperateDTO> getOperate(String productId, String entId, Integer operate, Pageable page, String openId) {

        List<OperateDTO> list = new ArrayList<OperateDTO>();
        long size = 0;
        QBankProductOperate qBankProductOperate = QBankProductOperate.bankProductOperate;
        QEmployeeWechatInfo qEmployeeWechatInfo = QEmployeeWechatInfo.employeeWechatInfo;

        BooleanExpression booleanExpression = qBankProductOperate.productId.eq(productId)
                .and(qBankProductOperate.entId.eq(entId));
        if (operate != null && operate != -1) {
            booleanExpression = booleanExpression.and(qBankProductOperate.operateType.eq(ProductOperateEnum.values()[operate]));
        } else {
            booleanExpression = booleanExpression.and(qBankProductOperate.openId.ne(openId));
        }

        size = bankProductOperateDao.selectFrom(qBankProductOperate).where(booleanExpression).groupBy(qBankProductOperate.openId).fetchCount();

        List<Tuple> tuples = bankProductOperateDao.select(qBankProductOperate.openId, qBankProductOperate.crtDateTime.max())
                .from(qBankProductOperate).where(booleanExpression).groupBy(qBankProductOperate.openId)
                .orderBy(qBankProductOperate.crtDateTime.max().desc()).offset(page.getOffset()).limit(page.getPageSize()).fetch();

        for (Tuple tuple : tuples) {
            OperateDTO operateDTO = new OperateDTO();
            operateDTO.setNowDate(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            operateDTO.setCrtDateTime(tuple.get(qBankProductOperate.crtDateTime.max()).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());

            BankProductOperate bankProductOperate = bankProductOperateDao.selectFrom(qBankProductOperate)
                    .where(qBankProductOperate.openId.eq(tuple.get(qBankProductOperate.openId))
                            .and(qBankProductOperate.crtDateTime.eq(tuple.get(qBankProductOperate.crtDateTime.max())))).fetchFirst();

            operateDTO.setOperate(bankProductOperate.getOperateType().getCode());
            //根据openId查询用户头像
            EmployeeWechatInfo wechatInfo = employeeWechatInfoDao.findFirstByOpenId(bankProductOperate.getOpenId());
            if (wechatInfo != null) {
                try {
                    operateDTO.setNickname(StringUtils.isEmpty(wechatInfo.getNickname()) ? "????" : URLDecoder.decode(wechatInfo.getNickname(), "UTF-8"));
                } catch (Exception e) {
                }
                operateDTO.setHeadimgurl(StringUtils.isEmpty(wechatInfo.getHeadimgurl()) ? null : wechatInfo.getHeadimgurl());
            }

            list.add(operateDTO);
        }

        return new PageImpl<OperateDTO>(list, page, size);
    }

    @Override
    public UserInfoDTO getUserInfo(String idNumber, String entId) {
        QEmployeeInfo qEmployeeInfo = QEmployeeInfo.employeeInfo;
        List<EmployeeInfo> employeeInfos = employeeInfoDao.selectFrom(qEmployeeInfo)
                .where(qEmployeeInfo.idNumber.eq(idNumber).and(qEmployeeInfo.entId.eq(entId))).fetch();
        ;
        if (employeeInfos == null || employeeInfos.size() <= 0) {
            throw new ParamsIllegalException(ErrorConstant.Error0001.format("用户"));
        }
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        userInfoDTO.setClientName(employeeInfos.get(0).getEmployeeName());

        EntErpriseInfo entErpriseInfo = entErpriseInfoDao.findById(entId)
                .orElseThrow(() -> new ParamsIllegalException(ErrorConstant.Error0001.format("用户企业")));

        ManagerInfo managerInfo = managerInfoDao.findFirstBySubBranchNoAndOfficer(entErpriseInfo.getBranch(), entErpriseInfo.getOfficer());
        if (managerInfo != null) {
            userInfoDTO.setCustManagerId(managerInfo.getId());
            userInfoDTO.setManagerName(managerInfo.getManagerName());
            userInfoDTO.setManagerPhone(managerInfo.getPhone());
            userInfoDTO.setManagerImg(managerInfo.getAvatarUrl());
        }

        //判断用户银行卡
        for (EmployeeInfo employeeInfo : employeeInfos) {
            for (EmployeeCardInfo employeeCardInfo : employeeInfo.getEmpCardList()) {
                if (employeeCardInfo.getIssuerName().indexOf("华夏") != -1) {
                    userInfoDTO.setHxBank(IsStatusEnum.YES.getCode());
                    break;
                }
            }
        }

        return userInfoDTO;
    }

    @Override
    public boolean isFollowWechat(String openId) {

        WechatFollowInfo wechatFollowInfo = wechatFollowInfoDao.findFirstByOpenIdAndDelStatusEnum(openId, DelStatusEnum.normal);
        if (wechatFollowInfo != null) {
            return true;
        }
        return false;
    }

    @Override
    public EmployeeInfo getEmpByIdNumberEnt(String idNumber, String entId) {
        QEmployeeInfo qEmployeeInfo = QEmployeeInfo.employeeInfo;
        EmployeeInfo employeeInfo = employeeInfoDao.selectFrom(qEmployeeInfo)
                .where(qEmployeeInfo.idNumber.eq(idNumber).and(qEmployeeInfo.entId.eq(entId))
                        .and(qEmployeeInfo.delStatusEnum.eq(DelStatusEnum.normal))).fetchFirst();
        return employeeInfo;
    }

    @Override
    public String getBankProductInfo() {
        QBankProductInfo qBankProductInfo = QBankProductInfo.bankProductInfo;
        BankProductInfo bankProductInfo = bankProductInfoDao.selectFrom(qBankProductInfo)
                .where(qBankProductInfo.productStatus.eq(ProductStatusEnum.UP).and(qBankProductInfo.delStatusEnum.eq(DelStatusEnum.normal)).and(qBankProductInfo.productType.eq(ProductTypeEnum.TYPE0)))
                .orderBy(qBankProductInfo.intentStartDate.desc()).fetchFirst();
        String id = bankProductInfo.getId();
        return id;
    }
    @Override
    public void addIntent(IntentRequestDTO intentRequestDTO) {
        //添加预约记录
        BankProductIntention bankProductIntention = new BankProductIntention();
        bankProductIntention.setEntId(intentRequestDTO.getEntId());
        bankProductIntention.setProductId(intentRequestDTO.getProductId());
        bankProductIntention.setCustManagerId(intentRequestDTO.getCustManagerId());
        bankProductIntention.setHandleFlag("0");
        bankProductIntention.setNewMark(IsStatusEnum.YES);
        bankProductIntention.setClientName(intentRequestDTO.getClientName());
        bankProductIntention.setClientPhone(intentRequestDTO.getClientPhone());
        bankProductIntention.setIdNumber(intentRequestDTO.getIdNumber());
        bankProductIntention.setIntentStatus(IntentStatusEnum.INTENT_SUCESS);
        bankProductIntention.setIntentDateTime(LocalDateTime.now());
        bankProductIntention.setCrtDateTime(LocalDateTime.now());
        bankProductIntention.setIntentAmount(intentRequestDTO.getIntentAmount());
        bankProductIntention.setProtocol(IsStatusEnum.values()[intentRequestDTO.getProtocol()]);

        bankProductIntentionDao.save(bankProductIntention);

        //添加操作记录
        BankProductOperate bankProductOperate = new BankProductOperate();
        bankProductOperate.setOpenId(intentRequestDTO.getOpenId());
        bankProductOperate.setProductId(intentRequestDTO.getProductId());
        bankProductOperate.setEntId(intentRequestDTO.getEntId());
        bankProductOperate.setOperateType(ProductOperateEnum.INTENT);
        bankProductOperate.setCrtDateTime(LocalDateTime.now());
        bankProductOperate.setChannel(intentRequestDTO.getChannel());
        bankProductOperate.setFxId(intentRequestDTO.getFxId());

        bankProductOperateDao.save(bankProductOperate);

    }

    @Override
    public void addBrowse(BrowseRequestDTO browseRequestDTO) {
        BankProductOperate bankProductOperate = new BankProductOperate();
        bankProductOperate.setOpenId(browseRequestDTO.getOpenId());
        bankProductOperate.setProductId(browseRequestDTO.getProductId());
        bankProductOperate.setEntId(browseRequestDTO.getEntId());
        bankProductOperate.setOperateType(ProductOperateEnum.BROWSE);
        bankProductOperate.setCrtDateTime(LocalDateTime.now());
        bankProductOperate.setChannel(browseRequestDTO.getChannel());
        bankProductOperate.setFxId(browseRequestDTO.getFxId());

        bankProductOperateDao.save(bankProductOperate);
    }
}
