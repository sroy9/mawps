package server;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utils.Pipeline;

public class GrammarCheck{
	
    public static int checkERG(String input) {
    		input = input.trim();
        try {
        		String line;
            PrintWriter writer = new PrintWriter(new File("scratch.txt"), "UTF-8");
//            String[] tokens = input.split("(?<=[.?!])");
	        for (String x: Pipeline.getSentences(input)){
	            writer.println(x);
	        }
	        writer.close();
	
	        String pathToAce = "ace-0.9.22/ace -g ace-0.9.22/erg-1214-osx-0.9.22.dat -X -R scratch.txt";
	
	        Process p= Runtime.getRuntime().exec(pathToAce);
	        Pattern re = Pattern.compile("(?<=parsed)(\\s+\\d+\\s/\\s\\d+\\s+)(?=sentences)");
	        BufferedReader bre = new BufferedReader(new InputStreamReader(p.getErrorStream()));
	        while ((line = bre.readLine()) != null) {
	            Matcher m = re.matcher(line);
	            if (m.find()){
	                String[] found = m.group(0).split("/");
	                if (Integer.parseInt(found[0].trim())!=Integer.parseInt(found[1].trim())){
	                    //System.out.println(m.group(0));
	                    return 0;
	                }
	                    else { }//System.out.println("Gramm"); }
	            }
	      }
	      bre.close();

	      return 1;
        } catch (Exception ex) { 
            return -1;
        }
    }        
        
  public static void main (String arg[]) {
      String test = "I bought 2.2 pounds of apple for 5.78 dollars.";
//      try {
//          	BufferedReader br = new BufferedReader(new FileReader("q.txt")); 
//		    String line;
//		    while ((line = br.readLine()) != null) {
//		        if (checkERG(line)==-1){System.out.println("ERROR");}
//		    }
//      } catch (Exception ex) {}
	  System.out.println(checkERG(test));
  }
}
