package chain.fxgj.server.payroll.config.feign;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;

/**
 * @program: cloud-account
 * @description: okhttp日志拦截器
 * @author: lius
 * @create: 2019/10/19 22:54
 */
@Slf4j
public class OkHttpLoggingInterceptor implements Interceptor {


    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        //这个chain里面包含了request和response，所以你要什么都可以从这里拿
        //Request request = chain.request();
        Request request = chain.request().newBuilder()
                .addHeader("Connection", "close")
                .build();

        //请求发起的时间
        long t1 = System.nanoTime();

        log.info(String.format("发送请求 %s on %s%n%s",
                request.url(), chain.connection(), request.headers()));

        Response response = chain.proceed(request);

        //收到响应的时间
        long t2 = System.nanoTime();

        //这里不能直接使用response.body().string()的方式输出日志
        //因为response.body().string()之后，response中的流会被关闭，程序会报错，我们需要创建出一个新的response给应用层处理
        ResponseBody responseBody = response.peekBody(1024 * 1024);

        //log.info("===>接收url: 【{}】", response.request().url());
        //log.info("===>响应header: 【{}】", response.headers());
        //log.info("===>返回信息: 【{}】", responseBody.string());
        //log.info("===>耗时: 【{}ms】", (t2 - t1) / 1e6d);

        log.info("===>Feign 接收url: 【{}】 耗时: 【{}ms】", response.request().url(),(t2 - t1) / 1e6d);

        return response;
    }

}
