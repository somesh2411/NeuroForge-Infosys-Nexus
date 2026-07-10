package com.neuroforge.nexus.devops.domain;

import com.neuroforge.nexus.shared.domain.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "environments")
public class Environment extends BaseEntity {

    @Id
    private String id;

    @Column(unique = true, nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_enabled")
    private boolean enabled = true;

    public Environment() {}

    public Environment(String id, String name, String description, boolean enabled) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.enabled = enabled;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public static EnvironmentBuilder builder() {
        return new EnvironmentBuilder();
    }

    public static class EnvironmentBuilder {
        private String id;
        private String name;
        private String description;
        private boolean enabled = true;

        public EnvironmentBuilder id(String id) {
            this.id = id;
            return this;
        }

        public EnvironmentBuilder name(String name) {
            this.name = name;
            return this;
        }

        public EnvironmentBuilder description(String description) {
            this.description = description;
            return this;
        }

        public EnvironmentBuilder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Environment build() {
            return new Environment(id, name, description, enabled);
        }
    }
}
