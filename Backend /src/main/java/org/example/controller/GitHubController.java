package org.example.controller;

import java.util.Map;

import org.example.service.GitHubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * GitHub API Controller for SkillMate AI Platform
 * Provides endpoints for GitHub integration and educational content discovery
 */
@RestController
@RequestMapping("/api/github")
@CrossOrigin(origins = "*")
public class GitHubController {

    @Autowired
    private GitHubService gitHubService;

    /**
     * Test GitHub API connection
     * GET /api/github/test
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testConnection() {
        Map<String, Object> result = gitHubService.testConnection();
        return ResponseEntity.ok(result);
    }

    /**
     * Search educational repositories
     * GET /api/github/search?q={query}&language={language}&limit={limit}
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchRepositories(
            @RequestParam String q,
            @RequestParam(required = false) String language,
            @RequestParam(defaultValue = "10") int limit) {

        Map<String, Object> result = gitHubService.searchEducationalRepositories(q, language, limit);
        return ResponseEntity.ok(result);
    }

    /**
     * Get repository details
     * GET /api/github/repo/{owner}/{repo}
     */
    @GetMapping("/repo/{owner}/{repo}")
    public ResponseEntity<Map<String, Object>> getRepositoryDetails(
            @PathVariable String owner,
            @PathVariable String repo) {

        Map<String, Object> result = gitHubService.getRepositoryDetails(owner, repo);
        return ResponseEntity.ok(result);
    }

    /**
     * Get trending repositories
     * GET /api/github/trending?language={language}&timeframe={timeframe}
     */
    @GetMapping("/trending")
    public ResponseEntity<Map<String, Object>> getTrendingRepositories(
            @RequestParam(required = false) String language,
            @RequestParam(defaultValue = "weekly") String timeframe) {

        Map<String, Object> result = gitHubService.getTrendingRepositories(language, timeframe);
        return ResponseEntity.ok(result);
    }

    /**
     * Search educational content by topic
     * GET /api/github/education?topic={topic}&language={language}
     */
    @GetMapping("/education")
    public ResponseEntity<Map<String, Object>> searchEducationalContent(
            @RequestParam String topic,
            @RequestParam(required = false) String language) {

        String educationalQuery = String.format("%s tutorial course learning", topic);
        Map<String, Object> result = gitHubService.searchEducationalRepositories(educationalQuery, language, 15);
        return ResponseEntity.ok(result);
    }

    /**
     * Get GitHub API status and configuration
     * GET /api/github/status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = Map.of(
                "service", "GitHub API Integration",
                "version", "1.0.0",
                "endpoints", Map.of(
                        "test", "/api/github/test",
                        "search", "/api/github/search?q={query}&language={language}&limit={limit}",
                        "repo_details", "/api/github/repo/{owner}/{repo}",
                        "trending", "/api/github/trending?language={language}&timeframe={timeframe}",
                        "education", "/api/github/education?topic={topic}&language={language}"),
                "features", new String[] {
                        "Repository Search",
                        "Educational Content Discovery",
                        "Trending Analysis",
                        "Repository Details Extraction",
                        "README Content Analysis"
                });
        return ResponseEntity.ok(status);
    }
}
