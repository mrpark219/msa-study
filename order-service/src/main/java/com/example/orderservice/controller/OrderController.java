package com.example.orderservice.controller;

import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.jpa.OrderEntity;
import com.example.orderservice.service.OrderService;
import com.example.orderservice.vo.RequestOrder;
import com.example.orderservice.vo.ResponseOrder;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/order-service")
public class OrderController {

	private final Environment env;

	private final OrderService orderService;

	public OrderController(Environment env, OrderService orderService) {
		this.env = env;
		this.orderService = orderService;
	}

	@PostMapping("/{userId}/orders")
	public ResponseEntity<ResponseOrder> createOrder(@PathVariable String userId, @RequestBody RequestOrder requestOrder) {

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

		OrderDto orderDto = mapper.map(requestOrder, OrderDto.class);
		orderDto.setUserId(userId);
		OrderDto createOrder = orderService.createOrder(orderDto);

		ResponseOrder responseOrder = mapper.map(createOrder, ResponseOrder.class);

		return ResponseEntity.status(HttpStatus.CREATED).body(responseOrder);
	}

	@GetMapping("/{userId}/orders")
	public ResponseEntity<List<ResponseOrder>> getOrder(@PathVariable String userId) {

		Iterable<OrderEntity> orderList = orderService.getOrdersByUserId(userId);

		List<ResponseOrder> result = new ArrayList<>();
		orderList.forEach(v -> {
			result.add(new ModelMapper().map(v, ResponseOrder.class));
		});

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@GetMapping("/health-check")
	public String status() {
		return String.format("It's Working in Order Service on PORT %s", env.getProperty("local.server.port"));
	}
}
