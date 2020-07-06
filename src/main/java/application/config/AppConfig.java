package application.config;

import application.server.utils.reader.ConfigureReader;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

/**
 * <h2>Web功能-MVC相关配置类</h2>
 * <p>该Spring配置类主要负责配置网页服务器的处理行为。</p>
 *
 * @author devcp
 */
@SpringBootApplication(
        exclude = {DataSourceAutoConfiguration.class},
        scanBasePackages = {"com.custom"})
@ServletComponentScan(
        basePackages = {"com.custom"})
@Import({
        CorsConfig.class
})
@Configuration
public class AppConfig extends ResourceHttpRequestHandler implements WebMvcConfigurer {

    /**
     * 开放给用户能访问的资源文件，出于安全考虑仅开放页面资源目录，webapp
     *
     * @param registry
     */
    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        //String[] openResourcesList = {"file:" + ConfigureReader.instance().getPath()};
        //String[] openResourcesList = {"file:" + ConfigureReader.instance().getWebAppPath(), "file:" + ConfigureReader.instance().getAppPath()};
        String[] openResourcesList = {"file:" + ConfigureReader.instance().getWebAppPath()};

        // 加入至资源路径中
        registry.addResourceHandler(new String[]{"/**"}).addResourceLocations(openResourcesList);
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    /**
     * RequestContextListener注册
     */
    @Bean
    public ServletListenerRegistrationBean<RequestContextListener> requestContextListenerRegistration() {
        return new ServletListenerRegistrationBean<>(new RequestContextListener());
    }

    /**
     * 设置默认首页
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
        WebMvcConfigurer.super.addViewControllers(registry);
    }

}
