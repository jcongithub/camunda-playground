package com.example.workflow.orderpizza;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Order implements Serializable{
	private static final long serialVersionUID = 1L;
	private int orderId;
	private String custmoer;
	private String note;
	private int quantity;
	private boolean verified;
}
