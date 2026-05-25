package com.kgu.life_watch.global.security;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.kgu.life_watch.domain.user.entity.User;

/**
 * @param user 실제 사용자 정보 보관
 */
public record CustomUserDetails(User user) implements UserDetails {

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.emptyList();
  }

  @Override
  public String getPassword() {
    return user.getPassword(); // 인증 시 사용할 비밀번호
  }

  @Override
  public String getUsername() {
    return user.getLoginId(); // 인증 시 사용할 아이디
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
