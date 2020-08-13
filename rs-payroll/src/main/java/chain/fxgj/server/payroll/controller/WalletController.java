package chain.fxgj.server.payroll.controller;

import chain.fxgj.server.payroll.web.UserPrincipal;
import chain.fxgj.server.payroll.web.WebContext;
import chain.payroll.client.feign.WalletFeignController;
import core.dto.request.BaseReqDTO;
import core.dto.response.wallet.EmpCardResDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 放薪钱包
 */
@RestController
@RequestMapping("/wallet")
@Slf4j
public class WalletController {

    @Autowired
    WalletFeignController walletFeignController;

    /**
     * 员工银行卡</p>
     *      查询当前企业下的银行卡数，去重
     *
     * @param baseReqDTO
     * @return
     */
    @PostMapping("/empCardList")
    public EmpCardResDTO empCardList(@RequestBody BaseReqDTO baseReqDTO){
        UserPrincipal userPrincipal = WebContext.getCurrentUser();
        String idNumber = userPrincipal.getIdNumber();
        String entId = userPrincipal.getEntId();

        EmpCardResDTO empCardResDTO = walletFeignController.empCardList(baseReqDTO);

        return empCardResDTO;
    }

}
