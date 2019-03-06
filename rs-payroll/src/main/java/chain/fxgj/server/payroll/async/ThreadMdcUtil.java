package chain.fxgj.server.payroll.async;

import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.Callable;

public class ThreadMdcUtil {

    public static <T> Callable<T> wrap(final Callable<T> callable,
                                       final Map<String, String> context) {
        return () -> {
            if (context == null) {
                MDC.clear();
            } else {
                MDC.setContextMap(context);
            }
            try {
                return callable.call();
            } finally {
                MDC.clear();
            }
        };
    }

    public static Runnable wrap(final Runnable runnable, final Map<String, String> context) {
        return () -> {
            if (context == null) {
                MDC.clear();
            } else {
                MDC.setContextMap(context);
            }
            try {
                runnable.run();
            } finally {
                MDC.clear();
            }
        };
    }
}