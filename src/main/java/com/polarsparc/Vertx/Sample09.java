/*
 * Topic:  Introduction to Vert.x
 * 
 * Name:   Sample 9
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
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

public class Sample09 {
	private static Logger LOGGER = Logger.getLogger(Sample09.class.getName());
	
	private static String ADDRESS = "msg.address";
	private static String MESSAGE = "Vert.x is Reactive";
	
	// Publisher verticle
	private static class MsgPublisherVerticle extends AbstractVerticle {
		@Override
		public void start(Future<Void> fut) {
			vertx.eventBus().publish(ADDRESS, String.format("[1] => %s", MESSAGE));
			vertx.eventBus().publish(ADDRESS, String.format("[2] => %s", MESSAGE));
			vertx.eventBus().publish(ADDRESS, String.format("[3] => %s", MESSAGE));
			
			vertx.eventBus().send(ADDRESS, String.format("[4] -> %s", MESSAGE));
			vertx.eventBus().send(ADDRESS, String.format("[5] -> %s", MESSAGE));
			
			LOGGER.log(Level.INFO, String.format("Messages published to address %s", ADDRESS));
			
			fut.complete();
		}
	}
	
	public static void main(String[] args) {
		ClusterManager manager = new HazelcastClusterManager();
		
		VertxOptions options = new VertxOptions().setClusterManager(manager);
		
		Vertx.clusteredVertx(options, cluster -> {
			if (cluster.succeeded()) {
				cluster.result().deployVerticle(new MsgPublisherVerticle(), res -> {
					if (res.succeeded()) {
						LOGGER.log(Level.INFO, "Deployed publisher instance ID: " + res.result());
					} else {
						res.cause().printStackTrace();
					}
		       });
			} else {
				cluster.cause().printStackTrace();
			}
		});
	}
}
