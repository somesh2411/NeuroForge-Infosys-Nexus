package com.neuroforge.nexus.devops.provider;

import com.neuroforge.nexus.devops.domain.Build;
import com.neuroforge.nexus.devops.domain.Pipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class JenkinsProvider implements PipelineProvider {

    private static final Logger log = LoggerFactory.getLogger(JenkinsProvider.class);

    @Override
    public String getProviderType() {
        return "JENKINS";
    }

    @Override
    public void executePipeline(Pipeline pipeline, Build build) throws Exception {
        log.info("JenkinsProvider: Triggering Jenkins Job '{}' for build #{}", pipeline.getJenkinsJobName(), build.getBuildNumber());
        // Simulations are handled asynchronously in BuildServiceImpl to support unified stage state-driven transitions
    }

    @Override
    public String fetchStageLogs(Pipeline pipeline, String buildId, String stageName) throws Exception {
        return switch (stageName.toUpperCase()) {
            case "BUILD" -> """
                [Jenkins-Agent] Running build stage for Maven project...
                [INFO] Scanning for projects...
                [INFO] ----------------< com.neuroforge.nexus:devops-service >----------------
                [INFO] Building devops-service 1.0.0
                [INFO] --------------------------------[ jar ]---------------------------------
                [INFO] --- compiler:3.11.0:compile (default-compile) @ devops-service ---
                [INFO] Compiling 24 source files to target/classes
                [INFO] BUILD SUCCESS
                """;
            case "TEST" -> """
                [Jenkins-Agent] Running Maven unit test suites...
                [INFO] --- surefire:3.1.2:test (default-test) @ devops-service ---
                [INFO] Running com.neuroforge.nexus.devops.PipelineServiceTests
                [INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.25s
                [INFO] Running com.neuroforge.nexus.devops.DeployServiceTests
                [INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.89s
                [INFO] Results:
                [INFO] Tests run: 20, Failures: 0, Errors: 0, Skipped: 0
                """;
            case "CODE QUALITY" -> """
                [Jenkins-Agent] Running SonarQube analysis...
                [INFO] User Cache: C:\\Users\\Jenkins\\.sonar\\cache
                [INFO] Load project settings for project 'nexus-devops-service'
                [INFO] Load quality profiles...
                [INFO] Quality profile for java: Sonar way (v12.2)
                [INFO] Sensor JavaSensor [java]
                [INFO] Sensor JavaSensor [java] (done) | time=854ms
                [INFO] Quality Gate: PASSED (Bugs: 0, Vulnerabilities: 0, Debt: 0min)
                """;
            case "DOCKER BUILD" -> """
                [Jenkins-Agent] Executing docker daemon build command...
                Sending build context to Docker daemon  2.45MB
                Step 1/5 : FROM openjdk:22-jdk-slim
                ---> d18274a2cb92
                Step 2/5 : WORKDIR /app
                ---> Running in 54ef92c10b2
                Step 3/5 : COPY target/*.jar app.jar
                ---> c1a2d48fba9c
                Step 4/5 : EXPOSE 8084
                ---> 9e102f483c01
                Step 5/5 : ENTRYPOINT ["java", "-jar", "app.jar"]
                ---> Running in 74df0983cb1a
                Successfully built image: neuroforge-devops-service:latest
                """;
            case "DEPLOY" -> """
                [Jenkins-Agent] Pushing docker image to AWS ECR...
                The push refers to repository [49281739281.dkr.ecr.us-east-1.amazonaws.com/neuroforge-devops-service]
                d18274a2cb92: Pushed
                c1a2d48fba9c: Pushed
                latest: digest: sha256:d8c0b2984cfb92d71e92d83b9c819c8fba9210283c74a0129bcfe9b21a8f98a2 size: 1482
                [INFO] Deployment successful. Version tag: v1.0.0-build-%s
                """.formatted(buildId.substring(buildId.length() - 5));
            default -> "[Jenkins-Agent] No console logs recorded for stage: " + stageName;
        };
    }
}
