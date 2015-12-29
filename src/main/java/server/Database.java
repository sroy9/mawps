package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	static final String SQL_URL = "jdbc:mysql://localhost/";
	static final String USER = "root";
	static final String PASS = "";

	public static void initDatabase() {
	   Connection conn = null;
	   Statement stmt = null;
	   try{
	      //STEP 2: Register JDBC driver
	      Class.forName("com.mysql.jdbc.Driver");

	      //STEP 3: Open a connection
	      System.out.println("Connecting to database...");
	      conn = DriverManager.getConnection(SQL_URL, USER, PASS);

	      //STEP 4: Execute a query
	      System.out.println("Creating database...");
	      stmt = conn.createStatement();
	      
	      String sql = "CREATE DATABASE PROBLEMS";
//	      stmt.executeUpdate(sql);
//	      System.out.println("Database created successfully...");
	      sql = "USE PROBLEMS";
	      stmt.executeUpdate(sql);
	      System.out.println("Using database ...");
	      sql = "CREATE TABLE problems ("
	      		+ "ID int NOT NULL AUTO_INCREMENT,"
	    		  	+ "Question varchar(255) NOT NULL,"
	    		  	+ "Equations varchar(255) NOT NULL,"
	    		  	+ "Solutions varchar(255) NOT NULL,"
	    		  	+ "PRIMARY KEY (ID))";
	      stmt.executeUpdate(sql);
	      System.out.println("Table created successfully...");
	   }catch(SQLException se){
	      //Handle errors for JDBC
	      se.printStackTrace();
	   }catch(Exception e){
	      //Handle errors for Class.forName
	      e.printStackTrace();
	   }finally{//finally block used to close resources
	      try{
	          if(stmt!=null)
	             stmt.close();
	       }catch(SQLException se2){
	       }// nothing we can do
	       try{
	          if(conn!=null)
	             conn.close();
	       }catch(SQLException se){
	          se.printStackTrace();
	       }//end finally try
	    }//end try
	    System.out.println("Goodbye!");
	 }//end main
	
	public static void main(String args[]) {
		initDatabase();
	}
}
