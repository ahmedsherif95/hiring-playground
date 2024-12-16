package com.celfocus.hiring.kickstarter.api;


import com.celfocus.hiring.kickstarter.db.entity.UserEntity;
import com.celfocus.hiring.kickstarter.db.repo.UserRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService implements UserDetailsService {

    protected final Log logger = LogFactory.getLog(this.getClass());
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByUsername(username);
        if (userEntity == null) {
            throw new UsernameNotFoundException("User not found!");
        }
        // Use the role from UserEntity
        List<String> roles = userEntity.getRoles();
        if (roles.isEmpty()) {
            logger.warn("No roles found for user: " + username);
        } else {
            logger.info("Roles for user " + username + ": " + roles);
        }
        String[] rolesArray = roles.toArray(new String[0]);

        return User
                .withUsername(userEntity.getUsername())
                .password(userEntity.getPassword())
                .roles(rolesArray)
                .build();
    }

}
