/*
 * Topic:  Introduction to Vert.x
 * 
 * Name:   Sample 10
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
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

public class Sample10Test {
	private static Logger LOGGER = Logger.getLogger(Sample10Test.class.getName());
	
	private static String ADDRESS = "contact.commands";
	
	// Publisher verticle that send "commands" and receives a response
	private static class CommandPublisherVerticle extends AbstractVerticle {
		@Override
		public void start() {
			// Add new contact
			{
				// Valid case - add fields present
				{
					JsonObject data = new JsonObject();
					data.put(Commands.FLD_FNAME, "Frank");
					data.put(Commands.FLD_LNAME, "Polymer");
					data.put(Commands.FLD_EMAIL, "frank_p@spacelab.io");
					data.put(Commands.FLD_MOBILE, "777-888-9999");
					
					JsonObject json = new JsonObject();
					json.put(Commands.FLD_COMMAND, Commands.ADD_NEW);
					json.put(Commands.FLD_PAYLOAD, data.encode());
					
					vertx.eventBus().send(ADDRESS, json.encode(), reply -> {
						if (reply.succeeded()) {
							LOGGER.log(Level.INFO, Commands.ADD_NEW + ":: Reply from " + ADDRESS + " => " + reply.result().body());
						} else {
							reply.cause().printStackTrace();
						}
					});
				}
				
				// Invalid case
				{
					JsonObject data = new JsonObject();
					data.put(Commands.FLD_LNAME, "Jupiter");
					data.put(Commands.FLD_MOBILE, "000-000-0000");
					data.put("dummy", "dummy");
					
					JsonObject json = new JsonObject();
					json.put(Commands.FLD_COMMAND, Commands.ADD_NEW);
					json.put(Commands.FLD_PAYLOAD, data.encode());
					
					vertx.eventBus().send(ADDRESS, json.encode(), reply -> {
						if (reply.succeeded()) {
							LOGGER.log(Level.INFO, Commands.ADD_NEW + ":: Reply from " + ADDRESS + " => " + reply.result().body());
						} else {
							reply.cause().printStackTrace();
						}
					});
				}
			}
			
			// Delete contact by lastname
			{
				// Valid case
				{
					JsonObject data = new JsonObject();
					data.put(Commands.FLD_LNAME, "Cracker");
					
					JsonObject json = new JsonObject();
					json.put(Commands.FLD_COMMAND, Commands.DEL_BY_LASTNAME);
					json.put(Commands.FLD_PAYLOAD, data.encode());
					
					vertx.eventBus().send(ADDRESS, json.encode(), reply -> {
						if (reply.succeeded()) {
							LOGGER.log(Level.INFO, Commands.DEL_BY_LASTNAME + ":: Reply from " + ADDRESS + " => " + reply.result().body());
						} else {
							reply.cause().printStackTrace();
						}
					});
				}
				
				// Invalid case
				{
					JsonObject data = new JsonObject();
					data.put(Commands.FLD_LNAME, "Jupiter");
					
					JsonObject json = new JsonObject();
					json.put(Commands.FLD_COMMAND, Commands.DEL_BY_LASTNAME);
					json.put(Commands.FLD_PAYLOAD, data.encode());
					
					vertx.eventBus().send(ADDRESS, json.encode(), reply -> {
						if (reply.succeeded()) {
							LOGGER.log(Level.INFO, Commands.DEL_BY_LASTNAME + ":: Reply from " + ADDRESS + " => " + reply.result().body());
						} else {
							reply.cause().printStackTrace();
						}
					});
				}
			}
			
			// Get contact by lastname
			{
				// Valid case
				{
					JsonObject data = new JsonObject();
					data.put(Commands.FLD_LNAME, "Martian");
					
					JsonObject json = new JsonObject();
					json.put(Commands.FLD_COMMAND, Commands.GET_BY_LASTNAME);
					json.put(Commands.FLD_PAYLOAD, data.encode());
					
					vertx.eventBus().send(ADDRESS, json.encode(), reply -> {
						if (reply.succeeded()) {
							LOGGER.log(Level.INFO, Commands.GET_BY_LASTNAME + ":: Reply from " + ADDRESS + " => " + reply.result().body());
						} else {
							reply.cause().printStackTrace();
						}
					});
				}
				
				// Invalid case
				{
					JsonObject data = new JsonObject();
					data.put(Commands.FLD_LNAME, "Jupiter");
					
					JsonObject json = new JsonObject();
					json.put(Commands.FLD_COMMAND, Commands.GET_BY_LASTNAME);
					json.put(Commands.FLD_PAYLOAD, data.encode());
					
					vertx.eventBus().send(ADDRESS, json.encode(), reply -> {
						if (reply.succeeded()) {
							LOGGER.log(Level.INFO, Commands.GET_BY_LASTNAME + ":: Reply from " + ADDRESS + " => " + reply.result().body());
						} else {
							reply.cause().printStackTrace();
						}
					});
				}
			}
			
			// Update contact by lastname
			{
				// Valid case - update mobile
				{
					JsonObject data = new JsonObject();
					data.put(Commands.FLD_LNAME, "Drummer");
					data.put(Commands.FLD_MOBILE, "300-111-2222");
					
					JsonObject json = new JsonObject();
					json.put(Commands.FLD_COMMAND, Commands.UPD_BY_LASTNAME);
					json.put(Commands.FLD_PAYLOAD, data.encode());
					
					vertx.eventBus().send(ADDRESS, json.encode(), reply -> {
						if (reply.succeeded()) {
							LOGGER.log(Level.INFO, Commands.UPD_BY_LASTNAME + ":: Reply from " + ADDRESS + " => " + reply.result().body());
						} else {
							reply.cause().printStackTrace();
						}
					});
				}
				
				// Valid case - update email and mobile
				{
					JsonObject data = new JsonObject();
					data.put(Commands.FLD_LNAME, "Earthling");
					data.put(Commands.FLD_EMAIL, "alice_01@antartica.ac");
					data.put(Commands.FLD_MOBILE, "501-222-2345");
					
					JsonObject json = new JsonObject();
					json.put(Commands.FLD_COMMAND, Commands.UPD_BY_LASTNAME);
					json.put(Commands.FLD_PAYLOAD, data.encode());
					
					vertx.eventBus().send(ADDRESS, json.encode(), reply -> {
						if (reply.succeeded()) {
							LOGGER.log(Level.INFO, Commands.UPD_BY_LASTNAME + ":: Reply from " + ADDRESS + " => " + reply.result().body());
						} else {
							reply.cause().printStackTrace();
						}
					});
				}
				
				// Invalid case
				{
					JsonObject data = new JsonObject();
					data.put(Commands.FLD_LNAME, "Martian");
					data.put("dummy", "dummy");
					
					JsonObject json = new JsonObject();
					json.put(Commands.FLD_COMMAND, Commands.UPD_BY_LASTNAME);
					json.put(Commands.FLD_PAYLOAD, data.encode());
					
					vertx.eventBus().send(ADDRESS, json.encode(), reply -> {
						if (reply.succeeded()) {
							LOGGER.log(Level.INFO, Commands.UPD_BY_LASTNAME + ":: Reply from " + ADDRESS + " => " + reply.result().body());
						} else {
							reply.cause().printStackTrace();
						}
					});
				}
			}
			
			// Get all contacts
			{
				JsonObject json = new JsonObject();
				json.put(Commands.FLD_COMMAND, Commands.GET_ALL);
				
				vertx.eventBus().send(ADDRESS, json.encode(), reply -> {
					if (reply.succeeded()) {
						LOGGER.log(Level.INFO, Commands.GET_ALL + ":: Reply from " + ADDRESS + " => " + reply.result().body());
					} else {
						reply.cause().printStackTrace();
					}
				});
			}
		}
	}
	
	public static void main(String[] args) {
		ClusterManager manager = new HazelcastClusterManager();
		
		VertxOptions options = new VertxOptions().setClusterManager(manager);
		
		Vertx.clusteredVertx(options, cluster -> {
			if (cluster.succeeded()) {
				cluster.result().deployVerticle(new CommandPublisherVerticle(), res -> {
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
