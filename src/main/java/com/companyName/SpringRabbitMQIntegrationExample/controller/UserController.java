package com.companyName.SpringRabbitMQIntegrationExample.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.companyName.SpringRabbitMQIntegrationExample.service.EventPublisherService;

@Controller
@RequestMapping("/")
public class UserController {
	@Autowired
	EventPublisherService eventPublisherService;

	@RequestMapping("/rabbitMq")
	public ModelAndView welcome() throws Exception  {

		eventPublisherService.publishEventInBulkAsync("This the message to be published asynchronously");
		
//		eventPublisherService.publishEventInBulkSync("This the message to be published synchronously");
		
		return new ModelAndView("index", "message", "In Action");
	}

}
