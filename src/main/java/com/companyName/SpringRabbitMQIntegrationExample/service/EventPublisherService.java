package com.companyName.SpringRabbitMQIntegrationExample.service;

import java.util.Date;
import java.util.UUID;

import org.springframework.amqp.core.AmqpMessageReturnedException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.AsyncRabbitTemplate;
import org.springframework.amqp.rabbit.AsyncRabbitTemplate.RabbitConverterFuture;
import org.springframework.amqp.rabbit.core.BatchingRabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.companyName.SpringRabbitMQIntegrationExample.util.Constants;


@Service
public class EventPublisherService {
	
	@Autowired
	AsyncRabbitTemplate asyncRabbitTemplate;
	
	public void publishEvent(Object message){
		
		for (int i = 0; i < 10; i++) {
			publish(message);
		}
	}
	

	private void publish(Object message){

		
		RabbitConverterFuture<String> future = this.asyncRabbitTemplate.convertSendAndReceive("",Constants.SNS_QUEUE, message,new MessagePostProcessor() {
		
			@Override
			public Message postProcessMessage(Message message) {
				message.getMessageProperties().setTimestamp(new Date());
				message.getMessageProperties().setMessageId(UUID.randomUUID().toString().substring(0,6));
				return message;
			}
		});
		
		
		/*
		 * The success block will be called when the messages reaches the "exchange" and the "queue" successfully
		 * The failure block is hit if there is some problem with the broker. For example: Incorrect/Non-Existing exchange name. This will also 
		 * hit the failure block of future callback with a timeout exception
		 * 
		 * ** This needs publisher confirm to true in configuration
		 * 
		 */
		ListenableFuture<Boolean> futureConfirm = future.getConfirm();
		futureConfirm.addCallback(new ListenableFutureCallback<Boolean>() {

			@Override
			public void onSuccess(Boolean result) {
				System.out.println("Publish Result " + result);
				
			}

			@Override
			public void onFailure(Throwable ex) {
				System.out.println("Publish Failed: " + ex);
			}
		});
		
		
		/*
		 * Note: 
		 * 
		 * This callback is usable once and that is just after publish is "tried"
		 *  
		 * Example: 
		 * 
		 * Success is called when reply is received from the receiver/subscriber. And thus acknowledged.
		 * 
		 * Failure can be called in multiple cases:
		 * 
		 * 1. Published a message. Listened. But the reply took time (say 5 sec). 
		 *    But timeout config was 3sec: So it will reach the failure block. But this message will be consumed at some point
		 *    of time as the message was successfully published. And then when the reply comes, the onsuccess won't be called. But it is consumed successfully.
		 * 
		 * 2. Wrong exchange name - the failure callback will be called with a timeout exception. This case will also hit the 
		 *    failure block of confirm callback above.
		 * 
		 * 3. In case wrong queue name/routing key is provided (But the message has reached the exchange 
		 *    as it passed the above confirm callback)   
		 *    then the failure block is reached with an exception. Now what kind of exception is received depends on 
		 *    "mandate" check. If enabled - AmqpMessageReturnedException is returned and if mandate is not enabled then failure 
		 *    block is hit with TimeOut exception.
		 *    
		 *    
		 * So considering 1 and 3 (with mandate disabled) -> The failure block can be reached due to timeout but 
		 * it do not convey if the msg was published or not. So you cannot blindly publish the msg again in the failure block.
		 * 
		 */
		future.addCallback(new ListenableFutureCallback<Object>() {

			@Override
			public void onSuccess(Object result) {
				
				System.out.println("Reply received from the subscriber and thus acknowledged: " + result);
			}

			@Override
			public void onFailure(Throwable ex) {
				
				System.out.println("Didn't got the reply..Failure occured..");
				ex.printStackTrace();
				if(ex instanceof AmqpMessageReturnedException){
					System.err.println(((AmqpMessageReturnedException) ex).getReturnedMessage().getMessageProperties().getMessageId());
				}
			}
		});
		
		//This is just a Java8 way of the above callback.
		/*future.addCallback( sampleResponseMessage -> 
									System.out.println("Response for request message:" + sampleResponseMessage),
									
									failure -> 
									System.out.println("Didn't got the reply..Failure occured.. " + failure.getMessage())
									);*/
		
		
		// The below commented block does exactly what the above block do, but asynchronously.
	   /* Object reply = null;
	    try {
	        reply = future.get();
	        System.out.println("Reply received and thus acknowledged: " +reply);
	    }
	    catch (ExecutionException e) {
	       System.out.println("Didn't got the reply..Failure occured..");
	       e.printStackTrace();
	    } catch (InterruptedException e) {
	    	 System.out.println("Didn't got the reply..Failure occured due to interruption");
			e.printStackTrace();
		}*/
		
	
	}
	
	
}


