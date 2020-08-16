package chain.fxgj.server.payroll.service.impl;

import chain.css.exception.ServiceHandleException;
import chain.fxgj.server.payroll.constant.ErrorConstant;
import chain.fxgj.server.payroll.service.EmployeeEncrytorService;
import chain.utils.commons.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.salt.RandomSaltGenerator;
import org.jasypt.salt.StringFixedSaltGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author chain
 * create by chain on 2018/8/31 下午9:42
 **/
@Component
@Slf4j
public class EmployeeEncrytorServiceImpl implements EmployeeEncrytorService {

    private PooledPBEStringEncryptor encryptorId = null;
    private PooledPBEStringEncryptor encryptorNo = null;

    @Value("${encrypt.salt:qwejoifgjkldfkgo}")
    private String salt;
    @Value("${encrypt.passwd:dfgklj213rk4l3o40ifgfdlmg24kmdfg}")
    private String passwd;
    @Value("${encrypt.poolSize:10}")
    private Integer poolSize = 10;

    private void initEncrypt() {
        log.info("====>passwd={}",passwd);
        log.info("====>salt={}",salt);

        if (encryptorId != null) {
            return;
        }
        log.info("==>salt={}",salt);
        log.info("==>passwd={}",passwd);
        encryptorId = new PooledPBEStringEncryptor();
        encryptorId.setSaltGenerator(new StringFixedSaltGenerator(salt));
        encryptorId.setPoolSize(poolSize);
        encryptorId.setPassword(passwd);
        encryptorNo = new PooledPBEStringEncryptor();
        encryptorNo.setPassword(passwd);
        encryptorNo.setPoolSize(poolSize);
        encryptorNo.setSaltGenerator(new RandomSaltGenerator());
    }

    @Override
    public String encryptEmployeeId(String employeeId) {
        try {
            initEncrypt();
            if (StringUtils.isEmpty(employeeId)) {
                return employeeId;
            }
            return encryptorId.encrypt(employeeId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("数据加密失败！"));
        }
    }

    @Override
    public String decryptEmployeeId(String employeeSid) {
        try {
            initEncrypt();
            if (StringUtils.isEmpty(employeeSid)) {
                return employeeSid;
            }
            return encryptorId.decrypt(employeeSid);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("数据解密失败！"));
        }
    }

    @Override
    public String encryptCardNo(String cardNo) {
        try {
            initEncrypt();
            if (StringUtils.isEmpty(cardNo)) {
                return cardNo;
            }
            return encryptorNo.encrypt(cardNo);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("数据加密失败！"));
        }
    }

    @Override
    public String decryptCardNo(String cardNo) {
        try {
            initEncrypt();
            if (StringUtils.isEmpty(cardNo)) {
                return cardNo;
            }
            return encryptorNo.decrypt(cardNo);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("数据解密失败！"));
        }
    }

    @Override
    public String encryptIdNumber(String idNumber) {
        try {
            initEncrypt();
            if (StringUtils.isEmpty(idNumber)) {
                return idNumber;
            }
            return encryptorId.encrypt(idNumber);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("数据加密失败！"));
        }
    }

    @Override
    public String decryptIdNumber(String idNumber) {
        try {
            initEncrypt();
            if (StringUtils.isEmpty(idNumber)) {
                return idNumber;
            }
            return encryptorId.decrypt(idNumber);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("数据解密失败！"));
        }
    }

    @Override
    public String encryptPhone(String phone) {
        try {
            initEncrypt();
            if (StringUtils.isEmpty(phone)) {
                return phone;
            }
            return encryptorNo.encrypt(phone);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("数据加密失败！"));
        }
    }

    @Override
    public String decryptPhone(String phone) {
        try {
            initEncrypt();
            if (StringUtils.isEmpty(phone)) {
                return phone;
            }
            return encryptorNo.decrypt(phone);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("数据解密失败！"));
        }
    }

    @Override
    public String encryptCustName(String custName) {
        try {
            initEncrypt();
            if (StringUtils.isEmpty(custName)) {
                return custName;
            }
            return encryptorNo.encrypt(custName);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("数据加密失败！"));
        }
    }

    @Override
    public String decryptCustName(String custName) {
        try {
            initEncrypt();
            if (StringUtils.isEmpty(custName)) {
                return custName;
            }
            return encryptorNo.decrypt(custName);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("数据解密失败！"));
        }
    }

    @Override
    public String encryptContent(String content) {
        try {
            initEncrypt();
            if (StringUtils.isEmpty(content)) {
                return content;
            }
            return encryptorNo.encrypt(content);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("数据加密失败！"));
        }
    }

    @Override
    public String decryptContent(String content) {
        try {
            initEncrypt();
            if (StringUtils.isEmpty(content)) {
                return content;
            }
            return encryptorNo.decrypt(content);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("数据解密失败！"));
        }
    }

    @Override
    public String encryptPwd(String pwd) {
        try {
            initEncrypt();
            if (StringUtils.isEmpty(pwd)) {
                return pwd;
            }
            return encryptorId.encrypt(pwd);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("数据加密失败！"));
        }
    }

    @Override
    public String decryptPwd(String pwd) {
        try {
            initEncrypt();
            if (StringUtils.isEmpty(pwd)) {
                return pwd;
            }
            return encryptorId.decrypt(pwd);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceHandleException(ErrorConstant.SYS_ERROR.format("数据解密失败！"));
        }
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }
}
