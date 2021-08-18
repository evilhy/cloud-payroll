package chain.fxgj.server.payroll.util;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Title: SensitiveInfoUtils.java
 * @Copyright: Copyright (c) 2016
 * @Description: <br>
 * 敏感信息屏蔽工具<br>
 */
public final class SensitiveInfoUtils {
    protected final static Logger log = LoggerFactory.getLogger(SensitiveInfoUtils.class);

    public static void main(String[] args) {

        //[中文姓名]
        System.out.println(SensitiveInfoUtils.chineseName("刘松"));  //刘*
        System.out.println(SensitiveInfoUtils.chineseName("刘德华")); //刘**

        System.out.println(SensitiveInfoUtils.chineseNameLeft("刘松")); //刘*
        System.out.println(SensitiveInfoUtils.chineseNameLeft("刘德华"));//刘**

        System.out.println(SensitiveInfoUtils.chineseNameRight("刘松")); //*松
        System.out.println(SensitiveInfoUtils.chineseNameRight("刘德华"));//**华

        //[身份证号] 显示最后四位
        System.out.println(SensitiveInfoUtils.idCardNum("420625199810100050"));//**************0050
        System.out.println(SensitiveInfoUtils.idCardNumDefined("420625199810100050"));//420***********0050
        System.out.println(SensitiveInfoUtils.idCardNumDefinedPrefix("420625199810100050",3));//420***********0050
        System.out.println(SensitiveInfoUtils.idCardNumDefinedPrefix("420625199810100050",6));//420625********0050

        //[固定电话] 后四位
        System.out.println(SensitiveInfoUtils.fixedPhone("02788888888"));//*******8888

        //[手机号码]
        System.out.println(SensitiveInfoUtils.mobilePhone("15802721921"));//*******1921
        System.out.println(SensitiveInfoUtils.mobilePhonePrefix("15802721921"));//158****1921

        //[地址] 只显示到地区
        System.out.println(SensitiveInfoUtils.address("北京朝阳区酒仙桥中路26号院4号楼人人大厦",8));//北京朝阳区酒仙桥中路26号********

        //[电子邮箱] 邮箱前缀仅显示第一个字母
        System.out.println(SensitiveInfoUtils.email("503965137@qq.com"));//5********@qq.com

        //[银行卡号] 前六位，后四位，其他用星号隐藏每位1个星号<例子:6222600**********1234>
        System.out.println(SensitiveInfoUtils.bankCard("6228480402565890018"));//622848*********0018
        System.out.println(SensitiveInfoUtils.bankCardSuffix("6228480402565890018"));//***************0018

        //[公司开户银行联号]
        System.out.println(SensitiveInfoUtils.cnapsCode("102100029679"));//10**********



    }


    /**
     * [中文姓名] 只显示第一个汉字，其他隐藏为2个星号<例子：李**>
     *
     * @param fullName
     * @return
     */
    public static String chineseName(String fullName) {
        return chineseNameLeft(fullName);
    }


    /**
     * [中文姓名] 只显示第一个汉字，其他隐藏为2个星号<例子：李**>
     *
     * @param fullName
     * @return
     */
    public static String chineseNameLeft(String fullName) {
        if (StringUtils.isBlank(fullName)) {
            return "";
        }
        String name = StringUtils.left(fullName, 1);
        return StringUtils.rightPad(name, fullName.length(), "*");
    }

    /**
     * [中文姓名] 只显示第一个汉字，其他隐藏为2个星号<例子：**松>
     *
     * @param fullName
     * @return
     */
    public static String chineseNameRight(String fullName) {
        if (StringUtils.isBlank(fullName)) {
            return "";
        }
        String name = StringUtils.right(fullName, 1);
        return StringUtils.leftPad(name, fullName.length(), "*");
    }


    /**
     * [中文姓名] 只显示第一个汉字，其他隐藏为2个星号<例子：李**>
     *
     * @param familyName
     * @param givenName
     * @return
     */
    public static String chineseName(String familyName, String givenName) {
        if (StringUtils.isBlank(familyName) || StringUtils.isBlank(givenName)) {
            return "";
        }
        return chineseName(familyName + givenName);
    }

    /**
     * [身份证号] 显示最后四位，其他隐藏。共计18位或者15位。<例子：*************5762>
     *
     * @param id
     * @return
     */
    public static String idCardNum(String id) {
        if (StringUtils.isBlank(id)) {
            return "";
        }
        String num = StringUtils.right(id, 4);
        return StringUtils.leftPad(num, id.length(), "*");
    }

    /**
     * [身份证号] 显示前三位，最后四位，其他隐藏。共计18位或者15位。<例子：420**********5762>
     *
     * @param id
     * @return
     */
    public static String idCardNumDefined(String id) {
        int prefix = 3;
        if (StringUtils.isBlank(id)) {
            return "";
        }
        String idPrefix = id.substring(0, prefix);
        id=id.substring(prefix);
        String num = StringUtils.right(id, 4);
        return idPrefix.concat(StringUtils.leftPad(num, id.length(), "*"));
    }

    /**
     * [身份证号] 显示前N位，最后四位，其他隐藏。共计18位或者15位。<例子：420**********5762>
     *
     * @param id
     * @return
     */
    public static String idCardNumDefinedPrefix(String id,int prefix) {
        if (StringUtils.isBlank(id)) {
            return "";
        }
        String idPrefix = id.substring(0, prefix);
        id=id.substring(prefix);
        String num = StringUtils.right(id, 4);
        return idPrefix.concat(StringUtils.leftPad(num, id.length(), "*"));
    }


    /**
     * [固定电话] 后四位，其他隐藏<例子：****1234>
     *
     * @param num
     * @return
     */
    public static String fixedPhone(String num) {
        if (StringUtils.isBlank(num)) {
            return "";
        }
        return StringUtils.leftPad(StringUtils.right(num, 4), num.length(), "*");
    }

    /**
     * [手机号码] 后四位，其他隐藏<例子:*********1234>
     *
     * @param num
     * @return
     */
    public static String mobilePhone(String num) {
        if (StringUtils.isBlank(num)) {
            return "";
        }
        String name = StringUtils.right(num, 4);
        return StringUtils.leftPad(name, num.length(), "*");      }

    /**
     * [手机号码] 前三位，后四位，其他隐藏<例子:138******1234>
     *
     * @param num
     * @return
     */
    public static String mobilePhonePrefix(String num) {
        if (StringUtils.isBlank(num)) {
            return "";
        }
        return StringUtils.left(num, 3).concat(StringUtils.removeStart(StringUtils.leftPad(StringUtils.right(num, 4), num.length(), "*"), "***"));
    }





    /**
     * [地址] 只显示到地区，不显示详细地址；我们要对个人信息增强保护<例子：北京市海淀区****>
     *
     * @param address
     * @param sensitiveSize
     *            敏感信息长度
     * @return
     */
    public static String address(String address, int sensitiveSize) {
        if (StringUtils.isBlank(address)) {
            return "";
        }
        int length = address.length();
        return StringUtils.rightPad(StringUtils.left(address, length - sensitiveSize), length, "*");
    }


    /**
     * [电子邮箱] 邮箱前缀仅显示第一个字母，前缀其他隐藏，用星号代替，@及后面的地址显示<例子:g**@163.com>
     *
     * @param email
     * @return
     */
    public static String email(String email) {
        if (StringUtils.isBlank(email)) {
            return "";
        }
        int index = StringUtils.indexOf(email, "@");
        if (index <= 1)
            return email;
        else
            return StringUtils.rightPad(StringUtils.left(email, 1), index, "*").concat(StringUtils.mid(email, index, email.length()));
    }

    /**
     * [银行卡号] 前六位，后四位，其他用星号隐藏每位1个星号<例子:6222600**********1234>
     *
     * @param cardNum
     * @return
     */
    public static String bankCard(String cardNum) {
        if (StringUtils.isBlank(cardNum)) {
            return "";
        }
        return StringUtils.left(cardNum, 6).concat(StringUtils.removeStart(StringUtils.leftPad(StringUtils.right(cardNum, 4), cardNum.length(), "*"), "******"));
    }

    /**
     * [银行卡号] 后四位，其他用星号隐藏每位1个星号<例子:6222600**********1234>
     *
     * @param cardNum
     * @return
     */
    public static String bankCardSuffix(String cardNum) {

        if (StringUtils.isBlank(cardNum)) {
            return "";
        }
        String name = StringUtils.right(cardNum, 4);
        return StringUtils.leftPad(name, cardNum.length(), "*");

    }


    /**
     * [公司开户银行联号] 公司开户银行联行号,显示前两位，其他用星号隐藏，每位1个星号<例子:12********>
     *
     * @param code
     * @return
     */
    public static String cnapsCode(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        return StringUtils.rightPad(StringUtils.left(code, 2), code.length(), "*");
    }

    /**
     * 获取脱敏json串 <注意：递归引用会导致java.lang.StackOverflowError>
     *
     * @param javaBean
     * @return
     */
//    public static String getJson(Object javaBean) {
//        String json = null;
//        if (null != javaBean) {
//            Class<? extends Object> raw = javaBean.getClass();
//            try {
//                if (raw.isInterface())
//                    return json;
//                Gson g = new Gson();
//                Object clone = g.fromJson(g.toJson(javaBean, javaBean.getClass()), javaBean.getClass());
//                Set<Integer> referenceCounter = new HashSet<Integer>();
//                SensitiveInfoUtils.replace(SensitiveInfoUtils.findAllField(raw), clone, referenceCounter);
//                json = JSON.toJSONString(clone, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteNullListAsEmpty);
//                referenceCounter.clear();
//                referenceCounter = null;
//            } catch (Throwable e) {
//                logger.error("SensitiveInfoUtils.getJson() ERROR", e);
//            }
//        }
//        return json;
//    }

//    private static Field[] findAllField(Class<?> clazz) {
//        Field[] fileds = clazz.getDeclaredFields();
//        while (null != clazz.getSuperclass() && !Object.class.equals(clazz.getSuperclass())) {
//            fileds = (Field[]) ArrayUtils.addAll(fileds, clazz.getSuperclass().getDeclaredFields());
//            clazz = clazz.getSuperclass();
//        }
//        return fileds;
//    }


//    private static void replace(Field[] fields, Object javaBean, Set<Integer> referenceCounter) throws IllegalArgumentException, IllegalAccessException {
//        if (null != fields && fields.length > 0) {
//            for (Field field : fields) {
//                field.setAccessible(true);
//                if (null != field && null != javaBean) {
//                    Object value = field.get(javaBean);
//                    if (null != value) {
//                        Class<?> type = value.getClass();
//                        // 1.处理子属性，包括集合中的
//                        if (type.isArray()) {
//                            int len = Array.getLength(value);
//                            for (int i = 0; i < len; i++) {
//                                Object arrayObject = Array.get(value, i);
//                                SensitiveInfoUtils.replace(SensitiveInfoUtils.findAllField(arrayObject.getClass()), arrayObject, referenceCounter);
//                            }
//                        } else if (value instanceof Collection<?>) {
//                            Collection<?> c = (Collection<?>) value;
//                            Iterator<?> it = c.iterator();
//                            while (it.hasNext()) {
//                                Object collectionObj = it.next();
//                                SensitiveInfoUtils.replace(SensitiveInfoUtils.findAllField(collectionObj.getClass()), collectionObj, referenceCounter);
//                            }
//                        } else if (value instanceof Map<?, ?>) {
//                            Map<?, ?> m = (Map<?, ?>) value;
//                            Set<?> set = m.entrySet();
//                            for (Object o : set) {
//                                Entry<?, ?> entry = (Entry<?, ?>) o;
//                                Object mapVal = entry.getValue();
//                                SensitiveInfoUtils.replace(SensitiveInfoUtils.findAllField(mapVal.getClass()), mapVal, referenceCounter);
//                            }
//                        } else if (!type.isPrimitive()
//                                   && !StringUtils.startsWith(type.getPackage().getName(), "javax.")
//                                   && !StringUtils.startsWith(type.getPackage().getName(), "java.")
//                                   && !StringUtils.startsWith(field.getType().getName(), "javax.")
//                                   && !StringUtils.startsWith(field.getName(), "java.")
//                                   && referenceCounter.add(value.hashCode())) {
//                            SensitiveInfoUtils.replace(SensitiveInfoUtils.findAllField(type), value, referenceCounter);
//                        }
//                    }
//                    // 2. 处理自身的属性
//                    SensitiveInfoUtils annotation = field.getAnnotation(SensitiveInfoUtils.class);
//                    if (field.getType().equals(String.class) && null != annotation) {
//                        String valueStr = (String) value;
//                        if (StringUtils.isNotBlank(valueStr)) {
//                            switch (annotation.type()) {
//                                case CHINESE_NAME: {
//                                    field.set(javaBean, SensitiveInfoUtils.chineseName(valueStr));
//                                    break;
//                                }
//                                case ID_CARD: {
//                                    field.set(javaBean, SensitiveInfoUtils.idCardNum(valueStr));
//                                    break;
//                                }
//                                case FIXED_PHONE: {
//                                    field.set(javaBean, SensitiveInfoUtils.fixedPhone(valueStr));
//                                    break;
//                                }
//                                case MOBILE_PHONE: {
//                                    field.set(javaBean, SensitiveInfoUtils.mobilePhone(valueStr));
//                                    break;
//                                }
//                                case ADDRESS: {
//                                    field.set(javaBean, SensitiveInfoUtils.address(valueStr, 4));
//                                    break;
//                                }
//                                case EMAIL: {
//                                    field.set(javaBean, SensitiveInfoUtils.email(valueStr));
//                                    break;
//                                }
//                                case BANK_CARD: {
//                                    field.set(javaBean, SensitiveInfoUtils.bankCard(valueStr));
//                                    break;
//                                }
//                                case CNAPS_CODE: {
//                                    field.set(javaBean, SensitiveInfoUtils.cnapsCode(valueStr));
//                                    break;
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }

//----------------------------------------------------------------------------------------------
//    public static Method [] findAllMethod(Class<?> clazz){
//        Method [] methods= clazz.getMethods();
//        return methods;
//    }

    //----------------------------------------------------------------------------------------------
//    public static enum SensitiveType {
//        /**
//         * 中文名
//         */
//        CHINESE_NAME,
//
//        /**
//         * 身份证号
//         */
//        ID_CARD,
//        /**
//         * 座机号
//         */
//        FIXED_PHONE,
//        /**
//         * 手机号
//         */
//        MOBILE_PHONE,
//        /**
//         * 地址
//         */
//        ADDRESS,
//        /**
//         * 电子邮件
//         */
//        EMAIL,
//        /**
//         * 银行卡
//         */
//        BANK_CARD,
//        /**
//         * 公司开户银行联号
//         */
//        CNAPS_CODE;
//    }
}