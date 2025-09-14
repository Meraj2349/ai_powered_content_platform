package org.example.service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * GitHub API Service for SkillMate AI Platform
 * Provides repository analysis, user data, and educational content discovery
 */
@Service
public class GitHubService {

    private static final Logger logger = LoggerFactory.getLogger(GitHubService.class);

    @Value("${github.api.token}")
    private String githubToken;

    @Value("${github.api.base-url:https://api.github.com}")
    private String baseUrl;

    @Value("${github.api.user-agent:SkillMate-AI-Platform}")
    private String userAgent;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GitHubService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Test GitHub API connection
     */
    public Map<String, Object> testConnection() {
        Map<String, Object> result = new HashMap<>();
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "/user", HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode userInfo = objectMapper.readTree(response.getBody());
                result.put("status", "✅ GitHub API Connected Successfully");
                result.put("user", userInfo.get("login").asText());
                result.put("name", userInfo.get("name").asText());
                result.put("public_repos", userInfo.get("public_repos").asInt());
                result.put("followers", userInfo.get("followers").asInt());
                result.put("following", userInfo.get("following").asInt());
                logger.info("GitHub API connection successful for user: {}", userInfo.get("login").asText());
            } else {
                result.put("status", "❌ GitHub API Connection Failed");
                result.put("error", "Unexpected response code: " + response.getStatusCode());
            }

        } catch (HttpClientErrorException e) {
            logger.error("GitHub API authentication failed: {}", e.getMessage());
            result.put("status", "❌ GitHub API Authentication Failed");
            result.put("error", "HTTP " + e.getStatusCode() + ": " + e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("GitHub API connection error: {}", e.getMessage());
            result.put("status", "❌ GitHub API Connection Error");
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * Search for educational repositories
     */
    public Map<String, Object> searchEducationalRepositories(String query, String language, int limit) {
        Map<String, Object> result = new HashMap<>();
        try {
            String searchQuery = String.format("%s language:%s sort:stars", query, language != null ? language : "");
            String url = String.format("%s/search/repositories?q=%s&per_page=%d",
                    baseUrl, searchQuery.replace(" ", "+"), Math.min(limit, 100));

            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode searchResult = objectMapper.readTree(response.getBody());
                List<Map<String, Object>> repositories = new ArrayList<>();

                for (JsonNode repo : searchResult.get("items")) {
                    Map<String, Object> repoInfo = new HashMap<>();
                    repoInfo.put("name", repo.get("name").asText());
                    repoInfo.put("full_name", repo.get("full_name").asText());
                    repoInfo.put("description", repo.has("description") ? repo.get("description").asText() : "");
                    repoInfo.put("html_url", repo.get("html_url").asText());
                    repoInfo.put("language", repo.has("language") ? repo.get("language").asText() : "Unknown");
                    repoInfo.put("stars", repo.get("stargazers_count").asInt());
                    repoInfo.put("forks", repo.get("forks_count").asInt());
                    repoInfo.put("updated_at", repo.get("updated_at").asText());
                    repositories.add(repoInfo);
                }

                result.put("status", "✅ Search Successful");
                result.put("total_count", searchResult.get("total_count").asInt());
                result.put("repositories", repositories);
                logger.info("Found {} educational repositories for query: {}", repositories.size(), query);
            }

        } catch (Exception e) {
            logger.error("GitHub repository search failed: {}", e.getMessage());
            result.put("status", "❌ Search Failed");
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * Get repository details and README for course content extraction
     */
    public Map<String, Object> getRepositoryDetails(String owner, String repo) {
        Map<String, Object> result = new HashMap<>();
        try {
            String url = String.format("%s/repos/%s/%s", baseUrl, owner, repo);

            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode repoData = objectMapper.readTree(response.getBody());

                result.put("status", "✅ Repository Details Retrieved");
                result.put("name", repoData.get("name").asText());
                result.put("full_name", repoData.get("full_name").asText());
                result.put("description", repoData.has("description") ? repoData.get("description").asText() : "");
                result.put("language", repoData.has("language") ? repoData.get("language").asText() : "Unknown");
                result.put("stars", repoData.get("stargazers_count").asInt());
                result.put("forks", repoData.get("forks_count").asInt());
                result.put("topics", repoData.get("topics"));
                result.put("license",
                        repoData.has("license") && !repoData.get("license").isNull()
                                ? repoData.get("license").get("name").asText()
                                : "No License");

                // Get README content
                try {
                    String readmeUrl = String.format("%s/repos/%s/%s/readme", baseUrl, owner, repo);
                    ResponseEntity<String> readmeResponse = restTemplate.exchange(
                            readmeUrl, HttpMethod.GET, entity, String.class);

                    if (readmeResponse.getStatusCode() == HttpStatus.OK) {
                        JsonNode readmeData = objectMapper.readTree(readmeResponse.getBody());
                        String readmeContent = new String(Base64.getDecoder().decode(
                                readmeData.get("content").asText().replace("\n", "")));
                        result.put("readme", readmeContent.substring(0, Math.min(readmeContent.length(), 2000)));
                    }
                } catch (Exception e) {
                    result.put("readme", "README not available");
                }

                logger.info("Retrieved details for repository: {}/{}", owner, repo);
            }

        } catch (Exception e) {
            logger.error("Failed to get repository details: {}", e.getMessage());
            result.put("status", "❌ Failed to get repository details");
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * Get trending repositories for course discovery
     */
    public Map<String, Object> getTrendingRepositories(String language, String timeframe) {
        Map<String, Object> result = new HashMap<>();
        try {
            String dateQuery = getTrendingDateQuery(timeframe);
            String searchQuery = String.format("created:%s", dateQuery);
            if (language != null && !language.isEmpty()) {
                searchQuery += String.format(" language:%s", language);
            }
            searchQuery += " sort:stars order:desc";

            String url = String.format("%s/search/repositories?q=%s&per_page=20",
                    baseUrl, searchQuery.replace(" ", "+"));

            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode searchResult = objectMapper.readTree(response.getBody());
                List<Map<String, Object>> repositories = new ArrayList<>();

                for (JsonNode repo : searchResult.get("items")) {
                    Map<String, Object> repoInfo = new HashMap<>();
                    repoInfo.put("name", repo.get("name").asText());
                    repoInfo.put("full_name", repo.get("full_name").asText());
                    repoInfo.put("description", repo.has("description") ? repo.get("description").asText() : "");
                    repoInfo.put("html_url", repo.get("html_url").asText());
                    repoInfo.put("language", repo.has("language") ? repo.get("language").asText() : "Unknown");
                    repoInfo.put("stars", repo.get("stargazers_count").asInt());
                    repoInfo.put("forks", repo.get("forks_count").asInt());
                    repositories.add(repoInfo);
                }

                result.put("status", "✅ Trending Repositories Retrieved");
                result.put("timeframe", timeframe);
                result.put("language", language);
                result.put("repositories", repositories);
                logger.info("Retrieved {} trending repositories for {}", repositories.size(), timeframe);
            }

        } catch (Exception e) {
            logger.error("Failed to get trending repositories: {}", e.getMessage());
            result.put("status", "❌ Failed to get trending repositories");
            result.put("error", e.getMessage());
        }

        return result;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + githubToken);
        headers.set("User-Agent", userAgent);
        headers.set("Accept", "application/vnd.github.v3+json");
        return headers;
    }

    private String getTrendingDateQuery(String timeframe) {
        Calendar cal = Calendar.getInstance();
        switch (timeframe.toLowerCase()) {
            case "daily":
                cal.add(Calendar.DAY_OF_MONTH, -1);
                break;
            case "weekly":
                cal.add(Calendar.WEEK_OF_YEAR, -1);
                break;
            case "monthly":
                cal.add(Calendar.MONTH, -1);
                break;
            default:
                cal.add(Calendar.WEEK_OF_YEAR, -1);
        }
        return String.format(">%04d-%02d-%02d",
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH));
    }
}
