package com.efkon.puconfigurationvalidator.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;

import com.efkon.puconfigurationvalidator.ex.oauth.CustomOauthException;

@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

	@Value("${client.id}")
	private String clientId;

	@Value("${client.secret}")
	private String clientSecret;

	private final AuthenticationManager authenticationManager;
	private final PasswordEncoder passwordEncoder;
	private final UserDetailsService userService;

	public AuthorizationServerConfiguration(AuthenticationManager authenticationManager,
			PasswordEncoder passwordEncoder, UserDetailsService userService) {
		this.authenticationManager = authenticationManager;
		this.passwordEncoder = passwordEncoder;
		this.userService = userService;
	}

	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		clients.inMemory().withClient(clientId).accessTokenValiditySeconds(-1).scopes("read", "write")
				.authorizedGrantTypes("password").secret(passwordEncoder.encode(clientSecret))
				.resourceIds("pu-configuration-validator-api");
	}

	@Override
	public void configure(final AuthorizationServerEndpointsConfigurer endpoints) {
		endpoints.authenticationManager(authenticationManager).userDetailsService(userService)
				.exceptionTranslator(exception -> {
					if (exception instanceof OAuth2Exception) {
						OAuth2Exception oAuth2Exception = (OAuth2Exception) exception;
						return ResponseEntity.status(oAuth2Exception.getHttpErrorCode())
								.body(new CustomOauthException(oAuth2Exception.getMessage()));
					} else {
						throw exception;
					}
				});

	}

}
