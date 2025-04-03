package com.example.userservice.security;

import com.example.userservice.dto.UserDto;
import com.example.userservice.service.UserService;
import com.example.userservice.vo.RequestLogin;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Objects;

@Slf4j
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private final UserService userService;

	private final Environment env;

	public AuthenticationFilter(AuthenticationManager authenticationManager, UserService userService, Environment env) {
		super.setAuthenticationManager(authenticationManager);
		this.userService = userService;
		this.env = env;
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

		try {
			RequestLogin credential = new ObjectMapper().readValue(request.getInputStream(), RequestLogin.class);

			return getAuthenticationManager().authenticate(
				new UsernamePasswordAuthenticationToken(credential.getEmail(), credential.getPassword(), new ArrayList<>()));
		}
		catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {

		String username = ((User) (authResult.getPrincipal())).getUsername();
		UserDto userDetails = userService.getUserDetailsByEmail(username);

		byte[] keyBytes = Decoders.BASE64.decode(env.getProperty("token.secret"));
		SecretKey secretKey = Keys.hmacShaKeyFor(keyBytes);

		Instant now = Instant.now();

		String token = Jwts.builder()
			.subject(userDetails.getUserId())
			.expiration(Date.from(now.plusMillis(Long.parseLong(env.getProperty("token.expiration_time")))))
			.issuedAt(Date.from(now))
			.signWith(secretKey)
			.compact();

		response.addHeader("token", token);
		response.addHeader("userId", userDetails.getUserId());
	}
}
