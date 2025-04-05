package com.example.userservice.security;

import com.example.userservice.service.UserService;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;

@EnableWebSecurity
@Configuration
public class WebSecurity {

	private final UserService userService;

	private final BCryptPasswordEncoder passwordEncoder;

	private final Environment env;

	public WebSecurity(UserService userService, BCryptPasswordEncoder passwordEncoder, Environment env) {
		this.userService = userService;
		this.passwordEncoder = passwordEncoder;
		this.env = env;
	}

	@Bean
	public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
		AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
		builder.userDetailsService(userService).passwordEncoder(passwordEncoder);
		return builder.build();
	}

	@Bean
	protected SecurityFilterChain configure(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {

		http
			.csrf(AbstractHttpConfigurer::disable)
			.headers((headers) ->
				headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
			)
			.authorizeHttpRequests(authorizationHttpRequests -> authorizationHttpRequests
				.requestMatchers("/actuator/**").permitAll()
				.requestMatchers("/**").access(
					new WebExpressionAuthorizationManager(
						"hasIpAddress('192.168.0.3')" // 로컬 컴퓨터 IP
					)
				)
				.requestMatchers(PathRequest.toH2Console()).permitAll()
			)
			.addFilter(getAuthenticationFilter(authenticationManager));

		return http.build();
	}

	private AuthenticationFilter getAuthenticationFilter(AuthenticationManager authenticationManager) {
		return new AuthenticationFilter(authenticationManager, userService, env);
	}

}
