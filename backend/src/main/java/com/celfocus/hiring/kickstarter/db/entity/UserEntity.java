package com.celfocus.hiring.kickstarter.db.entity;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "USERS")
public class UserEntity implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String username;
    private String password;


    @Column(name = "roles")
    private String roles;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private CartEntity cartEntity;


    public UserEntity(Integer id, String username, String password, List<String> roles) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.setRoles(roles);
    }

    public UserEntity() {}

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
//        return UserDetails.super.isAccountNonExpired();
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles != null && !roles.isEmpty()
                ? getRoles().stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)).collect(Collectors.toList())
                : Collections.emptyList();  // Return empty authorities if no roles
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getRoles() {
        return roles != null && !roles.isEmpty() ? Arrays.asList(roles.split(",")) : Collections.emptyList();

    }

    public void setRoles(List<String> roles) {
//        this.roles = roles != null ? String.join(",", roles) : null;
        if (roles == null || roles.isEmpty()) {
            this.roles = "";
        } else {
            this.roles = roles.stream()
                    .map(String::trim)  // Remove any extra spaces around roles
                    .filter(role -> !role.isEmpty())  // Remove empty roles
                    .collect(Collectors.joining(","));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEntity that = (UserEntity) o;
        return username.equals(that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

}
