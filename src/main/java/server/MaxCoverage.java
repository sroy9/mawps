import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import structure.Problem;
import utils.Params;
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
        //		selectByFileRoutine(k, lW, tW) --> when you have the file that is addressed in Util/Params.java
        //		(k == number of documents you wish to select, lW == lexicon overlap weight , tW == template overlap weight)
        
        //		selectByFileRoutine(k) --> when you have the file that is addressed in Util/Params.java
        //		(k == number of documents you wish to select)
        
        //		selectByData(entireRepo, k, reduceLexOverlap, reduceTemplateOverlap, useGrammaticality) --> problems are given as an input
        //		(entireRepo == the problem set, k == number of documents you wish to select, reduceLexOverlap & reduceTemplateOverlap & useGrammaticality == 3 boolean values)
        
        //		selectByData(entireRepo, k, reduceLexOverlap, reduceTemplateOverlap, useGrammaticality, lW, tW) --> problems are given as an input
        //		(entireRepo == the problem set, k == number of documents you wish to select,
        //		reduceLexOverlap & reduceTemplateOverlap & useGrammaticality == 3 boolean values, lW == lexicon overlap weight , tW == template overlap weight)
    }
    
    public static ArrayList<Problem> selectByData(List<Problem> entireRepo, int k, boolean reduceLexOverlap,
                                                  boolean reduceTemplateOverlap,
                                                  boolean useGrammaticality) {
        System.out.println(entireRepo.size());
        repoToDataStructure(entireRepo);
        select(k);
        ArrayList<Problem> selectedProblems = new ArrayList<>();
        for (int i = 0; i< selectedDocIndex.size(); i++) {
            selectedProblems.add(entireRepo.get(selectedDocIndex.get(i)));
        }
        return findSelectedProblems(entireRepo);
    }
    public static ArrayList<Problem> selectByData(List<Problem> entireRepo, int k, boolean reduceLexOverlap,
                                                  boolean reduceTemplateOverlap,
                                                  boolean useGrammaticality,
                                                  double lW, double tW ) {
        repoToDataStructure(entireRepo);
        select(k, lW, tW);
        return findSelectedProblems(entireRepo);
        
    }
    
    public static ArrayList<String> selectByFileRoutine(int k, double lW, double tW) {
        parse();
        return select(k, lW, tW);
    }
    
    public static ArrayList<String> selectByFileRoutine(int k) {
        parse();
        return select(k);
    }
    
    public static void repoToDataStructure(List<Problem> entireRepo) {
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
    }
    
    private static ArrayList<Problem> findSelectedProblems(List<Problem> entireRepo) {
        ArrayList<Problem> selectedProblems = new ArrayList<>();
        for (int i = 0; i< selectedDocIndex.size(); i++) {
            selectedProblems.add(entireRepo.get(selectedDocIndex.get(i)));
        }
        return selectedProblems;
    }
    
    
    public static ArrayList<String> select(int k, double lW, double tW) {
        lexiconWeight = lW;
        templateWeight = tW;
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
    
    public static void makeDataSet() {
        try {
            BufferedReader in = new BufferedReader(new FileReader(new File("data.txt")));
            BufferedReader intmpl = new BufferedReader(new FileReader(new File("templ.txt")));
            int index = 0;
            String text = in.readLine();
            int template = Integer.parseInt(intmpl.readLine());
            ArrayList<Problem> probs = new ArrayList<>();
            while(text != null) {
                if (index % 3 == 0) {
                    Problem prob = new Problem();
                    prob.sQuestion = text;
                    prob.templateNumber = template;
                    probs.add(prob);
                    template = Integer.parseInt(intmpl.readLine());
                }
                text = in.readLine();
                index++;
            }
            Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
            String json = gson.toJson(probs);
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(Params.problemsFile)));
            bw.write(json);
            bw.close();
            System.out.println(selectByData(probs, 50, true, true, true));
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
    public static void parse() {
        try {
            StanfordLemmatizer lemmatizer = new StanfordLemmatizer();
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader(utils.Params.problemsFile));
            JSONArray jsonArray =  (JSONArray) obj;
            for (int i = 0; i < jsonArray.size(); i++) {
                String doc = ((String) ((JSONObject) jsonArray.get(i)).get("sQuestion"));
                int templateNum = ((Long) ((JSONObject) jsonArray.get(i)).get("templateNumber")).intValue();
                templateForDocuments.add(templateNum);
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
