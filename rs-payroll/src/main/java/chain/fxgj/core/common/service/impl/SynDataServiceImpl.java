package chain.fxgj.core.common.service.impl;

import chain.fxgj.core.common.constant.Constants;
import chain.fxgj.core.common.service.SynDataService;
import chain.fxgj.core.jpa.dao.*;
import chain.fxgj.core.jpa.model.*;
import chain.payroll.client.feign.SynDataFeignController;
import chain.payroll.dto.sync.*;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.asm.Advice;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class SynDataServiceImpl implements SynDataService {


    @Autowired
    private WageDetailInfoDao wageDetailInfoDao;

    @Autowired
    private EmployeeInfoDao employeeInfoDao;
    @Autowired
    private EmployeeWechatInfoDao employeeWechatInfoDao;

    @Autowired
    private EntErpriseInfoDao entErpriseInfoDao;

    @Autowired
    private EntGroupInfoDao entGroupInfoDao;

    @Autowired
    private ManagerInfoDao managerInfoDao;

    @Autowired
    private EmployeeCardInfoDao employeeCardInfoDao;

    @Autowired
    private SynDataFeignController synDataFeignController;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private WageSheetInfoDao wageSheetInfoDaohee;
    @Autowired
    private WageFundTypeInfoDao fundTypeInfoDao;
    @Autowired
    private WageShowInfoDao wageShowInfoDao;
    @Autowired
    private RedisTemplate redisTemplate;

    public static final Integer PAGE_SIZE = 500;

    /**
     * 同步过年工资信息
     * @param date
     * @return
     */
    @Override
    public Integer wagedetail(String date) {
        int page=1;
        Integer result=0;
        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy");
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("MM");
        if (date.length()!=4){
            return 0;
        }
        //分页查询
        log.info("工资详情开始同步数据.....");
        LocalDateTime startDate=startDate(date);
        LocalDateTime endDate=endDate(date);

        while (true){
            int currentData=(page-1)*PAGE_SIZE;
            QWageDetailInfo qWageDetailInfo=QWageDetailInfo.wageDetailInfo;
            Predicate predicate = qWageDetailInfo.id.isNotEmpty();
            if (startDate != null) {
                predicate = ExpressionUtils.and(predicate, qWageDetailInfo.crtDateTime.after(startDate));
            }
            if (endDate != null) {
                predicate = ExpressionUtils.and(predicate, qWageDetailInfo.crtDateTime.before(endDate));
            }
            QueryResults<WageDetailInfo> wageDetailInfoQueryResults = wageDetailInfoDao.selectFrom(qWageDetailInfo).where(predicate)
                    .orderBy(qWageDetailInfo.crtDateTime.desc())
                    .offset(currentData)
                    .limit(PAGE_SIZE)
                    .fetchResults();
            List<WageDetailInfoDTO> wageDetailInfoDTOList=null;
            log.info("wageDetailInfoQueryResults--->size:{}",wageDetailInfoQueryResults.getResults().size());
            if (wageDetailInfoQueryResults.getResults()!=null){
                wageDetailInfoDTOList=new ArrayList<>();
                for (WageDetailInfo detail:wageDetailInfoQueryResults.getResults()){
                    WageDetailInfoDTO dto=new WageDetailInfoDTO();
                    dto.setCrtYear(formatter1.format(detail.getCrtDateTime()));
                    dto.setCrtMonth(formatter2.format(detail.getCrtDateTime()));
                    BeanUtils.copyProperties(detail,dto);
                    if (detail.getIsCountStatus()!=null){
                        dto.setIsCountStatus(detail.getIsCountStatus().getCode());
                    }
                    if (detail.getPayStatus()!=null){
                        dto.setPayStatus(detail.getPayStatus().getCode());
                    }
                    if(detail.getReportStatus()!=null){
                        dto.setReportStatus(detail.getReportStatus().getCode());
                    }
                    if(detail.getReceiptsStatus()!=null){
                        dto.setReceiptsStatus(detail.getReceiptsStatus().getCode());
                    }
                    if (detail.getPushStatus()!=null){
                        dto.setPushStatus(detail.getPushStatus().getCode());
                    }
                    if (detail.getPushStyle()!=null){
                        dto.setPushStyle(detail.getPushStyle().getCode());
                    }
                    if (detail.getPushType()!=null){
                        dto.setPushType(detail.getPushType().getCode());
                    }
                    if (detail.getIsRead()!=null){
                        dto.setIsRead(detail.getIsRead().getCode());
                    }
                    if (detail.getIsSplit()!=null){
                        dto.setIsSplit(detail.getIsSplit().getCode());
                    }
                    wageDetailInfoDTOList.add(dto);
                }
                log.info("wageDetailInfoQueryResults--->size:{}",wageDetailInfoDTOList.size());
                boolean b = synDataFeignController.syncWageDetail(formatter1.format(startDate),wageDetailInfoDTOList);
                if (b){
                    result+=wageDetailInfoDTOList.size();
                }
            }
            if (wageDetailInfoQueryResults.getResults().size()<PAGE_SIZE){
                break;
            }
            page=page+1;
            log.info("企业信息当前同步数据第{}页，同步数量:{}",page,wageDetailInfoDTOList.size());
        }
        return result;
    }

    private static LocalDateTime startDate(String date){
        LocalDateTime startDate=LocalDateTime.now();
        date=date.concat("-01-01");
        DateTimeFormatter df = DateTimeFormatter.ofPattern(Constants.DEFAULT_DATE_TIME_FORMAT);
        if (StringUtils.isNotEmpty(date)){
            date=date.concat(" 00:00:00.000");
            System.out.println(date);
            startDate = LocalDateTime.parse(date, df);
        }else{
            startDate = LocalDate.now().plusDays(-1).atTime(0, 0, 0);
        }
        log.info("startDate----{}",startDate);
        return startDate;
    }

    private static LocalDateTime endDate(String date){
        LocalDateTime endDate=LocalDateTime.now();
        date=date.concat("-12-31");
        DateTimeFormatter df = DateTimeFormatter.ofPattern(Constants.DEFAULT_DATE_TIME_FORMAT);
        if (StringUtils.isNotEmpty(date)){
            date=date.concat(" 23:59:59.999");
            endDate = LocalDateTime.parse(date, df);
        }else{
            endDate = LocalDate.now().plusDays(-1).atTime(23, 59, 59);
        }
        log.info("endDate----{}",endDate);
        return endDate;
    }

    private static LocalDateTime startMonthFirst(String date){
        LocalDateTime startDate=LocalDateTime.now();
        date=date.concat("-01");
        DateTimeFormatter df = DateTimeFormatter.ofPattern(Constants.DEFAULT_DATE_TIME_FORMAT);
        if (StringUtils.isNotEmpty(date)){
            date=date.concat(" 00:00:00.000");
            System.out.println(date);
            startDate = LocalDateTime.parse(date, df);
        }else{
            startDate = LocalDate.now().plusDays(-1).atTime(0, 0, 0);
        }
        log.info("startDate----{}",startDate);
        return startDate;
    }
    private static LocalDateTime startMonthLast(String date){
        LocalDateTime endDate=LocalDateTime.now();
        date=date.concat("-31");
        DateTimeFormatter df = DateTimeFormatter.ofPattern(Constants.DEFAULT_DATE_TIME_FORMAT);
        if (StringUtils.isNotEmpty(date)){
            date=date.concat(" 23:59:59.999");
            endDate = LocalDateTime.parse(date, df);
        }else{
            endDate = LocalDate.now().plusDays(-1).atTime(23, 59, 59);
        }
        log.info("endDate----{}",endDate);
        return endDate;
    }

    /**
     * 同步用户信息
     * @return
     */
    @Override
    public Integer empinfo(String date) {
        int page=1;
        Integer result=0;
        if (StringUtils.isEmpty(date)){
            return 0;
        }
        //分页查询
        log.info("用户信息开始同步数据.....");
        LocalDateTime startDate=startMonthFirst(date);
        LocalDateTime endDate=startMonthLast(date);
        log.info("信息开始同步数据:startDate:{},endDate:{}",startDate,endDate);
        while (true){
            int currentData=(page-1)*PAGE_SIZE;
            QEmployeeInfo qEmployeeInfo=QEmployeeInfo.employeeInfo;
            Predicate predicate = qEmployeeInfo.id.isNotEmpty();
            if (startDate != null) {
                predicate = ExpressionUtils.and(predicate, qEmployeeInfo.crtDateTime.after(startDate));
            }
            if (endDate != null) {
                predicate = ExpressionUtils.and(predicate, qEmployeeInfo.crtDateTime.before(endDate));
            }
            QueryResults<EmployeeInfo> employeeInfoQueryResults = employeeInfoDao.selectFrom(qEmployeeInfo).where(predicate)
                    .orderBy(qEmployeeInfo.crtDateTime.desc())
                    .offset(currentData)
                    .limit(PAGE_SIZE)
                    .fetchResults();
            List<EmployeeInfoDTO> employeeInfoDTOList=null;
            log.info("employeeInfoQueryResults--->size:{}",employeeInfoQueryResults.getResults().size());
            if (employeeInfoQueryResults.getResults()!=null){
                employeeInfoDTOList=new ArrayList<>();
                for (EmployeeInfo employeeInfo:employeeInfoQueryResults.getResults()){
                    EmployeeInfoDTO dto=new EmployeeInfoDTO();
                    BeanUtils.copyProperties(employeeInfo,dto);
                    if (employeeInfo.getIdType()!=null){
                        dto.setIdType(employeeInfo.getIdType().getCode());
                    }
                    if (employeeInfo.getEmployeeStatusEnum()!=null){
                        dto.setEmployeeStatus(employeeInfo.getEmployeeStatusEnum().getCode());
                    }
                    if (employeeInfo.getIsBindWechat()!=null){
                        dto.setIsBindWechat(employeeInfo.getIsBindWechat().getCode());
                    }
                    if (employeeInfo.getDelStatusEnum()!=null){
                        dto.setDelStatusEnum(employeeInfo.getDelStatusEnum().getCode());
                    }
                    //查询用户卡信息
                    QEmployeeCardInfo cardInfo=QEmployeeCardInfo.employeeCardInfo;
                    //读取用户的卡信息
                    List<EmployeeCardInfo> cardList=employeeCardInfoDao.selectFrom(cardInfo).
                            where(cardInfo.employeeInfo.id.eq(employeeInfo.getId())).fetch();
                    log.info("cardList--->{}",cardList.size());
                    //构建卡的信息
                    List<EmployeeCardInfoDTO> cardDTOList=null;
                    if (cardList!=null){
                        cardDTOList=new ArrayList<>();
                        for (EmployeeCardInfo card:cardList){
                            EmployeeCardInfoDTO dto1=new EmployeeCardInfoDTO();
                            BeanUtils.copyProperties(card,dto1);
                            if (card.getDelStatusEnum()!=null){
                                dto1.setDelStatusEnum(card.getDelStatusEnum().getCode());
                            }
                            if (card.getCardVerifyStatusEnum()!=null){
                                dto1.setCardVerifyStatus(card.getCardVerifyStatusEnum().getCode());
                            }
                            cardDTOList.add(dto1);
                        }
                    }
                    dto.setBankCardList(cardDTOList);
                    employeeInfoDTOList.add(dto);
                }
                log.info("employeeInfoQueryResults--->size:{}",employeeInfoDTOList.size());
                boolean b = synDataFeignController.syncEmpinfo(employeeInfoDTOList);
                if (b){
                    result+=employeeInfoDTOList.size();
                }
            }
            if (employeeInfoQueryResults.getResults().size()<PAGE_SIZE){
                break;
            }
            page=page+1;
            log.info("用户信息当前同步数据第{}页，同步数量:{}",page,employeeInfoDTOList.size());
        }
        return result;
    }

    @Override
    public Integer empwetchat() {
        int page=1;
        Integer result=0;
        //分页查询
        log.info("用户微信信息开始同步数据.....");
        while (true){
            int currentData=(page-1)*PAGE_SIZE;
            QEmployeeWechatInfo qEmployeeWechatInfo=QEmployeeWechatInfo.employeeWechatInfo;
            QueryResults<EmployeeWechatInfo> wechatInfoQueryResults = employeeWechatInfoDao.selectFrom(qEmployeeWechatInfo)
                    .orderBy(qEmployeeWechatInfo.crtDateTime.desc())
                    .offset(currentData)
                    .limit(PAGE_SIZE)
                    .fetchResults();
            List<EmployeeWechatInfoDTO> employeeWechatInfoDTOS=null;
            log.info("wechatInfoDTOS--->size:{}",wechatInfoQueryResults.getResults().size());
            if (wechatInfoQueryResults.getResults()!=null){
                employeeWechatInfoDTOS=new ArrayList<>();
                for (EmployeeWechatInfo wechatInfo:wechatInfoQueryResults.getResults()){
                    EmployeeWechatInfoDTO dto=new EmployeeWechatInfoDTO();
                    BeanUtils.copyProperties(wechatInfo,dto);
                    if (wechatInfo.getIdType()!=null){
                        dto.setIdType(wechatInfo.getIdType().getCode());
                    }
                    if (wechatInfo.getDelStatusEnum()!=null){
                        dto.setDelStatusEnum(wechatInfo.getDelStatusEnum().getCode());
                    }
                    if (wechatInfo.getAppPartner()!=null){
                        dto.setAppPartner(wechatInfo.getAppPartner().getCode());
                    }
                    if (wechatInfo.getRegisterType()!=null){
                        dto.setRegisterType(wechatInfo.getRegisterType().getCode());
                    }
                    employeeWechatInfoDTOS.add(dto);
                }
                log.info("wechatInfoDTOS--->size:{}",employeeWechatInfoDTOS.size());
                boolean b = synDataFeignController.syncEmpWetchat(employeeWechatInfoDTOS);
                if (b){
                    result+=employeeWechatInfoDTOS.size();
                }
            }
            if (wechatInfoQueryResults.getResults().size()<PAGE_SIZE){
                break;
            }
            page=page+1;
            log.info("企业信息当前同步数据第{}页，同步数量:{}",page,employeeWechatInfoDTOS.size());
        }
        return result;
    }

    @Override
    public Integer enterprise() {
        int page=1;
        Integer result=syncEntpriseByJDBC();
        if (result>0){
            return result;
        }
        //分页查询
        log.info("企业信息开始同步数据.....");
        while (true){
            int currentData=(page-1)*PAGE_SIZE;
            QEntErpriseInfo qEntErpriseInfo=QEntErpriseInfo.entErpriseInfo;
            QueryResults<EntErpriseInfo> entGroupInfoQueryResults = entErpriseInfoDao.selectFrom(qEntErpriseInfo)
                    .orderBy(qEntErpriseInfo.crtDateTime.desc())
                    .offset(currentData)
                    .limit(PAGE_SIZE)
                    .fetchResults();
            List<EntErpriseInfoDTO> erpriseInfoDTOS=null;
            log.info("entGroupInfoQueryResults--->size:{}",entGroupInfoQueryResults.getResults().size());
            if (entGroupInfoQueryResults.getResults()!=null){
                erpriseInfoDTOS=new ArrayList<>();
                for (EntErpriseInfo entErpriseInfo:entGroupInfoQueryResults.getResults()){
                    EntErpriseInfoDTO dto=new EntErpriseInfoDTO();
                    BeanUtils.copyProperties(entErpriseInfo,dto);
                    if (entErpriseInfo.getAscriptionChannel()!=null){
                        dto.setAscriptionChannel(entErpriseInfo.getAscriptionChannel().getCode());
                    }
                    if (entErpriseInfo.getAscriptionType()!=null){
                        dto.setAscriptionType(entErpriseInfo.getAscriptionType().getCode());
                    }
                    if (entErpriseInfo.getLiquidation()!=null){
                        dto.setLiquidation(entErpriseInfo.getLiquidation().getNum());
                    }
                    if (entErpriseInfo.getVersion()!=null){
                        dto.setVersion(entErpriseInfo.getVersion().getCode());
                    }
                    if (entErpriseInfo.getSubVersion()!=null){
                        dto.setSubVersion(entErpriseInfo.getSubVersion().getCode());
                    }
                    if (entErpriseInfo.getIsFtpUpload()!=null){
                        dto.setIsFtpUpload(entErpriseInfo.getIsFtpUpload().getCode());
                    }
                    if (entErpriseInfo.getEntStatus()!=null){
                        dto.setEntStatus(entErpriseInfo.getEntStatus().getCode());
                    }
                    erpriseInfoDTOS.add(dto);
                }
                log.info("entGroupInfoQueryResults--->size:{}",erpriseInfoDTOS.size());
                boolean b = synDataFeignController.syncEnterprise(erpriseInfoDTOS);
                if (b){
                    result+=erpriseInfoDTOS.size();
                }
            }
            if (entGroupInfoQueryResults.getResults().size()<PAGE_SIZE){
                break;
            }
            page=page+1;
            log.info("企业信息当前同步数据第{}页，同步数量:{}",page,erpriseInfoDTOS.size());
        }
        return result;
    }

    @Override
    public Integer entgroup() {
        int page=1;
        Integer result=queryGroupInfoByJDBCs();
        if (result>0){
            return result;
        }
        //分页查询
        log.info("机构信息开始同步数据.....");
        while (true){
            int currentData=(page-1)*PAGE_SIZE;
            QEntGroupInfo qEntGroupInfo=QEntGroupInfo.entGroupInfo;
            QueryResults<EntGroupInfo> entGroupInfoQueryResults = entGroupInfoDao.selectFrom(qEntGroupInfo)
                    .orderBy(qEntGroupInfo.crtDateTime.desc())
                    .offset(currentData)
                    .limit(PAGE_SIZE)
                    .fetchResults();
            List<EntGroupInfoDTO> entGroupInfoDTOS=null;
            log.info("groupInfoDTOS--->size:{}",entGroupInfoQueryResults.getResults().size());
            if (entGroupInfoQueryResults.getResults()!=null){
                entGroupInfoDTOS=new ArrayList<>();
                for (EntGroupInfo entGroupInfo:entGroupInfoQueryResults.getResults()){
                    EntGroupInfoDTO dto=new EntGroupInfoDTO();
                    BeanUtils.copyProperties(entGroupInfo,dto);
                    if (entGroupInfo.getUploadEmpLock()!=null){
                        dto.setUploadEmpLock(entGroupInfo.getUploadEmpLock().getCode());
                    }
                    if (entGroupInfo.getGroupStatusEnum()!=null){
                        dto.setGroupStatus(entGroupInfo.getGroupStatusEnum().getCode());
                    }
                    if (entGroupInfo.getDelStatusEnum()!=null){
                        dto.setDelStatus(entGroupInfo.getDelStatusEnum().getCode());
                    }
                    if (entGroupInfo.getCheckType()!=null){
                        dto.setCheckType(entGroupInfo.getCheckType().getCode());
                    }
                    if (entGroupInfo.getAscriptionType()!=null){
                        dto.setAscriptionType(entGroupInfo.getAscriptionType().getCode());
                    }
                    if (entGroupInfo.getAscriptionChannel()!=null){
                        dto.setAscriptionChannel(entGroupInfo.getAscriptionChannel().getCode());
                    }
                    if (entGroupInfo.getEnableMultiAccountEnum()!=null){
                        dto.setEnableMultiAccount(entGroupInfo.getEnableMultiAccountEnum().getCode());
                    }
                    entGroupInfoDTOS.add(dto);
                }
                log.info("groupInfoDTOS--->size:{}",entGroupInfoDTOS.size());
                boolean b = synDataFeignController.syncEntGroup(entGroupInfoDTOS);
                if (b){
                    result+=entGroupInfoDTOS.size();
                }
            }
            if (entGroupInfoQueryResults.getResults().size()<PAGE_SIZE){
                break;
            }
            page=page+1;
            log.info("机构信息当前同步数据第{}页，同步数量:{}",page,entGroupInfoDTOS.size());
        }
        return result;
    }

    @Override
    public Integer manager() {
        int page=1;
        Integer result=syncManger();
        if (result>0){
            return  result;
        }
        //分页查询
        log.info("银行经理人信息开始同步数据.....");
        while (true){
            int currentData=(page-1)*PAGE_SIZE;
            QManagerInfo managerInfoQ=QManagerInfo.managerInfo;
            QueryResults<ManagerInfo> managerInfoPage = managerInfoDao.selectFrom(managerInfoQ)
                    .orderBy(managerInfoQ.crtDateTime.desc())
                    .offset(currentData)
                    .limit(PAGE_SIZE)
                    .fetchResults();
            List<ManagerInfoDTO> managerInfoDTOS=null;
            log.info("managerInfoDTOS--->size:{}",managerInfoPage.getResults().size());
            if (managerInfoPage.getResults()!=null){
                managerInfoDTOS=new ArrayList<>();
                for (ManagerInfo managerInfo:managerInfoPage.getResults()){
                    ManagerInfoDTO dto=new ManagerInfoDTO();
                    BeanUtils.copyProperties(managerInfo,dto);
                    if (managerInfo.getCustStatus()!=null){
                        dto.setCustStatus(managerInfo.getCustStatus().getCode());
                    }
                    if (managerInfo.getHeadquartersBank()!=null){
                        dto.setHeadquartersBank(managerInfo.getHeadquartersBank().getNum());
                    }
                    managerInfoDTOS.add(dto);
                }
                log.info("managerInfoDTOS--->size:{}",managerInfoDTOS.size());
                boolean b = synDataFeignController.synManagerInfo(managerInfoDTOS);
                if (b){
                    result+=managerInfoDTOS.size();
                }
            }
            if (managerInfoPage.getResults().size()<PAGE_SIZE){
                break;
            }
            page=page+1;
            log.info("银行经理人当前同步数据第{}页，同步数量:{}",page,managerInfoDTOS.size());
        }
//        QManagerInfo managerInfoQ=QManagerInfo.managerInfo;
//        List<ManagerInfo> managerInfoList = managerInfoDao.selectFrom(managerInfoQ).fetch();
//        if (managerInfoList!=null){
//            int listSize = managerInfoList.size();
//            int toIndex = 100;
//            for (int i = 0; i<managerInfoList.size(); i+=100) {
//                if (i+100 > listSize){
//                    toIndex = listSize - i;
//                }
//                List<ManagerInfo> newList = managerInfoList.subList(i, i+toIndex);
//                List<ManagerInfoDTO> managerInfoDTOS=null;
//                if (newList!=null){
//                    managerInfoDTOS=new ArrayList<>();
//                    for (ManagerInfo managerInfo:newList){
//                        ManagerInfoDTO dto=new ManagerInfoDTO();
//                        BeanUtils.copyProperties(managerInfo,dto);
//                        managerInfoDTOS.add(dto);
//                    }
//                    log.info("managerInfoDTOS--->size:{}",managerInfoDTOS.size());
//                    boolean b = synDataFeignController.synManagerInfo(managerInfoDTOS);
//                    if (b){
//                        result+=managerInfoDTOS.size();
//                    }
//                }
//            }
//        }
        return result;
    }

    @Override
    public Integer wageSheet() {
        int page=1;
        Integer result=0;
        //查询所有的薪资发放类型，放入缓存
        String key="WageFundType_";
        QWageFundTypeInfo qWageFundTypeInfo=QWageFundTypeInfo.wageFundTypeInfo;
        QueryResults<WageFundTypeInfo> wfti=fundTypeInfoDao.selectFrom(qWageFundTypeInfo).fetchResults();
        log.info("wfti--size:{}",wfti.getResults().size());
        if (wfti.getResults()!=null){
            for (WageFundTypeInfo fundTypeInfo:wfti.getResults()){
                redisTemplate.opsForValue().set(key+fundTypeInfo.getId(),fundTypeInfo.getFundTypeVal());
            }
        }



        //分页查询
        log.info("WageSheet信息开始同步数据.....");
        while (true){
            int currentData=(page-1)*PAGE_SIZE;
            QWageSheetInfo qSheetInfo=QWageSheetInfo.wageSheetInfo;

            QueryResults<WageSheetInfo> wageSheetInfoQueryResults = wageSheetInfoDaohee.selectFrom(qSheetInfo)
                    .orderBy(qSheetInfo.crtDateTime.desc())
                    .offset(currentData)
                    .limit(PAGE_SIZE)
                    .fetchResults();
            List<WageSheetInfoDTO> wageSheetInfoDTOList=null;
            log.info("wageSheetInfoDTOList--->size:{}",wageSheetInfoQueryResults.getResults().size());
            if (wageSheetInfoQueryResults.getResults()!=null){
                wageSheetInfoDTOList=new ArrayList<>();
                for (WageSheetInfo sheetInfo:wageSheetInfoQueryResults.getResults()){
                    WageSheetInfoDTO wageSheetInfoDTO=new WageSheetInfoDTO();
                    BeanUtils.copyProperties(sheetInfo,wageSheetInfoDTO);
                    if (sheetInfo.getFundDate()!=null){
                        wageSheetInfoDTO.setFundDate(sheetInfo.getFundDate().getCode());
                    }
                    if (sheetInfo.getWageStatus()!=null){
                        wageSheetInfoDTO.setWageStatus(sheetInfo.getWageStatus().getCode());
                    }
                    if (sheetInfo.getCheckType()!=null){
                        wageSheetInfoDTO.setCheckType(sheetInfo.getCheckType().getCode());
                    }
                    if (sheetInfo.getDelStatusEnum()!=null){
                        wageSheetInfoDTO.setDelStatusEnum(sheetInfo.getDelStatusEnum().getCode());
                    }
                    if (sheetInfo.getAscriptionType()!=null){
                        wageSheetInfoDTO.setAscriptionType(sheetInfo.getAscriptionType().getCode());
                    }
                    if (sheetInfo.getFtpStatus()!=null){
                        wageSheetInfoDTO.setFtpStatus(sheetInfo.getFtpStatus().getCode());
                    }
                    Object fundTypeName=redisTemplate.opsForValue().get(key+sheetInfo.getFundType());
                    if (fundTypeName!=null){
                        wageSheetInfoDTO.setFundTypeName(fundTypeName+"");
                    }
                    wageSheetInfoDTOList.add(wageSheetInfoDTO);
                }
                log.info("wageSheetInfoDTOList--->size:{}",wageSheetInfoDTOList.size());
                boolean b = synDataFeignController.synWageSheet(wageSheetInfoDTOList);
                if (b){
                    result+=wageSheetInfoDTOList.size();
                }
            }
            if (wageSheetInfoQueryResults.getResults().size()<PAGE_SIZE){
                break;
            }
            page=page+1;
            log.info("WageSheet当前同步数据第{}页，同步数量:{}",page,wageSheetInfoDTOList.size());
        }
        return result;
    }

    @Override
    public Integer wageShow() {
        int pageSize=100;
        int page=1;
        Integer result=0;
        //分页查询
        log.info("WageShow信息开始同步数据.....");
        while (true){
            int currentData=(page-1)*pageSize;
            QWageShowInfo qwechatSheet=QWageShowInfo.wageShowInfo;
            QueryResults<WageShowInfo> wageShowInfoQueryResults = wageShowInfoDao.selectFrom(qwechatSheet)
                    .orderBy(qwechatSheet.crtDateTime.desc())
                    .offset(currentData)
                    .limit(pageSize)
                    .fetchResults();
            List<WageShowInfoDTO> wageShowInfoDTOS=null;
            log.info("wageShowInfoDTOS--->size:{}",wageShowInfoQueryResults.getResults().size());
            if (wageShowInfoQueryResults.getResults()!=null){
                wageShowInfoDTOS=new ArrayList<>();
                for (WageShowInfo wageShowInfo:wageShowInfoQueryResults.getResults()){
                    WageShowInfoDTO dto=new WageShowInfoDTO();
                    BeanUtils.copyProperties(wageShowInfo,dto);
                    if (wageShowInfo.getIsShow0()!=null){
                        dto.setIsShow0(wageShowInfo.getIsShow0().getCode());
                    }
                    if (wageShowInfo.getIsReceipt()!=null){
                        dto.setIsReceipt(wageShowInfo.getIsReceipt().getCode());
                    }
                    wageShowInfoDTOS.add(dto);
                }
                log.info("wageShowInfoDTOS--->size:{}",wageShowInfoDTOS.size());
                boolean b = synDataFeignController.synWageShowSheet(wageShowInfoDTOS);
                if (b){
                    result+=wageShowInfoDTOS.size();
                }
            }
            if (wageShowInfoQueryResults.getResults().size()<pageSize){
                break;
            }
            page=page+1;
            log.info("WageShow当前同步数据第{}页，同步数量:{}",page,wageShowInfoDTOS.size());
        }
        return result;
    }

    private Integer queryGroupInfoByJDBCs(){
        int page=1;
        Integer result=0;
        while (true){
            int currentData=(page-1)*PAGE_SIZE;
            String sql="select id,crt_date_time,del_status,ent_id,group_name,group_status,short_group_name,upd_date_time," +
                    "upload_emp_lock,group_invoice_id,is_open_sms,is_open_wechat,ascription_type,enable_multi_account," +
                    "ascription_channel,check_type,is_order,project_code FROM ent_group_info limit ?,? ";
            List<EntGroupInfoDTO> resultList=jdbcTemplate.query(sql,new Object[]{currentData,PAGE_SIZE} , new RowMapper<EntGroupInfoDTO>() {
                EntGroupInfoDTO dto=null;
                @Override
                public EntGroupInfoDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
                    dto=new EntGroupInfoDTO();
                    dto.setId(rs.getString("id"));
                    dto.setDelStatus(rs.getInt("del_status"));
                    dto.setEntId(rs.getString("ent_id"));
                    dto.setGroupName(rs.getString("group_name"));
                    dto.setGroupStatus(rs.getInt("group_status"));
                    dto.setShortGroupName(rs.getString("short_group_name"));
                    dto.setUploadEmpLock(rs.getInt("upload_emp_lock"));
                    dto.setGroupInvoiceId(rs.getString("group_invoice_id"));
                    dto.setIsOpenSms(rs.getString("is_open_sms"));
                    dto.setIsOpenWechat(rs.getString("is_open_wechat"));
                    dto.setAscriptionType(rs.getInt("ascription_type"));
                    dto.setEnableMultiAccount(rs.getInt("enable_multi_account"));
                    dto.setAscriptionChannel(rs.getInt("ascription_channel"));
                    dto.setCheckType(rs.getInt("check_type"));
                    dto.setIsOrder(rs.getString("is_order"));
                    dto.setProjectCode(rs.getString("project_code"));
                    String crtDate=rs.getString("crt_date_time");
                    if (StringUtils.isNotEmpty(crtDate)){
                        long crtLong=rs.getDate("crt_date_time").getTime();
                        Instant instant = Instant.ofEpochMilli(crtLong);
                        ZoneId zone = ZoneId.systemDefault();
                        dto.setCrtDateTime(LocalDateTime.ofInstant(instant, zone));
                    }
                    String updDate=rs.getString("upd_date_time");
                    if (StringUtils.isNotEmpty(updDate)){
                        long updDateLong=rs.getDate("upd_date_time").getTime();
                        Instant instant = Instant.ofEpochMilli(updDateLong);
                        ZoneId zone = ZoneId.systemDefault();
                        dto.setUpdDateTime(LocalDateTime.ofInstant(instant, zone));
                    }
                    return dto;
                }
            });
            //开始进行同步
            boolean b = synDataFeignController.syncEntGroup(resultList);
            if (b){
                result+=resultList.size();
            }
            log.info("EntGroup当前同步数据第{}页，同步数量:{}",page,resultList.size());
            if (resultList.size()<PAGE_SIZE){
                break;
            }
            page=page+1;
        }
        return result;
    }


    private Integer syncManger(){
        int page=1;
        Integer result=0;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        while (true){
            int currentData=(page-1)*PAGE_SIZE;
            String sql="SELECT id,avatar_url,branch_org_name," +
                    "branch_org_no,crt_date_time,is_confirmed,manager_name,mobile,officer,score," +
                    "STATUS,sub_branch_org_name,sub_branch_org_no,upd_date_time,wechat_id," +
                    "wechat_qr_imgae,wechat_qr_url,branch_name,branch_no,phone,sub_branch_name," +
                    "sub_branch_no,cust_status,headquarters_bank from manager_info limit ?,? ";
            log.info("sql-->{}",sql);
            List<ManagerInfoDTO> resultList=jdbcTemplate.query(sql,new Object[]{currentData,PAGE_SIZE} , new RowMapper<ManagerInfoDTO>() {
                ManagerInfoDTO dto=null;
                @Override
                public ManagerInfoDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
                    dto=new ManagerInfoDTO();
                    dto.setId(rs.getString("id"));
                    dto.setAvatarUrl(rs.getString("avatar_url"));
                    dto.setBranchName(rs.getString("branch_name"));
                    dto.setBranchNo(rs.getString("branch_no"));
                    dto.setIsConfirmed(rs.getString("is_confirmed"));
                    dto.setManagerName(rs.getString("manager_name"));
                    dto.setPhone(rs.getString("phone"));
                    dto.setOfficer(rs.getString("officer"));
                    dto.setScore(rs.getInt("score"));
                    dto.setCustStatus(rs.getInt("cust_status"));
                    dto.setSubBranchName(rs.getString("sub_branch_name"));
                    dto.setSubBranchNo(rs.getString("sub_branch_no"));
                    dto.setWechatId(rs.getString("wechat_id"));
                    dto.setWechatQrImgae(rs.getString("wechat_qr_imgae"));
                    dto.setWechatQrUrl(rs.getString("wechat_qr_url"));
                    dto.setHeadquartersBank(rs.getInt("headquarters_bank"));
                    String crtDate=rs.getString("crt_date_time");
                    if (StringUtils.isNotEmpty(crtDate)){
                        long crtLong=rs.getDate("crt_date_time").getTime();
                        Instant instant = Instant.ofEpochMilli(crtLong);
                        ZoneId zone = ZoneId.systemDefault();
                        dto.setCrtDateTime(LocalDateTime.ofInstant(instant, zone));
                    }
                    String updDate=rs.getString("upd_date_time");
                    if (StringUtils.isNotEmpty(updDate)){
                        long updDateLong=rs.getDate("upd_date_time").getTime();
                        Instant instant = Instant.ofEpochMilli(updDateLong);
                        ZoneId zone = ZoneId.systemDefault();
                        dto.setUpdDateTime(LocalDateTime.ofInstant(instant, zone));
                    }
                    return dto;
                }
            });
            log.info("Manager_size:{}",resultList.size());
            //开始进行同步
            boolean b = synDataFeignController.synManagerInfo(resultList);
            if (b){
                result+=resultList.size();
            }
            log.info("Manager当前同步数据第{}页，同步数量:{}",page,resultList.size());
            if (resultList.size()<PAGE_SIZE){
                break;
            }
            page=page+1;
        }
        return result;
    }

    private Integer syncEntpriseByJDBC(){
        int page=1;
        Integer result=0;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        while (true){
            int currentData=(page-1)*PAGE_SIZE;
            String sql="select id,ascription_bank,ascription_channel,branch,crt_date_time," +
                    "disable_row_with_blank_cell,ent_cust_no,ent_name," +
                    "ent_status,is_open_sp,officer,short_ent_name,upd_date_time,sub_industry," +
                    "cust_manager_id,ascription_type,active_date_time,liquidation,version," +
                    "ent_direct_client_no,is_ftp_upload,server_id,sub_version,is_open_sms," +
                    "is_open_wechat from ent_erprise_info limit ?,? ";
            log.info("sql-->{}",sql);
            List<EntErpriseInfoDTO> resultList=jdbcTemplate.query(sql,new Object[]{currentData,PAGE_SIZE} , new RowMapper<EntErpriseInfoDTO>() {
                EntErpriseInfoDTO dto=null;
                @Override
                public EntErpriseInfoDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
                    dto=new EntErpriseInfoDTO();
                    dto.setId(rs.getString("id"));
                    dto.setAscriptionBank(rs.getString("ascription_bank"));
                    dto.setAscriptionChannel(rs.getInt("ascription_channel"));
                    dto.setBranch(rs.getString("branch"));
                    dto.setDisableRowWithBlankCell(rs.getString("disable_row_with_blank_cell"));
                    dto.setEntCustNo(rs.getString("ent_cust_no"));
                    dto.setEntName(rs.getString("ent_name"));
                    dto.setEntStatus(rs.getInt("ent_status"));
                    dto.setIsOpenSp(rs.getString("is_open_sp"));
                    dto.setOfficer(rs.getString("officer"));
                    dto.setShortEntName(rs.getString("short_ent_name"));
                    dto.setSubIndustry(rs.getString("sub_industry"));
                    dto.setCustManagerId(rs.getString("cust_manager_id"));
                    dto.setAscriptionType(rs.getInt("ascription_type"));
                    dto.setLiquidation(rs.getInt("liquidation"));
                    dto.setVersion(rs.getString("version"));
                    dto.setEntDirectClientNo(rs.getString("ent_direct_client_no"));
                    dto.setIsFtpUpload(rs.getInt("is_ftp_upload"));
                    dto.setServerId(rs.getString("server_id"));
                    dto.setSubVersion(rs.getString("sub_version"));
                    dto.setIsOpenSms(rs.getString("is_open_sms"));
                    dto.setIsOpenWechat(rs.getString("is_open_wechat"));
                    String crtDate=rs.getString("crt_date_time");
                    if (StringUtils.isNotEmpty(crtDate)){
                        long crtLong=rs.getDate("crt_date_time").getTime();
                        Instant instant = Instant.ofEpochMilli(crtLong);
                        ZoneId zone = ZoneId.systemDefault();
                        dto.setCrtDateTime(LocalDateTime.ofInstant(instant, zone));
                    }
                    String updDate=rs.getString("upd_date_time");
                    if (StringUtils.isNotEmpty(updDate)){
                        long updDateLong=rs.getDate("upd_date_time").getTime();
                        Instant instant = Instant.ofEpochMilli(updDateLong);
                        ZoneId zone = ZoneId.systemDefault();
                        dto.setUpdDateTime(LocalDateTime.ofInstant(instant, zone));
                    }
                    String active_date_time=rs.getString("active_date_time");
                    if (StringUtils.isNotEmpty(active_date_time)){
                        long activeDateLong=rs.getDate("active_date_time").getTime();
                        Instant instant = Instant.ofEpochMilli(activeDateLong);
                        ZoneId zone = ZoneId.systemDefault();
                        dto.setUpdDateTime(LocalDateTime.ofInstant(instant, zone));
                    }
                    return dto;
                }
            });
            log.info("Entprise_size:{}",resultList.size());
            //开始进行同步
            boolean b = synDataFeignController.syncEnterprise(resultList);
            if (b){
                result+=resultList.size();
            }
            log.info("Entprise当前同步数据第{}页，同步数量:{}",page,resultList.size());
            if (resultList.size()<PAGE_SIZE){
                break;
            }
            page=page+1;
        }
        return result;
    }

    public static void main(String[] args) {
       // System.out.println(startDate("2018"));
        //System.out.println(endDate("2018"));
        System.out.println(startMonthFirst("2018-02"));
        System.out.println(startMonthLast("2020-02"));
    }

}
