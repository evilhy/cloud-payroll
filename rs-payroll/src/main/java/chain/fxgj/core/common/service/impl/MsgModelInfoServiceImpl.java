package chain.fxgj.core.common.service.impl;

import chain.fxgj.core.common.dto.msg.MsgModelInfoDTO;
import chain.fxgj.core.common.service.MsgModelInfoService;
import chain.fxgj.core.jpa.dao.MsgModelInfoDao;
import chain.fxgj.core.jpa.model.MsgModelInfo;
import chain.fxgj.core.jpa.model.QMsgModelInfo;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MsgModelInfoServiceImpl implements MsgModelInfoService {

    @Autowired
    MsgModelInfoDao msgModelInfoDao;

    @Override
    public MsgModelInfoDTO findByMsgModelInfo(MsgModelInfoDTO msgModelInfoDTO) {
        MsgModelInfo msgModelInfo = msgModelInfoDTO.createsgModelInfo();

        QMsgModelInfo qMsgModelInfo = QMsgModelInfo.msgModelInfo;

        Predicate predicate = qMsgModelInfo.systemId.eq(msgModelInfo.getSystemId());

        predicate = ExpressionUtils.and(predicate, qMsgModelInfo.checkType.eq(msgModelInfo.getCheckType()));
        predicate = ExpressionUtils.and(predicate, qMsgModelInfo.busiType.eq(msgModelInfo.getBusiType()));
        msgModelInfo = msgModelInfoDao.selectFrom(qMsgModelInfo).where(predicate).fetchFirst();
        return new MsgModelInfoDTO(msgModelInfo);
    }

    @Override
    public MsgModelInfo saveMsgModelInfo(MsgModelInfo msgModelInfo) {

        return msgModelInfoDao.save(msgModelInfo);
    }


}
