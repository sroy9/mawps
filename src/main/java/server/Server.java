package server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import reader.Reader;
import structure.Problem;
import utils.Params;

public class Server {
	
	public String addProblem(String question, String equations, String answers) {
		if(question.trim().equals("") && equations.trim().equals("") &&
				answers.trim().equals("")) {
			return "";
		}
		try {
			Problem prob = new Problem();
			prob.dataset = "Unspecified";
			prob.iIndex = -1;
			prob.fold = -1;
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
			List<Problem> allProblems = Reader.readGenericFormatProblems(
					Params.problemsFile);
			allProblems.add(prob);
			Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
			String json = gson.toJson(allProblems);  
			BufferedWriter bw = new BufferedWriter(
					new FileWriter(new File(Params.problemsFile)));  
			bw.write(json);  
			bw.close();  
//			System.out.println(json);
			return "Problem successfully uploaded";
		} catch (Exception e) {
			e.printStackTrace();
			return "Sorry, there was a problem";
		}
	}
	
	public String addDataset(String datasetName, String allQuestions) {
		if(datasetName.trim().equals("") && allQuestions.trim().equals("")) {
			return "";
		}
		try {
			List<Problem> allProblems = Reader.readGenericFormatProblems(
					Params.problemsFile);
			boolean allow = true;
			for(Problem prob : allProblems) {
				if(prob.dataset.equals(datasetName) && !datasetName.equals("Unspecified")) {
					allow = false;
					break;
				}
			}
			if(!allow) return "Dataset name already taken, try something else";
			List<Problem> uploadedProblems = new Gson().fromJson(allQuestions, 
					new TypeToken<List<Problem>>(){}.getType());
			for(Problem prob : uploadedProblems) {
				prob.dataset = datasetName;
				prob.fold = -1;
			}
			allProblems.addAll(uploadedProblems);
			Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
			String json = gson.toJson(allProblems);  
			BufferedWriter bw = new BufferedWriter(
					new FileWriter(new File(Params.problemsFile)));  
			bw.write(json);  
			bw.close();  
//			System.out.println(json);
			return "Dataset successfully uploaded, "+uploadedProblems.size()+
					" new problems added";
		} catch (Exception e) {
			e.printStackTrace();
			return "Sorry, there was a problem";
		}
	}
	
	public String viewFolds(String datasetName, 
			String templateOverlap, String lexicalOverlap) {
		if(datasetName.trim().equals("") && templateOverlap.trim().equals("") &&
				lexicalOverlap.trim().equals("")) {
			return "";
		}
		try {
			List<Problem> allProbs = Reader.readGenericFormatProblems(Params.problemsFile);
			List<Problem> outProbs = new ArrayList<>();
			for(Problem prob : allProbs) {
				if(prob.dataset.equals(datasetName) || datasetName.trim().equals("")) {
					outProbs.add(prob);
				}
			}
			Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
			return gson.toJson(outProbs);
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
//			serverConfig.setKeepAliveEnabled(true);
//			boolean res = serverConfig.isKeepAliveEnabled();
			webServer.start();
			System.out.println("Started successfully.");
			System.out.println("Accepting requests. (Halt program to stop.)");
		} catch (Exception exception) {
			System.err.println("JavaServer: " + exception);
		}
	}
}