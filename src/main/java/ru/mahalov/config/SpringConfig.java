package ru.mahalov.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;


import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@ComponentScan("ru.mahalov")
@EnableWebMvc
@EnableTransactionManagement
@PropertySource("classpath:hibernate.properties")
public class SpringConfig implements WebMvcConfigurer {

  private final ApplicationContext applicationContext;

  //Класс для параметров для подключения к БД через jdbc
  //private final DBSettings dbSettings;

  private final Environment env;

  @Autowired
  public SpringConfig(ApplicationContext applicationContext, Environment env) {
    this.applicationContext = applicationContext;
    this.env = env;
  }

  @Bean
  public SpringResourceTemplateResolver templateResolver() {
    SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
    templateResolver.setApplicationContext(applicationContext);
    templateResolver.setPrefix("/WEB-INF/classes/views/");
    templateResolver.setSuffix(".html");
    return templateResolver;
  }

  @Bean
  public SpringTemplateEngine templateEngine() {
    SpringTemplateEngine templateEngine = new SpringTemplateEngine();
    templateEngine.setTemplateResolver(templateResolver());
    templateEngine.setEnableSpringELCompiler(true);
    return templateEngine;
  }

  @Override
  public void configureViewResolvers(ViewResolverRegistry registry) {
    ThymeleafViewResolver resolver = new ThymeleafViewResolver();
    resolver.setTemplateEngine(templateEngine());
    resolver.setCharacterEncoding("UTF-8");

    registry.viewResolver(resolver);
  }

  @Bean
  public DataSource dataSource(){

    DriverManagerDataSource dataSource = new DriverManagerDataSource();

    dataSource.setDriverClassName(env.getRequiredProperty("hibernate.driver_class"));
    dataSource.setUrl(env.getRequiredProperty("hibernate.connection.url"));
    dataSource.setUsername(env.getRequiredProperty("hibernate.connection.username"));
    dataSource.setPassword(env.getRequiredProperty("hibernate.connection.password"));

    return dataSource;
  }

//  Используем Hibernate вместо jdbctemplate
//  @Bean
//  public JdbcTemplate jdbcTemplate(){
//    return new JdbcTemplate(dataSource());
//  }

  private Properties hibernateProperties(){
    Properties properties = new Properties();

    properties.put("hibernate.dialect",env.getRequiredProperty("hibernate.dialect"));
    properties.put("hibernate.show_sql",env.getRequiredProperty("hibernate.show_sql"));

    return properties;
  }

  @Bean
  public LocalSessionFactoryBean sessionFactoryBean(){
    LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
    sessionFactory.setDataSource(dataSource());
    sessionFactory.setPackagesToScan("ru.mahalov.model");
    sessionFactory.setHibernateProperties(hibernateProperties());

    return sessionFactory;
  }

  @Bean
  public PlatformTransactionManager hibernateTransactionManager(){
    HibernateTransactionManager hibernateTransactionManager = new HibernateTransactionManager();
    hibernateTransactionManager.setSessionFactory(sessionFactoryBean().getObject());

    return hibernateTransactionManager;
  }

}