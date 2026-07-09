package com.neuroforge.nexus.users.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class KeycloakAdminService {

    @Value("${keycloak.admin.url:http://localhost:9000}")
    private String keycloakUrl;

    @Value("${keycloak.admin.realm:master}")
    private String adminRealm;

    @Value("${keycloak.admin.client-id:admin-cli}")
    private String clientId;

    @Value("${keycloak.admin.username:admin}")
    private String adminUsername;

    @Value("${keycloak.admin.password:admin}")
    private String adminPassword;

    private final RestTemplate restTemplate = new RestTemplate();

    @SuppressWarnings("unchecked")
    private String getAdminToken() {
        String tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/token", keycloakUrl, adminRealm);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("client_id", clientId);
        map.add("username", adminUsername);
        map.add("password", adminPassword);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
        
        if (response.getBody() != null) {
            return (String) response.getBody().get("access_token");
        }
        throw new IllegalStateException("Failed to retrieve admin access token from Keycloak");
    }

    public String createUser(String username, String email, String password, String firstName, String lastName) {
        String adminToken = getAdminToken();
        String usersUrl = String.format("%s/admin/realms/neuroforge-realm/users", keycloakUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);

        Map<String, Object> credential = Map.of(
            "type", "password",
            "value", password,
            "temporary", false
        );

        Map<String, Object> userBody = Map.of(
            "username", username,
            "email", email,
            "enabled", true,
            "firstName", firstName != null ? firstName : "",
            "lastName", lastName != null ? lastName : "",
            "credentials", List.of(credential)
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(userBody, headers);
        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(usersUrl, request, Void.class);
            if (response.getStatusCode() == HttpStatus.CREATED) {
                List<String> locationHeader = response.getHeaders().get("Location");
                if (locationHeader != null && !locationHeader.isEmpty()) {
                    String location = locationHeader.get(0);
                    return location.substring(location.lastIndexOf("/") + 1);
                }
            }
            throw new RuntimeException("User registration failed in Keycloak with status: " + response.getStatusCode());
        } catch (HttpClientErrorException.Conflict ex) {
            log.error("Username or email already exists in Keycloak: {}", ex.getResponseBodyAsString());
            throw new IllegalArgumentException("User with this username or email already exists.");
        }
    }

    @SuppressWarnings("unchecked")
    public void assignRole(String userId, String roleName) {
        String adminToken = getAdminToken();
        
        // 1. Get Role Representation
        String getRoleUrl = String.format("%s/admin/realms/neuroforge-realm/roles/%s", keycloakUrl, roleName);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<Void> getRequest = new HttpEntity<>(headers);
        
        Map<String, Object> roleRep;
        try {
            ResponseEntity<Map> roleRes = restTemplate.exchange(getRoleUrl, HttpMethod.GET, getRequest, Map.class);
            roleRep = roleRes.getBody();
        } catch (Exception e) {
            log.error("Role {} not found in Keycloak", roleName, e);
            return;
        }

        if (roleRep == null) {
            log.warn("Role representation empty for: {}", roleName);
            return;
        }

        // 2. Assign Role to User
        String assignRoleUrl = String.format("%s/admin/realms/neuroforge-realm/users/%s/role-mappings/realm", keycloakUrl, userId);
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, Object> rolePayload = Map.of(
            "id", roleRep.get("id"),
            "name", roleRep.get("name")
        );

        HttpEntity<List<Map<String, Object>>> assignRequest = new HttpEntity<>(List.of(rolePayload), headers);
        restTemplate.postForEntity(assignRoleUrl, assignRequest, Void.class);
        log.info("Assigned role {} to Keycloak user ID: {}", roleName, userId);
    }

    @SuppressWarnings("unchecked")
    public void updateUserRole(String userId, String newRoleName) {
        String adminToken = getAdminToken();
        
        // 1. Get all current realm roles
        String getRolesUrl = String.format("%s/admin/realms/neuroforge-realm/users/%s/role-mappings/realm", keycloakUrl, userId);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<Void> getRequest = new HttpEntity<>(headers);
        
        ResponseEntity<List> rolesRes = restTemplate.exchange(getRolesUrl, HttpMethod.GET, getRequest, List.class);
        List<Map<String, Object>> currentRoles = rolesRes.getBody();
        
        if (currentRoles != null && !currentRoles.isEmpty()) {
            // Filter roles we manage to remove
            List<String> targetRoles = List.of("ADMIN", "ORGANIZATION_OWNER", "TEAM_LEAD", "DEVELOPER", "QA", "STAKEHOLDER");
            List<Map<String, Object>> rolesToRemove = currentRoles.stream()
                .filter(role -> targetRoles.contains(role.get("name")))
                .toList();
            
            if (!rolesToRemove.isEmpty()) {
                // Delete existing roles
                String deleteRolesUrl = String.format("%s/admin/realms/neuroforge-realm/users/%s/role-mappings/realm", keycloakUrl, userId);
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<List<Map<String, Object>>> deleteRequest = new HttpEntity<>(rolesToRemove, headers);
                restTemplate.exchange(deleteRolesUrl, HttpMethod.DELETE, deleteRequest, Void.class);
                log.info("Removed existing roles {} for user ID: {}", rolesToRemove, userId);
            }
        }
        
        // 2. Assign the new role
        assignRole(userId, newRoleName);
    }

    public void updateUser(String userId, String email, String firstName, String lastName) {
        String adminToken = getAdminToken();
        String updateUrl = String.format("%s/admin/realms/neuroforge-realm/users/%s", keycloakUrl, userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);

        Map<String, Object> updateBody = Map.of(
            "email", email,
            "firstName", firstName != null ? firstName : "",
            "lastName", lastName != null ? lastName : ""
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(updateBody, headers);
        restTemplate.exchange(updateUrl, HttpMethod.PUT, request, Void.class);
        log.info("Updated Keycloak user ID: {} fields email={}, firstName={}, lastName={}", userId, email, firstName, lastName);
    }

    public void deleteUser(String userId) {
        String adminToken = getAdminToken();
        String deleteUrl = String.format("%s/admin/realms/neuroforge-realm/users/%s", keycloakUrl, userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        restTemplate.exchange(deleteUrl, HttpMethod.DELETE, request, Void.class);
        log.info("Deleted Keycloak user ID: {}", userId);
    }

    @SuppressWarnings("unchecked")
    public String getUserRole(String userId) {
        String adminToken = getAdminToken();
        String getRolesUrl = String.format("%s/admin/realms/neuroforge-realm/users/%s/role-mappings/realm", keycloakUrl, userId);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<Void> getRequest = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<List> rolesRes = restTemplate.exchange(getRolesUrl, HttpMethod.GET, getRequest, List.class);
            List<Map<String, Object>> currentRoles = rolesRes.getBody();
            if (currentRoles != null && !currentRoles.isEmpty()) {
                List<String> targetRoles = List.of("ADMIN", "ORGANIZATION_OWNER", "TEAM_LEAD", "DEVELOPER", "QA", "STAKEHOLDER");
                for (Object roleObj : currentRoles) {
                    if (roleObj instanceof Map) {
                        Map<String, Object> role = (Map<String, Object>) roleObj;
                        String roleName = (String) role.get("name");
                        if (targetRoles.contains(roleName)) {
                            return roleName;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to fetch roles for user: {}", userId, e);
        }
        return "DEVELOPER"; // Fallback default
    }
}
