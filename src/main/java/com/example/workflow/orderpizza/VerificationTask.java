package com.example.workflow.orderpizza;

import java.util.Date;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VerificationTask {
	private String executionId;
	private String assignee;
	private Date creationTime;
	private Date dueDate;
	private Order order;
}
