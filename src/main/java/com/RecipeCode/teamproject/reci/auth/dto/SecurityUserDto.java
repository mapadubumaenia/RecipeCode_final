package com.RecipeCode.teamproject.reci.auth.dto;


import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.Collections;

@Getter
@Setter
public class SecurityUserDto extends User  {


    public SecurityUserDto(String userEmail, String password, Collection<? extends GrantedAuthority> authorities) {
        super(userEmail, password, authorities);
    }
}
