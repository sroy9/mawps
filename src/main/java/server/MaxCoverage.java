import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import structure.Problem;
import utils.StanfordLemmatizer;

public class MaxCoverage {
    
    public static double lexiconWeight = 0.5;
    public static double templateWeight = 1;
    public static double aveDocLength;
    public static String inputFileName = "lemmasDroppedFunctionWords.txt";
    public static String templateInputFileName = "templ.txt";
    
    public static ArrayList<ArrayList<String>> documentWords = new ArrayList<>();
    public static ArrayList<Integer> UsedTemplates = new ArrayList<>();
    public static ArrayList<Integer> templateForDocuments = new ArrayList<>();
    public static ArrayList<String> documents = new ArrayList<>();
    public static ArrayList<Integer> selectedDocIndex = new ArrayList<>();
    public static ArrayList<String> selectedWords = new ArrayList<>();
    
    public static void main (String argv[])
    {
        StanfordLemmatizer sl = new StanfordLemmatizer();
        System.out.println(sl.lemmatize("he eats an apple."));
        System.out.println(select(50));
    }
    
    public static ArrayList<Problem> selectByData(List<Problem> entireRepo, int k, boolean reduceLexOverlap,
                                                  boolean reduceTemplateOverlap,
                                                  boolean useGrammaticality) {
        for (int i = 0; i < entireRepo.size(); i++) {
            documents.add(entireRepo.get(i).sQuestion);
            templateForDocuments.add(entireRepo.get(i).templateNumber);
        }
        for (int i = 0; i < documents.size(); i++) {
            documentWords.add(new ArrayList<>());
            String[] temp = documents.get(i).split(" ");
            for (int j = 0; j < temp.length; j++) {
                documentWords.get(documentWords.size() - 1).add(temp[j]);
            }
        }
        select(k);
        ArrayList<Problem> selectedProblems = new ArrayList<>();
        for (int i = 0; i< selectedDocIndex.size(); i++) {
            selectedProblems.add(entireRepo.get(selectedDocIndex.get(i)));
        }
        return selectedProblems;
    }
    
    public static ArrayList<Problem> selectByData(List<Problem> entireRepo, int k, boolean reduceLexOverlap,
                                                  boolean reduceTemplateOverlap,
                                                  boolean useGrammaticality,
                                                  double lW, double tW ) {
        lexiconWeight = lW;
        templateWeight = tW;
        for (int i = 0; i < entireRepo.size(); i++) {
            documents.add(entireRepo.get(i).sQuestion);
            templateForDocuments.add(entireRepo.get(i).templateNumber);
        }
        for (int i = 0; i < documents.size(); i++) {
            documentWords.add(new ArrayList<>());
            String[] temp = documents.get(i).split(" ");
            for (int j = 0; j < temp.length; j++) {
                documentWords.get(documentWords.size() - 1).add(temp[j]);
            }
        }
        select(k);
        ArrayList<Problem> selectedProblems = new ArrayList<>();
        for (int i = 0; i< selectedDocIndex.size(); i++) {
            selectedProblems.add(entireRepo.get(selectedDocIndex.get(i)));
        }
        return selectedProblems;
    }
    
    public static ArrayList<String> select(int k, double lW, double tW) {
        lexiconWeight = lW;
        templateWeight = tW;
        parse();
        aveDocLength = calcAveDocLenght();
        for (int i = 0; i < k; i++) {
            int bestChoice = findNextBest();
            ChangeAccordingtoNextBase(bestChoice);
            if(i == documents.size()) {
                break;
            }
        }
        ArrayList<String> selectedQeustions = new ArrayList<>();
        for (int i = 0; i < selectedDocIndex.size(); i++) {
            selectedQeustions.add(documents.get(selectedDocIndex.get(i)));
        }
        return selectedQeustions;
    }
    public static ArrayList<String> select(int k) {
        parse();
        aveDocLength = calcAveDocLenght();
        for (int i = 0; i < k; i++) {
            int bestChoise = findNextBest();
            ChangeAccordingtoNextBase(bestChoise);
            if(i == documents.size()) {
                break;
            }
        }
        ArrayList<String> selectedQeustions = new ArrayList<>();
        for (int i = 0; i < selectedDocIndex.size(); i++) {
            selectedQeustions.add(documents.get(selectedDocIndex.get(i)));
        }
        return selectedQeustions;
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
        int maxCoverage = 0;
        int addingtemplateCost = 0;
        for (int i = 0; i < documentWords.size(); i++) {
            if (templateForDocuments.get(i) != null) {
                addingtemplateCost = 1;
            }
            else if (!UsedTemplates.contains(templateForDocuments.get(i))) {
                addingtemplateCost = 1;
            }
            int intersect = findIntersection(documentWords.get(i));
            if (((documentWords.get(i).size() - intersect) /aveDocLength) * lexiconWeight + templateWeight * (addingtemplateCost / templateForDocuments.size()) > maxCoverage) {
                bestIndex = i;
                maxCoverage = documentWords.get(i).size() - intersect;
            }
        }
        return bestIndex;
    }
    
    public static int findIntersection(ArrayList<String> words) {
        int res = 0;
        for (int i = 0; i < words.size(); i++) {
            if (selectedWords.contains(words.get(i))) {
                res++;
            }
        }
        return res;
    }
    
    public static void parse() {
        try {
            StanfordLemmatizer lemmatizer = new StanfordLemmatizer();
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader(utils.Params.problemsFile));
            JSONArray jsonArray =  (JSONArray) obj;
            for (int i = 0; i < jsonArray.size(); i++) {
                String doc = ((String) ((JSONObject) jsonArray.get(i)).get("sQuestion"));
                List<String> lemma = lemmatizer.lemmatize(doc);
                String lemmatizedDoc = "";
                for(int j = 0; j < lemma.size(); j++) {
                    lemmatizedDoc = lemmatizedDoc + lemma.get(j) + " ";
                }
                documents.add(lemmatizedDoc);
            }
            for (int i = 0; i < documents.size(); i++) {
                documentWords.add(new ArrayList<>());
                String[] temp = documents.get(i).split(" ");
                for (int j = 0; j < temp.length; j++) {
                    documentWords.get(documentWords.size() - 1).add(temp[j]);
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("File does not exsist");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Read Error");
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } 
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
