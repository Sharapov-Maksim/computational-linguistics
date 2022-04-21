package ngramms;

import concordance.Util;
import frequency_dictionary.Dictionary;

import javax.xml.stream.XMLStreamException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException, XMLStreamException {
        System.out.println("Start...");
        BufferedReader reader = new BufferedReader(
                new FileReader("src/main/resources/input.txt"));
        double threshold = Double.parseDouble(reader.readLine());
        String corpora = Files.readString(Paths.get("../corps/dataset_news_science.txt"));
        Dictionary dictionary = new Dictionary("../dictionary/dict.opcorpora.xml");

        System.out.println("Finding n-gramms...");
        Instant start = Instant.now();

        NGramms nGramms = new NGramms(corpora, threshold, dictionary);

        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        System.out.println("Finished");
        System.out.println("Elapsed time: " + timeElapsed + "ms");
    }
}
