package server;

import java.io.File;
import java.io.IOException;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import org.apache.commons.io.FileUtils;
import structure.Problem;
import utils.Pipeline;
import utils.TemplateParser;


public class MaxCoverage {
    
	public static Pipeline pipeline;
	
    public static double lexiconWeight;
    public static double templateWeight;
    
    public static List<Set<String>> documentWords;
    public static Map<IntPair, Double> pairwiseSim;
    public static List<Integer> templateForDocuments;
    public static List<String> documents;
    public static List<Integer> selectedDocIndex;
    
    static {
    		pipeline = new Pipeline();
    		documentWords = new ArrayList<Set<String>>();
    	    templateForDocuments = new ArrayList<Integer>();
    	    documents = new ArrayList<String>();
    	    selectedDocIndex = new ArrayList<Integer>();
    	    pairwiseSim = new HashMap<IntPair, Double>();
    }
    
    public static List<Problem> selectByData(List<Problem> entireRepo, int k, 
    		boolean reduceLexOverlap, boolean reduceTemplateOverlap) {
        System.out.println(entireRepo.size());
        repoToDataStructure(entireRepo);
        double lex = reduceLexOverlap ? 1.0 : 0.0;
        double tmpl = reduceTemplateOverlap ? 1.0 : 0.0;
        select(k, lex, tmpl);
        List<Problem> selectedProblems = new ArrayList<>();
        for (int i = 0; i< selectedDocIndex.size(); i++) {
            selectedProblems.add(entireRepo.get(selectedDocIndex.get(i)));
        }
        System.out.println("Lexical Overlap : "+computeLexOverlap());
        System.out.println("Template Overlap : "+computeTmplOverlap());
        return selectedProblems;
    }

    public static void repoToDataStructure(List<Problem> entireRepo) {
        documentWords.clear();
        templateForDocuments.clear();
        documents.clear();
        selectedDocIndex.clear();
        pairwiseSim.clear();
        TemplateParser.populateTemplateIndexes(entireRepo);
        for (int i = 0; i < entireRepo.size(); i++) {
            documents.add(entireRepo.get(i).sQuestion);
            templateForDocuments.add(entireRepo.get(i).templateNumber);
        }
        for (int i = 0; i < documents.size(); i++) {
            documentWords.add(new HashSet<String>());
            documentWords.get(documentWords.size() - 1).addAll(
            		pipeline.getUnigramsBigrams(documents.get(i)));
        }
        for(int i=0; i<documents.size(); ++i) {
            for(int j=0; j<documents.size(); ++j) {
                if(i==j) continue;
                pairwiseSim.put(new IntPair(i, j), getSim(
                        documentWords.get(i), documentWords.get(j)));
            }
        }
    }
    
    public static void select(int k, double lW, double tW) {
        lexiconWeight = lW;
        templateWeight = tW;
        for (int i = 0; i < k; i++) {
	        	if(i == documents.size()) {
	        		break;
	        	}
            int bestChoice = findNextBest(k);
            selectedDocIndex.add(bestChoice);
        }
    }
    
    public static int findNextBest(int k) {
        int bestIndex = -1;
        double min = Double.MAX_VALUE;
        for (int i = 0; i < documentWords.size(); i++) {
        		if(selectedDocIndex.contains(i)) continue;
            double cost = 
            		(lexiconWeight*getAvgLexSimWithSelectedDocs(i)) 
            		+ (templateWeight*getAvgTmplSimWithSelectedDocs(i));
//            System.out.println("Cost : "+cost);
            if (cost < min) {
                bestIndex = i;
                min = cost;
            }
        }
        System.out.println("Min Cost : "+min);
        return bestIndex;
    }
    
    public static double getSim(Set<String> set1, Set<String> set2) {
    		double intersect = 0, union = set2.size();
    		for(String str : set1) {
    			if(set2.contains(str)) intersect++;
    			if(!set2.contains(str)) union++;
    		}
    		return intersect / (union+0.001);
    }
    
    public static double getAvgLexSimWithSelectedDocs(int index) {
    		double avgSim = 0.0;
		for(int i : selectedDocIndex) {
			avgSim += pairwiseSim.get(new IntPair(index, i));
		}
		return avgSim / (selectedDocIndex.size()+0.001);
    }
    
    public static double getAvgTmplSimWithSelectedDocs(int index) {
    		double avgSim = 0.0;
		for(int i : selectedDocIndex) {
			if(templateForDocuments.get(i) == templateForDocuments.get(index)) {
				avgSim += 1.0;
			}
		}
		return avgSim / (selectedDocIndex.size()+0.001);
    }
    
    public static double computeLexOverlap() {
		double totalSim=0.0;
		for(int i : selectedDocIndex) {
			for(int j : selectedDocIndex) {
				if(i==j) continue;
				totalSim+=pairwiseSim.get(new IntPair(i, j)); 
			}
		}
		return (totalSim)/(selectedDocIndex.size()*(selectedDocIndex.size()-1)+0.001);
    }
    
    public static double computeTmplOverlap() {
	    	double totalSim=0.0;
	    	for(int i : selectedDocIndex) {
	    		for(int j : selectedDocIndex) {
	    			if(i==j) continue;
	    			if(templateForDocuments.get(i) == templateForDocuments.get(j)) {
	    				totalSim += 1.0;
	    			}
	    		}
	    	}
	    	return (totalSim)/(selectedDocIndex.size()*(selectedDocIndex.size()-1)+0.001);
    }

    public static void main(String args[]) throws IOException {
        if(args.length < 4) {
            System.out.println("Usage: java -cp target/classes:target/dependency/* " +
                    "server.MaxCoverage " +
                    "<questions_file> " +
                    "<num_output_questions> " +
                    "<reduce_lexical_overlap (true/false)> " +
                    "<reduce_template_overlap (true/false)> " +
                    "<output_file (optional)>");
        }

        String questionsFile = args[0];
        int numOutputQuestions = Integer.parseInt(args[1]);
        boolean reduceLex = Boolean.parseBoolean(args[2]);
        boolean reduceTmpl = Boolean.parseBoolean(args[3]);
        String outputFile = null;
        if(args.length >= 5) {
            outputFile = args[4];
        }
        String json = FileUtils.readFileToString(new File(questionsFile));
        List<Problem> probs = new Gson().fromJson(json,
                new TypeToken<List<Problem>>(){}.getType());
        List<Problem> outputQuestions = selectByData(probs, numOutputQuestions, reduceLex, reduceTmpl);
        List<Integer> indices = new ArrayList<>();
        for(Problem prob : outputQuestions) {
            indices.add(prob.iIndex);
        }
        Collections.shuffle(indices);
        String str = "";
        for(int i=0; i<indices.size(); ++i) {
            str += indices.get(i) + "\n";
        }
        if(args.length >= 5) {
            FileUtils.writeStringToFile(new File(outputFile), str);
            System.out.println("The selected problem indices are written to "+outputFile);
        } else {
            System.out.println("The selected problem indices are printed below\n"+str);
        }
    }
    
}
