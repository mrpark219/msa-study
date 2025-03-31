package com.example.userservice.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ResponseOrder {

	private String productId;

	private Integer qty;

	private Integer unitPrice;

	private Integer totalPrice;

	private LocalDateTime createdAt;

	private String orderId;

	public ResponseOrder() {
	}
}
