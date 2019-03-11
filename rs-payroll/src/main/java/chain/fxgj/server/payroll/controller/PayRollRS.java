package chain.fxgj.server.payroll.controller;

import chain.css.exception.ParamsIllegalException;
import chain.css.log.annotation.TrackLog;
import chain.fxgj.core.common.config.properties.PayrollProperties;
import chain.fxgj.core.common.constant.DictEnums.IsStatusEnum;
import chain.fxgj.core.common.constant.ErrorConstant;
import chain.fxgj.core.common.constant.FxgjDBConstant;
import chain.fxgj.core.common.service.EmployeeEncrytorService;
import chain.fxgj.core.common.service.WageWechatService;
import chain.fxgj.core.common.util.TransUtil;
import chain.fxgj.server.payroll.dto.response.*;
import chain.fxgj.server.payroll.service.WechatBindService;
import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.fxgj.server.payroll.web.WebSecurityContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 工资条
 */
@RestController
@Validated
@RequestMapping(value = "/roll")
@Slf4j
@SuppressWarnings("unchecked")
public class PayRollRS {

    @Inject
    WageWechatService wageWechatService;
    @Inject
    WechatBindService wechatBindService;
    @Inject
    EmployeeEncrytorService employeeEncrytorService;

    @Autowired
    InsideRS insideRS;

    @Autowired
    PayrollProperties payrollProperties;

    @Resource
    RedisTemplate redisTemplate;


    @GET
    @Path("sdt")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "服务当前时间", response = Long.class)
    @TrackLog
    @PermitAll
    public Response serverDateTime() {
        return Response.ok(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()).build();
    }


    @GET
    @Path("index")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "首页", response = IndexDTO.class)
    @TrackLog
    public Response index() {
        String idNumber = WebSecurityContext.getCurrentWebSecurityContext().getPrincipal().getIdNumber();

        IndexDTO.DataListBean bean = wageWechatService.newGroupPushInfo(idNumber);

        IndexDTO indexDTO = new IndexDTO();
        indexDTO.setBean(bean);
        //查询用户是否银行卡号变更有最新未读消息
        Integer isNew = wechatBindService.getCardUpdIsNew(idNumber);
        log.info("isNew:{}",isNew);
        indexDTO.setIsNew(isNew);
        return Response.ok(indexDTO).build();
    }

    @GET
    @Path("groupList")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "企业机构列表", response = IndexDTO.DataListBean.class, responseContainer = "List")
    @TrackLog
    public Response groupList() {
        String idNumber = WebSecurityContext.getCurrentWebSecurityContext().getPrincipal().getIdNumber();

        List<IndexDTO.DataListBean> list = wageWechatService.groupList(idNumber);


        return Response.ok(list).build();
    }

    @GET
    @Path("entEmp")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "根据身份账号返回手机和公司列表", response = Res100701.class)
    @TrackLog
    @PermitAll
    public Response entEmp(@ApiParam("身份证号") @QueryParam("idNumber") String idNumber) {
        //校验身份证号是否合法
//        if (IDCardUtil.isIdcard(idNumber) != 1) {
//            throw new ParamsIllegalException(ErrorConstant.IDERR.getErrorMsg());
//        }

        Res100701 res100701 = wechatBindService.getEntList(idNumber);
        if (res100701.getBindStatus().equals("1")) {
            throw new ParamsIllegalException(ErrorConstant.WECHAR_002.getErrorMsg());
        }
        if (res100701.getEmployeeList() == null || res100701.getEmployeeList().size() <= 0) {
            throw new ParamsIllegalException(ErrorConstant.WECHAR_001.getErrorMsg());
        }

        return Response.ok(res100701).build();
    }


    @GET
    @Path("wageList")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "个人薪资列表", response = Res100703.class)
    @TrackLog
    public Response wageList(@ApiParam("机构") @QueryParam("groupId") String groupId,
                             @ApiParam("年份") @QueryParam("year") String year,
                             @ApiParam("类型 0资金到账 1合计") @QueryParam("type") String type) {
        String idNumber = WebSecurityContext.getCurrentWebSecurityContext().getPrincipal().getIdNumber();

        Res100703 res100703 = null;
        if (LocalDate.now().getYear() == Integer.parseInt(year)) {
            res100703 = wageWechatService.wageList(idNumber, groupId, year, type);
        } else {
            res100703 = wageWechatService.wageHistroyList(idNumber, groupId, year, type);
        }
        res100703.setYears(wageWechatService.years(res100703.getEmployeeSid(),type));

        return Response.ok(res100703).build();
    }

    @GET
    @Path("wageDetail")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "查看工资条详情", response = WageDetailDTO.class, responseContainer = "List")
    @TrackLog
    public Response wageDetail(@ApiParam("方案id") @QueryParam("wageSheetId") String wageSheetId,
                               @ApiParam("机构id") @QueryParam("groupId") String groupId) {
        UserPrincipal principal = WebSecurityContext.getCurrentWebSecurityContext().getPrincipal();


        List<WageDetailDTO> list = new ArrayList<>();

        try {
            String redisKey = FxgjDBConstant.PREFIX + ":payroll:" + principal.getIdNumberEncrytor() + ":" + wageSheetId + ":wechatpush";
            Object value = redisTemplate.opsForValue().get(redisKey);
            if (value != null && StringUtils.isNotBlank(value.toString())) {
                ObjectMapper mapper = new ObjectMapper();
                JavaType javaType = mapper.getTypeFactory().constructParametricType(ArrayList.class, WageDetailDTO.class);
                list = (List<WageDetailDTO>) mapper.readValue(value.toString(), javaType);
                log.info("{}:读取redis工资,{}", principal.getOpenId(), list.size());
            } else {
                list = wageWechatService.getWageDetail(principal.getIdNumber(), groupId, wageSheetId);
            }
        } catch (Exception e) {
            log.debug("redis读取失败", e);
            list = wageWechatService.getWageDetail(principal.getIdNumber(), groupId, wageSheetId);
        }


        return Response.ok(list).build();
    }

//    @GET
//    @Path("wageTrend")
//    @Produces(MediaType.APPLICATION_JSON)
//    @Consumes(MediaType.APPLICATION_JSON)
//    @ApiOperation(value = "工资的走势",response = Res100712.class)
//    @TrackLog
//    public Response wageTrend(@HeaderParam("openId") String openId,
//                              @ApiParam("机构") @QueryParam("groupId") String groupId,
//                               @ApiParam("年份") @QueryParam("year") String year,
//                               @ApiParam("薪资类型(0=入账日1=按工资日)") @QueryParam("salaryType") String salaryType){
//
//
//        return Response.ok().build();
//    }

    @GET
    @Path("empInfo")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "员工个人信息", response = Res100708.class, responseContainer = "List")
    @TrackLog
    public Response empInfo() {
        String idNumber = WebSecurityContext.getCurrentWebSecurityContext().getPrincipal().getIdNumber();

        List<Res100708> res100708 = wechatBindService.empList(idNumber);

        return Response.ok(res100708).build();
    }

    @GET
    @Path("invoice")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "查询发票信息列表", response = GroupInvoiceDTO.class, responseContainer = "List")
    @TrackLog
    public Response invoice() {
        String idNumber = WebSecurityContext.getCurrentWebSecurityContext().getPrincipal().getIdNumber();

        List<GroupInvoiceDTO> list = wechatBindService.invoiceList(idNumber);

        return Response.ok(list).build();
    }

    @GET
    @Path("checkPwd")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "验证密码")
    @TrackLog
    public Response checkPwd(@ApiParam("查询密码") @QueryParam("pwd") String pwd) {
        UserPrincipal principal = WebSecurityContext.getCurrentWebSecurityContext().getPrincipal();
        if(StringUtils.isEmpty(pwd)){
            throw new ParamsIllegalException(ErrorConstant.WECHAR_007.getErrorMsg());
        }
        String openId = principal.getOpenId();
        String queryPwd = wechatBindService.getQueryPwd(openId);
        if(!queryPwd.equals(employeeEncrytorService.encryptPwd(pwd))){
            throw new ParamsIllegalException(ErrorConstant.WECHAR_007.getErrorMsg());
        }

        return Response.ok().build();
    }

    @GET
    @Path("checkCard")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "验证银行卡后六位")
    @TrackLog
    public Response checkCard(@ApiParam("身份证号") @QueryParam("idNumber") String idNumber,
                              @ApiParam("银行卡后6位") @QueryParam("cardNo") String cardNo) {

        int is=wechatBindService.checkCardNo(idNumber,cardNo);
        if(is== IsStatusEnum.NO.getCode()){
            throw new ParamsIllegalException(ErrorConstant.WECHAR_006.getErrorMsg());
        }

        return Response.ok().build();
    }


    @GET
    @Path("emp")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "员工个人信息", response = EmpInfoDTO.class)
    @TrackLog
    public Response emp() {
        UserPrincipal userPrincipal = WebSecurityContext.getCurrentWebSecurityContext().getPrincipal();

        EmpInfoDTO empInfoDTO=new EmpInfoDTO();
        empInfoDTO.setHeadimgurl(userPrincipal.getHeadimgurl());
        empInfoDTO.setIdNumber(userPrincipal.getIdNumber());
        empInfoDTO.setName(userPrincipal.getName());
        empInfoDTO.setPhone(userPrincipal.getPhone());
        empInfoDTO.setPhoneStar(TransUtil.phoneStar(userPrincipal.getPhone()));
        empInfoDTO.setIdNumberStar(TransUtil.idNumberStar(userPrincipal.getIdNumber()));
        //查询用户是否银行卡号变更有最新未读消息
        Integer isNew = wechatBindService.getCardUpdIsNew(userPrincipal.getIdNumber());
        log.info("isNew:{}",isNew);
        empInfoDTO.setIsNew(isNew);

        return Response.ok(empInfoDTO).build();
    }

    @GET
    @Path("empEnt")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "员工企业", response = EmpEntDTO.class, responseContainer = "List")
    @TrackLog
    public Response empEnt() {
        UserPrincipal userPrincipal = WebSecurityContext.getCurrentWebSecurityContext().getPrincipal();

        List<EmpEntDTO> list=wechatBindService.empEntList(userPrincipal.getIdNumber());

        return Response.ok(list).build();
    }

    @GET
    @Path("empCard")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "员工银行卡", response = EmpEntDTO.class, responseContainer = "List")
    @TrackLog
    public Response empCard() {
        UserPrincipal userPrincipal = WebSecurityContext.getCurrentWebSecurityContext().getPrincipal();

        List<EmpEntDTO> list=wechatBindService.empEntList(userPrincipal.getIdNumber());

        return Response.ok(list).build();
    }


    @GET
    @Path("empCardLog")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "员工银行卡修改记录", response = EmpCardLogDTO.class, responseContainer = "List")
    @TrackLog
    public Response empCardLog(@ApiParam("编号") @QueryParam("ids") String ids) {
        //ids "|"分割
        List<EmpCardLogDTO> list=wechatBindService.empCardLog(ids.split("\\|"));

        //异步更新记录已读
        List<String> logIds = list.stream().map(EmpCardLogDTO::getLogId).collect(Collectors.toList());
        if(logIds.size()>0 && logIds !=null){
            insideRS.bankCardIsNew(logIds);
        }


        return Response.ok(list).build();
    }

    @GET
    @Path("entPhone")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "手机号和公司列表", response = Res100701.EmployeeListBean.class, responseContainer = "List")
    @TrackLog
    public Response entPhone() {
        UserPrincipal userPrincipal = WebSecurityContext.getCurrentWebSecurityContext().getPrincipal();

        List<Res100701.EmployeeListBean>  list = wechatBindService.getEntPhone(userPrincipal.getIdNumber());


        return Response.ok(list).build();
    }

    @GET
    @Path("entUser")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "企业超管", response = EntUserDTO.class, responseContainer = "List")
    @TrackLog
    public Response entUser(@ApiParam("企业编号") @QueryParam("entId") String entId) {

        List<EntUserDTO> list=wechatBindService.entUser(entId);

        return Response.ok(list).build();
    }

}
