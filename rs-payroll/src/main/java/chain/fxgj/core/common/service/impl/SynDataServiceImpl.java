package chain.fxgj.core.common.service.impl;

import chain.fxgj.core.common.dto.employee.EmployeeCardDTO;
import chain.fxgj.core.common.service.SynDataService;
import chain.fxgj.core.jpa.dao.EmployeeCardInfoDao;
import chain.fxgj.core.jpa.dao.EmployeeInfoDao;
import chain.fxgj.core.jpa.dao.WageDetailInfoDao;
import chain.fxgj.core.jpa.model.*;
import chain.payroll.client.feign.SynDataFeignController;
import chain.payroll.dto.sync.EmployeeCardInfoDTO;
import chain.payroll.dto.sync.EmployeeInfoDTO;
import chain.payroll.dto.sync.WageDetailInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
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
        QWageDetailInfo detailInfo=QWageDetailInfo.wageDetailInfo;
        List<WageDetailInfo> wageDetailList = wageDetailInfoDao.select(detailInfo).fetch();
        SimpleDateFormat sdfYear=new SimpleDateFormat("yyyyy");
        SimpleDateFormat sdfMonth=new SimpleDateFormat("MM");
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
                        dto.setCrtYear(sdfYear.format(detail.getCrtDateTime()));
                        dto.setCrtMonth(sdfMonth.format(detail.getCrtDateTime()));
                        BeanUtils.copyProperties(detail,dto);
                        newDtoList.add(dto);
                    }
                    //调用远程服务进行保存
                    log.info("newDtoList--->",newDtoList.size());
                    synDataFeignController.syncWageDetail("2019",newDtoList);
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
        QEmployeeInfo employeeInfo=QEmployeeInfo.employeeInfo;
        List<EmployeeInfo> employeeInfoListList = employeeInfoDao.select(employeeInfo).fetch();
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
        return null;
    }

    @Override
    public Integer enterprise() {
        return null;
    }

    @Override
    public Integer entgroup() {
        return null;
    }

    @Override
    public Integer manager() {
        return null;
    }


}
