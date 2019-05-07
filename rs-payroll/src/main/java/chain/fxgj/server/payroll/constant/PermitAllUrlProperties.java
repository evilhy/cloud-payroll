package chain.fxgj.server.payroll.constant;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

@Component
@ConfigurationProperties(prefix = "auth", ignoreInvalidFields = true)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class PermitAllUrlProperties {

    private static List<Pattern> permitallUrlPattern;

    private Gateway gateway = new Gateway();


    /**
     * 权限准许的路径列表
     * 此列表下访问路径，不验证token
     */
    private List<url> permitall;

    @Data
    public static class Gateway {
        /**
         * 验证权限 服务ID
         */
        private String id;
        /**
         * 登录验证
         */
        private String loginCheckPath;
        /**
         * 权限验证
         */
        private String authCheckPath;
    }


    @Data
    public static class url {
        /**
         * 访问路径
         */
        private String pattern;
    }


    public String[] getPermitallPatterns() {
        List<String> urls = new ArrayList<>();
        Iterator<url> iterator = permitall.iterator();
        while (iterator.hasNext()) {
            urls.add(iterator.next().getPattern());
        }
        return urls.toArray(new String[0]);
    }

    /**
     * 项目启动时 ，初始化  permitallUrlPattern
     */
    @PostConstruct
    public void init() {
        if (permitall != null && permitall.size() > 0) {
            this.permitallUrlPattern = new ArrayList<>();
            Iterator<url> iterator = permitall.iterator();
            while (iterator.hasNext()) {
                String currentUrl = iterator.next().getPattern().replaceAll("\\*\\*", "(.*?)");
                Pattern currentPattern = Pattern.compile(currentUrl, Pattern.CASE_INSENSITIVE); //用不区分大小写的匹配
                permitallUrlPattern.add(currentPattern);
            }

        }
    }

    /**
     * 验证url 是否需要验证 jsessionId
     */
    public boolean isPermitAllUrl(String url) {
        for (Pattern pattern : permitallUrlPattern) {
            if (pattern.matcher(url).find()) {
                log.info("访问路径{}, 不需要，验证jsessionId", url);
                return true;
            }
        }
        log.info("访问路径{}, 需要，验证jsessionId", url);
        return false;
    }
}

