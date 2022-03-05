import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws XMLStreamException, IOException {
        Dictionary dictionary = new Dictionary("src/main/resources/dict.opcorpora.xml");
        System.out.println("Mapping size: " + dictionary.lemmatizationMapping.size());
        HashMap<Lemma, Integer> frequency = new HashMap<>();
        HashMap<String, Integer> errorTokenFreq = new HashMap<>();
        // read file and count frequences of lemmas
        File file = new File("../corps/dataset_news_science.txt");
        Scanner reader = new Scanner(file, StandardCharsets.UTF_8);
        int totalCount = 0;
        int definitelyCount = 0;
        int ambiguousCount = 0;
        BufferedWriter writerErr = new BufferedWriter(new FileWriter("src/main/resources/errors.txt"));
        while (reader.hasNextLine()) {
            String data = reader.nextLine();
            List<String> tokens = tokenize(data);
            for (String token : tokens) {
                totalCount++;
                Dictionary.PossibleLemmas possibleLemmas = dictionary.lemmatizationMapping.get(token);
                if (possibleLemmas != null) {
                    ArrayList<Lemma> lemmas = possibleLemmas.getNormalizedLemmas();
                    if (lemmas.size() == 1) { // Если не нужно разрешать омонимию
                        definitelyCount++;
                    }
                    else if (lemmas.size() > 1) {
                        ambiguousCount++;
                    }

                    for (Lemma curr_lemma : lemmas) {
                        // Подсчёт количества вхождений леммы
                        if (frequency.get(curr_lemma) != null) {
                            frequency.put(curr_lemma, frequency.get(curr_lemma) + 1);
                        } else {
                            frequency.put(curr_lemma, 1);
                        }
                    }
                }
                else {
                    errorTokenFreq.merge(token, 1, Integer::sum);
                    //writerErr.write("Coldn`t find lemma for token: " + token + '\n');
                }
            }
        }
        reader.close();

        System.out.println("Tokens count: " + totalCount);
        System.out.println("Found unique lemmas :" + frequency.size());
        System.out.println("Found lemmas for " + (definitelyCount + ambiguousCount) + " tokens over total "
                + totalCount + " tokens (" + (1. * definitelyCount + ambiguousCount)/totalCount + ")");
        System.out.println("Definite tokens: " + definitelyCount);
        System.out.println("Ambiguous tokens: " + ambiguousCount);
        System.out.println("Not lemmatized: " + (totalCount - definitelyCount - ambiguousCount));
        // sort by frequency
        var sortedFrequency = frequency.entrySet().stream().sorted((o1, o2) -> -1 * o1.getValue().compareTo(o2.getValue())).toList();
        var sortedFreqErr = errorTokenFreq.entrySet().stream().sorted((o1, o2) -> -1 * o1.getValue().compareTo(o2.getValue())).toList();
        // write result
        BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/resources/output.txt"));
        BufferedWriter writerN = new BufferedWriter(new FileWriter("src/main/resources/nouns.txt"));
        BufferedWriter writerP = new BufferedWriter(new FileWriter("src/main/resources/prepositions.txt"));
        BufferedWriter writerV = new BufferedWriter(new FileWriter("src/main/resources/verbs.txt"));
        BufferedWriter writerA = new BufferedWriter(new FileWriter("src/main/resources/adjectives.txt"));

        for (Map.Entry<Lemma, Integer> e : sortedFrequency) {
            //write to common stats
            writeFrequency(e, writer);
            switch (e.getKey().initailForm.grammemes.get(0)) {
                // nouns stats
                case "NOUN" -> writeFrequency(e, writerN);
                // prepositions stats
                case "PREP" -> writeFrequency(e, writerP);
                // verbs stats
                case "VERB" -> writeFrequency(e, writerV);
                // adjectives stats
                case "ADJF" -> writeFrequency(e, writerA);
            }
        }
        for (Map.Entry<String, Integer> e : sortedFreqErr) {
            writerErr.write("Error token: \"" + e.getKey() + "\" count: " + e.getValue() + '\n');
        }
        writerErr.close();
        writer.close();
        writerN.close();
        writerP.close();
        writerV.close();
        writerA.close();
        System.out.println("Completed!");
    }


    private static List<String> tokenize(String text) {
        return Arrays.stream(text.replace("\n", "").toLowerCase().split("[ \",.()?:!'-]")).
                filter((x) -> x.length() > 0).collect(Collectors.toList());
    }

    private static void writeFrequency (Map.Entry<Lemma, Integer> e, BufferedWriter writer) {
        try {
            writer.write('\"' + e.getKey().initailForm.word + '\"' + ", " +
                    e.getKey().initailForm.grammemes.get(0) + ", " +
                    e.getValue() + "\n");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
