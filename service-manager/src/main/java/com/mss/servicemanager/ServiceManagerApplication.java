package com.mss.servicemanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.envers.repository.support.EnversRevisionRepositoryFactoryBean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.mss", "org.example"})
@EnableJpaRepositories(basePackages = {"com.mss.servicemanager"}, repositoryFactoryBeanClass = EnversRevisionRepositoryFactoryBean.class)
@EnableAspectJAutoProxy
@EnableDiscoveryClient
@ComponentScan(basePackages = {
        "com.mss.servicemanager",
        "com.mss.servicemanager.config.cors",
        "org.example"
})
public class ServiceManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceManagerApplication.class, args);
    }
}
