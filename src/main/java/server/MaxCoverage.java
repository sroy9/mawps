package server;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import structure.Problem;
import utils.StanfordLemmatizer;
import utils.TemplateParser;

public class MaxCoverage {
    
    public static double lexiconWeight;
    public static double templateWeight;
    
    public static List<Set<String>> documentWords;
    public static List<Integer> usedTemplates;
    public static List<Integer> templateForDocuments;
    public static List<String> documents;
    public static List<Integer> selectedDocIndex;
    public static List<String> selectedWords;
    
    public static StanfordLemmatizer lemmatizer;
    
    static {
    		lemmatizer = new StanfordLemmatizer();
    		documentWords = new ArrayList<Set<String>>();
    	    usedTemplates = new ArrayList<Integer>();
    	    templateForDocuments = new ArrayList<Integer>();
    	    documents = new ArrayList<String>();
    	    selectedDocIndex = new ArrayList<Integer>();
    	    selectedWords = new ArrayList<String>();
    }
    
    public static void main (String argv[])
    {    
//		selectByData(entireRepo, k, reduceLexOverlap, reduceTemplateOverlap) 
//    	--> problems are given as an input
//		(entireRepo == the problem set, k == number of documents you wish to select, 
//    	reduceLexOverlap & reduceTemplateOverlap == 2 boolean values)
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
        // Compute lexical overlap
        System.out.println("Lexical Variety : "+selectedWords.size()*1.0/k);
        System.out.println("Template Variety : "+usedTemplates.size()*1.0/k);
        System.out.println("Lexical Overlap : "+k*1.0/selectedWords.size());
        System.out.println("Template Overlap : "+k*1.0/usedTemplates.size());
        return selectedProblems;
    }

    public static void repoToDataStructure(List<Problem> entireRepo) {
    		documentWords.clear();
        usedTemplates.clear();
        templateForDocuments.clear();
        documents.clear();
        selectedDocIndex.clear();
        selectedWords.clear();
        TemplateParser.populateTemplateIndexes(entireRepo);
        for (int i = 0; i < entireRepo.size(); i++) {
            documents.add(entireRepo.get(i).sQuestion);
            templateForDocuments.add(entireRepo.get(i).templateNumber);
        }
        for (int i = 0; i < documents.size(); i++) {
            documentWords.add(new HashSet<String>());
//            String[] temp = documents.get(i).split(" ");
//            for (int j = 0; j < temp.length; j++) {
//                documentWords.get(documentWords.size() - 1).add(temp[j].trim());
//            }
            documentWords.get(documentWords.size() - 1).addAll(
            		lemmatizer.lemmatize(documents.get(i)));
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
            ChangeAccordingtoNextBase(bestChoice);
        }
    }
    
    public static void ChangeAccordingtoNextBase(int nextBestIndex) {
        for (String word : documentWords.get(nextBestIndex)) {
            if (!selectedWords.contains(word)) {
                selectedWords.add(word);
            }
        }
        if (!usedTemplates.contains(templateForDocuments.get(nextBestIndex))) {
            usedTemplates.add(templateForDocuments.get(nextBestIndex));
        }
        selectedDocIndex.add(nextBestIndex);
    }
    
    public static int findNextBest(int k) {
        int bestIndex = -1;
        double maxCoverage = -1.0;
        int addingtemplateCost;
        for (int i = 0; i < documentWords.size(); i++) {
            	if (!usedTemplates.contains(templateForDocuments.get(i))) {
                addingtemplateCost = 1;
            } else {
            		addingtemplateCost = 0;
            }
            double coverage = 
            		(lexiconWeight*findNumOfNewWords(documentWords.get(i))*1.0/k) 
            		+ (templateWeight *addingtemplateCost*1.0/k);
            if (coverage > maxCoverage && !selectedDocIndex.contains(i)) {
                bestIndex = i;
                maxCoverage = coverage;
            }
        }
        System.out.println("Coverage : "+maxCoverage);
        return bestIndex;
    }
    
    public static int findNumOfNewWords(Set<String> words) {
        Set<String> newWords = new HashSet<String>();
        for (String word : words) {
            if (!selectedWords.contains(word)) {
                newWords.add(word);
            }
        }
        return newWords.size();
    }
    
}