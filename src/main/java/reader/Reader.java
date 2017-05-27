package reader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import structure.DolphinFormat;
import structure.DrawFormat;
import structure.Problem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;


public class Reader {

	public static List<DrawFormat> readDrawFormatProblems(String fileName) throws Exception {
		String json = FileUtils.readFileToString(new File(fileName));
		List<DrawFormat> probs = new Gson().fromJson(json, 
				new TypeToken<List<DrawFormat>>(){}.getType());
		return probs;
	}

	public static List<DolphinFormat> readDolphinFormatProblems(String fileName) throws Exception {
		String json = FileUtils.readFileToString(new File(fileName));
		List<DolphinFormat> probs = new Gson().fromJson(json, 
				new TypeToken<List<DolphinFormat>>(){}.getType());
		return probs;
	}
	
	public static List<Problem> readGenericFormatProblems(String fileName) throws Exception {
		String json = FileUtils.readFileToString(new File(fileName));
		List<Problem> probs = new Gson().fromJson(json, 
				new TypeToken<List<Problem>>(){}.getType());
		return probs;
	}
	
	public static void cleanKushmanProblems() throws Exception {
		List<Problem> probs = readGenericFormatProblems("/Users/subhroroy/Desktop/algebra.json");
		System.out.println("Kushman all : "+probs.size());
		List<Problem> probsWithEq = new ArrayList<>();
		for(Problem prob : probs) {
			if(prob.lEquations != null) {
				probsWithEq.add(prob);
			}
		}
		Gson gson = new GsonBuilder().disableHtmlEscaping()
        		.setPrettyPrinting().create();
		FileUtils.writeStringToFile(new File("/Users/subhroroy/Desktop/algebraWithEq.json"), 
				gson.toJson(probsWithEq));
		System.out.println("Kushman with Eq : "+probsWithEq.size());
	}
	
	public static void convertRikProblems() throws Exception {
		List<String> lines = FileUtils.readLines(new File(
				"data/singleEq/FullDataset.txt"));
		List<Problem> probs = new ArrayList<>();
		for(int i=0; i<lines.size(); i+=3) {
			Problem prob = new Problem();
			prob.sQuestion = lines.get(i).trim();
			prob.lEquations = new ArrayList<>();
			prob.lEquations.add(lines.get(i+1).trim());
			prob.lSolutions = new ArrayList<>();
			prob.lSolutions.add(Double.parseDouble(lines.get(i+2).trim()));
			probs.add(prob);
		}
		Gson gson = new GsonBuilder().disableHtmlEscaping()
        		.setPrettyPrinting().create();
		FileUtils.writeStringToFile(new File("data/singleEq/all.json"), 
				gson.toJson(probs));
	}
	
	public static void main(String args[]) throws Exception {

//		List<DrawFormat> kushman = Reader.readDrawFormatProblems("data/draw/kushman.json");	
//		List<DrawFormat> draw = Reader.readDrawFormatProblems("data/draw/final.json");	
//		List<DolphinFormat> dolphin = Reader.readDolphinFormatProblems("data/dolphin/number_word_std.test.json");
//		System.out.println("Kushman : "+kushman.size());
//		System.out.println("Draw : "+draw.size());
//		System.out.println("Dolphin : "+dolphin.size());
//		cleanKushmanProblems();
		convertRikProblems();
		
	}
}
