package com.jaberrantisi.registry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaberrantisi.model.AlfredObj;
import com.jaberrantisi.model.AlfredService;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class AlfredServiceManager {
    private final Map<String, List<AlfredService>> serviceMap = new ConcurrentHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    public void addService(String service) throws JsonProcessingException {
        AlfredService alfredService = this.mapper.readValue(service, AlfredService.class);
        String serviceName = alfredService.getServiceName();
        List<AlfredService> serviceLocations =
                serviceMap.computeIfAbsent(serviceName, k -> new ArrayList<>());
        serviceLocations.add(alfredService);
    }

    public String getServiceAsString(AlfredObj alfredObj) throws JsonProcessingException {
        Object obj = alfredObj.getPayload();
        return this.mapper.writeValueAsString(obj);
    }




}
