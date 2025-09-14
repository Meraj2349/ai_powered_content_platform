package org.example.repository;

import org.example.model.Role;
import org.example.model.RoleName;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Role entity
 */
@Repository
public interface RoleRepository extends MongoRepository<Role, String> {

    Optional<Role> findByName(RoleName roleName);
}
