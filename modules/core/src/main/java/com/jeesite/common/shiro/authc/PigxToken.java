package com.jeesite.common.shiro.authc;

import lombok.Setter;

import java.util.Map;

public class PigxToken extends FormToken{
    @Setter
    private String code;

    public PigxToken(){

    }

    public PigxToken(String username, char[] password, boolean rememberMe,
                     String host, Map<String, Object> params) {
        super(username, password, rememberMe, null, host, params);
    }
}
