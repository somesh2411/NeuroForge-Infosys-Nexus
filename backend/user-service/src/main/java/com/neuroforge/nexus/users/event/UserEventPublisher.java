package com.neuroforge.nexus.users.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishUserCreated(UserCreatedEvent event) {
        log.info("Publishing UserCreatedEvent for user: {}", event.username());
        kafkaTemplate.send("user-created", event.id(), event);
    }

    public void publishUserUpdated(UserUpdatedEvent event) {
        log.info("Publishing UserUpdatedEvent for user: {}", event.username());
        kafkaTemplate.send("user-updated", event.id(), event);
    }

    public void publishTeamCreated(TeamCreatedEvent event) {
        log.info("Publishing TeamCreatedEvent for team: {}", event.code());
        kafkaTemplate.send("team-created", event.id(), event);
    }

    public void publishTeamDeleted(TeamDeletedEvent event) {
        log.info("Publishing TeamDeletedEvent for team id: {}", event.id());
        kafkaTemplate.send("team-deleted", event.id(), event);
    }
}
