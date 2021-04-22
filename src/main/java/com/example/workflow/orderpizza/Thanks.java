package com.example.workflow.orderpizza;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Thanks implements JavaDelegate{

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		Order order = (Order) execution.getVariable("order");
		log.info("Order completed: {}", order.getOrderId());		
	}

}
