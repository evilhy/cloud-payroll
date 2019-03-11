package chain.fxgj.server.payroll.config;

import chain.fxgj.server.payroll.async.ThreadPoolTaskExecutorMdcWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

/**
 * @author chain
 * create by chain on 2018/9/3 下午2:44
 **/
@Configuration
@EnableAsync
public class AsyncConfigurer implements org.springframework.scheduling.annotation.AsyncConfigurer {
    private static final Logger log = LoggerFactory.getLogger(AsyncConfigurer.class);

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor threadPool = new ThreadPoolTaskExecutorMdcWrapper();
        threadPool.setCorePoolSize(1000);//当前线程数
        threadPool.setQueueCapacity(1000000);//线程池所使用的缓冲队列
        threadPool.setWaitForTasksToCompleteOnShutdown(true);//等待任务在关机时完成--表明等待所有线程执行完
        threadPool.setAwaitTerminationSeconds(60);// 等待时间 （默认为0，此时立即停止），并没等待xx秒后强制停止
        threadPool.setThreadNamePrefix("async-");//  线程名称前缀
        threadPool.initialize(); // 初始化
        return threadPool;
    }


    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {

        return new MyAsyncExceptionHandler();
    }

    /**
     * 自定义异常处理类
     *
     * @author hry
     */
    class MyAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

        //手动处理捕获的异常
        @Override
        public void handleUncaughtException(Throwable throwable, Method method, Object... obj) {
            log.info("Exception message - " + throwable.getMessage());
            log.info("Method name - " + method.getName());
            for (Object param : obj) {
                log.info("Parameter value - " + param);
            }
        }

    }
}
