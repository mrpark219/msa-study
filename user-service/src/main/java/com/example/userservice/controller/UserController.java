package com.example.userservice.controller;

import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.service.UserService;
import com.example.userservice.vo.Greeting;
import com.example.userservice.vo.RequestUser;
import com.example.userservice.vo.ResponseUser;
import io.micrometer.core.annotation.Timed;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/")
public class UserController {

	private final Environment env;
	private final Greeting greeting;
	private final UserService userService;

	public UserController(Environment env, Greeting greeting, UserService userService) {
		this.env = env;
		this.greeting = greeting;
		this.userService = userService;
	}

	@PostMapping("/users")
	public ResponseEntity<ResponseUser> createUser(@RequestBody RequestUser user) {

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

		UserDto userDto = mapper.map(user, UserDto.class);
		userService.createUser(userDto);

		ResponseUser responseUser = mapper.map(userDto, ResponseUser.class);

		return ResponseEntity.status(HttpStatus.CREATED).body(responseUser);
	}

	@GetMapping("/users")
	public ResponseEntity<List<ResponseUser>> getUsers() {
		Iterable<UserEntity> userList = userService.getUserByAll();

		List<ResponseUser> result = new ArrayList<>();
		userList.forEach(v -> {
			result.add(new ModelMapper().map(v, ResponseUser.class));
		});

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@GetMapping("/users/{userId}")
	public ResponseEntity<ResponseUser> getUser(@PathVariable String userId) {

		UserDto userDto = userService.getUserByUserId(userId);

		ResponseUser returnValue = new ModelMapper().map(userDto, ResponseUser.class);

		return ResponseEntity.status(HttpStatus.OK).body(returnValue);
	}

	@GetMapping("/health-check")
	@Timed(value = "users.status", longTask = true)
	public String status() {
		return String.format("It's Working in User Service"
			+ ", port(local.server.port)=" + env.getProperty("local.server.port")
			+ ", port(server.port)=" + env.getProperty("server.port")
			+ ", token secret=" + env.getProperty("token.secret")
			+ ", token expiration time=" + env.getProperty("token.expiration_time"));
	}

	@GetMapping("/welcome")
	@Timed(value = "users.welcome", longTask = true)
	public String welcome() {
		//return env.getProperty("greeting.message");
		return greeting.getMessage();
	}
}
