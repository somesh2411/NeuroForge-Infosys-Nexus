package com.neuroforge.nexus.projects.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishProjectCreated(ProjectCreatedEvent event) {
        log.info("Publishing ProjectCreatedEvent for project: {}", event.name());
        kafkaTemplate.send("project-created", event.id(), event);
    }
}
