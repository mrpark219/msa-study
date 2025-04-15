package com.example.orderservice.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class Schema {

	private String type;

	private List<Field> fields;

	private boolean optional;

	private String name;
}
