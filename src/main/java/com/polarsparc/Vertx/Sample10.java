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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

public class Sample10 {
	private static Logger LOGGER = Logger.getLogger(Sample10.class.getName());
	
	private static String ADDRESS = "contact.commands";
	
	private static Map<String, Contact> CONTACTS = new HashMap<>();
	
	// Consumer verticle that responds to "commands"
	private static class CommandConsumerVerticle extends AbstractVerticle {
		@Override
		public void start() {
			vertx.eventBus().consumer(ADDRESS, message -> {
				String payload = message.body().toString();
						
				LOGGER.log(Level.INFO, "Received payload - " + payload);
				 
				JsonObject req = new JsonObject(payload);
				
				String cmd = req.getString(Commands.FLD_COMMAND);
				
				switch (cmd) {
					case Commands.ADD_NEW: {
						JsonObject data = new JsonObject(req.getString(Commands.FLD_PAYLOAD));
						
						String fname = data.getString(Commands.FLD_FNAME);
						String lname = data.getString(Commands.FLD_LNAME);
						String email = data.getString(Commands.FLD_EMAIL);
						String mobile = data.getString(Commands.FLD_MOBILE);
						
						LOGGER.log(Level.INFO, Commands.ADD_NEW + ":: Contact first name - " + fname
							+ ", last name: " + lname + ", email: " + email + ", mobile: " + mobile);
						
						message.reply(addContact(fname, lname, email, mobile));
						
						break;
					}
					case Commands.DEL_BY_LASTNAME: {
						JsonObject data = new JsonObject(req.getString(Commands.FLD_PAYLOAD));
						
						String name = data.getString(Commands.FLD_LNAME);
						
						LOGGER.log(Level.INFO, Commands.DEL_BY_LASTNAME + ":: Contact last name - " + name);
						
						message.reply(deleteContactByLastName(name));
						
						break;
					}
					case Commands.GET_ALL: {
						message.reply(getAllContacts());
						
						break;
					}
					case Commands.GET_BY_LASTNAME: {
						JsonObject data = new JsonObject(req.getString(Commands.FLD_PAYLOAD));
						
						String name = data.getString(Commands.FLD_LNAME);
						
						LOGGER.log(Level.INFO, Commands.GET_BY_LASTNAME + ":: Contact last name - " + name);
						
						message.reply(getContactByLastName(name));
						
						break;
					}
					case Commands.UPD_BY_LASTNAME: {
						JsonObject data = new JsonObject(req.getString(Commands.FLD_PAYLOAD));
						
						String email = data.getString(Commands.FLD_EMAIL);
						String mobile = data.getString(Commands.FLD_MOBILE);
						String name = data.getString(Commands.FLD_LNAME);
						
						LOGGER.log(Level.INFO, Commands.UPD_BY_LASTNAME + ":: Contact last name - " + name + ", email: "
							+ email + ", mobile: " + mobile);
						
						message.reply(updateContactByLastName(name, email, mobile));
						
						break;
					}
				}
			});
		}
	}
	
	// Initialize pre-canned contacts
	private static void initFakeContacts() {
		Contact c1 = new Contact("Alice", "Earthling", "alice@earth.io", "123-456-1100");
		Contact c2 = new Contact("Bob", "Martian", "bob@mars.co", "789-123-1080");
		Contact c3 = new Contact("Charlie", "Drummer", "charlie@musician.org", "666-777-9006");
		Contact c4 = new Contact("Dummy", "Cracker", "dummy@cracker.org", "000-000-0000");
		
		CONTACTS.put(c1.getLastName().toLowerCase(), c1);
		CONTACTS.put(c2.getLastName().toLowerCase(), c2);
		CONTACTS.put(c3.getLastName().toLowerCase(), c3);
		CONTACTS.put(c4.getLastName().toLowerCase(), c4);
	}
	
	// Add a new contact
	private static String addContact(String fname, String lname, String email, String mobile) {
		JsonObject json = new JsonObject();
		
		// Valid contact:
		// 1. First name and last name are required
		// 2. Email and/or mobile required (either or both)
		if ((fname != null && fname.trim().length() > 0) &&
			(lname != null && lname.trim().length() > 0)) {
			json.put(Commands.FLD_ERRCODE, 1);
			
			Contact con = new Contact(fname, lname, "", "");
			if (email != null && email.trim().length() > 0) {
				json.put(Commands.FLD_ERRCODE, 0);
				con.setEmailId(email);
				CONTACTS.put(con.getLastName().toLowerCase(), con);
			}
			if (mobile != null && mobile.trim().length() > 0) {
				json.put(Commands.FLD_ERRCODE, 0);
				con.setMobile(mobile);
				CONTACTS.put(con.getLastName().toLowerCase(), con);
			}
			
			json.put(Commands.FLD_PAYLOAD, JsonObject.mapFrom(con).encode());
		}
		else {
			json.put(Commands.FLD_ERRCODE, 1);
		}
		
		String response = json.encode();
		
		LOGGER.log(Level.INFO, "addContact() - " + response);
		
		return response;
	}
	
	// Delete a contact by last name
	private static String deleteContactByLastName(String name) {
		JsonObject json = new JsonObject();
		
		Contact con = CONTACTS.remove(name.toLowerCase());
		if (con != null) {
			json.put(Commands.FLD_ERRCODE, 0);
			json.put(Commands.FLD_PAYLOAD, JsonObject.mapFrom(con).encode());
		}
		else {
			json.put(Commands.FLD_ERRCODE, 1);
		}
		
		String response = json.encode();
		
		LOGGER.log(Level.INFO, "deleteContactByLastName() - " + response);
		
		return response;
	}
	
	// Fetch all contacts
	private static String getAllContacts() {
		JsonArray array = new JsonArray(CONTACTS.values().stream().collect(Collectors.toList()));
		
		JsonObject json = new JsonObject();
		json.put(Commands.FLD_ERRCODE, 0);
		json.put(Commands.FLD_PAYLOAD, array.encode());
		
		String response = json.encode();
		
		LOGGER.log(Level.INFO, "getAllContacts() - " + response);
		
		return response;
	}
	
	// Fetch a contact by last name
	private static String getContactByLastName(String name) {
		JsonObject json = new JsonObject();
		
		Contact con = CONTACTS.get(name.toLowerCase());
		if (con != null) {
			json.put(Commands.FLD_ERRCODE, 0);
			json.put(Commands.FLD_PAYLOAD, JsonObject.mapFrom(con).encode());
		}
		else {
			json.put(Commands.FLD_ERRCODE, 1);
		}
		
		String response = json.encode();
		
		LOGGER.log(Level.INFO, "getContactByLastName() - " + response);
		
		return response;
	}
	
	// Update a contact by last name
	private static String updateContactByLastName(String name, String email, String mobile) {
		JsonObject json = new JsonObject();
		
		Contact con = CONTACTS.get(name.toLowerCase());
		if (con != null) {
			json.put(Commands.FLD_ERRCODE, 1);
			if (email != null && email.trim().length() > 0) {
				json.put(Commands.FLD_ERRCODE, 0);
				con.setEmailId(email);
			}
			if (mobile != null && mobile.trim().length() > 0) {
				json.put(Commands.FLD_ERRCODE, 0);
				con.setMobile(mobile);
			}
			json.put(Commands.FLD_PAYLOAD, JsonObject.mapFrom(con).encode());
		}
		else {
			json.put(Commands.FLD_ERRCODE, 1);
		}
		
		String response = json.encode();
		
		LOGGER.log(Level.INFO, "updateContactByLastName() - " + response);
		
		return response;
	}
	
	// ----- Main -----
	
	public static void main(String[] args) {
		initFakeContacts();
		
		ClusterManager manager = new HazelcastClusterManager();
		
		VertxOptions options = new VertxOptions().setClusterManager(manager);
		
		Vertx.clusteredVertx(options, cluster -> {
			if (cluster.succeeded()) {
				cluster.result().deployVerticle(new CommandConsumerVerticle(), res -> {
					if (res.succeeded()) {
						LOGGER.log(Level.INFO, "Deployed command consumer instance ID: " + res.result());
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
