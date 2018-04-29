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
	public ModelAndView welcome()  {

		for (int i = 0; i < 10; i++) {
			eventPublisherService.publishEvent("This the message to be published");
		}
		
		return new ModelAndView("index", "message", "In Action");
	}

}
