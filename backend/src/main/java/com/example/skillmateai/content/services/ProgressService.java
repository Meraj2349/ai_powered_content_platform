package com.example.skillmateai.content.services;

import com.example.skillmateai.content.entities.UserCourseProgressEntity;
import com.example.skillmateai.content.repositories.TopicRepository;
import com.example.skillmateai.content.repositories.UserCourseProgressRepository;
import com.example.skillmateai.user.entities.UserEntity;
import com.example.skillmateai.user.utilities.GetAuthenticatedUserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProgressService {

    private final TopicRepository topicRepository;
    private final UserCourseProgressRepository userCourseProgressRepository;
    private final GetAuthenticatedUserUtil getAuthenticatedUserUtil;

    public UserCourseProgressEntity getUserProgress(String coursePathId){
        try {
            UserEntity user = getAuthenticatedUserUtil.getAuthenticatedUser();
            if(user == null){
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
            }

            return userCourseProgressRepository.findByUserIdAndCoursePathId(user.getId(), coursePathId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No progress found for this course path"));
        } catch (ResponseStatusException e){
            throw e;
        } catch (Exception e){
            log.error("Unexpected error fetching user progress: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error fetching progress");
        }
    }
}
