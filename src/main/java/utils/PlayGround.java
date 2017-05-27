package utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.FileUtils;
import structure.Problem;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by subhror on 5/19/16.
 */

/*
Example(
        "John has y dollars to spend on some new CDs from Music Plus, an online record store. He can buy any CDs at the members' price of x dollars each. To be a member, John has to pay a one-time fee of $19. Which of the following expressions represents the number of CDs John can purchase from Music Plus?",
        "",
        List(

        )
)
*/

class KaplanFormat {
    int id;
    String exam;
    int sectionNumber;
    int sectionLength;
    int originalQuestionNumber;
    String question;
    String answer;
    Map<String, String> choices;
    List<String> tags;
}

public class PlayGround {

    public static void createVariedSet(String outputFile, int max) throws Exception {
        String json = FileUtils.readFileToString(new File(
                "data/allArith/questions.json"));
        List<Problem> probsArith = new Gson().fromJson(json,
                new TypeToken<List<Problem>>(){}.getType());
        for(Problem prob : probsArith) {
            prob.dataset = "allArith";
        }
        json = FileUtils.readFileToString(new File(
                "data/kushman/questions.json"));
        List<Problem> probsAlgebra = new Gson().fromJson(json,
                new TypeToken<List<Problem>>(){}.getType());
        for(Problem prob : probsAlgebra) {
            prob.dataset = "Kushman";
        }
        List<Problem> allProbs = new ArrayList<>();
        allProbs.addAll(probsArith);
        allProbs.addAll(probsAlgebra);
        TemplateParser.populateTemplateIndexes(allProbs);
        Map<Integer, List<Integer>> templateMap = new HashMap<>();
        for(int i=0; i<allProbs.size(); ++i) {
            Problem prob = allProbs.get(i);
            if(!templateMap.containsKey(prob.templateNumber)) {
                templateMap.put(prob.templateNumber, new ArrayList<Integer>());
            }
            templateMap.get(prob.templateNumber).add(i);
        }
        List<Problem> selectedProblems = new ArrayList<>();
        int prevSize = -1;
        while(true) {
            for (Integer templateNo : templateMap.keySet()) {
                if (templateMap.get(templateNo).size() == 0) continue;
                Collections.shuffle(templateMap.get(templateNo));
                selectedProblems.add(allProbs.get(templateMap.get(templateNo).get(0)));
                if (selectedProblems.size() >= max) break;
                templateMap.get(templateNo).remove(0);
            }
            if (selectedProblems.size() >= max) break;
            if(selectedProblems.size() == prevSize) break;
            prevSize = selectedProblems.size();
        }
        Gson gson = new GsonBuilder().disableHtmlEscaping()
                .setPrettyPrinting().create();
        FileUtils.writeStringToFile(new File(outputFile),
                gson.toJson(selectedProblems));
    }

    public static void printExampleFormat() throws IOException {
        String json = FileUtils.readFileToString(new File(
                "../euclid/src/main/resources/openalgebra/openalgebra.json"));
        List<Problem> probsOpenAlg = new Gson().fromJson(json,
                new TypeToken<List<Problem>>(){}.getType());
        for(Problem prob : probsOpenAlg) {
            System.out.println("Example(");
            System.out.println("\""+prob.dataset+"\",");
            System.out.println(prob.iIndex+",");
            System.out.println("\""+prob.sQuestion+"\",");
            System.out.println("\""+Arrays.asList(prob.lSolutions)+"\",");
            System.out.println("List(\n\n\n)\n),");
        }
        json = FileUtils.readFileToString(new File(
                "../euclid/src/main/resources/SATQuestionsBatch1.json"));
        List<KaplanFormat> probsKaplan = new Gson().fromJson(json,
                new TypeToken<List<KaplanFormat>>(){}.getType());
        for(KaplanFormat prob : probsKaplan) {
            if(!prob.tags.contains("open")) continue;
            String answer = null;
            System.out.println("Example(");
            System.out.println("\"Kaplan\",");
            System.out.println(prob.id+",");
            System.out.println("\""+prob.question+"\",");
            if(prob.answer.equals("A") || prob.answer.equals("B") ||
                    prob.answer.equals("C") || prob.answer.equals("D") ||
                    prob.answer.equals("E")) {
                answer = prob.choices.get(prob.answer);
            } else {
                answer = prob.answer;
            }
            System.out.println("\""+answer+"\",");
            System.out.println("List(\n\n\n)\n),");
        }
    }

    public static void main(String args[]) throws Exception {
//        createVariedSet("openalgebra.json", 200);
        printExampleFormat();
    }

}
