package com.companyName.SpringRabbitMQIntegrationExample.util;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@EnableRabbit
@Component
public class RabbitMqMessageListener {

	@RabbitListener(queues = Constants.SNS_QUEUE)
	@RabbitHandler
	public String subscribeToRequestQueue(@Payload String sampleRequestMessage, Message message) throws InterruptedException{
		System.out.println("I'm the subscriber..");
		return "Thanks! I Got the message with message id: " + message.getMessageProperties().getMessageId();
	
	}
	
	
	//useful in case you set the AcknowledgeMode to non-auto mode in SimpleMessageListenerContainer for the reply queue
	/*@RabbitListener(queues = Constants.REPLY_QUEUE)
	@RabbitHandler
	public void subscribeToReplyQueue(@Payload String sampleRequestMessage, Message message){

		//message: this is the full payload. Use if needed.
		System.out.println("Acknowledgement received in reply queue: " + sampleRequestMessage + " for message with id: " + message.getMessageProperties().getMessageId());
		
	}*/
	
	

}

