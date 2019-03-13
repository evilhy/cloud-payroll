package chain.fxgj.server.payroll.util;

import chain.fxgj.server.payroll.dto.wechat.WeixinTextMsgBaseDTO;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息处理工具类
 *
 * @author myt
 */
public class MsgUtil {


    //请求消息类型：事件推送
    public static final String REQ_MESSAGE_TYPE_EVENT = "event";

    //事件类型：subscribe(订阅)
    public static final String EVENT_TYPE_SUBSCRIBE = "subscribe";

    //事件类型：unsubscribe(取消订阅)
    public static final String EVENT_TYPE_UNSUBSCRIBE = "unsubscribe";

    /**
     * 扩展xstream，使其支持CDATA块
     *
     * @date
     */
    private static XStream xstream = new XStream(new XppDriver() {
        public HierarchicalStreamWriter createWriter(Writer out) {
            return new PrettyPrintWriter(out) {
                // 对所有xml节点的转换都增加CDATA标记
                boolean cdata = true;

                public void startNode(String name, Class clazz) {
                    super.startNode(name, clazz);
                }

                protected void writeText(QuickWriter writer, String text) {
                    if (cdata) {
                        writer.write("<![CDATA[");
                        writer.write(text);
                        writer.write("]]>");
                    } else {
                        writer.write(text);
                    }
                }
            };
        }
    });

    /**
     * 解析微信发来的请求（XML）
     *
     * @param inputStream
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> parseXml(InputStream inputStream) throws Exception {
        // 将解析结果存储在HashMap中
        Map<String, String> map = new HashMap<String, String>();

        // 从request中取得输入流
        //InputStream inputStream =inputStream;
        // 读取输入流
        SAXReader reader = new SAXReader();
        Document document = reader.read(inputStream);
        // 得到根元素
        Element root = document.getRootElement();
        // 得到根元素的所有子节点
        List<Element> elementList = root.elements();

        // 遍历所有子节点
        for (Element e : elementList)
            map.put(e.getName(), e.getText());

        // 释放资源
        inputStream.close();
        inputStream = null;

        return map;
    }


    /**
     * 文本消息对象转换成xml
     *
     * @param weixinTextMsgBaseDTO 文本消息对象
     * @return xml
     */
    public static String messageToXml(WeixinTextMsgBaseDTO weixinTextMsgBaseDTO) {
        xstream.alias("xml", weixinTextMsgBaseDTO.getClass());
        return xstream.toXML(weixinTextMsgBaseDTO);
    }

//    public static void main(String[] str) {
//        WeixinTextMsgBaseDTO weixinTextMsgBaseDTO = new WeixinTextMsgBaseDTO();
//        weixinTextMsgBaseDTO.setToUserName("开发者微信号");
//        weixinTextMsgBaseDTO.setFromUserName("发送方帐号（一个OpenID）");
//        weixinTextMsgBaseDTO.setCreateTime((new Date()).getTime());
//        weixinTextMsgBaseDTO.setMsgType("text");
//        weixinTextMsgBaseDTO.setMsgId("99999");
//        weixinTextMsgBaseDTO.setContent("文本消息内容");
//        String msg = MsgUtil.messageToXml(weixinTextMsgBaseDTO);
//
//        System.out.println("msg=" + msg);
//
//
//    }


}