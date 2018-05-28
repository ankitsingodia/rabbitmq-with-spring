package com.companyName.SpringRabbitMQIntegrationExample.config;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ConfirmCallback;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ReturnCallback;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@EnableRabbit
@Configuration
public class RabbitMQSyncConfiguration {

	
	/**
	 * Note:
	 * <li> setPublisherConfirms has to be kept false here because in {@link #rabbitTemplate()} we are defining the confirm callbacks
	 * @return
	 */
	@Bean
	public ConnectionFactory connectionFactory() {
		CachingConnectionFactory connectionFactory = new CachingConnectionFactory("localhost");
		connectionFactory.setPublisherReturns(true);	//to enable the return callback (which is usually called in case messgae failed to be routed from exchange to the queue)
//		connectionFactory.setPublisherConfirms(true);//either this can be true or channelTransacted can be true(in rabbittemplate) otherwise exception: cannot switch from tx to confirm mode
		return connectionFactory;
	}

	@Bean
	public AmqpAdmin amqpAdmin() {
		return new RabbitAdmin(connectionFactory());
	}

	
	/**
	 * Notes:
	 * 
	 * <li> if mandatory is not set to true then the return callback would never be called in case of incorrect queue name
	 * <li> channel transacted is kept true to support @transactional serivces ,i.e,if we want rabbit operations to be executed at the end of transaction to regard the atomicity. 
	 * <li> if channel transacted is kept true then note than the confirmation callback would not be called in case of successful publishing, but will be called in case of error at the exchange level.(for example wrong exchange name)
	 * <li> return callback will be called if the message fails to be routed to the queue.<br><br>
	 * 
	 * Troubleshooting:
	 * <li> either channelTransacted can be true or setPublisherConfirms can be true(in connectionFactory) otherwise <b>exception: publisher confirm channel callback is closed.cannot switch from tx to confirm mode</b>
	 * <li> Exception: <b>rabbit template can have at most one publish confirm callback.</b> This occurs when rabbit template 
	 * have more than 1 confirm callback. Lets say we have defined a rabbit template bean with confirm callback as below. Then 
	 * <ol><li> Same rabbit template is injected while defining asyncrabbittemplate and then
	 * again we write publish confirm as true (see bookmark1) OR 
	 * <li> Publisher confirm in the connection factory is true.
	 * 
	 * @return
	 */
	@Bean(name="rabbitsynctemplate")
	public RabbitTemplate rabbitTemplate() {
		RabbitTemplate rabbitTemplate =  new RabbitTemplate(connectionFactory());
		rabbitTemplate.setMandatory(true);
		rabbitTemplate.setChannelTransacted(true);
		rabbitTemplate.setConfirmCallback(new ConfirmCallback() {
			
			@Override
			public void confirm(CorrelationData correlationData, boolean ack, String cause) {
				
				System.out.println("Confirmation block reached with ack: "  + " " + ack);
				
			}
		});
		
		//this has to have mandate as true otherwise it wont work
		rabbitTemplate.setReturnCallback(new ReturnCallback() {
			
			@Override
			public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
				System.out.println("Return callback reached..");
			}
		});
		
		return rabbitTemplate;
	}

	/**
	 * bookmark1
	 * 
	 */
	/*@Bean
	AsyncRabbitTemplate template() {
		RabbitTemplate rabbit = rabbitTemplate();
		
		AsyncRabbitTemplate asyncRabbitTemplate = new AsyncRabbitTemplate(rabbit, rpcReplyMessageListenerContainer(connectionFactory()));
		asyncRabbitTemplate.setEnableConfirms(true);  
		asyncRabbitTemplate.setMandatory(true);			
																			
		return asyncRabbitTemplate;
	}*/
	
	@Bean(name="rabbitListenerContainerFactory")
	 public SimpleRabbitListenerContainerFactory listenerFactory(){
	  SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
	  factory.setConnectionFactory(connectionFactory());
	  return factory;
	 }
	

}
