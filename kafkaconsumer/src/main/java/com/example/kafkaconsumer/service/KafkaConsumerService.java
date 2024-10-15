package com.example.kafkaconsumer.service;

import com.example.kafkaconsumer.entity.AccessLog;
import com.example.kafkaconsumer.repository.AccessLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class KafkaConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);

    @Autowired
    private AccessLogRepository accessLogRepository;

    @KafkaListener(topics = "msstopic", groupId = "group_id")
    public void consume(String message) {
        logger.debug("Received message: {}", message);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            AccessLog accessLog = new AccessLog();
            accessLog.setAccessToken(jsonNode.get("accessToken").asText());
            accessLog.setEndpoint(jsonNode.get("endpoint").asText());

            JsonNode timestampNode = jsonNode.get("timestamp");
            if (timestampNode != null && timestampNode.isArray() && timestampNode.size() >= 6) {
                int year = timestampNode.get(0).asInt();
                int month = timestampNode.get(1).asInt();
                int day = timestampNode.get(2).asInt();
                int hour = timestampNode.get(3).asInt();
                int minute = timestampNode.get(4).asInt();
                int second = timestampNode.get(5).asInt();
                LocalDateTime timestamp = LocalDateTime.of(year, month, day, hour, minute, second);

                // Only process logs with today's date
                if (timestamp.toLocalDate().equals(LocalDate.now())) {
                    accessLog.setTimestamp(timestamp);
                    accessLog.setGroupId(jsonNode.get("groupId").asLong());
                    accessLog.setUserId(jsonNode.get("userId").asText());
                    accessLogRepository.save(accessLog);
                    logger.debug("Saved access log: {}", accessLog);
                } else {
                    logger.debug("Skipped log with timestamp: {}", timestamp);
                }
            } else {
                logger.warn("Received empty or invalid timestamp, skipping setting the timestamp.");
            }
        } catch (JsonProcessingException e) {
            logger.error("Failed to process message", e);
        }
    }
}
