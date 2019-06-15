/*
 * Topic:  Introduction to Vert.x
 * 
 * Name:   Commands
 * 
 * Author: Bhaskar S
 * 
 * URL:    https://www.polarsparc.com
 */

package com.polarsparc.Vertx;

public interface Commands {
	public static String FLD_COMMAND = "command";
	public static String FLD_PAYLOAD = "payload";
	public static String FLD_ERRCODE = "errcode";
	
	public static String FLD_EMAIL   = "email";
	public static String FLD_MOBILE  = "mobile";
	public static String FLD_FNAME   = "fname";
	public static String FLD_LNAME   = "lname";
	
	public static String ADD_NEW = "addNew";
	public static String DEL_BY_LASTNAME = "deleteByLastName";
	public static String GET_ALL = "getAllContacts";
	public static String GET_BY_LASTNAME = "getByLastName";
	public static String UPD_BY_LASTNAME = "updateByLastName";
}
