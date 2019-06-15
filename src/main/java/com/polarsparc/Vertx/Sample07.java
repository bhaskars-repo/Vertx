/*
 * Topic:  Introduction to Vert.x
 * 
 * Name:   Sample 7
 * 
 * Author: Bhaskar S
 * 
 * URL:    https://www.polarsparc.com
 */

package com.polarsparc.Vertx;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

public class Sample07 {
	private static Logger LOGGER = Logger.getLogger(Sample07.class.getName());
	
	private static String ADDRESS = "msg.address";
	private static String MESSAGE = "Bojour from Vert.x";
	
	// Publisher verticle
	private static class MsgPublisherVerticle extends AbstractVerticle {
		@Override
		public void start(Future<Void> fut) {
			vertx.eventBus().publish(ADDRESS, String.format("[1] %s", MESSAGE));
			vertx.eventBus().publish(ADDRESS, String.format("[2] %s", MESSAGE));
			vertx.eventBus().publish(ADDRESS, String.format("[3] %s", MESSAGE));
			
			LOGGER.log(Level.INFO, String.format("Messages published to address %s", ADDRESS));
			
			fut.complete();
		}
	}
	
	// Consumer verticle
	private static class MsgConsumerVerticle extends AbstractVerticle {
		String name;
		
		MsgConsumerVerticle(String str) {
			this.name = str;
		}
		
		@Override
		public void start() {
			vertx.eventBus().consumer(ADDRESS, res -> {
				 LOGGER.log(Level.INFO, String.format("[%s] - Received message - %s", name, res.body()));
			});
		}
	}
	
	private static Future<Void> deployConsumer(String name, Vertx vertx) {
		Future<Void> fut = Future.future();
		
		vertx.deployVerticle(new MsgConsumerVerticle(name), res -> {
			if (res.succeeded()) {
				LOGGER.log(Level.INFO, "Deployed consumer <" + name + "> with instance ID: " + res.result());
				
				fut.complete();
			} else {
				fut.fail(res.cause());
			}
		});
		
		return fut;
	}
	
	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		
		Future<Void> f1 = deployConsumer("C1", vertx);
		Future<Void> f2 = deployConsumer("C2", vertx);
		
		CompositeFuture.join(f1, f2).setHandler(res -> {
			if (res.succeeded()) {
				LOGGER.log(Level.INFO, "Deployed consumer instances");
			} else {
				res.cause().printStackTrace();
			}
		});
		
		// Wait for deployment of consumers
		try {
			Thread.sleep(1000);
		}
		catch (Exception ex) {
		}
		
		vertx.deployVerticle(new MsgPublisherVerticle(), res -> {
			if (res.succeeded()) {
				LOGGER.log(Level.INFO, "Deployed publisher instance ID: " + res.result());
			} else {
				res.cause().printStackTrace();
			}
		});
	}
}
