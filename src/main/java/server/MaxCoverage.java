package server;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import structure.Problem;
import utils.TemplateParser;

public class MaxCoverage {
    
    public static double lexiconWeight;
    public static double templateWeight;
    public static double totalWords, totalTemplates;
    
    public static List<List<String>> documentWords;
    public static List<Integer> usedTemplates;
    public static List<Integer> templateForDocuments;
    public static List<String> documents;
    public static List<Integer> selectedDocIndex;
    public static List<String> selectedWords;
    
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
        System.out.println("Fraction of words covered : "+
        		selectedWords.size()*1.0/totalWords);
        System.out.println("Fraction of templates covered : "+
        		usedTemplates.size()*1.0/totalTemplates);
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
            documentWords.add(new ArrayList<String>());
            String[] temp = documents.get(i).split(" ");
            for (int j = 0; j < temp.length; j++) {
                documentWords.get(documentWords.size() - 1).add(temp[j].trim());
            }
        }
        // Get unique templates and words
        Set<String> allWords = new HashSet<String>();
        for(List<String> doc : documentWords) {
        		allWords.addAll(doc);
        }
        totalWords = allWords.size();
        Set<Integer> allTemplates = new HashSet<Integer>();
        allTemplates.addAll(templateForDocuments);
        totalTemplates = allTemplates.size();
        
    }
    
    public static void select(int k, double lW, double tW) {
        lexiconWeight = lW;
        templateWeight = tW;
        for (int i = 0; i < k; i++) {
	        	if(i == documents.size()) {
	        		break;
	        	}
            int bestChoice = findNextBest();
            ChangeAccordingtoNextBase(bestChoice);
        }
    }
    
    public static void ChangeAccordingtoNextBase(int nextBestIndex) {
        for (int i = 0; i < documentWords.get(nextBestIndex).size(); i++) {
            if (!selectedWords.contains(documentWords.get(nextBestIndex).get(i))) {
                selectedWords.add(documentWords.get(nextBestIndex).get(i));
            }
        }
        if (!usedTemplates.contains(templateForDocuments.get(nextBestIndex))) {
            usedTemplates.add(templateForDocuments.get(nextBestIndex));
        }
        selectedDocIndex.add(nextBestIndex);
    }
    
    public static int findNextBest() {
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
            		(lexiconWeight*findNumOfNewWords(documentWords.get(i))*1.0/totalWords) 
            		+ (templateWeight *addingtemplateCost*1.0/totalTemplates);
            if (coverage > maxCoverage && !selectedDocIndex.contains(i)) {
                bestIndex = i;
                maxCoverage = coverage;
            }
        }
        System.out.println("Coverage : "+maxCoverage);
        return bestIndex;
    }
    
    public static int findNumOfNewWords(List<String> words) {
        Set<String> newWords = new HashSet<String>();
        for (int i = 0; i < words.size(); i++) {
            if (!selectedWords.contains(words.get(i))) {
                newWords.add(words.get(i));
            }
        }
        return newWords.size();
    }
    
//    public static int findIntersection(List<String> words) {
//        int res = 0;
//        for (int i = 0; i < words.size(); i++) {
//            if (selectedWords.contains(words.get(i))) {
//                res++;
//            }
//        }
//        return res;
//    }
    
}