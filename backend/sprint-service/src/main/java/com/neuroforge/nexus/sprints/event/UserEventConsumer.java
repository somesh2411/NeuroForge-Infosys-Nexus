package com.neuroforge.nexus.sprints.event;

import com.neuroforge.nexus.sprints.domain.User;
import com.neuroforge.nexus.sprints.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventConsumer {

    private final UserRepository userRepository;

    @KafkaListener(topics = "user-created", groupId = "sprint-service-group")
    public void consumeUserCreated(UserCreatedEvent event) {
        log.info("Received UserCreatedEvent for user: {}", event.username());
        User user = new User();
        user.setId(event.id());
        user.setUsername(event.username());
        user.setFirstName(event.firstName());
        user.setLastName(event.lastName());
        userRepository.save(user);
        log.info("Successfully replicated user {} locally in sprint-service.", event.username());
    }

    @KafkaListener(topics = "user-updated", groupId = "sprint-service-group")
    public void consumeUserUpdated(UserUpdatedEvent event) {
        log.info("Received UserUpdatedEvent for user: {}", event.username());
        userRepository.findById(event.id()).ifPresent(user -> {
            user.setFirstName(event.firstName());
            user.setLastName(event.lastName());
            userRepository.save(user);
            log.info("Successfully updated user {} replication locally in sprint-service.", event.username());
        });
    }
}
