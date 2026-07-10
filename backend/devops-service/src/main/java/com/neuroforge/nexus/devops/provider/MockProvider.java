package com.neuroforge.nexus.devops.provider;

import com.neuroforge.nexus.devops.domain.Build;
import com.neuroforge.nexus.devops.domain.Pipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MockProvider implements PipelineProvider {

    private static final Logger log = LoggerFactory.getLogger(MockProvider.class);

    @Override
    public String getProviderType() {
        return "MOCK";
    }

    @Override
    public void executePipeline(Pipeline pipeline, Build build) throws Exception {
        log.info("MockProvider: Simulating execution of pipeline '{}'", pipeline.getName());
    }

    @Override
    public String fetchStageLogs(Pipeline pipeline, String buildId, String stageName) throws Exception {
        return switch (stageName.toUpperCase()) {
            case "BUILD" -> "Mock: Compiling source files... Maven compile success.";
            case "TEST" -> "Mock: Executing tests... All 42 tests passed.";
            case "CODE QUALITY" -> "Mock: Analyzing with SonarQube... Quality Gate: PASSED (Bugs: 0, Debt: 0).";
            case "DOCKER BUILD" -> "Mock: Packaging container image... Image tag neuroforge-nexus:v1.0.0 created.";
            case "DEPLOY" -> "Mock: Syncing with cluster... Container deployed successfully.";
            default -> "Mock: Stage " + stageName + " completed.";
        };
    }
}
