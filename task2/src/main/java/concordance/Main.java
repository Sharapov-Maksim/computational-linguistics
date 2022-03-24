package concordance;

import frequency_dictionary.Dictionary;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException, XMLStreamException {
        BufferedReader reader = new BufferedReader(
                new FileReader("src/main/resources/input.txt"));
        System.out.println("Enter phrase:");
        String phrase = reader.readLine();
        int n = Integer.parseInt(reader.readLine());
        Integer threshold = Integer.parseInt(reader.readLine());
        String corpora = Files.readString(Paths.get("../corps/dataset_news_science.txt"));
        Dictionary dictionary = new Dictionary("src/main/resources/dict.opcorpora.xml");
        Concordance concordance = new Concordance(phrase, corpora, n, dictionary);
        concordance.printStatistics("src/main/resources/output.txt", threshold);
    }
}
