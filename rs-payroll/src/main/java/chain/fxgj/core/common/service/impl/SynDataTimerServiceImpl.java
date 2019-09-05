package chain.fxgj.core.common.service.impl;

import chain.fxgj.core.common.constant.Constants;
import chain.fxgj.core.common.service.SynDataTimerService;
import chain.fxgj.core.jpa.dao.*;
import chain.fxgj.core.jpa.model.*;
import chain.payroll.client.feign.SynDataFeignController;
import chain.payroll.dto.sync.*;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SynDataTimerServiceImpl implements SynDataTimerService {


    @Autowired
    @Qualifier("applicationTaskExecutor")
    Executor executor;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    private WageDetailInfoDao wageDetailInfoDao;
    @Autowired
    private SynDataFeignController synDataFeignController;
    @Autowired
    private WageSheetInfoDao wageSheetInfoDaohee;
    @Autowired
    private WageShowInfoDao wageShowInfoDao;
    @Autowired
    private EmployeeInfoDao employeeInfoDao;
    @Autowired
    private EmployeeCardInfoDao employeeCardInfoDao;
    @Autowired
    private EmployeeWechatInfoDao employeeWechatInfoDao;
    @Autowired
    private EntErpriseInfoDao entErpriseInfoDao;
    @Resource
    private JdbcTemplate jdbcTemplate;

    private static  final String DataConstant="data_task_mongodb_";
    public static final Integer SCHEDULE_TIME_OUT = 360;
    public static final Integer PAGE_SIZE = 200;



    @Override
    public Integer synWageDataInfo(String date) {
        log.info("start sync synWageDataInfo......");
        long startTime = System.currentTimeMillis();
        String wageDetailredisKey = DataConstant.concat("WageDetail");
        String wageSheetRedisKey = DataConstant.concat("WageSheet");
        String wageShowRedisKey = DataConstant.concat("WageShow");
        String wageManagerRedisKey = DataConstant.concat("Manager");
        try{
            LocalDateTime startDate =startDate(date);
            LocalDateTime endDate =endDate(date);
            //同步WageDetail信息
            if (!redisTemplate.hasKey(wageDetailredisKey)){
                redisTemplate.opsForValue().set(wageDetailredisKey, "running", SCHEDULE_TIME_OUT, TimeUnit.MINUTES);
                Runnable wageDetail = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            log.info("开始处理同步WageDetail信息。。。。");
                            syncWageDetailInfo(startDate, endDate);
                        } catch (Exception e) {
                            log.error("WageDetail同步异常", e);
                        }
                    }
                };
                executor.execute(wageDetail);
            }else{
                log.info("WageDetail正在同步中....");
            }
            //同步WageSheet信息
            if (!redisTemplate.hasKey(wageSheetRedisKey)){
                redisTemplate.opsForValue().set(wageSheetRedisKey, "running", SCHEDULE_TIME_OUT, TimeUnit.MINUTES);
                //开始同步WageSheet
                Runnable wageSheet = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            log.info("开始处理同步WageSheet信息。。。。");
                            syncWageSheetInfo(startDate, endDate);
                        } catch (Exception e) {
                            log.error("WageSheet同步异常", e);
                        }
                    }
                };
                executor.execute(wageSheet);
            }else{
                log.info("WageSheet正在同步中....");
            }
            //同步WageShow信息
            if (!redisTemplate.hasKey(wageSheetRedisKey)){
                redisTemplate.opsForValue().set(wageShowRedisKey, "running", SCHEDULE_TIME_OUT, TimeUnit.MINUTES);
                Runnable wageShow = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            log.info("开始处理同步WageShow信息。。。。");
                            syncWageShowInfo(startDate, endDate);
                        } catch (Exception e) {
                            log.error("WageShow同步异常", e);
                        }
                    }
                };
                executor.execute(wageShow);
            }else{
                log.info("WageShow正在同步中....");
            }

            //同步Manager信息
            if (!redisTemplate.hasKey(wageManagerRedisKey)){
                redisTemplate.opsForValue().set(wageManagerRedisKey, "running", SCHEDULE_TIME_OUT, TimeUnit.MINUTES);
                Runnable wageShow = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            log.info("开始处理同步Manager信息。。。。");
                            syncManagerInfo(startDate, endDate);
                        } catch (Exception e) {
                            log.error("WageShow同步异常", e);
                        }
                    }
                };
                executor.execute(wageShow);
            }else{
                log.info("WageShow正在同步中....");
            }

        }catch (Exception e){
            log.error("synWageDataInfo异常", e);
        }finally {
            redisTemplate.delete(wageDetailredisKey);
            redisTemplate.delete(wageSheetRedisKey);
            redisTemplate.delete(wageShowRedisKey);
            redisTemplate.delete(wageManagerRedisKey);
        }
        log.info("synWageDataInfo 同步共耗时:{}",System.currentTimeMillis()-startTime);
        return null;
    }

    private Integer syncWageDetailInfo(LocalDateTime startDate, LocalDateTime endDate){
        Integer result=0;
        try {
            int page=1;
            DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy");
            DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("MM");
            //分页查询
            log.info("工资详情开始同步数据.....");
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
                    boolean b = synDataFeignController.syncWageDetail("2019",wageDetailInfoDTOList);
                    if (b){
                        result+=wageDetailInfoDTOList.size();
                    }
                }
                if (wageDetailInfoQueryResults.getResults().size()<PAGE_SIZE){
                    break;
                }
                page=page+1;
                log.info("企工资详情开始同步数据第{}页，同步数量:{}",page,wageDetailInfoDTOList.size());
            }
        }catch (Exception e){
            log.info("工资详情开始同步出现异常:{}",e);
        }
        return result;
    }

    private Integer syncWageSheetInfo(LocalDateTime startDate, LocalDateTime endDate){
        Integer result=0;
        try {
            int page=1;
            log.info("工资发放方案开始同步数据.....");
            while (true){
                int currentData=(page-1)*PAGE_SIZE;
                QWageSheetInfo qSheetInfo=QWageSheetInfo.wageSheetInfo;
                Predicate predicate = qSheetInfo.id.isNotEmpty();
                if (startDate != null) {
                    predicate = ExpressionUtils.and(predicate, qSheetInfo.updDateTime.after(startDate));
                }
                if (endDate != null) {
                    predicate = ExpressionUtils.and(predicate, qSheetInfo.updDateTime.before(endDate));
                }
                QueryResults<WageSheetInfo> wageSheetInfoQueryResults = wageSheetInfoDaohee.selectFrom(qSheetInfo).where(predicate)
                        .orderBy(qSheetInfo.updDateTime.desc())
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
        }catch (Exception e){
            log.info("工资发放方案同步出现异常:{}",e);
        }
        return result;
    }

    private Integer syncWageShowInfo(LocalDateTime startDate, LocalDateTime endDate){
        Integer result=0;
        try {
            int page=1;
            log.info("工资发放显示方案开始同步数据.....");
            while (true){
                int currentData=(page-1)*PAGE_SIZE;
                QWageShowInfo qwechatSheet=QWageShowInfo.wageShowInfo;
                Predicate predicate = qwechatSheet.id.isNotEmpty();
                if (startDate != null) {
                    predicate = ExpressionUtils.and(predicate, qwechatSheet.crtDateTime.after(startDate));
                }
                if (endDate != null) {
                    predicate = ExpressionUtils.and(predicate, qwechatSheet.crtDateTime.before(endDate));
                }
                QueryResults<WageShowInfo> wageShowInfoQueryResults = wageShowInfoDao.selectFrom(qwechatSheet).
                        where(predicate)
                        .orderBy(qwechatSheet.crtDateTime.desc())
                        .offset(currentData)
                        .limit(PAGE_SIZE)
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
                if (wageShowInfoQueryResults.getResults().size()<PAGE_SIZE){
                    break;
                }
                page=page+1;
                log.info("WageShow当前同步数据第{}页，同步数量:{}",page,wageShowInfoDTOS.size());
            }
        }catch (Exception e){
            log.info("工资发放显示方案同步出现异常:{}",e);
        }
        return result;
    }

    @Override
    public Integer synEmpInfoDataInfo(String date) {
        log.info("start sync synEmpInfoDataInfo......");
        long startTime = System.currentTimeMillis();
        String empRedisKey = DataConstant.concat("Employee");
        String empWechatRedisKey = DataConstant.concat("EmployeeWechat");
        Integer result=0;
        try {
            LocalDateTime startDate = startDate(date);
            LocalDateTime endDate = endDate(date);
            //开始同步Employee数据
            if (!redisTemplate.hasKey(empRedisKey)) {
                redisTemplate.opsForValue().set(empRedisKey, "running", SCHEDULE_TIME_OUT, TimeUnit.MINUTES);
                Runnable emp = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            log.info("开始处理同步Employee信息。。。。");
                            syncWageDetailInfo(startDate, endDate);
                        } catch (Exception e) {
                            log.error("Employee 同步异常", e);
                        }
                    }
                };
                executor.execute(emp);
            } else {
                log.info("Employee 正在同步中....");
            }
            //开始同步EmployeeWechat数据
            if (!redisTemplate.hasKey(empWechatRedisKey)) {
                redisTemplate.opsForValue().set(empWechatRedisKey, "running", SCHEDULE_TIME_OUT, TimeUnit.MINUTES);
                Runnable empWetchat = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            log.info("开始处理同步EmployeeWechat信息。。。。");
                            syncEmployeeWechatInfo(startDate, endDate);
                        } catch (Exception e) {
                            log.error("EmployeeWechat 同步异常", e);
                        }
                    }
                };
                executor.execute(empWetchat);
            } else {
                log.info("EmployeeWechat正在同步中....");
            }
        }catch (Exception e){
            log.info("synEmpInfoDataInfo exception:{}",e);
        }finally {
            redisTemplate.delete(empRedisKey);
            redisTemplate.delete(empWechatRedisKey);
        }
        log.info("synEmpInfoDataInfo同步共耗时:{}",System.currentTimeMillis()-startTime);
        return result;
    }


    private Integer syncEmployeeInfo(LocalDateTime startDate, LocalDateTime endDate){
        Integer result=0;
        try {
            int page=1;
            log.info("Employee开始同步数据.....");
            while (true){
                int currentData=(page-1)*PAGE_SIZE;
                QEmployeeInfo qEmployeeInfo=QEmployeeInfo.employeeInfo;
                Predicate predicate = qEmployeeInfo.id.isNotEmpty();
                if (startDate != null) {
                    predicate = ExpressionUtils.and(predicate, qEmployeeInfo.updDateTime.after(startDate));
                }
                if (endDate != null) {
                    predicate = ExpressionUtils.and(predicate, qEmployeeInfo.updDateTime.before(endDate));
                }
                QueryResults<EmployeeInfo> employeeInfoQueryResults = employeeInfoDao.selectFrom(qEmployeeInfo)
                        .where(predicate)
                        .orderBy(qEmployeeInfo.crtDateTime.desc())
                        .offset(currentData)
                        .limit(PAGE_SIZE)
                        .fetchResults();
                List<EmployeeInfoDTO> employeeInfoDTOList=null;
                log.info("syncEmployeeInfo--->size:{}",employeeInfoQueryResults.getResults().size());
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
                    if (employeeInfoDTOList!=null && employeeInfoDTOList.size()>0){
                        boolean b = synDataFeignController.syncEmpinfo(employeeInfoDTOList);
                        if (b){
                            result+=employeeInfoDTOList.size();
                        }
                    }
                }
                if (employeeInfoQueryResults.getResults().size()<PAGE_SIZE){
                    break;
                }
                page=page+1;
                log.info("用户信息当前同步数据第{}页，同步数量:{}",page,employeeInfoDTOList.size());
            }
        }catch (Exception e){
            log.info("Employee出现异常:{}",e);
        }
        return result;
    }


    private Integer syncEmployeeWechatInfo(LocalDateTime startDate, LocalDateTime endDate){
        Integer result=0;
        try {
            int page=1;
            log.info("EmployeeWetchat开始同步数据.....");
            while (true){
                int currentData=(page-1)*PAGE_SIZE;
                QEmployeeWechatInfo qEmployeeWechatInfo=QEmployeeWechatInfo.employeeWechatInfo;
                Predicate predicate = qEmployeeWechatInfo.id.isNotEmpty();
                if (startDate != null) {
                    predicate = ExpressionUtils.and(predicate, qEmployeeWechatInfo.updDateTime.after(startDate));
                }
                if (endDate != null) {
                    predicate = ExpressionUtils.and(predicate, qEmployeeWechatInfo.updDateTime.before(endDate));
                }
                QueryResults<EmployeeWechatInfo> wechatInfoQueryResults = employeeWechatInfoDao.selectFrom(qEmployeeWechatInfo)
                        .where(predicate)
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
                    if (employeeWechatInfoDTOS!=null && employeeWechatInfoDTOS.size()>0){
                        boolean b = synDataFeignController.syncEmpWetchat(employeeWechatInfoDTOS);
                        if (b){
                            result+=employeeWechatInfoDTOS.size();
                        }
                    }
                }
                if (wechatInfoQueryResults.getResults().size()<PAGE_SIZE){
                    break;
                }
                page=page+1;
                log.info("EmployeeWetchat当前同步数据第{}页，同步数量:{}",page,employeeWechatInfoDTOS.size());
            }
        }catch (Exception e){
            log.info("EmployeeWetchat出现异常:{}",e);
        }
        return result;
    }


    @Override
    public Integer synEntGroupDataInfo(String date) {
        log.info("start sync synEntGroupDataInfo......");
        long startTime = System.currentTimeMillis();
        Integer result=0;
        String entGroupRedisKey = DataConstant.concat("EntpriseGroup");
        String entRedisKey = DataConstant.concat("Entprise");
        try {
            LocalDateTime startDate = startDate(date);
            LocalDateTime endDate = endDate(date);

            if (!redisTemplate.hasKey(entRedisKey)) {
                redisTemplate.opsForValue().set(entRedisKey, "running", SCHEDULE_TIME_OUT, TimeUnit.MINUTES);
                Runnable emp = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            log.info("开始处理同步Entprise信息。。。。");
                            syncEntpriseInfo(startDate, endDate);
                        } catch (Exception e) {
                            log.error("Entprise 同步异常", e);
                        }
                    }
                };
                executor.execute(emp);
            } else {
                log.info("Entprise 正在同步中....");
            }
            if (!redisTemplate.hasKey(entGroupRedisKey)) {
                redisTemplate.opsForValue().set(entGroupRedisKey, "running", SCHEDULE_TIME_OUT, TimeUnit.MINUTES);
                Runnable empWetchat = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            log.info("开始处理同步EntpriseGroup信息。。。。");
                            syncEntpriseGroupInfo(startDate, endDate);
                        } catch (Exception e) {
                            log.error("EntpriseGroup 同步异常", e);
                        }
                    }
                };
                executor.execute(empWetchat);
            } else {
                log.info("EntpriseGroup正在同步中....");
            }
        }catch (Exception e){
            log.info("synEntGroupDataInfo exception:{}",e);
        }finally {
            redisTemplate.delete(entGroupRedisKey);
            redisTemplate.delete(entRedisKey);
        }
        log.info("synEntGroupDataInfo同步共耗时:{}",System.currentTimeMillis()-startTime);
        return result;
    }

    private Integer syncEntpriseInfo(LocalDateTime startDate, LocalDateTime endDate){
        Integer result=0;
        try {
            int page=1;
            log.info("EntpriseInfo开始同步数据.....");
            while (true){
                int currentData=(page-1)*PAGE_SIZE;
                QEntErpriseInfo qEntErpriseInfo=QEntErpriseInfo.entErpriseInfo;
                Predicate predicate = qEntErpriseInfo.id.isNotEmpty();
                if (startDate != null) {
                    predicate = ExpressionUtils.and(predicate, qEntErpriseInfo.updDateTime.after(startDate));
                }
                if (endDate != null) {
                    predicate = ExpressionUtils.and(predicate, qEntErpriseInfo.updDateTime.before(endDate));
                }
                QueryResults<EntErpriseInfo> entGroupInfoQueryResults = entErpriseInfoDao.selectFrom(qEntErpriseInfo)
                        .where(predicate)
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
                    if (erpriseInfoDTOS.size()>0){
                        boolean b = synDataFeignController.syncEnterprise(erpriseInfoDTOS);
                        if (b){
                            result+=erpriseInfoDTOS.size();
                        }
                    }
                }
                if (entGroupInfoQueryResults.getResults().size()<PAGE_SIZE){
                    break;
                }
                page=page+1;
                log.info("EntpriseInfo当前同步数据第{}页，同步数量:{}",page,erpriseInfoDTOS.size());
            }
        }catch (Exception e){
            log.info("EntpriseInfo出现异常:{}",e);
        }
        return result;
    }

    private Integer syncEntpriseGroupInfo(LocalDateTime startDate, LocalDateTime endDate){
        Integer result=0;
        try {
            int page=1;
            log.info("EntpriseInfo开始同步数据.....");
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            while (true){
                int currentData=(page-1)*PAGE_SIZE;
                String sql="select id,crt_date_time,del_status,ent_id,group_name,group_status,short_group_name,upd_date_time," +
                        "upload_emp_lock,group_invoice_id,is_open_sms,is_open_wechat,ascription_type,enable_multi_account," +
                        "ascription_channel,check_type,is_order,project_code FROM ent_group_info where " +
                        "DATE_FORMAT(upd_date_time,'%Y-%m-%d')=? ORDER BY upd_date_time asc limit ?,? ";
                log.info("sql-->{}",sql);
                List<EntGroupInfoDTO> resultList=jdbcTemplate.query(sql,new Object[]{df.format(startDate),currentData,PAGE_SIZE} , new RowMapper<EntGroupInfoDTO>() {
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
                        Date crtDate=rs.getDate("crt_date_time");
                        return dto;
                    }
                });
                //开始进行同步
                if (resultList!=null && resultList.size()>0){
                    boolean b = synDataFeignController.syncEntGroup(resultList);
                    if (b){
                        result+=resultList.size();
                    }
                }
                log.info("WageShow当前同步数据第{}页，同步数量:{}",page,resultList.size());
                if (resultList.size()<PAGE_SIZE){
                    break;
                }
            }

        }catch (Exception e){
            log.info("EntpriseInfo出现异常:{}",e);
        }
        return result;
    }

    private Integer syncManagerInfo(LocalDateTime startDate, LocalDateTime endDate){
        Integer result=0;
        try {
            int page=1;
            log.info("Manager开始同步数据.....");
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            while (true){
                int currentData=(page-1)*PAGE_SIZE;
                String sql="SELECT id,avatar_url,branch_org_name," +
                        "branch_org_no,crt_date_time,is_confirmed,manager_name,mobile,officer,score," +
                        "STATUS,sub_branch_org_name,sub_branch_org_no,upd_date_time,wechat_id," +
                        "wechat_qr_imgae,wechat_qr_url,branch_name,branch_no,phone,sub_branch_name," +
                        "sub_branch_no,cust_status,headquarters_bank from manager_info where DATE_FORMAT(upd_date_time,'%Y-%m-%d')=? ORDER BY upd_date_time asc  limit ?,? ";
                log.info("sql-->{}",sql);
                List<ManagerInfoDTO> resultList=jdbcTemplate.query(sql,new Object[]{df.format(startDate),currentData,PAGE_SIZE} , new RowMapper<ManagerInfoDTO>() {
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
                if (resultList!=null && resultList.size()>0){
                    boolean b = synDataFeignController.synManagerInfo(resultList);
                    if (b){
                        result+=resultList.size();
                    }
                }
                log.info("Manager当前同步数据第{}页，同步数量:{}",page,resultList.size());
                if (resultList.size()<PAGE_SIZE){
                    break;
                }
                page=page+1;
            }

        }catch (Exception e){
            log.info("Manager出现异常:{}",e);
        }
        return result;
    }


    private static LocalDateTime startDate(String date){
        LocalDateTime startDate=LocalDateTime.now();
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

    public static void main(String[] args) {
        System.out.println(startDate("2019-09-12"));
        System.out.println(endDate("2019-09-12"));
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        System.out.println(df.format(LocalDate.now()));
    }


}
