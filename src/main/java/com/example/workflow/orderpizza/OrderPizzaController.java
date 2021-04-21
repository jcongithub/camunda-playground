package com.example.workflow.orderpizza;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.camunda.bpm.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(path = "order")
@Slf4j
public class OrderPizzaController {
	@Autowired
	private RuntimeService runtimeService;
	private String workflowName = "camunda-playground-process";
	private AtomicInteger orderCount = new AtomicInteger();

	@PostMapping
	public int order(@RequestParam String customer, @RequestParam String note, @RequestParam int quantity) {
		log.info("Received an order from: {} quantity: {} for: {}", customer, quantity, note);
		
		Order order = Order.builder().orderId(orderCount.incrementAndGet()).custmoer(customer).note(note)
				.quantity(quantity).build();
		
		Map<String, Object> data = new TreeMap<>();
		data.put("order", order);
		
		runtimeService.startProcessInstanceByKey(workflowName, "bond-1", data);		
		
		return order.getOrderId();
	}
}
