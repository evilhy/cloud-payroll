package chain.fxgj.server.payroll.service;

import chain.fxgj.core.common.dto.msg.MsgModelInfoDTO;
import chain.fxgj.core.jpa.model.MsgModelInfo;

public interface MsgModelInfoService {

    MsgModelInfoDTO findByMsgModelInfo(MsgModelInfoDTO msgModelInfoDTO);

    MsgModelInfo saveMsgModelInfo(MsgModelInfo msgModelInfo);


}
