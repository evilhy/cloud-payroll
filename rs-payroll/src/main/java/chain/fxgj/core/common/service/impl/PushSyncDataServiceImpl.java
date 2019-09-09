package chain.fxgj.core.common.service.impl;

import chain.fxgj.core.common.constant.Constants;
import chain.fxgj.core.common.constant.DictEnums.AppPartnerEnum;
import chain.fxgj.core.common.constant.DictEnums.DelStatusEnum;
import chain.fxgj.core.common.service.EmpWechatService;
import chain.fxgj.core.common.service.EmployeeEncrytorService;
import chain.fxgj.core.common.service.PushSyncDataService;
import chain.fxgj.core.jpa.dao.*;
import chain.fxgj.core.jpa.model.*;
import chain.fxgj.server.payroll.config.properties.MerchantsProperties;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.payroll.client.feign.SynDataFeignController;
import chain.payroll.dto.sync.*;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import lombok.extern.slf4j.Slf4j;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PushSyncDataServiceImpl implements PushSyncDataService {

    @Autowired
    WageDetailInfoDao wageDetailInfoDao;
    @Autowired
    WageSheetInfoDao wageSheetInfoDao;
    @Autowired
    EmployeeEncrytorService employeeEncrytorService;
    @Autowired
    EmpWechatService empWechatService;
    @Autowired
    WageFundTypeInfoDao wageFundTypeInfoDao;
    @Autowired
    MerchantsProperties merchantProperties;
    @Autowired
    private SynDataFeignController synDataFeignController;
    @Resource
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private EmployeeCardInfoDao employeeCardInfoDao;
    @Autowired
    private EmployeeInfoDao employeeInfoDao;
    @Autowired
    EmployeeWechatInfoDao employeeWechatInfoDao;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private WageShowInfoDao wageShowInfoDao;
    @Autowired
    private WageFundTypeInfoDao fundTypeInfoDao;
    @Autowired
    private WageSheetInfoDao wageSheetInfoDaohee;
    @Override
    public void pushSyncDataToCache(String idNumber, String groupId, String year, String type, UserPrincipal principal) {
        //查询员工id
        pushEmpInfo(idNumber,groupId,principal.getAppPartner());
        //同步机构信息
        String entId=pushEntGroup(groupId);
        if (StringUtils.isNotEmpty(entId)){
            //企业信息
            pushEntenprise(entId);
        }
        //同步show信息
        pushWageDetail(year,idNumber,groupId);
    }

    /**
     * 推送员工信息到MongoDB
     * 同步员工信息，员工微信信息，员工卡信息
     */
    private void pushEmpInfo(String idNumber,String groupId, AppPartnerEnum appPartner){
        try {
            //查询员工信息
            syncEmpInfo(idNumber,groupId);
            syncEmpWetchatInfo(idNumber,appPartner);
        }catch (Exception e){
            log.info("pushEmpInfo Exception--->{}",e);
        }
    }

    /**
     * 推送Employee表数据
     * @param idNumber
     * @param groupId
     */
    private void syncEmpInfo(String idNumber, String groupId){
        try {
            QEmployeeInfo qEmployeeInfo=QEmployeeInfo.employeeInfo;
            Predicate predicate = qEmployeeInfo.delStatusEnum.eq(DelStatusEnum.normal);
            if (StringUtils.isNotEmpty(idNumber)) {
                //判断微信是否绑定
                idNumber = idNumber.toUpperCase();  //证件号码 转成大写
                String idNumberEncrytor = employeeEncrytorService.encryptIdNumber(idNumber);
                log.info("====>加密后的身份证：{}", idNumberEncrytor);
                predicate = ExpressionUtils.and(predicate, qEmployeeInfo.idNumber.eq(idNumber));
            }
            if (StringUtils.isNotEmpty(groupId)){
                predicate = ExpressionUtils.and(predicate, qEmployeeInfo.groupId.eq(groupId));
            }
            QueryResults<EmployeeInfo> employeeInfoQueryResults = employeeInfoDao.selectFrom(qEmployeeInfo)
                    .orderBy(qEmployeeInfo.crtDateTime.desc())
                    .where(predicate)
                    .fetchResults();
            List<EmployeeInfoDTO> employeeInfoDTOList=null;
            log.info("employeeInfoQueryResults--->size:{}",employeeInfoQueryResults.getResults().size());
            if (employeeInfoQueryResults.getResults()!=null) {
                employeeInfoDTOList = new ArrayList<>();
                for (EmployeeInfo employeeInfo : employeeInfoQueryResults.getResults()) {
                    EmployeeInfoDTO dto = new EmployeeInfoDTO();
                    BeanUtils.copyProperties(employeeInfo, dto);
                    if (employeeInfo.getIdType() != null) {
                        dto.setIdType(employeeInfo.getIdType().getCode());
                    }
                    if (employeeInfo.getEmployeeStatusEnum() != null) {
                        dto.setEmployeeStatus(employeeInfo.getEmployeeStatusEnum().getCode());
                    }
                    if (employeeInfo.getIsBindWechat() != null) {
                        dto.setIsBindWechat(employeeInfo.getIsBindWechat().getCode());
                    }
                    if (employeeInfo.getDelStatusEnum() != null) {
                        dto.setDelStatusEnum(employeeInfo.getDelStatusEnum().getCode());
                    }
                    //查询用户卡信息
                    QEmployeeCardInfo cardInfo = QEmployeeCardInfo.employeeCardInfo;
                    //读取用户的卡信息
                    List<EmployeeCardInfo> cardList = employeeCardInfoDao.selectFrom(cardInfo).
                            where(cardInfo.employeeInfo.id.eq(employeeInfo.getId())).fetch();
                    log.info("cardList--->{}", cardList.size());
                    //构建卡的信息
                    List<EmployeeCardInfoDTO> cardDTOList = null;
                    if (cardList != null) {
                        cardDTOList = new ArrayList<>();
                        for (EmployeeCardInfo card : cardList) {
                            EmployeeCardInfoDTO dto1 = new EmployeeCardInfoDTO();
                            BeanUtils.copyProperties(card, dto1);
                            if (card.getDelStatusEnum() != null) {
                                dto1.setDelStatusEnum(card.getDelStatusEnum().getCode());
                            }
                            if (card.getCardVerifyStatusEnum() != null) {
                                dto1.setCardVerifyStatus(card.getCardVerifyStatusEnum().getCode());
                            }
                            cardDTOList.add(dto1);
                        }
                    }
                    dto.setBankCardList(cardDTOList);
                    employeeInfoDTOList.add(dto);
                }
                log.info("employeeInfoQueryResults--->size:{}", employeeInfoDTOList.size());
                boolean b = synDataFeignController.syncEmpinfo(employeeInfoDTOList);
            }

        }catch (Exception e){
            log.info("synvEmpInfo_Exception:{}",e);
        }
    }

    private void syncEmpWetchatInfo(String idNumber,AppPartnerEnum appPartner){
        try {
            QEmployeeWechatInfo qEmployeeWechatInfo = QEmployeeWechatInfo.employeeWechatInfo;
            Predicate predicate = qEmployeeWechatInfo.delStatusEnum.eq(DelStatusEnum.normal);
            if (StringUtils.isNotEmpty(idNumber)) {
                //判断微信是否绑定
                idNumber = idNumber.toUpperCase();  //证件号码 转成大写
                String idNumberEncrytor = employeeEncrytorService.encryptIdNumber(idNumber);
                log.info("====>加密后的身份证：{}", idNumberEncrytor);
                predicate = ExpressionUtils.and(predicate, qEmployeeWechatInfo.idNumber.eq(idNumberEncrytor));
            }
            if (appPartner != null) {
                predicate = ExpressionUtils.and(predicate, qEmployeeWechatInfo.appPartner.eq(appPartner));
            }
            EmployeeWechatInfo wechatInfo = employeeWechatInfoDao.select(qEmployeeWechatInfo)
                    .from(qEmployeeWechatInfo)
                    .where(predicate)
                    .fetchFirst();
            List<EmployeeWechatInfoDTO> wechatInfoDTOS=null;
            EmployeeWechatInfoDTO dto=null;
            if (wechatInfo!=null){
                dto=new EmployeeWechatInfoDTO();
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
                wechatInfoDTOS=new ArrayList<>();
                wechatInfoDTOS.add(dto);
                boolean bool=synDataFeignController.syncEmpWetchat(wechatInfoDTOS);
                log.info("synvEmpWetchatInfo--->{}",bool);
            }
        }catch (Exception e){
            log.info("synvEmpWetchatInfo--->{}",e);
        }
    }

    private  LocalDateTime startDate(String date){
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

    private  LocalDateTime endDate(String date){
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

    /**
     * 推送发送工资薪资到MongoDB
     */
    private void pushWageDetail(String year,String idNumber,String groupId){
        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy");
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("MM");
        try {
            QWageDetailInfo qWageDetailInfo=QWageDetailInfo.wageDetailInfo;
            Predicate predicate = qWageDetailInfo.id.isNotEmpty();
            LocalDateTime startDate=startDate(year);
            LocalDateTime endDate=endDate(year);
            if (startDate != null) {
                predicate = ExpressionUtils.and(predicate, qWageDetailInfo.crtDateTime.after(startDate));
            }
            if (endDate != null) {
                predicate = ExpressionUtils.and(predicate, qWageDetailInfo.crtDateTime.before(endDate));
            }
            if (StringUtils.isNotEmpty(idNumber)){
                String idNumberEncrytor = employeeEncrytorService.encryptIdNumber(idNumber);
                log.info("====>加密后的身份证：{}", idNumberEncrytor);
                predicate= ExpressionUtils.and(predicate, qWageDetailInfo.idNumber.eq(idNumberEncrytor));
            }
            if (StringUtils.isNotEmpty(groupId)){
                predicate= ExpressionUtils.and(predicate, qWageDetailInfo.groupId.eq(groupId));
            }
            QueryResults<WageDetailInfo> wageDetailInfoQueryResults = wageDetailInfoDao.selectFrom(qWageDetailInfo).where(predicate)
                    .orderBy(qWageDetailInfo.crtDateTime.desc())
                    .fetchResults();
            List<WageDetailInfoDTO> wageDetailInfoDTOList=null;
            log.info("wageDetailInfoQueryResults--->size:{}",wageDetailInfoQueryResults.getResults().size());
            if (wageDetailInfoQueryResults.getResults()!=null) {
                wageDetailInfoDTOList = new ArrayList<>();
                for (WageDetailInfo detail : wageDetailInfoQueryResults.getResults()) {
                    WageDetailInfoDTO dto = new WageDetailInfoDTO();
                    dto.setCrtYear(formatter1.format(detail.getCrtDateTime()));
                    dto.setCrtMonth(formatter2.format(detail.getCrtDateTime()));
                    BeanUtils.copyProperties(detail, dto);
                    if (detail.getIsCountStatus() != null) {
                        dto.setIsCountStatus(detail.getIsCountStatus().getCode());
                    }
                    if (detail.getPayStatus() != null) {
                        dto.setPayStatus(detail.getPayStatus().getCode());
                    }
                    if (detail.getReportStatus() != null) {
                        dto.setReportStatus(detail.getReportStatus().getCode());
                    }
                    if (detail.getReceiptsStatus() != null) {
                        dto.setReceiptsStatus(detail.getReceiptsStatus().getCode());
                    }
                    if (detail.getPushStatus() != null) {
                        dto.setPushStatus(detail.getPushStatus().getCode());
                    }
                    if (detail.getPushStyle() != null) {
                        dto.setPushStyle(detail.getPushStyle().getCode());
                    }
                    if (detail.getPushType() != null) {
                        dto.setPushType(detail.getPushType().getCode());
                    }
                    if (detail.getIsRead() != null) {
                        dto.setIsRead(detail.getIsRead().getCode());
                    }
                    if (detail.getIsSplit() != null) {
                        dto.setIsSplit(detail.getIsSplit().getCode());
                    }
                    wageDetailInfoDTOList.add(dto);
                    pushWageSheet(dto.getWageSheetId());
                    pushWageShow(dto.getWageSheetId());
                }
                log.info("wageDetailInfoQueryResults--->size:{}", wageDetailInfoDTOList.size());
                boolean b = synDataFeignController.syncWageDetail("2019", wageDetailInfoDTOList);
            }
        }catch (Exception e){
            log.info("pushWageDetail Exception--->{}",e);
        }

    }
    /**
     * 推送薪资方案到MongoDB
     */
    private void pushWageSheet(String sheetId){
        try {
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
                QWageSheetInfo qSheetInfo=QWageSheetInfo.wageSheetInfo;
                Predicate predicate = qSheetInfo.id.eq(sheetId);
                QueryResults<WageSheetInfo> wageSheetInfoQueryResults = wageSheetInfoDaohee.selectFrom(qSheetInfo).where(predicate)
                        .orderBy(qSheetInfo.crtDateTime.desc())
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
            }
        }catch (Exception e){
            log.info("pushWageSheet Exception--->{}",e);
        }
    }
    /**
     * 推送薪资方案显示信息到MongoDB
     */
    private void  pushWageShow(String wageSheetId){
        try {
            log.info("pushWageShow开始同步:{}",wageSheetId);
                QWageShowInfo qwechatSheet=QWageShowInfo.wageShowInfo;
                Predicate predicate = qwechatSheet.wageSheetId.eq(wageSheetId);
                QueryResults<WageShowInfo> wageShowInfoQueryResults = wageShowInfoDao.selectFrom(qwechatSheet)
                        .where(predicate)
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
                }
        }catch (Exception e){
            log.info("pushWageShow Exception--->{}",e);
        }
    }
    /**
     * 推送企业机构信息到MongoDB
     */
    private void  pushEntenprise(String entId){
        log.info("pushEntenprise开始同步:{}",entId);
        try {
            Integer result=0;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String sql="select id,ascription_bank,ascription_channel,branch,crt_date_time," +
                    "disable_row_with_blank_cell,ent_cust_no,ent_name," +
                    "ent_status,is_open_sp,officer,short_ent_name,upd_date_time,sub_industry," +
                    "cust_manager_id,ascription_type,active_date_time,liquidation,version," +
                    "ent_direct_client_no,is_ftp_upload,server_id,sub_version,is_open_sms," +
                    "is_open_wechat from ent_erprise_info where id=? ";
            log.info("sql-->{}",sql);
            List<EntErpriseInfoDTO> resultList=jdbcTemplate.query(sql,new Object[]{entId} , new RowMapper<EntErpriseInfoDTO>() {
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
        }catch (Exception e){
            log.info("pushEntenprise Exception--->{}",e);
        }
    }
    /**
     * 推送企业信息到MongoDB
     */
    private String  pushEntGroup(String groupId) {
        log.info("pushEntGroup开始同步:{}",groupId);
        String entpriseId="";
        try {
            String sql = "select id,crt_date_time,del_status,ent_id,group_name,group_status,short_group_name,upd_date_time," +
                    "upload_emp_lock,group_invoice_id,is_open_sms,is_open_wechat,ascription_type,enable_multi_account," +
                    "ascription_channel,check_type,is_order,project_code FROM ent_group_info where id=? ";
            List<EntGroupInfoDTO> resultList = jdbcTemplate.query(sql, new Object[]{groupId}, new RowMapper<EntGroupInfoDTO>() {
                EntGroupInfoDTO dto = null;

                @Override
                public EntGroupInfoDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
                    dto = new EntGroupInfoDTO();
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
                    String crtDate = rs.getString("crt_date_time");
                    if (StringUtils.isNotEmpty(crtDate)) {
                        long crtLong = rs.getDate("crt_date_time").getTime();
                        Instant instant = Instant.ofEpochMilli(crtLong);
                        ZoneId zone = ZoneId.systemDefault();
                        dto.setCrtDateTime(LocalDateTime.ofInstant(instant, zone));
                    }
                    String updDate = rs.getString("upd_date_time");
                    if (StringUtils.isNotEmpty(updDate)) {
                        long updDateLong = rs.getDate("upd_date_time").getTime();
                        Instant instant = Instant.ofEpochMilli(updDateLong);
                        ZoneId zone = ZoneId.systemDefault();
                        dto.setUpdDateTime(LocalDateTime.ofInstant(instant, zone));
                    }
                    return dto;
                }
            });
            //开始进行同步
            boolean b = synDataFeignController.syncEntGroup(resultList);
            if (resultList!=null){
                return resultList.get(0).getEntId();
            }
        }catch (Exception e){
            log.info("pushEntGroup Exception--->{}",e);
        }
        return entpriseId;
    }
}
