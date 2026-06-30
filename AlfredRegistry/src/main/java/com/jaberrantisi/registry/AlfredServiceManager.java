package com.jaberrantisi.registry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaberrantisi.model.AlfredHeartbeat;
import com.jaberrantisi.model.AlfredService;
import com.jaberrantisi.model.AlfredServiceStatus;

import lombok.Getter;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Getter
public class AlfredServiceManager {
    private final Map<String, List<AlfredService>> serviceMap;
    private final ObjectMapper mapper;
    private final ScheduledExecutorService scheduler;

    public AlfredServiceManager() {
        this.serviceMap = new ConcurrentHashMap<>();
        this.mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public void startStatusUpdater() {
        try {
            scheduler.scheduleAtFixedRate(this::updateStatus, 45, 45, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.err.println("Status updater failed. Error: " + e.getMessage());
        }
    }

    public void addService(AlfredService alfredService) {
        String serviceName = alfredService.getServiceName();
        List<AlfredService> serviceLocations =
                this.serviceMap.computeIfAbsent(serviceName, k -> new ArrayList<>());
        serviceLocations.add(alfredService);
    }

//    public String getPayloadAsString(AlfredObj alfredObj) throws JsonProcessingException {
//        Object obj = alfredObj.getPayload();
//        return this.mapper.writeValueAsString(obj);
//    }

    public void acceptHeartbeat(AlfredHeartbeat heartbeat) {
        Optional<AlfredService> opService = this.serviceMap.get(heartbeat.getServiceName())
                .stream()
                .filter(alfredService -> alfredService.getInstanceId().equals(heartbeat.getInstanceId()))
                .findFirst();
        opService.ifPresent(service ->
            service.setLastHeartbeat(heartbeat.getTimeSent())
        );
    }

    public void updateStatus() {
        this.serviceMap.forEach((serviceName, serviceList) ->
            serviceList.removeIf(alfredService -> alfredService.getStatus() == AlfredServiceStatus.DOWN));

        this.serviceMap.forEach((serviceName, serviceList) ->
            serviceList.stream()
                    .filter(alfredService ->
                            (ChronoUnit.SECONDS.between(alfredService.getLastHeartbeat(), Instant.now()) > 45))
                    .forEach(alfredService -> {
                        long timeSinceHeartbeat = ChronoUnit.SECONDS.between(alfredService.getLastHeartbeat(), Instant.now());
                        long ttl = alfredService.getTtl();

                        if (timeSinceHeartbeat < ttl) alfredService.setStatus(AlfredServiceStatus.LIMBO);
                        else if (timeSinceHeartbeat > ttl) alfredService.setStatus(AlfredServiceStatus.DOWN);
                    })
        );
    }
}
