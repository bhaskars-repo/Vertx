/*
 * Topic:  Introduction to Vert.x
 * 
 * Name:   Sample 8
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
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

public class Sample08 {
	private static Logger LOGGER = Logger.getLogger(Sample08.class.getName());
	
	private static String ADDRESS = "msg.address";
	
	// Consumer verticle
	private static class MsgConsumerVerticle extends AbstractVerticle {
		String name;
		
		MsgConsumerVerticle(String str) {
			this.name = str;
		}
		
		@Override
		public void start() {
			vertx.eventBus().consumer(ADDRESS, res -> {
				 LOGGER.log(Level.INFO, String.format("[%s] :: Received message - %s", name, res.body()));
			});
		}
	}
	
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.printf("Usage: java %s <name>\n", Sample08.class.getName());
			System.exit(1);
		}
		
		ClusterManager manager = new HazelcastClusterManager();
		
		VertxOptions options = new VertxOptions().setClusterManager(manager);
		
		Vertx.clusteredVertx(options, cluster -> {
			if (cluster.succeeded()) {
				cluster.result().deployVerticle(new MsgConsumerVerticle(args[0]), res -> {
					if (res.succeeded()) {
						LOGGER.log(Level.INFO, "Deployed consumer <" + args[0] + "> with instance ID: " + res.result());
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
