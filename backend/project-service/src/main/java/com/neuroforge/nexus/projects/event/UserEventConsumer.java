package com.neuroforge.nexus.projects.event;

import com.neuroforge.nexus.projects.domain.Team;
import com.neuroforge.nexus.projects.domain.User;
import com.neuroforge.nexus.projects.repository.TeamRepository;
import com.neuroforge.nexus.projects.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventConsumer {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    @KafkaListener(topics = "user-created", groupId = "project-service-group")
    public void consumeUserCreated(UserCreatedEvent event) {
        log.info("Received UserCreatedEvent for user: {}", event.username());
        User user = new User();
        user.setId(event.id());
        user.setUsername(event.username());
        user.setFirstName(event.firstName());
        user.setLastName(event.lastName());
        userRepository.save(user);
        log.info("Successfully replicated user {} locally.", event.username());
    }

    @KafkaListener(topics = "user-updated", groupId = "project-service-group")
    public void consumeUserUpdated(UserUpdatedEvent event) {
        log.info("Received UserUpdatedEvent for user: {}", event.username());
        userRepository.findById(event.id()).ifPresent(user -> {
            user.setFirstName(event.firstName());
            user.setLastName(event.lastName());
            userRepository.save(user);
            log.info("Successfully updated user {} replication locally.", event.username());
        });
    }

    @KafkaListener(topics = "team-created", groupId = "project-service-group")
    public void consumeTeamCreated(TeamCreatedEvent event) {
        log.info("Received TeamCreatedEvent for team: {}", event.code());
        Team team = new Team();
        team.setId(event.id());
        team.setName(event.name());
        team.setCode(event.code());
        teamRepository.save(team);
        log.info("Successfully replicated team {} locally.", event.code());
    }

    @KafkaListener(topics = "team-deleted", groupId = "project-service-group")
    public void consumeTeamDeleted(TeamDeletedEvent event) {
        log.info("Received TeamDeletedEvent for team ID: {}", event.id());
        if (teamRepository.existsById(event.id())) {
            teamRepository.deleteById(event.id());
            log.info("Successfully deleted team replication locally.");
        }
    }
}
