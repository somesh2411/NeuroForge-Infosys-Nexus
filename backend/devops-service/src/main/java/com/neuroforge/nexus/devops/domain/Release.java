package com.neuroforge.nexus.devops.domain;

import com.neuroforge.nexus.shared.domain.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "releases")
public class Release extends BaseEntity {

    @Id
    private String id;

    @Column(unique = true, nullable = false, length = 50)
    private String version; // Semantic version e.g. 1.2.0

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "release_notes", columnDefinition = "TEXT")
    private String releaseNotes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "build_id")
    private Build build;

    @Column(nullable = false, length = 50)
    private String status; // DRAFT, TESTING, APPROVED, RELEASED, ARCHIVED

    @Column(name = "released_by", length = 100)
    private String releasedBy;

    @Column(name = "released_at")
    private LocalDateTime releasedAt;

    public Release() {}

    public Release(String id, String version, String name, String releaseNotes, Build build, 
                   String status, String releasedBy, LocalDateTime releasedAt) {
        this.id = id;
        this.version = version;
        this.name = name;
        this.releaseNotes = releaseNotes;
        this.build = build;
        this.status = status;
        this.releasedBy = releasedBy;
        this.releasedAt = releasedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReleaseNotes() {
        return releaseNotes;
    }

    public void setReleaseNotes(String releaseNotes) {
        this.releaseNotes = releaseNotes;
    }

    public Build getBuild() {
        return build;
    }

    public void setBuild(Build build) {
        this.build = build;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReleasedBy() {
        return releasedBy;
    }

    public void setReleasedBy(String releasedBy) {
        this.releasedBy = releasedBy;
    }

    public LocalDateTime getReleasedAt() {
        return releasedAt;
    }

    public void setReleasedAt(LocalDateTime releasedAt) {
        this.releasedAt = releasedAt;
    }

    public static ReleaseBuilder builder() {
        return new ReleaseBuilder();
    }

    public static class ReleaseBuilder {
        private String id;
        private String version;
        private String name;
        private String releaseNotes;
        private Build build;
        private String status;
        private String releasedBy;
        private LocalDateTime releasedAt;

        public ReleaseBuilder id(String id) {
            this.id = id;
            return this;
        }

        public ReleaseBuilder version(String version) {
            this.version = version;
            return this;
        }

        public ReleaseBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ReleaseBuilder releaseNotes(String releaseNotes) {
            this.releaseNotes = releaseNotes;
            return this;
        }

        public ReleaseBuilder build(Build build) {
            this.build = build;
            return this;
        }

        public ReleaseBuilder status(String status) {
            this.status = status;
            return this;
        }

        public ReleaseBuilder releasedBy(String releasedBy) {
            this.releasedBy = releasedBy;
            return this;
        }

        public ReleaseBuilder releasedAt(LocalDateTime releasedAt) {
            this.releasedAt = releasedAt;
            return this;
        }

        public Release build() {
            return new Release(id, version, name, releaseNotes, build, status, releasedBy, releasedAt);
        }
    }
}
