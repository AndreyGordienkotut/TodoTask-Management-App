package com.userService.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String username;
    @Column(nullable = false,unique = true)
    private String email;
    @Column(nullable = false)
    private String password;
    public User(User user) {
        this.id = user.getId();
        this.username = user.getEmail();
        this.password = user.getPassword();
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null; // В простейшем случае пока можно оставить null или пустой список
    }

    @Override
    public String getPassword() {
        return password; // Возвращаем пароль пользователя
    }

    @Override
    public String getUsername() {
        return email; // В качестве "имени пользователя" используем email
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
