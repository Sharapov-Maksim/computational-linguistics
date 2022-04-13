package ngramms;

import frequency_dictionary.Dictionary;

import javax.xml.stream.XMLStreamException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;

public class Main {
    public static void main(String[] args) throws IOException, XMLStreamException {
        BufferedReader reader = new BufferedReader(
                new FileReader("src/main/resources/input.txt"));
        int n = Integer.parseInt(reader.readLine());
        double threshold = Double.parseDouble(reader.readLine());
        String corpora = Files.readString(Paths.get("../corps/dataset_news_science.txt"));
        Dictionary dictionary = new Dictionary("../dictionary/dict.opcorpora.xml");

        System.out.println("Finding n-gramms...");
        Instant start = Instant.now();

        NGramms nGramms = new NGramms(corpora, n, threshold, dictionary);
        nGramms.printOverallStatistics("src/main/resources/ngramms.txt");
        nGramms.printFilteredStatistics("src/main/resources/ngramms_filtered.txt");

        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        System.out.println("Finished");
        System.out.println("Elapsed time: " + timeElapsed + "ms");
    }
}
