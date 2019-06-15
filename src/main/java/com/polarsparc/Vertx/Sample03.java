/*
 * Topic:  Introduction to Vert.x
 * 
 * Name:   Sample 3
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
import io.vertx.core.json.JsonObject;

public class Sample03 {
	private static Logger LOGGER = Logger.getLogger(Sample03.class.getName());
	
	private static String HTML = "<html>" +
			                       "<head><title>HttpServer Verticle</title></head>" +
			                       "<body><center>" +
			                       "<h3><font color='red'>Vert.x is AWESOME !!!</font></h3>" +
			                       "</center></body>" +
			                       "</html>";
	
	private static class HttpServerVerticle extends AbstractVerticle {
		@Override
		public void start(Future<Void> fut) {
			ConfigRetriever retriever = ConfigRetriever.create(vertx);
			retriever.getConfig(json -> {
				if (json.succeeded()) {
					JsonObject config = json.result();
					
					int port = config.getInteger("http.port");
					
					LOGGER.log(Level.INFO, "Server port: " + port);
					
					vertx.createHttpServer()
						 .requestHandler(req -> {
							 LOGGER.log(Level.INFO, "Request from port: " + req.remoteAddress().port());
							 req.response()
							 	.putHeader("Content-Type", "text/html")
							 	.end(HTML);
						 })
						 .listen(port, res -> {
							 if (res.succeeded()) {
								 fut.complete();
							 } else {
								 fut.fail(res.cause());
							 }
						 });
					
					LOGGER.log(Level.INFO, "Started http server on localhost:" + port + "...");
				} else {
					json.cause().printStackTrace();
				}
			});
		}
	}

	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(new HttpServerVerticle(), res -> {
			if (res.succeeded()) {
				LOGGER.log(Level.INFO, "Deployed instance ID: " + res.result());
			} else {
				res.cause().printStackTrace();
			}
		});
	}
}
