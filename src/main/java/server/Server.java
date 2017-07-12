package server;

import java.util.ArrayList;
import java.util.List;

import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import structure.Problem;

public class Server {
    
    public String addProblem(String question, String equations, String answers) {
		System.out.println("Called addProblem");
        try {
            Problem prob = new Problem();
            prob.dataset = "Unspecified";
            prob.sQuestion = question.trim();
            prob.lEquations= new ArrayList<>();
            for(String eq : equations.split("\n")) {
                if(eq.trim().equals("")) continue;
                prob.lEquations.add(eq.trim());
            }
            prob.lSolutions = new ArrayList<>();
            for(String val : answers.split("\n")) {
                if(val.trim().equals("")) continue;
                prob.lSolutions.add(Double.parseDouble(val.trim()));
            }
            prob.grammarCheck = GrammarCheck.checkERG(prob.sQuestion);
            Database.add(prob);
            return "Problem successfully uploaded";
        } catch (Exception e) {
            e.printStackTrace();
            return "Sorry, there was a problem";
        }
    }
    
    public String addDataset(String datasetName, String allQuestions) {
		System.out.println("Called addDataset");
        try {
            List<Problem> uploadedProblems = new Gson().fromJson(
            		allQuestions, new TypeToken<List<Problem>>(){}.getType());
            int count = 0;
            for(Problem prob : uploadedProblems) {
                prob.dataset = datasetName;
                prob.grammarCheck = GrammarCheck.checkERG(prob.sQuestion);
                count++;
                if(count % 10 == 0) {
                		System.out.println("Problems checked : "+count);
                }
            }
            Database.add(uploadedProblems);
            return "Dataset successfully uploaded, "+uploadedProblems.size()+
            " new problems added";
        } catch (Exception e) {
            e.printStackTrace();
            return "Sorry, there was a problem";
        }
    }

//    public boolean ifDuplicate(Problem problem) {
//        try {
//            List<Problem> allProblems = Reader.readGenericFormatProblems(Params.problemsFile);
//            String sqTest = problem.sQuestion.replaceAll("[!?,;]",".");
//            while(sqTest.contains("  ")) {
//                sqTest = sqTest.replaceAll("  ", " ");
//            }
//            for (int i = 0; i < allProblems.size(); i++) {
//                String sq = allProblems.get(i).sQuestion.replaceAll("[!?,;]",".");
//                while(sq.contains("  ")) {
//                    sq = sq.replaceAll("  ", " ");
//                }
//
//                if(sq.equals(sqTest)) {
//                    return true;
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return false;
//    }

//    public Integer countProblemsWithoutDuplicates() {
//        Set<String> uniqueProblems = new HashSet<>();
//        try {
//            List<Problem> allProblems = Reader.readGenericFormatProblems(Params.problemsFile);
//            for (int i = 0; i < allProblems.size(); i++) {
//                String sq = allProblems.get(i).sQuestion.replaceAll("[!?,;]",".");
//                while(sq.contains("  ")) {
//                    sq = sq.replaceAll("  ", " ");
//                }
//                uniqueProblems.add(sq);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return uniqueProblems.size();
//    }
    
    public String getDatasetWithProperties(String datasetName, String size, 
    		String reduceLexOverlap, String reduceTemplateOverlap, String grammarCheck) {
    		System.out.println("Called getDataset");
    		boolean lex = reduceLexOverlap.equals("Y") ? true : false;
    		boolean tmpl = reduceTemplateOverlap.equals("Y") ? true : false;
    		boolean gc = grammarCheck.equals("Y") ? true : false;
        try {
        		int k = Integer.parseInt(size.trim());
	        	String intro = "Dataset : "+datasetName+"\nSize : "+k+"\nReduceLexicalOverlap : "+
	        			lex+"\nReduceTemplateOverlap : "+tmpl+"\nGrammatically Correct : "+gc+
	        			"\n\n\n";
            List<Problem> allProbs = Database.get(datasetName, gc);
            List<Problem> outProbs = MaxCoverage.selectByData(allProbs, k, lex, tmpl);
            Gson gson = new GsonBuilder().disableHtmlEscaping()
            		.setPrettyPrinting().create();
            return intro+gson.toJson(outProbs);
        } catch (Exception e) {
            e.printStackTrace();
            return "Sorry, there was a problem";
        }
    }
    
    public static void main(String[] args) throws Exception {
        startServer(8082);	
    }
    
    public static void startServer(int portNumber) {
        try {
            System.out.println("Attempting to start XML-RPC Server...");
            WebServer webServer = new WebServer(portNumber);
            XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();
            PropertyHandlerMapping phm = new PropertyHandlerMapping();
            phm.addHandler("sample", Server.class); //new JavaServer().getClass());
            xmlRpcServer.setHandlerMapping(phm);
            XmlRpcServerConfigImpl serverConfig = (XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
            serverConfig.setEnabledForExtensions(true);
            serverConfig.setContentLengthOptional(false);
            webServer.start();
            System.out.println("Started successfully.");
            System.out.println("Accepting requests. (Halt program to stop.)");
        } catch (Exception exception) {
            System.err.println("JavaServer: " + exception);
        }
    }
}