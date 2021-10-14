package chain.fxgj.server.payroll.rest;

import chain.fxgj.server.payroll.util.RSAUtils;
import org.apache.commons.codec.binary.Base64;
import net.sf.json.JSONObject;

public class RsaTest {
    public static class Content {

        public String cManagerId;
        public String cManagerName;
        public String invitId;
        public String invitName;
        public String invitPhone;

        public String getcManagerId() {
            return cManagerId;
        }

        public void setcManagerId(String cManagerId) {
            this.cManagerId = cManagerId;
        }

        public String getcManagerName() {
            return cManagerName;
        }

        public void setcManagerName(String cManagerName) {
            this.cManagerName = cManagerName;
        }

        public String getInvitId() {
            return invitId;
        }

        public void setInvitId(String invitId) {
            this.invitId = invitId;
        }

        public String getInvitName() {
            return invitName;
        }

        public void setInvitName(String invitName) {
            this.invitName = invitName;
        }

        public String getInvitPhone() {
            return invitPhone;
        }

        public void setInvitPhone(String invitPhone) {
            this.invitPhone = invitPhone;
        }
    }

    public static void main(String[] args) throws Exception {

        //国联证券  跳转url(测试地址)   后面拼上&content=
        String accessUrl = "https://soft.thinkive.com:720/khv4/h5/open/views/account/index.html?sign_channel=wjhxym&content=";

        //放薪管家 rsa公钥
        String rsaPublicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCGYHGGPlZvE4DE7ExTBMDNwJlDKXBiQYaprvxGZ+rf7YqJhxO08UnecTHKpPdA0KGe6vMwgT58AN3Cj1WsytIQ6Y2ybiqSwlpjlFQaNb3jiiE4gnSMkMvxxzRaHQ+Y10Qtfil47wqVq2TCKMMWrgSfMNINoTbSEp10FFbhbVrxpQIDAQAB";
        //放薪管家 rsa私钥  分配给券商
        String rsaPrivateKey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAIZgcYY+Vm8TgMTsTFMEwM3AmUMpcGJBhqmu/EZn6t/tiomHE7TxSd5xMcqk90DQoZ7q8zCBPnwA3cKPVazK0hDpjbJuKpLCWmOUVBo1veOKITiCdIyQy/HHNFodD5jXRC1+KXjvCpWrZMIowxauBJ8w0g2hNtISnXQUVuFtWvGlAgMBAAECgYAjwCzz5kngq3Oq8KMtwyn4k7Ey6Sd5PK2zH1cG9EbM5Mni5QkdLsTUZZE1tMYDfH5DZYbl9LzHCQP262OD4UIZz2mhotBzT6UaZPhdMNsHzNojIwQa+syHTBgFMe39AuDyyes+0pG4rAlolPDMuywgb5yIK+1eMvPiW8nXZvg9AQJBAPOInM/ybSt7iqzL0Am9GOsOsvBXAlKxzUvEAdPi7YcFn5pmHTVQeymeDj+qlMYm56lB14UcTakEdwL/5SYLRXUCQQCNQWJAwDDdKcFw6vtH5WoDh4KeDGupLCQ89g1RpLoZtwq0oe46VexC59EfhG1Kz9zTi2YVrfRnc+lH5WpTLgVxAkEAqENOnXrRpQaB5SwY/HGT4uzQA7EKYNqKjvvJi32yQeVHxiUhrzGBN1sGW0Tf8Bz3WQGuCEFrAwmbtQ3bZLLK9QJAXU6yc2lBHebGNCvUfyKJC/nIi1RTDbXt3iL+m06/69qghL9umTRG089DsZkNhNyX11l+vpVhG7FSiL5/pKCC0QJAMHErhJK0pHG1e64bVbMQNhTPBPlt7WwMMkNU7iewNglPAwUzzeTHbf0wAHfHSo2vpz4BKrKRU6F2HxMOyzaTnQ==df723820";

        Content cont = new Content();
        cont.setcManagerId("12345678901234567890123456789012");
        cont.setcManagerName("zhouhao");
        cont.setInvitId("12345678901234567890123456789012");
        cont.setInvitName("小芳");
        cont.setInvitPhone("15123563214");
        String content = JSONObject.fromObject(cont).toString();
        System.out.println("content: " + content);

        //明文 通过rsa 公钥 加密
        byte[] content_ras_enc = RSAUtils.encryptByPublicKey(content.getBytes(), rsaPublicKey);

        Base64 base64 = new Base64();
        //加密之后转成 base64
        byte[] content_ras_enc_encode = base64.encode(content_ras_enc);
        String content_ras_enc_str = new String(content_ras_enc_encode);

        // 拼接后数据请求发给券商
        content_ras_enc_str = accessUrl  + content_ras_enc_str;
        System.out.println("content_ras_enc_str: " + content_ras_enc_str);
        // 截取加密content片段
        String subUrl = content_ras_enc_str.replace(accessUrl,"");
        System.out.println("subUrl: " + subUrl);
        System.out.println("content_ras_enc_str: " + content_ras_enc_str);

        //base64 解码
        // byte[]  content_ras_decode= base64.decode(content_ras_enc_str);
        byte[]  content_ras_decode= base64.decode(subUrl);

        //base64 解码  --> 私钥 解密
        byte[] content_ras_decrypt = RSAUtils.decryptByPrivateKey(content_ras_decode,rsaPrivateKey);
        System.out.println("rsaDecrypt: " + new String(content_ras_decrypt));
    }
}
