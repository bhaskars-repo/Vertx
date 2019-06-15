/*
 * Topic:  Introduction to Vert.x
 * 
 * Name:   Sample 5
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

public class Sample05 {
	private static Logger LOGGER = Logger.getLogger(Sample05.class.getName());
	
	private static String ADDRESS = "msg.address";
	private static String MESSAGE = "Hello from Vert.x";
	
	// Publisher verticle
	private static class MsgSendVerticle extends AbstractVerticle {
		@Override
		public void start() {
			vertx.eventBus().send(ADDRESS, MESSAGE);
			
			LOGGER.log(Level.INFO, "Message send to address " + ADDRESS);
		}
	}
	
	// Consumer verticle
	private static class MsgConsumerVerticle extends AbstractVerticle {
		@Override
		public void start() {
			vertx.eventBus().consumer(ADDRESS, res -> {
				 LOGGER.log(Level.INFO, "Received message - " + res.body());
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
