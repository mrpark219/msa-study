package com.example.userservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserDto {

	private String email;

	private String name;

	private String pwd;

	private String userId;

	private LocalDateTime createdAt;

	private String encryptedPwd;
}
