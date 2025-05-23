package com.example.orderservice.controller;

import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.jpa.OrderEntity;
import com.example.orderservice.messagequeue.KafkaProducer;
import com.example.orderservice.messagequeue.OrderProducer;
import com.example.orderservice.service.OrderService;
import com.example.orderservice.vo.RequestOrder;
import com.example.orderservice.vo.ResponseOrder;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/order-service")
public class OrderController {

	private final Environment env;
	private final OrderService orderService;
	private final KafkaProducer kafkaProducer;
	private final OrderProducer orderProducer;

	public OrderController(Environment env, OrderService orderService, KafkaProducer kafkaProducer, OrderProducer orderProducer) {
		this.env = env;
		this.orderService = orderService;
		this.kafkaProducer = kafkaProducer;
		this.orderProducer = orderProducer;
	}

	@PostMapping("/{userId}/orders")
	public ResponseEntity<ResponseOrder> createOrder(@PathVariable String userId, @RequestBody RequestOrder requestOrder) {

		log.info("Before add orders data");

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

		OrderDto orderDto = mapper.map(requestOrder, OrderDto.class);
		orderDto.setUserId(userId);

		orderDto.setOrderId(UUID.randomUUID().toString());
		orderDto.setTotalPrice(requestOrder.getQty() * requestOrder.getUnitPrice());

		kafkaProducer.send("example-catalog-topic", orderDto);
		orderProducer.send("orders", orderDto);

		ResponseOrder responseOrder = mapper.map(orderDto, ResponseOrder.class);

		log.info("After added orders data");

		return ResponseEntity.status(HttpStatus.CREATED).body(responseOrder);
	}

	@GetMapping("/{userId}/orders")
	public ResponseEntity<List<ResponseOrder>> getOrder(@PathVariable String userId) {

		log.info("Before retrieve orders data");

		Iterable<OrderEntity> orderList = orderService.getOrdersByUserId(userId);

		List<ResponseOrder> result = new ArrayList<>();
		orderList.forEach(v -> {
			result.add(new ModelMapper().map(v, ResponseOrder.class));
		});

		log.info("Add retrieved orders data");

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@GetMapping("/health-check")
	public String status() {
		return String.format("It's Working in Order Service on PORT %s", env.getProperty("local.server.port"));
	}
}
