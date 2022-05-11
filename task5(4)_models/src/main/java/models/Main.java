package models;

import concordance.Util;
import frequency_dictionary.Dictionary;
import frequency_dictionary.Lemma;
import ngramms.NGramms;

import javax.xml.stream.XMLStreamException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Handler;

public class Main {
    public static void main(String[] args) throws IOException, XMLStreamException {
        /*BufferedReader reader = new BufferedReader(
                new FileReader("src/main/resources/input.txt"));*/
        //double threshold = Double.parseDouble(reader.readLine());
        String corpora = Files.readString(Paths.get("../corps/dataset_news_science.txt"));
        Dictionary dictionary = new Dictionary("../dictionary/dict.opcorpora.xml");

        System.out.println("Finding n-gramms...");
        Instant start = Instant.now();


        ArrayList<Model> models = Model.loadModels("src/main/java/models/models.txt", "src/main/java/models/model_elements.txt", dictionary);
        List<String> sentences = Util.splitOnSentences(corpora);
        List<List<String>> sentencesTokens = Util.splitSentencesTokens(sentences);
        ArrayList<ArrayList<Lemma>> sentencesLemmas = dictionary.lemmatizeTextTokens(sentencesTokens);
        HashMap<Model, ArrayList<Integer>> foundedSentences = Model.findModels(sentencesLemmas);
        Model.printFoundModels(foundedSentences, sentences, "src/main/resources/output.txt");
        /*String ss = new String("Земля смещается".getBytes(), StandardCharsets.UTF_8);
        sentences.stream().map(s -> {
            if (s.contains(new String("Земля смещается".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8)))
                System.out.println("lol");
            return s;
        });*/
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        System.out.println("Finished");
        System.out.println("Elapsed time: " + timeElapsed + "ms");
    }
}
