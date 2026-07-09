package com.neuroforge.nexus.sprints.event;

import com.neuroforge.nexus.sprints.domain.Project;
import com.neuroforge.nexus.sprints.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectEventConsumer {

    private final ProjectRepository projectRepository;

    @KafkaListener(topics = "project-created", groupId = "sprint-service-group")
    public void consumeProjectCreated(ProjectCreatedEvent event) {
        log.info("Received ProjectCreatedEvent for project: {}", event.name());
        Project project = new Project();
        project.setId(event.id());
        project.setName(event.name());
        project.setKey(event.key());
        projectRepository.save(project);
        log.info("Successfully replicated project {} locally in sprint-service.", event.name());
    }
}
