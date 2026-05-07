//package com.smartparking.shared.config;
//
//import org.apache.kafka.clients.admin.NewTopic;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.kafka.config.TopicBuilder;
//
//@Configuration
//public class KafkaConfig {
//
//    public static final String VEHICLE_ENTRY  = "vehicle-entry";
//    public static final String VEHICLE_EXIT   = "vehicle-exit";
//    public static final String BARRIER_CMD    = "barrier-command";
//    public static final String ALERT_VIOLATION = "alert-violation";
//
//    @Bean
//    public NewTopic vehicleEntryTopic() {
//        return TopicBuilder.name(VEHICLE_ENTRY).partitions(3).replicas(1).build();
//    }
//
//    @Bean
//    public NewTopic vehicleExitTopic() {
//        return TopicBuilder.name(VEHICLE_EXIT).partitions(3).replicas(1).build();
//    }
//
//    @Bean
//    public NewTopic barrierCommandTopic() {
//        return TopicBuilder.name(BARRIER_CMD).partitions(3).replicas(1).build();
//    }
//
//    @Bean
//    public NewTopic alertViolationTopic() {
//        return TopicBuilder.name(ALERT_VIOLATION).partitions(1).replicas(1).build();
//    }
//}
