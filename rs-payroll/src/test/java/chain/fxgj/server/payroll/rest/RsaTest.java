package chain.fxgj.server.payroll.rest;

import chain.fxgj.server.payroll.util.RSAEncrypt;
import chain.fxgj.server.payroll.util.RSAUtils;
import org.apache.commons.codec.binary.Base64;
//import com.itextpdf.xmp.impl.Base64;
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
        //
        String accessUrl = "https://soft.thinkive.com:720/khv4/h5/open/views/account/index.html?sign_channel=wjhxym&content=";


        // #rsa 公钥 -> 平台自已
        String rsaPublicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCGYHGGPlZvE4DE7ExTBMDNwJlDKXBiQYaprvxGZ+rf7YqJhxO08UnecTHKpPdA0KGe6vMwgT58AN3Cj1WsytIQ6Y2ybiqSwlpjlFQaNb3jiiE4gnSMkMvxxzRaHQ+Y10Qtfil47wqVq2TCKMMWrgSfMNINoTbSEp10FFbhbVrxpQIDAQAB";
        // rsa 私钥 -> 平台自已
        String rsaPrivateKey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAIZgcYY+Vm8TgMTsTFMEwM3AmUMpcGJBhqmu/EZn6t/tiomHE7TxSd5xMcqk90DQoZ7q8zCBPnwA3cKPVazK0hDpjbJuKpLCWmOUVBo1veOKITiCdIyQy/HHNFodD5jXRC1+KXjvCpWrZMIowxauBJ8w0g2hNtISnXQUVuFtWvGlAgMBAAECgYAjwCzz5kngq3Oq8KMtwyn4k7Ey6Sd5PK2zH1cG9EbM5Mni5QkdLsTUZZE1tMYDfH5DZYbl9LzHCQP262OD4UIZz2mhotBzT6UaZPhdMNsHzNojIwQa+syHTBgFMe39AuDyyes+0pG4rAlolPDMuywgb5yIK+1eMvPiW8nXZvg9AQJBAPOInM/ybSt7iqzL0Am9GOsOsvBXAlKxzUvEAdPi7YcFn5pmHTVQeymeDj+qlMYm56lB14UcTakEdwL/5SYLRXUCQQCNQWJAwDDdKcFw6vtH5WoDh4KeDGupLCQ89g1RpLoZtwq0oe46VexC59EfhG1Kz9zTi2YVrfRnc+lH5WpTLgVxAkEAqENOnXrRpQaB5SwY/HGT4uzQA7EKYNqKjvvJi32yQeVHxiUhrzGBN1sGW0Tf8Bz3WQGuCEFrAwmbtQ3bZLLK9QJAXU6yc2lBHebGNCvUfyKJC/nIi1RTDbXt3iL+m06/69qghL9umTRG089DsZkNhNyX11l+vpVhG7FSiL5/pKCC0QJAMHErhJK0pHG1e64bVbMQNhTPBPlt7WwMMkNU7iewNglPAwUzzeTHbf0wAHfHSo2vpz4BKrKRU6F2HxMOyzaTnQ==df723820";

        Content cont = new Content();
        cont.setcManagerId("12345678901234567890123456789012");
        cont.setcManagerName("zhouhao");
        cont.setInvitId("12345678901234567890123456789012");
        cont.setInvitName("小芳");
        cont.setInvitPhone("15123563214");
        String content = JSONObject.fromObject(cont).toString();
        System.out.println("content: " + content);


        byte[] rsaEncrypt = RSAUtils.encryptByPublicKey(content.getBytes(), rsaPublicKey);
        System.out.println("rsaEncrypt: " + rsaEncrypt);

        Base64 base64 = new Base64();
        byte[] strBase64Encode = base64.encode(rsaEncrypt);

        // Base64编码
        //String strBase64Encode = base64.encodeToString(content.getBytes("UTF-8"));
//        String strBase64Encode = new String(base64.encode(content.getBytes()));

        //Base64Converter.encode(content);
        // RSA加密
        //String rsaEncrypt = RSAEncrypt.encrypt(strBase64Encode, rsaPublicKey);

        String url = new String(strBase64Encode);
        url = accessUrl  + url;
        System.out.println("url: " + url);

        String subUrl = url.replace(accessUrl,"");
        System.out.println("subUrl: " + subUrl);
        System.out.println("subUrl.getBytes(): " + subUrl.getBytes());

        // Base64解码
        String strBase64Decode = new String(base64.decode(subUrl.getBytes()));

        // RSA解密
        // byte[] rsaPublicKeyDecode = base64.decodeBase64(rsaPublicKey);
        // byte[] rsaDecrypt = RSAUtils.decryptByPrivateKey(rsaEncrypt,rsaPrivateKey);
        byte[] rsaDecrypt = RSAUtils.decryptByPrivateKey(strBase64Decode.getBytes(),rsaPrivateKey);
        String strBase64 = new String(rsaDecrypt,"UTF-8");

        //String strBase64Decode =new String(base64.decode(rsaDecrypt.getBytes("UTF-8")), "UTF-8");
        //String strBase64Decode =new String(base64.decode(strBase64Encode.getBytes()));
        System.out.println("strBase64: " + strBase64);


    }
}
