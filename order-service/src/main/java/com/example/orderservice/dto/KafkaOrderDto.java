package com.example.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
public class KafkaOrderDto implements Serializable {

	private Schema schema;

	private Payload payload;
}
