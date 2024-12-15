package com.celfocus.hiring.kickstarter.db.repo;

import com.celfocus.hiring.kickstarter.db.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    List<UserEntity> findByUsername(String username);
    List<UserEntity> findByUsernameAndPassword(String username, String password);
}
