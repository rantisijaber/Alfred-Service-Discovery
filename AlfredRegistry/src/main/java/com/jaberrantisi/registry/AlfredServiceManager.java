package com.jaberrantisi.registry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaberrantisi.model.AlfredHeartbeat;
import com.jaberrantisi.model.AlfredQuery;
import com.jaberrantisi.model.AlfredService;
import com.jaberrantisi.model.AlfredServiceStatus;

import lombok.Getter;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;

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
        startStatusUpdater();
    }

    public void startStatusUpdater() {
        try {
            scheduler.scheduleAtFixedRate(this::updateStatus, 45, 45, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.err.println("Status updater failed. Error: " + e.getMessage());
        }
    }

    public List<AlfredService> getServiceList(AlfredQuery query) {
        List<AlfredService> ref = this.getServiceMap().get(query.getServiceName());
        return ref == null ? List.of() : ref
                .stream()
                .filter(alfredService ->
                        alfredService.getStatus() == AlfredServiceStatus.HEALTHY)
                .toList();
    }

    public void addService(AlfredService alfredService) {
        String serviceName = alfredService.getServiceName();
        List<AlfredService> serviceLocations =
                this.serviceMap.computeIfAbsent(serviceName, k -> new CopyOnWriteArrayList<>());
        serviceLocations.add(alfredService);
    }

//    public String getPayloadAsString(AlfredObj alfredObj) throws JsonProcessingException {
//        Object obj = alfredObj.getPayload();
//        return this.mapper.writeValueAsString(obj);
//    }

    public void acceptHeartbeat(AlfredHeartbeat heartbeat) {
        List<AlfredService> opService = this.serviceMap.get(heartbeat.getServiceName());
        if (opService == null) return;
        opService.stream().filter(alfredService ->
                alfredService.getInstanceId().equals(heartbeat.getInstanceId()))
                .findFirst()
                .ifPresent(alfredService ->
                        alfredService.setLastHeartbeat(heartbeat.getTimeSent()));

    }

    public void updateStatus() {
        this.serviceMap.forEach((serviceName, serviceList) -> {
            serviceList.removeIf(alfredService -> alfredService.getStatus() == AlfredServiceStatus.DOWN);
            Instant time = Instant.now();
            serviceList.stream()
                    .filter(alfredService ->
                            (ChronoUnit.SECONDS.between(alfredService.getLastHeartbeat(), time) > 45))
                    .forEach(alfredService -> {
                        long timeSinceHeartbeat = ChronoUnit.SECONDS.between(alfredService.getLastHeartbeat(), time);
                        long ttl = alfredService.getTtl();

                        if (timeSinceHeartbeat < ttl) alfredService.setStatus(AlfredServiceStatus.LIMBO);
                        else alfredService.setStatus(AlfredServiceStatus.DOWN);
                    });
        });
    }
}
