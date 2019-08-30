package chain.fxgj.core.common.service.impl;

import chain.fxgj.core.common.dto.employee.EmployeeCardDTO;
import chain.fxgj.core.common.service.SynDataService;
import chain.fxgj.core.jpa.dao.*;
import chain.fxgj.core.jpa.model.*;
import chain.payroll.client.feign.SynDataFeignController;
import chain.payroll.dto.sync.*;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
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
        List<WageDetailInfo> wageDetailList = wageDetailInfoDao.findAll();
        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy");
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("MM");
        log.info("wagedetail--->{}",wageDetailList.size());
        if (wageDetailList!=null){
            int listSize = wageDetailList.size();
            int toIndex = 100;
            for (int i = 0; i<wageDetailList.size(); i+=100) {
                if (i+100 > listSize){
                    toIndex = listSize - i;
                }
                List<WageDetailInfo> newList = wageDetailList.subList(i, i+toIndex);
                List<WageDetailInfoDTO> newDtoList=null;
                if (newList!=null){
                    newDtoList=new ArrayList<>();
                    for (WageDetailInfo detail:newList){
                        WageDetailInfoDTO dto=new WageDetailInfoDTO();
                        dto.setCrtYear(formatter1.format(detail.getCrtDateTime()));
                        dto.setCrtMonth(formatter2.format(detail.getCrtDateTime()));
                        BeanUtils.copyProperties(detail,dto);
                        newDtoList.add(dto);
                        log.info("wagedetail--->{}", dto);
                    }
                    //调用远程服务进行保存
                    log.info("newDtoList--->{}",newDtoList.size());
                    boolean b = synDataFeignController.syncWageDetail("2019", newDtoList);
                }
            }
        }
        return null;
    }

    /**
     * 同步用户信息
     * @return
     */
    @Override
    public Integer empinfo() {
        List<EmployeeInfo> employeeInfoListList = employeeInfoDao.findAll();
        log.info("empinfo--->{}",employeeInfoListList.size());
        if (employeeInfoListList!=null){
            int listSize = employeeInfoListList.size();
            int toIndex = 100;
            for (int i = 0; i<employeeInfoListList.size(); i+=100) {
                if (i+100 > listSize){
                    toIndex = listSize - i;
                }
                List<EmployeeInfo> newList = employeeInfoListList.subList(i, i+toIndex);
                List<EmployeeInfoDTO> newDtoList=null;
                if (newList!=null){
                    newDtoList=new ArrayList<>();
                    for (EmployeeInfo detail:newList){
                        EmployeeInfoDTO dto=new EmployeeInfoDTO();
                        //复制用户信息
                        BeanUtils.copyProperties(detail,dto);
                        QEmployeeCardInfo cardInfo=QEmployeeCardInfo.employeeCardInfo;
                        //读取用户的卡信息
                        List<EmployeeCardInfo> cardList=employeeCardInfoDao.select(cardInfo).
                                where(cardInfo.employeeInfo.id.eq(detail.getId())).fetch();
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
                        newDtoList.add(dto);
                    }
                    //调用远程服务进行保存
                    log.info("newDtoList--->",newDtoList.size());
                    synDataFeignController.syncEmpinfo(newDtoList);
                }
            }
        }
        return null;
    }

    @Override
    public Integer empwetchat() {
        Integer result=0;
        List<EmployeeWechatInfo> employeeInfoListList = employeeWechatInfoDao.findAll();
        if (employeeInfoListList!=null){
            int listSize = employeeInfoListList.size();
            int toIndex = 100;
            for (int i = 0; i<employeeInfoListList.size(); i+=100) {
                if (i+100 > listSize){
                    toIndex = listSize - i;
                }
                List<EmployeeWechatInfo> newList = employeeInfoListList.subList(i, i+toIndex);
                List<EmployeeWechatInfoDTO> wechatInfoDTOS=null;
                if (newList!=null){
                    wechatInfoDTOS=new ArrayList<>();
                    for (EmployeeWechatInfo wechat:newList){
                        EmployeeWechatInfoDTO dto=new EmployeeWechatInfoDTO();
                        BeanUtils.copyProperties(wechat,dto);
                        wechatInfoDTOS.add(dto);
                    }
                    log.info("wechatInfoDTOS--->size:{}",wechatInfoDTOS.size());
                    boolean b = synDataFeignController.syncEmpWetchat(wechatInfoDTOS);
                    if (b){
                        result+=wechatInfoDTOS.size();
                    }
                }
            }
        }
        return result;
    }

    @Override
    public Integer enterprise() {
        Integer result=0;
        List<EntErpriseInfo> entErpriseInfoList = entErpriseInfoDao.findAll();
        if (entErpriseInfoList!=null){
            int listSize = entErpriseInfoList.size();
            int toIndex = 100;
            for (int i = 0; i<entErpriseInfoList.size(); i+=100) {
                if (i+100 > listSize){
                    toIndex = listSize - i;
                }
                List<EntErpriseInfo> newList = entErpriseInfoList.subList(i, i+toIndex);
                List<EntErpriseInfoDTO> erpriseInfoDTOS=null;
                if (newList!=null){
                    erpriseInfoDTOS=new ArrayList<>();
                    for (EntErpriseInfo wechat:newList){
                        EntErpriseInfoDTO dto=new EntErpriseInfoDTO();
                        BeanUtils.copyProperties(wechat,dto);
                        erpriseInfoDTOS.add(dto);
                    }
                    log.info("erpriseInfoDTOS--->size:{}",erpriseInfoDTOS.size());
                    boolean b = synDataFeignController.syncEnterprise(erpriseInfoDTOS);
                    if (b){
                        result+=erpriseInfoDTOS.size();
                    }
                }
            }
        }
        return result;
    }

    @Override
    public Integer entgroup() {
        Integer result=0;
        List<EntGroupInfo> entGroupInfoList = entGroupInfoDao.findAll();
        if (entGroupInfoList!=null){
            int listSize = entGroupInfoList.size();
            int toIndex = 100;
            for (int i = 0; i<entGroupInfoList.size(); i+=100) {
                if (i+100 > listSize){
                    toIndex = listSize - i;
                }
                List<EntGroupInfo> newList = entGroupInfoList.subList(i, i+toIndex);
                List<EntGroupInfoDTO> groupInfoDTOS=null;
                if (newList!=null){
                    groupInfoDTOS=new ArrayList<>();
                    for (EntGroupInfo groupInfo:newList){
                        EntGroupInfoDTO dto=new EntGroupInfoDTO();
                        BeanUtils.copyProperties(groupInfo,dto);
                        groupInfoDTOS.add(dto);
                    }
                    log.info("groupInfoDTOS--->size:{}",groupInfoDTOS.size());
                    boolean b = synDataFeignController.syncEntGroup(groupInfoDTOS);
                    if (b){
                        result+=groupInfoDTOS.size();
                    }
                }
            }
        }
        return result;
    }

    @Override
    public Integer manager() {
        Integer result=0;
        List<ManagerInfo> managerInfoList = managerInfoDao.findAll();
        if (managerInfoList!=null){
            int listSize = managerInfoList.size();
            int toIndex = 100;
            for (int i = 0; i<managerInfoList.size(); i+=100) {
                if (i+100 > listSize){
                    toIndex = listSize - i;
                }
                List<ManagerInfo> newList = managerInfoList.subList(i, i+toIndex);
                List<ManagerInfoDTO> managerInfoDTOS=null;
                if (newList!=null){
                    managerInfoDTOS=new ArrayList<>();
                    for (ManagerInfo managerInfo:newList){
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
            }
        }
        return result;
    }


}
