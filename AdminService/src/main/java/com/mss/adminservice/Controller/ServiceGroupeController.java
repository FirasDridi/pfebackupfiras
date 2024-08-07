package com.mss.adminservice.Controller;


import com.mss.adminservice.Entities.Group;
import com.mss.adminservice.Entities.ServiceGroupMapping;
import com.mss.adminservice.Repo.GroupRepository;
import com.mss.adminservice.Repo.ServiceGroupMappingRepository;
import jakarta.validation.constraints.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin("*")
public class ServiceGroupeController {
    @Autowired
    ServiceGroupMappingRepository serviceGroupMappingRepository;
    @Autowired
    GroupRepository groupRepository;


    //el methode hedhy

    @GetMapping("/gets/{idg}/servicesId")
    List<String>findserv(@PathVariable Long idg){
        List<ServiceGroupMapping>nl=new ArrayList<>();
        List<String> firas=new ArrayList<>();
        Group g=groupRepository.findById(idg).orElse(null);
        List<ServiceGroupMapping>ls=serviceGroupMappingRepository.findAll();
        for (ServiceGroupMapping s:ls){
            if (s.getGroup()==g){
                nl.add(s);
                firas.add(s.getServiceId());

            }
        }
        return firas;
    }

}
