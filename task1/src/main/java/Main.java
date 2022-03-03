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

        // read file and count frequences of lemmas
        File file = new File("../corps/dataset_news_science.txt");
        Scanner reader = new Scanner(file, StandardCharsets.UTF_8);
        int totalCount = 0;
        int parsedCount = 0;
        while (reader.hasNextLine()) {
            String data = reader.nextLine();
            List<String> tokens = splitBySpaces(data);
            for (String token : tokens) {
                totalCount++;
                Dictionary.PossibleLemmas possibleLemmas = dictionary.lemmatizationMapping.get(token);
                if (possibleLemmas != null) {
                    ArrayList<Lemma> lemmas = possibleLemmas.getNormalizedLemmas();
                    if (lemmas.size() == 1) { // Если не нужно разрешать омонимию
                        parsedCount++;
                        // Подсчёт количества вхождений леммы
                        Lemma curr_lemma = lemmas.get(0);
                        if (frequency.get(curr_lemma) != null) {
                            frequency.put(curr_lemma, frequency.get(curr_lemma) + 1);
                        }
                        else {
                            frequency.put(curr_lemma, 1);
                        }
                    }
                }
            }
        }
        reader.close();
        System.out.println("Found unique lemmas :" + frequency.size());
        System.out.println("Found lemmas for " + parsedCount + " tokens over total "
                + totalCount + " tokens (" + (1. * parsedCount)/totalCount + ")");
        // sort by frequency
        var sortedFrequency = frequency.entrySet().stream().sorted((o1, o2) -> -1 * o1.getValue().compareTo(o2.getValue())).toList();

        // write result
        BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/resources/output.txt"));
        var i = 1;
        for (Map.Entry<Lemma, Integer> e : sortedFrequency) {
            i++;
            writer.write('\"' + e.getKey().initailForm.word + '\"' + ", " +
                    e.getKey().initailForm.grammemes.get(0) + ", " +
                    e.getValue() + "\n");
        }
        writer.close();
        System.out.println("Completed!");
    }


    private static List<String> splitBySpaces(String text) {
        return Arrays.stream(text.replace("\n", "").toLowerCase().split("[ \",.()?:!']")).
                filter((x) -> x.length() > 0).collect(Collectors.toList());
    }
}
