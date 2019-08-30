package chain.fxgj.core.common.service.impl;

import chain.fxgj.core.common.service.SynDataService;
import chain.fxgj.core.jpa.dao.*;
import chain.fxgj.core.jpa.model.*;
import chain.payroll.client.feign.SynDataFeignController;
import chain.payroll.dto.sync.*;
import com.querydsl.core.QueryResults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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


    /**
     * 同步过年工资信息
     * @param date
     * @return
     */
    @Override
    public Integer wagedetail(String date) {
        int pageSize=100;
        int page=1;
        Integer result=0;
        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy");
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("MM");
        //分页查询
        log.info("工资详情开始同步数据.....");
        while (true){
            int currentData=(page-1)*pageSize;
            QWageDetailInfo qWageDetailInfo=QWageDetailInfo.wageDetailInfo;
            QueryResults<WageDetailInfo> wageDetailInfoQueryResults = wageDetailInfoDao.selectFrom(qWageDetailInfo)
                    .orderBy(qWageDetailInfo.crtDateTime.desc())
                    .offset(currentData)
                    .limit(pageSize)
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
                    wageDetailInfoDTOList.add(dto);
                }
                log.info("wageDetailInfoQueryResults--->size:{}",wageDetailInfoDTOList.size());
                boolean b = synDataFeignController.syncWageDetail("2019",wageDetailInfoDTOList);
                if (b){
                    result+=wageDetailInfoDTOList.size();
                }
            }
            if (wageDetailInfoQueryResults.getResults().size()<pageSize){
                break;
            }
            page=page+1;
            log.info("企业信息当前同步数据第{}页，同步数量:{}",page,pageSize);
        }
        return result;
    }

    /**
     * 同步用户信息
     * @return
     */
    @Override
    public Integer empinfo() {
        int pageSize=100;
        int page=1;
        Integer result=0;
        //分页查询
        log.info("用户信息开始同步数据.....");
        while (true){
            int currentData=(page-1)*pageSize;
            QEmployeeInfo qEmployeeInfo=QEmployeeInfo.employeeInfo;
            QueryResults<EmployeeInfo> employeeInfoQueryResults = employeeInfoDao.selectFrom(qEmployeeInfo)
                    .orderBy(qEmployeeInfo.crtDateTime.desc())
                    .offset(currentData)
                    .limit(pageSize)
                    .fetchResults();
            List<EmployeeInfoDTO> employeeInfoDTOList=null;
            log.info("employeeInfoQueryResults--->size:{}",employeeInfoQueryResults.getResults().size());
            if (employeeInfoQueryResults.getResults()!=null){
                employeeInfoDTOList=new ArrayList<>();
                for (EmployeeInfo employeeInfo:employeeInfoQueryResults.getResults()){
                    EmployeeInfoDTO dto=new EmployeeInfoDTO();
                    BeanUtils.copyProperties(employeeInfo,dto);
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
            if (employeeInfoQueryResults.getResults().size()<pageSize){
                break;
            }
            page=page+1;
            log.info("用户信息当前同步数据第{}页，同步数量:{}",page,pageSize);
        }
        return result;
    }

    @Override
    public Integer empwetchat() {
        int pageSize=100;
        int page=1;
        Integer result=0;
        //分页查询
        log.info("用户微信信息开始同步数据.....");
        while (true){
            int currentData=(page-1)*pageSize;
            QEmployeeWechatInfo qEmployeeWechatInfo=QEmployeeWechatInfo.employeeWechatInfo;
            QueryResults<EmployeeWechatInfo> wechatInfoQueryResults = employeeWechatInfoDao.selectFrom(qEmployeeWechatInfo)
                    .orderBy(qEmployeeWechatInfo.crtDateTime.desc())
                    .offset(currentData)
                    .limit(pageSize)
                    .fetchResults();
            List<EmployeeWechatInfoDTO> employeeWechatInfoDTOS=null;
            log.info("wechatInfoDTOS--->size:{}",wechatInfoQueryResults.getResults().size());
            if (wechatInfoQueryResults.getResults()!=null){
                employeeWechatInfoDTOS=new ArrayList<>();
                for (EmployeeWechatInfo wechatInfo:wechatInfoQueryResults.getResults()){
                    EmployeeWechatInfoDTO dto=new EmployeeWechatInfoDTO();
                    BeanUtils.copyProperties(wechatInfo,dto);
                    employeeWechatInfoDTOS.add(dto);
                }
                log.info("wechatInfoDTOS--->size:{}",employeeWechatInfoDTOS.size());
                boolean b = synDataFeignController.syncEmpWetchat(employeeWechatInfoDTOS);
                if (b){
                    result+=employeeWechatInfoDTOS.size();
                }
            }
            if (wechatInfoQueryResults.getResults().size()<pageSize){
                break;
            }
            page=page+1;
            log.info("企业信息当前同步数据第{}页，同步数量:{}",page,pageSize);
        }
        return result;
    }

    @Override
    public Integer enterprise() {
        int pageSize=100;
        int page=1;
        Integer result=0;
        //分页查询
        log.info("企业信息开始同步数据.....");
        while (true){
            int currentData=(page-1)*pageSize;
            QEntErpriseInfo qEntErpriseInfo=QEntErpriseInfo.entErpriseInfo;
            QueryResults<EntErpriseInfo> entGroupInfoQueryResults = entErpriseInfoDao.selectFrom(qEntErpriseInfo)
                    .orderBy(qEntErpriseInfo.crtDateTime.desc())
                    .offset(currentData)
                    .limit(pageSize)
                    .fetchResults();
            List<EntErpriseInfoDTO> erpriseInfoDTOS=null;
            log.info("entGroupInfoQueryResults--->size:{}",entGroupInfoQueryResults.getResults().size());
            if (entGroupInfoQueryResults.getResults()!=null){
                erpriseInfoDTOS=new ArrayList<>();
                for (EntErpriseInfo entErpriseInfo:entGroupInfoQueryResults.getResults()){
                    EntErpriseInfoDTO dto=new EntErpriseInfoDTO();
                    BeanUtils.copyProperties(entErpriseInfo,dto);
                    erpriseInfoDTOS.add(dto);
                }
                log.info("entGroupInfoQueryResults--->size:{}",erpriseInfoDTOS.size());
                boolean b = synDataFeignController.syncEnterprise(erpriseInfoDTOS);
                if (b){
                    result+=erpriseInfoDTOS.size();
                }
            }
            if (entGroupInfoQueryResults.getResults().size()<pageSize){
                break;
            }
            page=page+1;
            log.info("企业信息当前同步数据第{}页，同步数量:{}",page,pageSize);
        }
        return result;
    }

    @Override
    public Integer entgroup() {
        int pageSize=100;
        int page=1;
        Integer result=0;
        //分页查询
        log.info("机构信息开始同步数据.....");
        while (true){
            int currentData=(page-1)*pageSize;
            QEntGroupInfo qEntGroupInfo=QEntGroupInfo.entGroupInfo;
            QueryResults<EntGroupInfo> entGroupInfoQueryResults = entGroupInfoDao.selectFrom(qEntGroupInfo)
                    .orderBy(qEntGroupInfo.crtDateTime.desc())
                    .offset(currentData)
                    .limit(pageSize)
                    .fetchResults();
            List<EntGroupInfoDTO> entGroupInfoDTOS=null;
            log.info("groupInfoDTOS--->size:{}",entGroupInfoQueryResults.getResults().size());
            if (entGroupInfoQueryResults.getResults()!=null){
                entGroupInfoDTOS=new ArrayList<>();
                for (EntGroupInfo entGroupInfo:entGroupInfoQueryResults.getResults()){
                    EntGroupInfoDTO dto=new EntGroupInfoDTO();
                    BeanUtils.copyProperties(entGroupInfo,dto);
                    entGroupInfoDTOS.add(dto);
                }
                log.info("groupInfoDTOS--->size:{}",entGroupInfoDTOS.size());
                boolean b = synDataFeignController.syncEntGroup(entGroupInfoDTOS);
                if (b){
                    result+=entGroupInfoDTOS.size();
                }
            }
            if (entGroupInfoQueryResults.getResults().size()<pageSize){
                break;
            }
            page=page+1;
            log.info("机构信息当前同步数据第{}页，同步数量:{}",page,pageSize);
        }
        return result;
    }

    @Override
    public Integer manager() {
        int pageSize=100;
        int page=1;
        Integer result=0;
        //分页查询
        log.info("银行经理人信息开始同步数据.....");
        while (true){
            int currentData=(page-1)*pageSize;
            QManagerInfo managerInfoQ=QManagerInfo.managerInfo;
            QueryResults<ManagerInfo> managerInfoPage = managerInfoDao.selectFrom(managerInfoQ)
                    .orderBy(managerInfoQ.crtDateTime.desc())
                    .offset(currentData)
                    .limit(pageSize)
                    .fetchResults();
            List<ManagerInfoDTO> managerInfoDTOS=null;
            log.info("managerInfoDTOS--->size:{}",managerInfoPage.getResults().size());
            if (managerInfoPage.getResults()!=null){
                managerInfoDTOS=new ArrayList<>();
                for (ManagerInfo managerInfo:managerInfoPage.getResults()){
                    ManagerInfoDTO dto=new ManagerInfoDTO();
                    BeanUtils.copyProperties(managerInfo,dto);
                    managerInfoDTOS.add(dto);
                }
                log.info("managerInfoDTOS--->size:{}",managerInfoDTOS.size());
                boolean b = synDataFeignController.synManagerInfo(managerInfoDTOS);
                if (b){
                    result+=managerInfoDTOS.size();
                }
            }
            if (managerInfoPage.getResults().size()<pageSize){
                break;
            }
            page=page+1;
            log.info("银行经理人当前同步数据第{}页，同步数量:{}",page,pageSize);
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


}
