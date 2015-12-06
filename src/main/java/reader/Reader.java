package reader;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;

import structure.DolphinFormat;
import structure.DrawFormat;
import structure.Problem;

import com.google.gson.Gson;
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
	
	public static void main(String args[]) throws Exception {

		List<DrawFormat> kushman = Reader.readDrawFormatProblems("data/draw/kushman.json");	
		List<DrawFormat> draw = Reader.readDrawFormatProblems("data/draw/final.json");	
		List<DolphinFormat> dolphin = Reader.readDolphinFormatProblems("data/dolphin/number_word_std.test.json");
		System.out.println("Kushman : "+kushman.size());
		System.out.println("Draw : "+draw.size());
		System.out.println("Dolphin : "+dolphin.size());
		
	}
}
