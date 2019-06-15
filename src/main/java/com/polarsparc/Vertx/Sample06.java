/*
 * Topic:  Introduction to Vert.x
 * 
 * Name:   Sample 6
 * 
 * Author: Bhaskar S
 * 
 * URL:    https://www.polarsparc.com
 */

package com.polarsparc.Vertx;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;

public class Sample06 {
	private static Logger LOGGER = Logger.getLogger(Sample06.class.getName());
	
	private static String ADDRESS = "msg.address";
	private static String MESSAGE = "Hola from Vert.x";
	
	// Producer verticle that expects a reply from the consumer
	private static class MsgSendVerticle extends AbstractVerticle {
		@Override
		public void start() {
			vertx.eventBus().send(ADDRESS, MESSAGE, reply -> {
				if (reply.succeeded()) {
					LOGGER.log(Level.INFO, "Reply from " + ADDRESS + " => " + reply.result().body());
				} else {
					reply.cause().printStackTrace();
				}
			});
			
			LOGGER.log(Level.INFO, "Message send to address " + ADDRESS);
		}
	}
	
	// Consumer verticle that sends a reply back to the producer
	private static class MsgConsumerVerticle extends AbstractVerticle {
		@Override
		public void start() {
			vertx.eventBus().consumer(ADDRESS, message -> {
				 LOGGER.log(Level.INFO, "Received message - " + message.body());
				 
				 message.reply("Fantastico !!!");
			});
		}
	}
	
	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(new MsgConsumerVerticle(), res1 -> {
			if (res1.succeeded()) {
				LOGGER.log(Level.INFO, "Deployed consumer instance ID: " + res1.result());
				
				vertx.deployVerticle(new MsgSendVerticle(), res2 -> {
					if (res2.succeeded()) {
						LOGGER.log(Level.INFO, "Deployed sender instance ID: " + res2.result());
					} else {
						res2.cause().printStackTrace();
					}
				});
			} else {
				res1.cause().printStackTrace();
			}
		});
	}
}
