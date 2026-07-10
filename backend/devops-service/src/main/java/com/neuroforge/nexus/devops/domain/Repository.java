package com.neuroforge.nexus.devops.domain;

import com.neuroforge.nexus.shared.domain.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "repositories")
public class Repository extends BaseEntity {

    @Id
    private String id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private String url;

    @Column(name = "default_branch", length = 50)
    private String defaultBranch = "main";

    @Column(name = "last_commit_id", length = 50)
    private String lastCommitId;

    @Column(name = "last_commit_message", columnDefinition = "TEXT")
    private String lastCommitMessage;

    @Column(name = "last_commit_author", length = 100)
    private String lastCommitAuthor;

    @Column(name = "last_commit_at")
    private LocalDateTime lastCommitAt;

    public Repository() {}

    public Repository(String id, String name, String url, String defaultBranch, 
                      String lastCommitId, String lastCommitMessage, String lastCommitAuthor, 
                      LocalDateTime lastCommitAt) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.defaultBranch = defaultBranch;
        this.lastCommitId = lastCommitId;
        this.lastCommitMessage = lastCommitMessage;
        this.lastCommitAuthor = lastCommitAuthor;
        this.lastCommitAt = lastCommitAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDefaultBranch() {
        return defaultBranch;
    }

    public void setDefaultBranch(String defaultBranch) {
        this.defaultBranch = defaultBranch;
    }

    public String getLastCommitId() {
        return lastCommitId;
    }

    public void setLastCommitId(String lastCommitId) {
        this.lastCommitId = lastCommitId;
    }

    public String getLastCommitMessage() {
        return lastCommitMessage;
    }

    public void setLastCommitMessage(String lastCommitMessage) {
        this.lastCommitMessage = lastCommitMessage;
    }

    public String getLastCommitAuthor() {
        return lastCommitAuthor;
    }

    public void setLastCommitAuthor(String lastCommitAuthor) {
        this.lastCommitAuthor = lastCommitAuthor;
    }

    public LocalDateTime getLastCommitAt() {
        return lastCommitAt;
    }

    public void setLastCommitAt(LocalDateTime lastCommitAt) {
        this.lastCommitAt = lastCommitAt;
    }

    public static RepositoryBuilder builder() {
        return new RepositoryBuilder();
    }

    public static class RepositoryBuilder {
        private String id;
        private String name;
        private String url;
        private String defaultBranch = "main";
        private String lastCommitId;
        private String lastCommitMessage;
        private String lastCommitAuthor;
        private LocalDateTime lastCommitAt;

        public RepositoryBuilder id(String id) {
            this.id = id;
            return this;
        }

        public RepositoryBuilder name(String name) {
            this.name = name;
            return this;
        }

        public RepositoryBuilder url(String url) {
            this.url = url;
            return this;
        }

        public RepositoryBuilder defaultBranch(String defaultBranch) {
            this.defaultBranch = defaultBranch;
            return this;
        }

        public RepositoryBuilder lastCommitId(String lastCommitId) {
            this.lastCommitId = lastCommitId;
            return this;
        }

        public RepositoryBuilder lastCommitMessage(String lastCommitMessage) {
            this.lastCommitMessage = lastCommitMessage;
            return this;
        }

        public RepositoryBuilder lastCommitAuthor(String lastCommitAuthor) {
            this.lastCommitAuthor = lastCommitAuthor;
            return this;
        }

        public RepositoryBuilder lastCommitAt(LocalDateTime lastCommitAt) {
            this.lastCommitAt = lastCommitAt;
            return this;
        }

        public Repository build() {
            return new Repository(id, name, url, defaultBranch, lastCommitId, lastCommitMessage, lastCommitAuthor, lastCommitAt);
        }
    }
}
