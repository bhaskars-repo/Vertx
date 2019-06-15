/*
 * Topic:  Introduction to Vert.x
 * 
 * Name:   Sample 1
 * 
 * Author: Bhaskar S
 * 
 * URL:    https://www.polarsparc.com
 */

package com.polarsparc.Vertx;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

public class Sample01 {
	private static Logger LOGGER = Logger.getLogger(Sample01.class.getName());
	
	private static class HelloVerticle extends AbstractVerticle {
		@Override
		public void start(Future<Void> fut) {
			LOGGER.log(Level.INFO, "Welcome to the world of Vert.x !!!");
			fut.complete();
		}
		
		@Override
		public void stop(Future<Void> fut) {
			LOGGER.log(Level.INFO, "Goodbye from Vert.x !!!");
			fut.complete();
		}
	}

	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		
		vertx.deployVerticle(new HelloVerticle(), res -> {
			if (res.succeeded()) {
				String depId = res.result();
				
				LOGGER.log(Level.INFO, "Deployed instance ID: " + depId);
				LOGGER.log(Level.INFO, "Getting ready to undeploy...");
				
				vertx.undeploy(depId, res2 -> {
					if (res2.succeeded()) {
						LOGGER.log(Level.INFO, "Undeployed instance ID: " + depId);
						vertx.close();
					} else {
						res2.cause().printStackTrace();
					}
				});
			} else {
				res.cause().printStackTrace();
			}
		});
	}
}
