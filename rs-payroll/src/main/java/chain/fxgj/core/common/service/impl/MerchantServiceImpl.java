package chain.fxgj.core.common.service.impl;

import chain.fxgj.core.common.constant.DictEnums.DelStatusEnum;
import chain.fxgj.core.common.service.MerchantService;
import chain.fxgj.core.jpa.dao.EmployeeWechatInfoDao;
import chain.fxgj.core.jpa.model.EmployeeWechatInfo;
import chain.fxgj.core.jpa.model.QEmployeeWechatInfo;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MerchantServiceImpl implements MerchantService {

    @Autowired
    EmployeeWechatInfoDao employeeWechatInfoDao;


    /**
     * 保存工资条绑定信息
     *
     * @param employeeWechatInfo 员工微信信息
     * @return
     */
    @Override
    public EmployeeWechatInfo saveMerchant(EmployeeWechatInfo employeeWechatInfo) {
        EmployeeWechatInfo employeeWechat = employeeWechatInfoDao.save(employeeWechatInfo);
        return employeeWechat;
    }

    /**
     * 工资条绑定信息
     *
     * @param employeeWechatInfo 员工微信信息
     * @return
     */
    @Override
    public EmployeeWechatInfo findMerchant(EmployeeWechatInfo employeeWechatInfo) {
        QEmployeeWechatInfo qEmployeeWechatInfo = QEmployeeWechatInfo.employeeWechatInfo;

        Predicate predicate = qEmployeeWechatInfo.delStatusEnum.eq(DelStatusEnum.normal);
        predicate = ExpressionUtils.and(predicate, qEmployeeWechatInfo.idNumber.eq(employeeWechatInfo.getIdNumber()));
        predicate = ExpressionUtils.and(predicate, qEmployeeWechatInfo.appPartner.eq(employeeWechatInfo.getAppPartner()));

        EmployeeWechatInfo employeeWechat = employeeWechatInfoDao.selectFrom(qEmployeeWechatInfo)
                .where(predicate)
                .fetchOne();

        return employeeWechat;
    }
}
