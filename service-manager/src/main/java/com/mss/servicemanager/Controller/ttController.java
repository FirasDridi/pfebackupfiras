package com.mss.servicemanager.Controller;


import com.mss.servicemanager.DTO.ServiceDetailsDto;
import com.mss.servicemanager.DTO.ServiceDto;
import com.mss.servicemanager.Repositories.ServiceUsageRepo;
import com.mss.servicemanager.Repositories.TestRepo;
import com.mss.servicemanager.Services.ServiceUsageService;
import com.mss.servicemanager.entities.service;
import org.example.annotations.Payant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tt")
public class ttController {
    @Autowired
    ServiceUsageService serviceUsageService;
    @Autowired
    ServiceUsageRepo serviceUsageRepo;

    @Autowired
    TestRepo repo;

    @GetMapping("/listWithDetails")
    public Mono<List<ServiceDetailsDto>> listServicesWithDetails() {
        return serviceUsageService.findAllServicesWithDetails();
    }

    @GetMapping("/getservice/{ids}/services")
    List<String>findservicenamefront(@PathVariable List<String>ids){
        List<String> firas = new ArrayList<>();
        for (String i:ids){

            UUID uuid = UUID.fromString(i);

            service s =serviceUsageRepo.findById(uuid).orElse(null);
            firas.add(s.getName());


        }



        return firas;

    }
    //elmetohde hedhy .
    @GetMapping("/gets/{ids}/services")


            List<ServiceDto>ff(@PathVariable List<UUID>ids){
            List<ServiceDto>ss=new ArrayList<>();
            for(UUID i:ids){
                service s=serviceUsageRepo.findById(i).orElse(null);
                 ServiceDto x= new ServiceDto();
                x.setId(s.getId());
                x.setDescription(s.getDescription());
                x.setConfiguration(s.getConfiguration());
                x.setName(s.getName());
                x.setVersion(s.getVersion());
             ss.add(x);


            }



        return ss;

        }

    @GetMapping("/service/{id}")
    public ResponseEntity<ServiceDto> finds(@PathVariable UUID id) {
        service x = serviceUsageService.findById(id).orElse(null);
        ServiceDto sdto=new ServiceDto();
        if (x == null) {
            System.out.println("Service not found for ID: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

       sdto.setId(x.getId());
        sdto.setDescription(x.getDescription());
        sdto.setConfiguration(x.getConfiguration());
        sdto.setName(x.getName());
        sdto.setVersion(x.getVersion());
        return ResponseEntity.ok(sdto);
    }

    @Payant
    @PostMapping("/firastesttttttt8")
    public ResponseEntity<String> ttyytc() {
        return ResponseEntity.ok("firas");
    }

    @Payant
    @PostMapping("/hamdollh")
    public ResponseEntity<String> ttysytc() {
        return ResponseEntity.ok("firas");
    }

    @Payant
    @PostMapping("/yaAllah")
    public ResponseEntity<String> ttyssytc() {
        return ResponseEntity.ok("firas");
    }
    @Payant
    @PostMapping("/yaRabRbob")
    public ResponseEntity<String> ttysdssytc() {
        return ResponseEntity.ok("firas");
    }
}
