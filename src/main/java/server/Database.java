package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import structure.Problem;

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
	      stmt = conn.createStatement();

	      //STEP 4: Execute a query
	      System.out.println("Creating database ...");	      
	      String sql = "CREATE DATABASE PROBLEMS";
	      stmt.executeUpdate(sql);
	      System.out.println("Database created successfully ...");
	      
	      sql = "USE PROBLEMS";
	      stmt.executeUpdate(sql);
	      System.out.println("Using database ...");
	      
	      sql = "CREATE TABLE problems ("
	      		+ "iIndex int NOT NULL AUTO_INCREMENT,"
	      		+ "dataset varchar(255) NOT NULL,"
	    		  	+ "sQuestion varchar(255) NOT NULL,"
	    		  	+ "lEquations varchar(255) NOT NULL,"
	    		  	+ "lSolutions varchar(255) NOT NULL,"
	    		  	+ "PRIMARY KEY (iIndex))";
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
	
	public static void addSingleProblem(Problem prob) {
	   Connection conn = null;
	   Statement stmt = null;
	   try{
	      //STEP 2: Register JDBC driver
	      Class.forName("com.mysql.jdbc.Driver");

	      //STEP 3: Open a connection
	      System.out.println("Connecting to database...");
	      conn = DriverManager.getConnection(SQL_URL, USER, PASS);
	      stmt = conn.createStatement();

	      //STEP 4: Execute a query
	      System.out.println("Creating database ...");		      
	      String sql = "CREATE DATABASE PROBLEMS";
	      stmt.executeUpdate(sql);
	      System.out.println("Database created successfully ...");
	      
	      sql = "USE PROBLEMS";
	      stmt.executeUpdate(sql);
	      System.out.println("Using database ...");
	      
	      sql = "INSERT INTO TABLE problems (dataset, sQuestion, lEquations, lSolutions) "
	      		+ "VALUES ("
	    		  	+ prob.dataset + ","
	    		  	+ prob.sQuestion + ","
	    		  	+ joinString(prob.lEquations) + ","
	    		  	+ joinDouble(prob.lSolutions) + ")";
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
	
	public static String joinString(List<String> arr) {
		String str = "";
		for(String a : arr) {
			str += a.toString() + "|||";
		}
		str = str.substring(0, str.length()-3);
		return str;
	}
	
	public static String joinDouble(List<Double> arr) {
		String str = "";
		for(Double a : arr) {
			str += a + "|||";
		}
		str = str.substring(0, str.length()-3);
		return str;
	}
	
	public static void main(String args[]) {
		initDatabase();
	}
}
