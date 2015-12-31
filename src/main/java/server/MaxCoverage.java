package server;

import java.util.ArrayList;
import java.util.List;

import structure.Problem;
import utils.TemplateParser;

public class MaxCoverage {
    
    public static double lexiconWeight = 0.5;
    public static double templateWeight = 1;
    public static double aveDocLength;
    
    public static List<List<String>> documentWords = new ArrayList<>();
    public static List<Integer> UsedTemplates = new ArrayList<>();
    public static List<Integer> templateForDocuments = new ArrayList<>();
    public static List<String> documents = new ArrayList<>();
    public static List<Integer> selectedDocIndex = new ArrayList<>();
    public static List<String> selectedWords = new ArrayList<>();
    
    public static void main (String argv[])
    {    
//		selectByData(entireRepo, k, reduceLexOverlap, reduceTemplateOverlap) 
//    	--> problems are given as an input
//		(entireRepo == the problem set, k == number of documents you wish to select, 
//    	reduceLexOverlap & reduceTemplateOverlap & useGrammaticality == 3 boolean values)
    }
    
    public static List<Problem> selectByData(List<Problem> entireRepo, int k, 
    		boolean reduceLexOverlap, boolean reduceTemplateOverlap) {
        System.out.println(entireRepo.size());
        repoToDataStructure(entireRepo);
        double lex = reduceLexOverlap ? 1.0 : 0.0;
        double tmpl = reduceTemplateOverlap ? 1.0 : 0.0;
        if(lex < 0.001 && tmpl < 0.001) {
        		lex = 0.5;
        		tmpl = 1.0;
        }
        select(k, lex, tmpl);
        List<Problem> selectedProblems = new ArrayList<>();
        for (int i = 0; i< selectedDocIndex.size(); i++) {
            selectedProblems.add(entireRepo.get(selectedDocIndex.get(i)));
        }
        return selectedProblems;
    }

    public static void repoToDataStructure(List<Problem> entireRepo) {
    		documentWords.clear();
        UsedTemplates.clear();
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
                documentWords.get(documentWords.size() - 1).add(temp[j]);
            }
        }
    }
    
    public static void select(int k, double lW, double tW) {
        lexiconWeight = lW;
        templateWeight = tW;
        aveDocLength = calcAveDocLenght();
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
        if (!UsedTemplates.contains(templateForDocuments.get(nextBestIndex)))
            UsedTemplates.add(templateForDocuments.get(nextBestIndex));
        selectedDocIndex.add(nextBestIndex);
    }
    
    public static int findNextBest() {
        int bestIndex = 0;
        double maxCoverage = 0;
        int addingtemplateCost = 0;
        for (int i = 0; i < documentWords.size(); i++) {
//		ASK AIDA        	
//            if (templateForDocuments.get(i) != null) {
//                addingtemplateCost = 1;
//            } else 
            	if (!UsedTemplates.contains(templateForDocuments.get(i))) {
                addingtemplateCost = 1;
            }
            int intersect = findIntersection(documentWords.get(i));
            double coverage = 
            		(((documentWords.get(i).size() - intersect)*1.0/aveDocLength) * lexiconWeight) 
            		+ (templateWeight * (addingtemplateCost*1.0/templateForDocuments.size()));
            if (coverage > maxCoverage) {
                bestIndex = i;
                maxCoverage = coverage;
            }
        }
        return bestIndex;
    }
    
    public static int findIntersection(List<String> words) {
        int res = 0;
        for (int i = 0; i < words.size(); i++) {
            if (selectedWords.contains(words.get(i))) {
                res++;
            }
        }
        return res;
    }
    
    public static double calcAveDocLenght() {
        double ave = 0.0;
        for (int i = 0; i < documents.size(); i++) {
            String[] temp = documents.get(i).split("[ !?.,]");
            ave += temp.length;
        }
        return (ave + 0.0)/documents.size();	
    }
    
}