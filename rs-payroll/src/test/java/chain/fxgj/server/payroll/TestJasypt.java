package chain.fxgj.server.payroll;

import junitparams.JUnitParamsRunner;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.salt.StringFixedSaltGenerator;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;


//@FixMethodOrder(MethodSorters.JVM)
////@RunWith(SpringRunner.class)
//@RunWith(JUnitParamsRunner.class)
//@SpringBootTest(webEnvironment =
//        SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class TestJasypt {

    private PooledPBEStringEncryptor encryptorId = null;

    private int count = 0;

    //private synchronized void initEncrypt() {
    private void initEncrypt() {
        String salt = "GUPxMCeBWXw3TteCYVikKVXvHkayakWh";
        int poolSize = 10;
        String passwd = "MvEpyqjedb4Hvai3vnFakmYxQsebEVCe84RNcVxw79USzc85FPAmbA9NuPxqfGsMhkayakWh";

        //log.info("====>passwd={}", passwd);
        //log.info("====>salt={}", salt);

        if (encryptorId != null) {
            return;
        }
        //log.info("==>salt={}", salt);
        //log.info("==>passwd={}", passwd);
        encryptorId = new PooledPBEStringEncryptor();

        try {
            if (count > 0) {
                Thread.sleep(1000);

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        encryptorId.setSaltGenerator(new StringFixedSaltGenerator(salt));
        encryptorId.setPoolSize(poolSize);
        encryptorId.setPassword(passwd);
    }


    public String getValue1() {
        initEncrypt();

        String id;
        id = "123456";

        String encrypt = encryptorId.encrypt(id);
        //log.info("=====>1." + encrypt);


        return encrypt;
    }


    //    @Test
//    public void encryptPwd() {
    public static void main(String[] args) {

        String u= null;
        String tranTime = Optional.ofNullable(u).orElse("000000");
    System.out.println("tranTime=>"+tranTime);


//        TestJasypt syncronizedTest = new TestJasypt();
//
//        Runnable runnable = () -> {
//            for (int i = 0; i < 100; i++) {
//                syncronizedTest.setCount(i);
//
//                //nNbQ7ev8rDM= 12
////                if (syncronizedTest.getValue1().length() > 12) {
//                    System.out.println(Thread.currentThread().getName() + ",值：" + syncronizedTest.getValue1());
////
////                }
////                try {
////                    Thread.sleep(1000);
////                } catch (InterruptedException e) {
////                    e.printStackTrace();
////                }
//            }
//        };
//
//        Thread thread1 = new Thread(runnable);
//        thread1.start();
////        Thread thread2 = new Thread(runnable);
////        thread2.start();


//        EmployeeEncrytorServiceImpl im = new EmployeeEncrytorServiceImpl();
//        im.setPasswd("MvEpyqjedb4Hvai3vnFakmYxQsebEVCe84RNcVxw79USzc85FPAmbA9NuPxqfGsMhkayakWh");
//        im.setSalt("GUPxMCeBWXw3TteCYVikKVXvHkayakWh");
//
//        //im.setPasswd("dfgklj213rk4l3o40ifgfdlmg24kmdfg");
//        //im.setSalt("qwejoifgjkldfkgo");
//
//
//
//
////        log.info("=====>" + im.decryptPwd("tyD+asSAHfE="));
////        log.info("=====>" + im.decryptPwd("Kg6MBiEx+zTuqcsaQi/LJQ=="));
//
//
////        log.info("=====>" + im.decryptIdNumber("3MbkdYuwyIANTaual9thi3mJlDAL8/rpmHBY1Qhaxps="));
//
//        //String id = "14022719940524033X";
//        String id = "342201199602027744";
//
//        String encrypt = im.encryptIdNumber(id);
//        log.info("=====>" + encrypt);
//
//        encrypt  ="uFaMyMKRro3EbuVtrRyvAzsmkOqfNcTvduKITnje4ZE=";
//
//        String decrypt = im.decryptIdNumber(encrypt);
//        log.info("=====>decrypt:" + decrypt);
//
//        //encrypt = "p4jCkS6NbZIIE5wpQC21j84M5gPhU9Dy7u0SfrcEeeU=";
//        //encrypt ="2evyIjflzHjgwKPo4pAiOcW2ts55WU827ywHAPa0MLs=";
//        //Q3LBW2aMdF+rRxWj+OvKxh8Mk0k4fvOJ
//        //log.info("=====>" + im.decryptIdNumber(encrypt));


    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
