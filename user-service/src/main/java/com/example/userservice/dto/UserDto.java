package com.example.userservice.dto;

import com.example.userservice.vo.ResponseOrder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class UserDto {

	private String email;

	private String name;

	private String pwd;

	private String userId;

	private LocalDateTime createdAt;

	private String encryptedPwd;

	private List<ResponseOrder> orders;
}
