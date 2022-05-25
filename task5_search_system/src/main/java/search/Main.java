package search;
import frequency_dictionary.Dictionary;
import frequency_dictionary.Lemma;
import models.Model;

import javax.json.*;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static concordance.Util.tokenizeBy;

public class Main {
    public static void main(String[] args) throws IOException, XMLStreamException {
        String corpora = Files.readString(Paths.get("../corps/dataset_news_science.txt"));
        Dictionary dictionary = new Dictionary("../dictionary/dict.opcorpora.xml");
        Text.dictionary = dictionary;
        SearchSystem searchSystem = new SearchSystem("src/main/java/search/descriptors.json", corpora, dictionary);
        System.out.println(searchSystem);
    }
}
