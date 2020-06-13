package com.ylpu.thales.scheduler.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {

    @Bean
    public LoginInterceptor securityInterceptor() {
        return new LoginInterceptor();
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("/login");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(securityInterceptor()).addPathPatterns("/**/paging", "/**/getTaskSummary","/**/getWorkerSummary","/**/getJobTypeSummary",
                "/**/getWorkerCpuUsage","/**/getWorkerMemoryUsage")
                .excludePathPatterns("/**/login", "/**/logout");
        super.addInterceptors(registry);
    }
}
