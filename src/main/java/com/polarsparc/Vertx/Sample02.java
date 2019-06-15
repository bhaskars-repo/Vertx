/*
 * Topic:  Introduction to Vert.x
 * 
 * Name:   Sample 2
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

public class Sample02 {
	private static Logger LOGGER = Logger.getLogger(Sample02.class.getName());
	
	private static String HTML = "<html>" +
			                       "<head><title>HttpServer Verticle</title></head>" +
			                       "<body><center>" +
			                       "<h3><font color='blue'>Vert.x is COOL !!!</font></h3>" +
			                       "</center></body>" +
			                       "</html>";
	
	private static class HttpServerVerticle extends AbstractVerticle {
		@Override
		public void start(Future<Void> fut) {
			vertx.createHttpServer()
				 .requestHandler(req -> {
					 LOGGER.log(Level.INFO, "Request from port: " + req.remoteAddress().port());
					 req.response()
					 	.putHeader("Content-Type", "text/html")
					 	.end(HTML);
				 })
				 .listen(8080, res -> {
					 if (res.succeeded()) {
						 fut.complete();
					 } else {
						 fut.fail(res.cause());
					 }
				 });
			LOGGER.log(Level.INFO, "Started http server on localhost:8080...");
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
