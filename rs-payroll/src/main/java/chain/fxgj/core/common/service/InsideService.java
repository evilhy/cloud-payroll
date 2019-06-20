package chain.fxgj.core.common.service;

import chain.fxgj.core.common.constant.DictEnums.AppPartnerEnum;
import chain.fxgj.server.payroll.dto.request.ReadWageDTO;
import chain.fxgj.server.payroll.dto.request.ResReceiptDTO;
import chain.fxgj.server.payroll.dto.request.UpdBankCardDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface InsideService {

    /**
     * 回执确认
     *
     * @param resReceiptDTO
     */
    void recepitConfirm(ResReceiptDTO resReceiptDTO);

    /**
     * 绑定微信
     */
    void bandWechat(String openId, String idNumber, String phone,AppPartnerEnum appPartner);

    /**
     * 绑定微信,更新手机号
     */
    void bandWechatAndPhone(String openId, String idNumber, String phone, String pwd,AppPartnerEnum appPartner);

    /**
     * 已读
     */
    void readWage(ReadWageDTO readWageDTO);

    void login(String openId, String jsessionId, String nickname, String headimgurl,String id);

    void setPwd(String wechatId, String pwd);

    /**
     * 修改手机号
     */
    void updPhone(String wechatId, String idNumber, String phone);

    /**
     * 修改银行卡
     */
    void updBankCard(UpdBankCardDTO updBankCardDTO);

    /**
     * 修改银行卡已读
     *
     * @param logIds
     */
    void bankCardIsNew(List<String> logIds);


}
