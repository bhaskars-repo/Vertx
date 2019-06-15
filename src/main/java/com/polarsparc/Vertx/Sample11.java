/*
 * Topic:  Introduction to Vert.x
 * 
 * Name:   Sample 11
 * 
 * Author: Bhaskar S
 * 
 * URL:    https://www.polarsparc.com
 */

package com.polarsparc.Vertx;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

public class Sample11 {
	private static Logger LOGGER = Logger.getLogger(Sample11.class.getName());
	
	private static String ADDRESS = "contact.commands";
	
	private static Vertx VERTX = null;
	
	private static class ContactsServiceVerticle extends AbstractVerticle {
		@Override
		public void start(Future<Void> fut) {
			ConfigRetriever retriever = ConfigRetriever.create(vertx);
			
			ConfigRetriever.getConfigAsFuture(retriever).compose(config -> {
				int port = config.getInteger("http.port");
				
				LOGGER.log(Level.INFO, "Configured server port: " + port);
				
				Router router = Router.router(vertx);
				
				router.route().handler(BodyHandler.create().setHandleFileUploads(false));
				router.delete("/api/contacts/v1/deleteByLastName/:lastName").handler(Sample11::deleteContactByLastName);
				router.get("/api/contacts/v1/getAll").handler(Sample11::getAll);
				router.get("/api/contacts/v1/getByLastName/:lastName").handler(Sample11::getContactByLastName);
				router.post("/api/contacts/v1/addContact").handler(Sample11::addContact);
				router.put("/api/contacts/v1/updateByLastName/:lastName").handler(Sample11::updateContactByLastName);
				
				Future<Void> next = Future.future();
				
				vertx.createHttpServer().requestHandler(router).listen(port, res -> {
					 if (res.succeeded()) {
						 LOGGER.log(Level.INFO, "Started contacts service on localhost:" + port + "...");
						 
						 next.complete();
					 } else {
						 next.fail(res.cause());
					 }
				 });
				
				return next;
			})
			.setHandler(res -> {
				 if (res.succeeded()) {
					 fut.complete();
				 } else {
					 fut.fail(res.cause());
				 }
			});
		}
	}
	
	// Common request-response handler
	private static void requestResponseHandler(JsonObject json, HttpServerResponse response) {
		VERTX.eventBus().send(ADDRESS, json.encode(), reply -> {
			if (reply.succeeded()) {
				LOGGER.log(Level.INFO, json.getString(Commands.FLD_COMMAND) + ":: Reply from " + ADDRESS + " => "
					+ reply.result().body());
				
				JsonObject payload = new JsonObject(reply.result().body().toString());
				
				response.putHeader("content-type", "application/json").end(payload.encode());
			} else {
				reply.cause().printStackTrace();
				
				response.setStatusCode(400).end();
			}
		});
	}
	
	// Add new contact
	private static void addContact(RoutingContext context) {
		JsonObject data = context.getBodyAsJson();
		
		LOGGER.log(Level.INFO, Commands.ADD_NEW + ":: payload to send: " + data.encode());
		
		JsonObject json = new JsonObject();
		json.put(Commands.FLD_COMMAND, Commands.ADD_NEW);
		json.put(Commands.FLD_PAYLOAD, data.encode());
		
		requestResponseHandler(json, context.response());
	}
	
	// Delete contact by last name
	private static void deleteContactByLastName(RoutingContext context) {
		HttpServerResponse response = context.response();
		
		String lname = context.request().getParam("lastName");
		if (lname == null || lname.trim().isEmpty()) {
			response.setStatusCode(400).end();
		}
		
		LOGGER.log(Level.INFO, Commands.DEL_BY_LASTNAME + ":: Last name: " + lname);
		
		JsonObject data = new JsonObject();
		data.put(Commands.FLD_LNAME, lname);
		
		JsonObject json = new JsonObject();
		json.put(Commands.FLD_COMMAND, Commands.DEL_BY_LASTNAME);
		json.put(Commands.FLD_PAYLOAD, data.encode());
		
		requestResponseHandler(json, response);
	}
	
	// Get all contacts
	private static void getAll(RoutingContext context) {
		JsonObject json = new JsonObject();
		json.put(Commands.FLD_COMMAND, Commands.GET_ALL);
		
		requestResponseHandler(json, context.response());
	}
	
	// Get contact by last name
	private static void getContactByLastName(RoutingContext context) {
		HttpServerResponse response = context.response();
		
		String lname = context.request().getParam("lastName");
		if (lname == null || lname.trim().isEmpty()) {
			response.setStatusCode(400).end();
		}
		
		LOGGER.log(Level.INFO, Commands.GET_BY_LASTNAME + ":: Last name: " + lname);
		
		JsonObject data = new JsonObject();
		data.put(Commands.FLD_LNAME, lname);
		
		JsonObject json = new JsonObject();
		json.put(Commands.FLD_COMMAND, Commands.GET_BY_LASTNAME);
		json.put(Commands.FLD_PAYLOAD, data.encode());
		
		requestResponseHandler(json, response);
	}
	
	// Update contact by last name
	private static void updateContactByLastName(RoutingContext context) {
		HttpServerResponse response = context.response();
		
		String lname = context.request().getParam("lastName");
		if (lname == null || lname.trim().isEmpty()) {
			response.setStatusCode(400).end();
		}
		
		LOGGER.log(Level.INFO, Commands.UPD_BY_LASTNAME + ":: Last name: " + lname);
		
		JsonObject data = context.getBodyAsJson();
		data.put(Commands.FLD_LNAME, lname);
		
		LOGGER.log(Level.INFO, Commands.UPD_BY_LASTNAME + ":: payload to send: " + data.encode());
		
		JsonObject json = new JsonObject();
		json.put(Commands.FLD_COMMAND, Commands.UPD_BY_LASTNAME);
		json.put(Commands.FLD_PAYLOAD, data.encode());
		
		requestResponseHandler(json, response);
	}

	public static void main(String[] args) {
		ClusterManager manager = new HazelcastClusterManager();
		
		VertxOptions options = new VertxOptions().setClusterManager(manager);
		
		Vertx.clusteredVertx(options, cluster -> {
			if (cluster.succeeded()) {
				VERTX = cluster.result();
				VERTX.deployVerticle(new ContactsServiceVerticle(), res -> {
					if (res.succeeded()) {
						LOGGER.log(Level.INFO, "Deployed contacts service instance ID: " + res.result());
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
