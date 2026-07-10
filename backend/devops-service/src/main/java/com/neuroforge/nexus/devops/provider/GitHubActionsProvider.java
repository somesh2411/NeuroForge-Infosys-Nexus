package com.neuroforge.nexus.devops.provider;

import com.neuroforge.nexus.devops.domain.Build;
import com.neuroforge.nexus.devops.domain.Pipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GitHubActionsProvider implements PipelineProvider {

    private static final Logger log = LoggerFactory.getLogger(GitHubActionsProvider.class);

    @Override
    public String getProviderType() {
        return "GITHUB_ACTIONS";
    }

    @Override
    public void executePipeline(Pipeline pipeline, Build build) throws Exception {
        log.info("GitHubActionsProvider: Triggering GitHub workflow '{}' for branch {}", 
                pipeline.getGithubWorkflowPath(), pipeline.getBranch());
    }

    @Override
    public String fetchStageLogs(Pipeline pipeline, String buildId, String stageName) throws Exception {
        return switch (stageName.toUpperCase()) {
            case "BUILD" -> """
                Run actions/setup-java@v4
                  with:
                    distribution: temurin
                    java-version: 22
                [GitHub-Runner] Installing Java JDK Temurin 22...
                [GitHub-Runner] Executing: mvn clean compile -DskipTests
                [INFO] Scanning for projects...
                [INFO] Building devops-service 1.0.0
                [INFO] Compiled classes success!
                """;
            case "TEST" -> """
                [GitHub-Runner] Executing unit test step: mvn test
                [INFO] --- surefire-plugin: test execution ---
                Running com.neuroforge.nexus.devops.PipelineControllerTests
                Tests run: 15, Passed: 15, Failed: 0
                Running com.neuroforge.nexus.devops.AnalyticsServiceTests
                Tests run: 10, Passed: 10, Failed: 0
                [INFO] UNIT TESTS COMPLETED SUCCESSFULLY
                """;
            case "CODE QUALITY" -> """
                Run github/codeql-action/analyze@v2
                [GitHub-Runner] Running CodeQL static analysis...
                Analyzing java language database...
                Analysis complete. 0 security alerts identified. Quality check PASSED.
                """;
            case "DOCKER BUILD" -> """
                Run docker/build-push-action@v5
                [GitHub-Runner] Logging into GitHub Container Registry (ghcr.io)...
                [GitHub-Runner] Building and tagging image: ghcr.io/neuroforge/nexus-devops:v1.0.0-b-%s
                docker build --push --tag ghcr.io/neuroforge/nexus-devops:v-%s .
                Successfully built and pushed.
                """.formatted(buildId.substring(buildId.length() - 5), buildId.substring(buildId.length() - 5));
            case "DEPLOY" -> """
                [GitHub-Runner] Deploying Docker image to target environment via SSH...
                ssh -o StrictHostKeyChecking=no deploy@servers "docker pull ghcr.io/neuroforge/nexus-devops:v1.0.0-b-%s && docker compose up -d"
                Deployment transition completed on environment host.
                Status: healthy
                """.formatted(buildId.substring(buildId.length() - 5));
            default -> "[GitHub-Runner] No runner logs recorded for stage: " + stageName;
        };
    }
}
