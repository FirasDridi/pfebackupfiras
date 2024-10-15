package com.mss.adminservice.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class ServiceConfig {

    @Value("${services.available}")
    private String availableServices;

    @Value("#{'${groups.Service1.access}'.split(',')}")
    private List<String> service1AccessGroups;

    @Value("#{'${groups.Service2.access}'.split(',')}")
    private List<String> service2AccessGroups;

    @Value("#{'${groups.Service3.access}'.split(',')}")
    private List<String> service3AccessGroups;

    public String getAvailableServices() {
        return availableServices;
    }

    public List<String> getService1AccessGroups() {
        return service1AccessGroups;
    }

    public List<String> getService2AccessGroups() {
        return service2AccessGroups;
    }

    public List<String> getService3AccessGroups() {
        return service3AccessGroups;
    }
}
