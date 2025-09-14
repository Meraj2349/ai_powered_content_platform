package org.example.repository;

import org.example.model.Content;
import org.example.model.ContentStatus;
import org.example.model.ContentType;
import org.example.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Content entity
 */
@Repository
public interface ContentRepository extends MongoRepository<Content, String> {

    Page<Content> findByAuthor(User author, Pageable pageable);

    Page<Content> findByStatus(ContentStatus status, Pageable pageable);

    Page<Content> findByContentType(ContentType contentType, Pageable pageable);

    Page<Content> findByStatusAndContentType(ContentStatus status, ContentType contentType, Pageable pageable);

    List<Content> findByTagsContaining(String tag);

    List<Content> findByTitleContainingIgnoreCaseOrBodyContainingIgnoreCase(String title, String body);
}
