package chain.fxgj.server.payroll.config.feign;

import feign.Feign;
import okhttp3.ConnectionPool;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * @program: cloud-account
 * @description: 配置okhttp与连接池
 * <p>
 * ConnectionPool默认创建5个线程，保持5分钟长连接
 * @author: lius
 * @create: 2019/10/19 22:23
 */
@Configuration
@ConditionalOnClass(Feign.class)
@AutoConfigureBefore(FeignAutoConfiguration.class)
public class OkHttpConfig {

    @Bean
    public okhttp3.OkHttpClient okHttpClient() {
        return new okhttp3.OkHttpClient.Builder()
                //设置连接超时
                .connectTimeout(200, TimeUnit.SECONDS)
                //设置读超时
                .readTimeout(200, TimeUnit.SECONDS)
                //设置写超时
                .writeTimeout(200, TimeUnit.SECONDS)
                //是否自动重连
                .retryOnConnectionFailure(false)
                .connectionPool(new ConnectionPool(10, 5L, TimeUnit.MINUTES))
                .addInterceptor(new OkHttpLoggingInterceptor())
                .build();
    }
}
