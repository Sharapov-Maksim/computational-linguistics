package models;

import concordance.Util;
import frequency_dictionary.Dictionary;
import frequency_dictionary.Lemma;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException, XMLStreamException {
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

        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        System.out.println("Finished");
        System.out.println("Elapsed time: " + timeElapsed + "ms");
    }
}
