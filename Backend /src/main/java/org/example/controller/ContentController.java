package org.example.controller;

import java.util.Optional;

import org.example.dto.request.ContentGenerationRequest;
import org.example.dto.response.ApiResponse;
import org.example.dto.response.ContentGenerationResponse;
import org.example.model.Content;
import org.example.model.ContentStatus;
import org.example.model.User;
import org.example.repository.ContentRepository;
import org.example.repository.UserRepository;
import org.example.security.UserPrincipal;
import org.example.service.AIContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Content Controller
 * Handles content management and AI generation
 */
@RestController
@RequestMapping("/api/content")
public class ContentController {

    @Autowired
    private AIContentService aiContentService;

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/generate")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> generateContent(@RequestBody ContentGenerationRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            User user = userRepository.findById(userPrincipal.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            ContentGenerationResponse response = aiContentService.generateContent(request, user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to generate content: " + e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<Content>> getUserContent(@AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            User user = userRepository.findById(userPrincipal.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Content> contents = contentRepository.findByAuthor(user, pageable);

            return ResponseEntity.ok(contents);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getContentById(@PathVariable String id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            Optional<Content> content = contentRepository.findById(id);

            if (content.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Check if user owns the content or is admin
            if (!content.get().getAuthor().getId().equals(userPrincipal.getId()) &&
                    !userPrincipal.getAuthorities().stream()
                            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
                return ResponseEntity.status(403)
                        .body(new ApiResponse(false, "Access denied"));
            }

            return ResponseEntity.ok(content.get());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to retrieve content"));
        }
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> publishContent(@PathVariable String id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            Optional<Content> contentOpt = contentRepository.findById(id);

            if (contentOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Content content = contentOpt.get();

            // Check if user owns the content
            if (!content.getAuthor().getId().equals(userPrincipal.getId())) {
                return ResponseEntity.status(403)
                        .body(new ApiResponse(false, "Access denied"));
            }

            content.setStatus(ContentStatus.PUBLISHED);
            contentRepository.save(content);

            return ResponseEntity.ok(new ApiResponse(true, "Content published successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to publish content"));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteContent(@PathVariable String id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            Optional<Content> contentOpt = contentRepository.findById(id);

            if (contentOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Content content = contentOpt.get();

            // Check if user owns the content or is admin
            if (!content.getAuthor().getId().equals(userPrincipal.getId()) &&
                    !userPrincipal.getAuthorities().stream()
                            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
                return ResponseEntity.status(403)
                        .body(new ApiResponse(false, "Access denied"));
            }

            contentRepository.delete(content);

            return ResponseEntity.ok(new ApiResponse(true, "Content deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to delete content"));
        }
    }
}
