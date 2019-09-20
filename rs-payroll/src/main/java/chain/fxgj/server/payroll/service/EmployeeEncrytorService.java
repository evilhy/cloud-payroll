package chain.fxgj.server.payroll.service;

/**
 * @author chain
 * create by chain on 2018/8/31 下午9:38
 **/
public interface EmployeeEncrytorService {

    String encryptEmployeeId(String employeeId);

    String decryptEmployeeId(String employeeSid);

    String encryptCardNo(String cardNo);

    String decryptCardNo(String cardNo);

    String encryptIdNumber(String idNumber);

    String decryptIdNumber(String idNumber);

    String encryptPhone(String phone);

    String decryptPhone(String phone);

    String encryptCustName(String custName);

    String decryptCustName(String custName);

    String encryptContent(String content);

    String decryptContent(String content);

    String encryptPwd(String pwd);

    String decryptPwd(String pwd);


}
