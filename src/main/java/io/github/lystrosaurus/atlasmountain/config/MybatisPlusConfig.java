package io.github.lystrosaurus.atlasmountain.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.dialects.MySqlDialect;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

@Configuration
@MapperScan("io.github.lystrosaurus.atlasmountain.**.mapper")
public class MybatisPlusConfig {

    @Bean
    MybatisPlusInterceptor mybatisPlusInterceptor() {
        return new MybatisPlusInterceptor();
    }

    @Bean
    MySqlDialect mySqlDialect() {
        return new MySqlDialect();
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource, MybatisPlusInterceptor mybatisPlusInterceptor) throws Exception {
        MybatisSqlSessionFactoryBean bean = new MybatisSqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setPlugins(mybatisPlusInterceptor);
        bean.setTypeAliasesPackage("io.github.lystrosaurus.atlasmountain");
        return bean.getObject();
    }
}
