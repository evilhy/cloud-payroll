package chain.fxgj.server.payroll;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableCaching
@EnableDiscoveryClient
//@EnableFeignClients({"chain"})
@EnableMongoRepositories(basePackages = "chain.fxgj.server.payroll.mongodb")
@EnableMongoAuditing
@SpringBootApplication(scanBasePackages = {"chain"})
public class PayrollApplication {

    public static void main(String[] args) {
        SpringApplication.run(PayrollApplication.class, args);
    }
}
