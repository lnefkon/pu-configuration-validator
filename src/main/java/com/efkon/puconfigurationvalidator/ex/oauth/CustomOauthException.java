package com.efkon.puconfigurationvalidator.ex.oauth;

import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@SuppressWarnings("serial")
@JsonSerialize(using = CustomOauthExceptionSerializer.class)
public class CustomOauthException extends OAuth2Exception {
	
    public CustomOauthException(String msg) {
        super(msg);
    }
    
}