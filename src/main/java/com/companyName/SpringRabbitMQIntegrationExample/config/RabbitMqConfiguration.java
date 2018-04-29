package com.companyName.SpringRabbitMQIntegrationExample.config;


import java.util.concurrent.Executors;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.AsyncRabbitTemplate;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.companyName.SpringRabbitMQIntegrationExample.util.Constants;

@Configuration
public class RabbitMqConfiguration {
	@Bean
	public ConnectionFactory connectionFactory() {
		CachingConnectionFactory connectionFactory = new CachingConnectionFactory("localhost");
//		connectionFactory.setChannelCacheSize(1000);
		connectionFactory.setPublisherConfirms(true);
		connectionFactory.setPublisherReturns(true);
		return connectionFactory;
	}

	@Bean
	public AmqpAdmin amqpAdmin() {
		return new RabbitAdmin(connectionFactory());
	}

	@Bean
	public RabbitTemplate rabbitTemplate() {
		return new RabbitTemplate(connectionFactory());
	}

	@Bean
	public Queue myQueue() {
		return new Queue(Constants.SNS_QUEUE);
	}
	@Bean
    public SimpleMessageListenerContainer rpcReplyMessageListenerContainer(ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer simpleMessageListenerContainer = new SimpleMessageListenerContainer(connectionFactory);
        simpleMessageListenerContainer.setQueueNames(Constants.REPLY_QUEUE);
//      simpleMessageListenerContainer.setReceiveTimeout(2000); //useful only when acknowledge mode is not set to true
        simpleMessageListenerContainer.setTaskExecutor(Executors.newCachedThreadPool());	//you can define your own task executor
        simpleMessageListenerContainer.setAcknowledgeMode(AcknowledgeMode.AUTO);
        return simpleMessageListenerContainer;
    }
	
	@Bean
	AsyncRabbitTemplate template() {
		RabbitTemplate rabbit = rabbitTemplate();
//		rabbit.setChannelTransacted(true);       
		
		AsyncRabbitTemplate asyncRabbitTemplate = new AsyncRabbitTemplate(rabbit, rpcReplyMessageListenerContainer(connectionFactory()));
		asyncRabbitTemplate.setEnableConfirms(true);	//this requires publisher confirms set to true and is done by setting publisherConfirm to true on the CachingConnectionFactory
		asyncRabbitTemplate.setMandatory(true);			//if the message cannot be delivered to a queue an AmqpMessageReturnedException will be thrown~
																			//This feature uses publisher returns and is enabled by setting publisherReturns to true on the CachingConnectionFactory~~ this can also be done at the message level using property mandatory-expression
//		asyncRabbitTemplate.setReceiveTimeout(3000);	//By default, the send and receive methods will timeout after 5 seconds
		return asyncRabbitTemplate;
	}
	
	@Bean(name="rabbitListenerContainerFactory")
	 public SimpleRabbitListenerContainerFactory listenerFactory(){
	  SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
	  factory.setConnectionFactory(connectionFactory());
	  return factory;
	 }
	
	
	/****************************** For Batch Messaging *******************************/
	
	/*@Bean
	public BatchingRabbitTemplate batchingRabbitTemplate(){
		BatchingStrategy batchingStrategy = new SimpleBatchingStrategy(100, 100, 10000);
		BatchingRabbitTemplate template = new BatchingRabbitTemplate(batchingStrategy, threadPoolTaskScheduler());
		template.setConnectionFactory(connectionFactory());
		return template;
	}
	
	@Bean
	public ThreadPoolTaskScheduler threadPoolTaskScheduler(){
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(1);
		scheduler.initialize();
		return scheduler;
		
	}*/
	

}
