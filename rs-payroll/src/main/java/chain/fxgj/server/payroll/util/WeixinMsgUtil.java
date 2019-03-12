package chain.fxgj.server.payroll.util;

import chain.fxgj.core.common.dto.weixin.msg.WeixinTextMsgBaseDTO;
import chain.fxgj.core.common.util.MsgUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author chain
 * create by chain on 2018/8/9 下午3:05
 **/
public class WeixinMsgUtil {
    private static final Logger log = LoggerFactory.getLogger(chain.fxgj.core.common.util.WeixinMsgUtil.class);

    static String[][] menu;

    static {
        menu = new String[][]{
                {"如何查看工资条内容？", "您好，点击菜单栏【我的工资条】，在【我的收入】里即可查看您的工资条内容。\n(如若没有过往工资条内容，说明您所在企业暂时未使用放薪管家推送工资条)"},
                {"首次登录用户如何认证身份信息？认证时输入身份证后报未维护？", "您好，首次登录的用户请点击蓝字进行身份认证(<a href=\"${oauth_url}\">跳转身份认证页面</a>)，认证后才能查看工资条内容。\n输入身份证后报未维护原因如下：\n①您所在企业没有使用放薪管家平台代发工资；\n②企业没有维护您的信息；\n③维护的信息有误，具体详情请咨询企业相关人员。"},
                {"如何修改手机号/银行卡？", "您好，请点击菜单栏【我的工资条】，在【个人中心】里的【手机号】/【银行卡】进行修改。\n(修改银行卡需要企业进行确认生效)"},
                {"接收不到短信验证码？", "您好，无法接收验证码，请勿多次重复点击获取，以免触发短信防重发机制，请将您的手机号码留言到微信公众号后台或可致电客服400-882-1081寻求帮助！"},
                {"对工资/工资条明细有疑问？", "您好，放薪管家平台是将企业制作的工资条和明细展示出来，如若金额、明细等有误，具体详情请直接联系企业相关人员。"},
                {"更换/解绑微信号？", "您好，如若要更换或解绑微信号，请将您的手机号发送到微信公众号后台，我们将第一时间为您处理。"}
        };
    }

    public static String processRequest(WeixinTextMsgBaseDTO textMessage, String Content, Map<String, String> wxMap, String Event, String MsgType) throws Exception {
        if (Content.contains("豆")) {
            textMessage.setContent(processParams(wxMap, menuSelect(Integer.valueOf(6))));
            return MsgUtil.messageToXml(textMessage);
        } else if (StringUtils.isNumeric(Content)) {
            Integer index = Integer.valueOf(Content.toString());
            if (index > 0 && index <= menu.length) {
                textMessage.setContent(processParams(wxMap, menuSelect(Integer.valueOf(Content))));
                return MsgUtil.messageToXml(textMessage);
            }
        }

        if (MsgType.equals(MsgUtil.REQ_MESSAGE_TYPE_EVENT)) {
            // 事件类型
            String eventType = Event;
            // 关注
            if (eventType.equals(MsgUtil.EVENT_TYPE_SUBSCRIBE)) {
                String message = subscribe();
                textMessage.setContent(message.replace("${oauth_url}", wxMap.get("oauth_url").toString()));

                return MsgUtil.messageToXml(textMessage);
            }
            // 取消关注
            else if (eventType.equals(MsgUtil.EVENT_TYPE_UNSUBSCRIBE)) {
                // TODO 取消订阅后用户不会再收到公众账号发送的消息，因此不需要回复
            }
        }
        textMessage.setContent(processParams(wxMap, defaultMsg()));
        return MsgUtil.messageToXml(textMessage);
    }

    static String subscribe() {
        StringBuffer msg = new StringBuffer("Hello,终于等到你~\n");
        msg.append("欢迎来到您贴心的薪资管家。\n");
        msg.append("查看工资明细、接收公司福利、理财规划专属客户经理，尽在放薪管家。\n");
        msg.append("<a href=\"${oauth_url}\">立即完成身份认证查看工资</a>");
        msg.append("如有疑问欢迎拨打客服热线 400-882-1081咨询或回复【客服】 \n");
        return msg.toString();
    }

    static String menuSelect(Integer index) {
        return menu[index - 1][1];
    }

    static String defaultMsg() {
        StringBuffer msg = new StringBuffer("您好，欢迎来到放薪管家，请回复序号查看服务：\n");
        for (int i = 0; i < menu.length; i++) {
            String[] config = menu[i];
            msg.append("[" + (i + 1) + "]" + config[0]);
            msg.append("\n");
        }
        msg.append("问题未解决？请您留言给我，我会尽快回复哦！\n");
        msg.append("如比较着急，也可以拨打客服热线：400-882-1081。\n");
        return msg.toString();
    }

    static String processParams(Map<String, String> wxMap, String msg) {
        return msg.replace("${oauth_url}", wxMap.get("oauth_url").toString());
        //.replace("${appid}", wxMap.get("appid").toString())
        //.replace("${mpid}", wxMap.get("mpid").toString());
    }
}
