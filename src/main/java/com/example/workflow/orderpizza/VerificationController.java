package com.example.workflow.orderpizza;

import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(path = "verification")
@Slf4j
public class VerificationController {
	@Autowired
	private TaskService taskService;
	@Autowired
	private RuntimeService runtimeService;

	@GetMapping(path = "/list")
	public List<VerificationTask> listAllVerificationTasks() {
		log.info("List all verfication tasks");
		List<Task> tasks = taskService.createTaskQuery().taskName("Verification").active().list();

		List<VerificationTask> allVerficationTasks = tasks.stream().map(task -> {
			String executionId = task.getExecutionId();
			Order order = (Order) runtimeService.getVariable(executionId, "order");
			return VerificationTask.builder().order(order).creationTime(task.getCreateTime())
					.dueDate(task.getDueDate()).assignee(task.getAssignee()).executionId(executionId).build();
		}).sorted((t1, t2)->{return t1.getCreationTime().compareTo(t2.getCreationTime());}).collect(Collectors.toList());

		log.info("Active verification tasks: {}", allVerficationTasks.size());
		return allVerficationTasks;
	}
	
	@PostMapping(path="verify")
	public void verify(@RequestParam String executionId, @RequestParam boolean passed) {
		TaskQuery query = taskService.createTaskQuery().executionId(executionId);
		if(query != null) {
			List<Task> tasks = query.list();
			tasks.forEach(task->{
				taskService.complete(task.getId());
				log.info("Task: {} is completed", task.getExecutionId());
			});
			
		} else {
			throw new RuntimeException("bad parameters");
		}		
	}
	
	
}
