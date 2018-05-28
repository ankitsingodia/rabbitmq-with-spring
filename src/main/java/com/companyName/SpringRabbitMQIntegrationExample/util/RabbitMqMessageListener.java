package com.companyName.SpringRabbitMQIntegrationExample.util;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;


@Component
public class RabbitMqMessageListener {

	@RabbitListener(queues = Constants.SNS_QUEUE)
	@RabbitHandler
	public String subscribeToRequestQueue(@Payload String sampleRequestMessage, Message message) throws InterruptedException{
		System.out.println("I'm the subscriber for sns queue");
		return "Thanks! I Got the message with message id: " + message.getMessageProperties().getMessageId();
	
	}
	
	@RabbitListener(queues = Constants.SNS_QUEUE_2)
	@RabbitHandler
	public void subscribeToRequestQueue2(@Payload String sampleRequestMessage, Message message) throws Exception{
		
		System.out.println("I'm the subscriber for sns queue 2");
	}
	
	
	//useful in case you set the AcknowledgeMode to MANUAL mode in SimpleMessageListenerContainer for the reply queue
	/*@RabbitListener(queues = Constants.REPLY_QUEUE)
	@RabbitHandler
	public void subscribeToReplyQueue(@Payload String sampleRequestMessage, Message message){

		//message: this is the full payload. Use if needed.
		System.out.println("Acknowledgement received in reply queue: " + sampleRequestMessage + " for message with id: " + message.getMessageProperties().getMessageId());
		
	}*/
	
	

}

