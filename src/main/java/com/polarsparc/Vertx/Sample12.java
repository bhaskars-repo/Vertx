/*
 * Topic:  Introduction to Vert.x
 * 
 * Name:   Sample 12
 * 
 * Author: Bhaskar S
 * 
 * URL:    https://www.polarsparc.com
 */

package com.polarsparc.Vertx;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

public class Sample12 {
	private static Logger LOGGER = Logger.getLogger(Sample12.class.getName());
	
	private static String ADDRESS = null;
	
	private static JDBCClient JDBC = null;
	
	private static String SQL_ADD_CONTACT     = "INSERT INTO CONTACTS (FIRST_NAME, LAST_NAME, EMAIL_ID, MOBILE) VALUES (?, ?, ?, ?)";
	private static String SQL_DEL_CONTACT     = "DELETE FROM CONTACTS WHERE LAST_NAME = ?";
	private static String SQL_QRY_ALL_CONTACT = "SELECT FIRST_NAME, LAST_NAME, EMAIL_ID, MOBILE FROM CONTACTS";
	private static String SQL_QRY_ONE_CONTACT = "SELECT FIRST_NAME, LAST_NAME, EMAIL_ID, MOBILE FROM CONTACTS WHERE LAST_NAME = ?";
	private static String SQL_UPD_CONTACT     = "UPDATE CONTACTS SET EMAIL_ID = ?, MOBILE = ? WHERE LAST_NAME = ?";
	
	// Consumer verticle that responds to "commands"
	private static class DbCommandConsumerVerticle extends AbstractVerticle {
		@Override
		public void start(Future<Void> fut) {
			ConfigRetriever retriever = ConfigRetriever.create(vertx);
			
			ConfigRetriever.getConfigAsFuture(retriever).compose(config -> {
				Future<Void> next = Future.future();
				
				ADDRESS = config.getString("commands.endpoint");
				
				LOGGER.log(Level.INFO, "Eventbus commands address: " + ADDRESS);
				
				JDBC = JDBCClient.createShared(vertx, config, "ContactsMgmt");
				
				return next;
			})
			.setHandler(confres -> {
				 if (confres.succeeded()) {
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
								
								addContact(fname, lname, email, mobile).setHandler(res -> {
									if (res.succeeded()) {
										message.reply(res.result());
									}
									else {
										message.reply(errorResponse());
									}
								});
								
								break;
							}
							case Commands.DEL_BY_LASTNAME: {
								JsonObject data = new JsonObject(req.getString(Commands.FLD_PAYLOAD));
								
								String name = data.getString(Commands.FLD_LNAME);
								
								LOGGER.log(Level.INFO, Commands.DEL_BY_LASTNAME + ":: Contact last name - " + name);
								
								deleteContactByLastName(name).setHandler(res -> {
									if (res.succeeded()) {
										message.reply(res.result());
									}
									else {
										message.reply(errorResponse());
									}
								});
								
								break;
							}
							case Commands.GET_ALL: {
								getAllContacts().setHandler(res -> {
									if (res.succeeded()) {
										message.reply(res.result());
									}
									else {
										message.reply(errorResponse());
									}
								});
								
								break;
							}
							case Commands.GET_BY_LASTNAME: {
								JsonObject data = new JsonObject(req.getString(Commands.FLD_PAYLOAD));
								
								String name = data.getString(Commands.FLD_LNAME);
								
								LOGGER.log(Level.INFO, Commands.GET_BY_LASTNAME + ":: Contact last name - " + name);
								
								getContactByLastName(name).setHandler(res -> {
									if (res.succeeded()) {
										message.reply(res.result());
									}
									else {
										message.reply(errorResponse());
									}
								});
								
								break;
							}
							case Commands.UPD_BY_LASTNAME: {
								JsonObject data = new JsonObject(req.getString(Commands.FLD_PAYLOAD));
								
								String email = data.getString(Commands.FLD_EMAIL);
								String mobile = data.getString(Commands.FLD_MOBILE);
								String name = data.getString(Commands.FLD_LNAME);
								
								LOGGER.log(Level.INFO, Commands.UPD_BY_LASTNAME + ":: Contact last name - " + name + ", email: "
									+ email + ", mobile: " + mobile);
								
								updateContactByLastName(name, email, mobile).setHandler(res -> {
									if (res.succeeded()) {
										message.reply(res.result());
									}
									else {
										message.reply(errorResponse());
									}
								});
								
								break;
							}
						}
					 });
					
					 fut.complete();
				 } else {
					 fut.fail(confres.cause());
				 }
			});
		}
	}
	
	// ----- Database Operation(s) -----
	
	// Get an SQL connection
	private static Future<SQLConnection> connect() {
		Future<SQLConnection> future = Future.future();
		
		JDBC.getConnection(res -> {
			if (res.succeeded()) {
				future.complete(res.result());
			}
			else {
				future.fail(res.cause());
			}
		});
		
		return future;
	}
	
	// Database insert
	private static Future<Contact> insert(SQLConnection connection, Contact obj) {
		Future<Contact> future = Future.future();
		
		LOGGER.log(Level.INFO, "insert() - fname: " + obj.getFirstName() + ", lname: " + obj.getLastName()
			+ ", email: " + obj.getEmailId() + ", mobile: " + obj.getMobile());
		
		JsonArray params = new JsonArray();
		params.add(obj.getFirstName()).add(obj.getLastName()).add(obj.getEmailId()).add(obj.getMobile());
		
		connection.updateWithParams(SQL_ADD_CONTACT, params, res -> {
			connection.close();
			if (res.succeeded()) {
				future.complete(obj);
			}
			else {
				future.fail(res.cause());
			}
		});
		
		return future;
	}
	
	// Database delete
	private static Future<Void> delete(SQLConnection connection, String name) {
		Future<Void> future = Future.future();
		
		LOGGER.log(Level.INFO, "delete() - name: " + name);
		
		JsonArray params = new JsonArray();
		params.add(name);
		
		connection.updateWithParams(SQL_DEL_CONTACT, params, res -> {
			connection.close();
			if (res.succeeded()) {
				if (res.result().getUpdated() == 0) {
					future.fail(new NoSuchElementException("delete() - No contact with LAST_NAME = " + name));
				}
				else {
					future.complete();
				}
			}
			else {
				future.fail(res.cause());
			}
		});
		
		return future;
	}
	
	// Database select all
	private static Future<List<JsonObject>> select(SQLConnection connection) {
		Future<List<JsonObject>> future = Future.future();
		
		connection.query(SQL_QRY_ALL_CONTACT, res -> {
			connection.close();
			if (res.succeeded()) {
				future.complete(res.result().getRows().stream().collect(Collectors.toList()));
			}
			else {
				future.fail(res.cause());
			}
		});
		
		return future;
	}
	
	// Database select one
	private static Future<JsonObject> selectOne(SQLConnection connection, String name) {
		Future<JsonObject> future = Future.future();
		
		LOGGER.log(Level.INFO, "selectOne() - name: " + name);
		
		JsonArray params = new JsonArray();
		params.add(name);
		
		connection.queryWithParams(SQL_QRY_ONE_CONTACT, params, res -> {
			connection.close();
			if (res.succeeded()) {
				if (res.result().getRows().size() == 0) {
					future.fail(new NoSuchElementException("selectOne() - No contact with LAST_NAME = " + name));
				}
				else {
					future.complete(res.result().getRows().get(0));
				}
			}
			else {
				future.fail(res.cause());
			}
		});
		
		return future;
	}
	
	// Database update
	private static Future<Void> update(SQLConnection connection, String name, String email, String mobile) {
		Future<Void> future = Future.future();
		
		LOGGER.log(Level.INFO, "update() - name: " + name + ", email: " + email + ", mobile:" + mobile);
		
		JsonArray params = new JsonArray();
		params.add(email);
		params.add(mobile);
		params.add(name);
		
		connection.updateWithParams(SQL_UPD_CONTACT, params, res -> {
			connection.close();
			if (res.succeeded()) {
				if (res.result().getUpdated() == 0) {
					future.fail(new NoSuchElementException("update() - No contact with LAST_NAME = " + name));
				}
				else {
					future.complete();
				}
			}
			else {
				future.fail(res.cause());
			}
		});
		
		return future;
	}
	
	// ----- Command Handler(s) -----
	
	// Error JSON response
	private static String errorResponse() {
		 JsonObject json = new JsonObject();
		 json.put(Commands.FLD_ERRCODE, 1);
		 
		 return json.encode();
	}
	
	// Add a new contact
	private static Future<String> addContact(String fname, String lname, String email, String mobile) {
		Future<String> future = Future.future();
		
		// Valid contact:
		// 1. First name and last name are required
		// 2. Email and/or mobile required (either or both)
		if ((fname != null && fname.trim().length() > 0) &&
			(lname != null && lname.trim().length() > 0)) {
			Contact obj = new Contact(fname, lname, "", "");
			if (email != null && email.trim().length() > 0) {
				obj.setEmailId(email);
			}
			if (mobile != null && mobile.trim().length() > 0) {
				obj.setMobile(mobile);
			}
			
			// Database insert
			connect().setHandler(sqlres -> {
				if (sqlres.succeeded()) {
					insert(sqlres.result(), obj).setHandler(conres -> {
						 if (conres.succeeded()) {
							 JsonObject json = new JsonObject();
							 json.put(Commands.FLD_ERRCODE, 0);
							 json.put(Commands.FLD_PAYLOAD, JsonObject.mapFrom(conres.result()).encode());
							 
							 String response = json.encode();
							
							 LOGGER.log(Level.INFO, "addContact() - " + response);
							
							 future.complete(response);
						 }
						 else {
							 future.fail(conres.cause());
						 }
					});
				}
				else {
					future.fail(sqlres.cause());
				}
			});
		}
		else {
			future.fail(new IllegalArgumentException("addContact - Invalid name for contact"));
		}
		
		return future;
	}
	
	// Delete a contact by last name
	private static Future<String> deleteContactByLastName(String name) {
		Future<String> future = Future.future();

		if (name != null && name.trim().length() > 0) {
			// Database delete
			connect().setHandler(sqlres -> {
				if (sqlres.succeeded()) {
					delete(sqlres.result(), name).setHandler(conres -> {
						 if (conres.succeeded()) {
							 JsonObject json = new JsonObject();
							 json.put(Commands.FLD_ERRCODE, 0);
							 json.put(Commands.FLD_PAYLOAD, new JsonObject().put(Commands.FLD_LNAME, name).encode());
							 
							 String response = json.encode();
							
							 LOGGER.log(Level.INFO, "deleteContactByLastName() - " + response);
							 
							 future.complete(response);
						 }
						 else {
							 future.fail(conres.cause());
						 }
					});
				}
				else {
					future.fail(sqlres.cause());
				}
			});
		}
		else {
			future.fail(new IllegalArgumentException("deleteContactByLastName - Invalid last name for contact"));
		}
		
		return future;
	}
	
	// Fetch all contacts
	private static Future<String> getAllContacts() {
		Future<String> future = Future.future();
		
		// Database select all
		connect().setHandler(sqlres -> {
			if (sqlres.succeeded()) {
				select(sqlres.result()).setHandler(conres -> {
					 if (conres.succeeded()) {
						 JsonArray array = new JsonArray(conres.result());
						 JsonObject json = new JsonObject();
						 json.put(Commands.FLD_ERRCODE, 0);
						 json.put(Commands.FLD_PAYLOAD, array.encode());
						 
						 String response = json.encode();
						
						 LOGGER.log(Level.INFO, "getAllContacts() - " + response);
						 
						 future.complete(response);
					 }
					 else {
						 future.fail(conres.cause());
					 }
				});
			}
			else {
				future.fail(sqlres.cause());
			}
		});
		
		return future;
	}
	
	// Fetch a contact by last name
	private static Future<String> getContactByLastName(String name) {
		Future<String> future = Future.future();
		
		if (name != null && name.trim().length() > 0) {
			// Database delete
			connect().setHandler(sqlres -> {
				if (sqlres.succeeded()) {
					selectOne(sqlres.result(), name).setHandler(conres -> {
						 if (conres.succeeded()) {
							 JsonObject json = new JsonObject();
							 json.put(Commands.FLD_ERRCODE, 0);
							 json.put(Commands.FLD_PAYLOAD, conres.result().encode());
							 
							 String response = json.encode();
							
							 LOGGER.log(Level.INFO, "getContactByLastName() - " + response);
							 
							 future.complete(response);
						 }
						 else {
							 future.fail(conres.cause());
						 }
					});
				}
				else {
					future.fail(sqlres.cause());
				}
			});
		}
		else {
			future.fail(new IllegalArgumentException("getContactByLastName - Invalid last name for contact"));
		}
		
		return future;
	}
	
	// Update a contact by last name
	private static Future<String> updateContactByLastName(String name, String email, String mobile) {
		Future<String> future = Future.future();

		if ((name != null && name.trim().length() > 0) &&
			(email != null && email.trim().length() > 0) &&
			(mobile != null && mobile.trim().length() > 0)) {
			// Database update
			connect().setHandler(sqlres -> {
				if (sqlres.succeeded()) {
					update(sqlres.result(), name, email, mobile).setHandler(conres -> {
						 if (conres.succeeded()) {
							 JsonObject json = new JsonObject();
							 json.put(Commands.FLD_ERRCODE, 0);
							 json.put(Commands.FLD_PAYLOAD, new JsonObject().put(Commands.FLD_LNAME, name).encode());
							 
							 String response = json.encode();
							
							 LOGGER.log(Level.INFO, "updateContactByLastName() - " + response);
							 
							 future.complete(response);
						 }
						 else {
							 future.fail(conres.cause());
						 }
					});
				}
				else {
					future.fail(sqlres.cause());
				}
			});
		}
		else {
			future.fail(new IllegalArgumentException("updateContactByLastName - Invalid last name, email, mobile for contact"));
		}
		
		return future;
	}
	
	// ----- Main -----
	
	public static void main(String[] args) {
		ClusterManager manager = new HazelcastClusterManager();
		
		VertxOptions options = new VertxOptions().setClusterManager(manager);
		
		Vertx.clusteredVertx(options, cluster -> {
			if (cluster.succeeded()) {
				cluster.result().deployVerticle(new DbCommandConsumerVerticle(), res -> {
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
