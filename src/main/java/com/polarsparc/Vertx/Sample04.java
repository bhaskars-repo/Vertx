/*
 * Topic:  Introduction to Vert.x
 * 
 * Name:   Sample 4
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

public class Sample04 {
	private static Logger LOGGER = Logger.getLogger(Sample04.class.getName());
	
	private static String HTML = "<html>" +
			                       "<head><title>HttpServer Verticle</title></head>" +
			                       "<body><center>" +
			                       "<h3><font color='forestgreen'>Vert.x works <u>GREAT</u> !!!</font></h3>" +
			                       "</center></body>" +
			                       "</html>";
	
	private static class HttpServerVerticle extends AbstractVerticle {
		@Override
		public void start(Future<Void> fut) {
			ConfigRetriever retriever = ConfigRetriever.create(vertx);
			
			ConfigRetriever.getConfigAsFuture(retriever)
				.compose(config -> {
					int port = config.getInteger("http.port");
					
					LOGGER.log(Level.INFO, "Configured server port: " + port);
					
					Future<Void> next = Future.future();
					
					vertx.createHttpServer()
					 .requestHandler(req -> {
						 LOGGER.log(Level.INFO, "Request from port: " + req.remoteAddress().port());
						 req.response()
						 	.putHeader("Content-Type", "text/html")
						 	.end(HTML);
					 })
					 .listen(port, res -> {
						 if (res.succeeded()) {
							 LOGGER.log(Level.INFO, "Started http server on localhost:" + port + "...");
							 
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
