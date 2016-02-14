package utils;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.util.CoreMap;

public class Pipeline {

    protected StanfordCoreNLP pipeline;
    
    public Pipeline() {
        // Create StanfordCoreNLP object properties, with POS tagging
        // (required for lemmatization), and lemmatization
        Properties props;
        props = new Properties();
        props.put("annotators", "tokenize, ssplit");
        // StanfordCoreNLP loads a lot of models, so you probably
        // only want to do this once per execution
        this.pipeline = new StanfordCoreNLP(props);
    }

    public Set<String> lemmatize(String documentText) {
        Set<String> lemmas = new HashSet<String>();
        Annotation document = new Annotation(documentText);
        this.pipeline.annotate(document);
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        for(CoreMap sentence: sentences) {
            for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
                lemmas.add(token.get(LemmaAnnotation.class));
            }
        }
        return lemmas;
    }
    
    public Set<String> getUnigramsBigrams(String documentText) {
        Set<String> ngrams = new HashSet<String>();
        Annotation document = new Annotation(documentText);
        this.pipeline.annotate(document);
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        for(CoreMap sentence: sentences) {
            // Iterate over all tokens in a sentence
            for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
                // Retrieve and add the lemma for each word into the list of lemmas
                ngrams.add(token.originalText());
            }
            for (int i=0; i<sentence.get(TokensAnnotation.class).size()-1; ++i) {
            		CoreLabel token1 = sentence.get(TokensAnnotation.class).get(i);
            		CoreLabel token2 = sentence.get(TokensAnnotation.class).get(i+1);
                ngrams.add(token1+"_"+token2);
            }
        }
        return ngrams;
    }
    
    public static List<String> getSentences(String documentText) {
	    	Reader reader = new StringReader(documentText);
	    	DocumentPreprocessor dp = new DocumentPreprocessor(reader);
	    	List<String> sentenceList = new ArrayList<String>();
	    	for (List<HasWord> sentence : dp) {
	    	   String sentenceString = Sentence.listToString(sentence);
	    	   sentenceList.add(sentenceString.toString());
	    	}
	    	return sentenceList;
	}
    public static void main(String args[]) {
    		System.out.println(Arrays.asList(Pipeline.getSentences("I have 4 dogs. I also have 3.5 dollars.")));
    		System.out.println(Arrays.asList(new Pipeline().getUnigramsBigrams("I have 4 dogs. I also have 3.5 dollars.")));
    }
}