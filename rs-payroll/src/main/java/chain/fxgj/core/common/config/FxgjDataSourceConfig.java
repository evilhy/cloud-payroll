package chain.fxgj.core.common.config;

import chain.fxgj.core.common.constant.FxgjDBConstant;
import chain.utils.jpa.dao.config.ChainRepositoryFactoryBean;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import java.util.Map;

/**
 * The type User data source config.
 *
 * @author chain  create by xiongjj on 2018 /4/28 下午4:02
 */
@Configuration
@EnableJpaRepositories(
        entityManagerFactoryRef = FxgjDBConstant.ENTITY_MANAGER_FACTORY_REF,
        transactionManagerRef = FxgjDBConstant.TRANSACTION_MANAGER_REF,
        repositoryFactoryBeanClass = ChainRepositoryFactoryBean.class,
        basePackages = {FxgjDBConstant.DAO_PAGES})
public class FxgjDataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(FxgjDataSourceConfig.class);
    @Resource
    private JpaProperties jpaProperties;

    /**
     * Primary data source data source.
     *
     * @return the data source
     */
    @Bean(name = FxgjDBConstant.DATA_SOURCE_NAME)
    @Primary
    @ConfigurationProperties(prefix = FxgjDBConstant.DATA_SOURCE_PROPERTIES)
    public HikariDataSource primaryDataSource() {
        log.info(" dataSource {} 加载完成", FxgjDBConstant.DATA_SOURCE_NAME);
        return new HikariDataSource();
    }

    @Primary
    @Bean(name = FxgjDBConstant.ENTITY_MANAGER_REF)
    public EntityManager entityManager(EntityManagerFactoryBuilder builder) {
        return entityManagerFactory(builder).getObject().createEntityManager();
    }


    private Map<String, String> getVendorProperties() {
        //return jpaProperties.getHibernateProperties(new HibernateSettings());
        return  jpaProperties.getProperties();
    }

    /**
     * 设置实体类所在位置
     */
    @Primary
    @Bean(name = FxgjDBConstant.ENTITY_MANAGER_FACTORY_REF)
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(primaryDataSource())
                .packages(FxgjDBConstant.ENTITY_PAGES)
                .persistenceUnit(FxgjDBConstant.PERSISTENCE_UNIT)
                .properties(getVendorProperties())
                .build();
    }

    @Primary
    @Bean(name = FxgjDBConstant.TRANSACTION_MANAGER_REF)
    public PlatformTransactionManager transactionManagerFoo(EntityManagerFactoryBuilder builder) {
        return new JpaTransactionManager(entityManagerFactory(builder).getObject());
    }

    @Bean(name = FxgjDBConstant.JDBC_TEMPLATE_REF)
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(primaryDataSource());
    }
}
