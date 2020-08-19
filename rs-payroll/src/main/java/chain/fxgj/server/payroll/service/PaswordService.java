package chain.fxgj.server.payroll.service;

import chain.fxgj.server.payroll.dto.handpassword.HandPasswordDTO;
import chain.ids.core.commons.dto.softkeyboard.KeyboardResponse;
import core.dto.response.wechat.EmployeeWechatDTO;


/**
 * @Description:
 * @Author: du
 * @Date: 2020/8/11 20:48
 */
public interface PaswordService {
    /**
     * 查询用户是否开启手势密码
     *
     * @param wechatId 用户微信绑定ID
     * @return
     */
    HandPasswordDTO queryHandPassword(String wechatId);

    /**
     * 校验密码
     *
     * @param wechatId 用户微信绑定ID
     * @param password 密码
     * @param type     密码类型 0：数字密码  1：手势密码
     * @return
     */
    EmployeeWechatDTO checkPassword(String wechatId, String password, String type);

    /**
     * 添加密码
     *
     * @param wechatId 用户微信绑定ID
     * @param password 密码
     * @param type     密码类型 0：数字密码  1：手势密码
     * @return
     */
    EmployeeWechatDTO savePassword(String wechatId, String password, String type);

    /**
     * 关闭手势密码
     *
     * @param wechatId 用户微信绑定ID
     * @return
     */
    EmployeeWechatDTO closeHandPassword(String wechatId);

    /**
     * 数字密码键盘生成
     *
     * @param keyboardId
     * @return
     */
    KeyboardResponse crateNumericKeypad(String keyboardId);

    /**
     * 数字键盘密码解密
     *
     * @param passsword
     * @param wechatId
     * @return
     */
    String checkNumberPassword(String passsword, String wechatId);
}
