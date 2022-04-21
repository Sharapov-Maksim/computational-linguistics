package ngramms;

import concordance.Concordance;
import concordance.Util;
import frequency_dictionary.Dictionary;
import frequency_dictionary.Lemma;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

// TODO TF-IDF

public class NGramms {
    private final Dictionary dictionary;
    private final int MAX_N_LIMIT = 66;
    private final int MIN_N_LIMIT = 2;
    private double threshold;
    private final ArrayList<ArrayList<Lemma>> normalizedTextsLemmas;
    private HashMap<List<Lemma>, Statistics> ngrammsStats;
    private List<Map.Entry<List<Lemma>, Statistics>> filteredByStability;

    public static class Statistics {
        public Integer count = null;
        public ArrayList<Integer> indexesInTexts = new ArrayList<>();
        public ArrayList<Integer> indexesOfTexts = new ArrayList<>();
        public Double stability = null;
        public Integer maxExtensionFrequency = null;
        public Statistics() {};

        public Statistics(int count, int firstIdx, int firstTextIdx) {
            this.count = count;
            this.indexesInTexts.add(firstIdx);
            this.indexesOfTexts.add(firstTextIdx);
        }
    }

    public NGramms(String corpora, double threshold, Dictionary dict) throws IOException {
        this.dictionary = dict;
        this.threshold = threshold;

        // Tokenize and normalize
        // TODO возможно придётся переписать на набор текстов... надеюсь нет... зря надеялся(((((
        List<List<String>> textTokens = Util.splitTextsTokens(corpora);
        this.normalizedTextsLemmas = dict.lemmatizeTextTokens(textTokens);

        // Store all n-gramms
        int maxn = MIN_N_LIMIT;
        for (int n = MIN_N_LIMIT; ; n++) {
            ngrammsStats = new HashMap<>();
            boolean founded = false;
            for (int textIdx = 0; textIdx < normalizedTextsLemmas.size(); textIdx++) {
                // Count all n-gramms in text
                ArrayList<Lemma> textLemmas = normalizedTextsLemmas.get(textIdx);
                for (int i = 0; i < textLemmas.size()-n+1; i++){
                    List<Lemma> ngramm = textLemmas.subList(i, i+n);
                    Statistics stats = ngrammsStats.get(ngramm);
                    if (stats == null) {
                        ngrammsStats.put(ngramm, new Statistics(1, i, textIdx));
                    }
                    else {
                        stats.count ++;
                        stats.indexesInTexts.add(i);
                        stats.indexesOfTexts.add(textIdx);
                        founded = true;
                    }
                }
            }
            if (!founded) break;
            System.out.println("n = " + n);

            filterStability(n);
            printOverallStatistics("src/main/resources/" + n + "-gramms.txt");
            if (filteredByStability != null)
                printFilteredStatistics("src/main/resources/" + n + "-gramms_filtered.txt");

            filteredByStability = null;
            ngrammsStats = null;
            maxn = n;
        }
        System.out.println("Ngramms founded for n = 2.." + maxn);
    }

    private void filterStability(int n) {
        //System.out.println("Filtering by stability with threshold: " + threshold + "...");
        var entries = ngrammsStats.entrySet().stream().filter(e -> e.getValue().count>1).
                sorted((e1, e2) -> -1 * e1.getValue().count.compareTo(e2.getValue().count)).toList();

        this.filteredByStability = new ArrayList<>();
        for (Map.Entry<List<Lemma>, Statistics> entry : entries){
            if (entry.getValue().count < 2)
                continue;
            Concordance concordance = new Concordance(entry.getKey(), this.normalizedTextsLemmas, 1, dictionary, entry.getValue().indexesInTexts, entry.getValue().indexesOfTexts);
            var axn = concordance.getLeftContexts().entrySet().stream().filter(e -> e.getKey().lemmas.size() >= n+1).
                    max((e1, e2) -> e1.getValue().count.compareTo(e2.getValue().count));
            var xnb = concordance.getRightContexts().entrySet().stream().filter(e -> e.getKey().lemmas.size() >= n+1).
                    max(Comparator.comparing(e -> e.getValue().count));
            Integer fax;
            Integer fxb;
            if (axn.isPresent())
                fax = axn.get().getValue().count;
            else fax = 0;
            if (xnb.isPresent())
                fxb = xnb.get().getValue().count;
            else fxb = 0;

            entry.getValue().stability = Double.max((1. * fax / entry.getValue().count), (1. * fxb / entry.getValue().count));
            entry.getValue().maxExtensionFrequency = Integer.max(fax, fxb);
            if (entry.getValue().stability <= threshold){
                filteredByStability.add(entry);
            }
        }
    }


    public void printOverallStatistics(String path) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));
        var entries = ngrammsStats.entrySet().stream().sorted((e1, e2) -> -1 * e1.getValue().count.compareTo(e2.getValue().count)).toList();
        for (Map.Entry<List<Lemma>, Statistics> e : entries) {
            if (e.getValue().count < 2)
                continue;
            for (Lemma l : e.getKey())
                writer.write(l.initailForm.word + " ");
            writer.write(":  count:" + e.getValue().count + "  textCnt:" + e.getValue().indexesOfTexts.stream().distinct().count() + "\n");
        }
        writer.close();
    }

    public void printFilteredStatistics(String path) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));
        writer.write("Filtered by stability with threshold: " + threshold + "\n");
        var entries = filteredByStability.stream().sorted((e1, e2) -> -1 * e1.getValue().count.compareTo(e2.getValue().count)).toList();
        for (Map.Entry<List<Lemma>, Statistics> e : entries) {
            writer.write("[");
            for (Lemma l : e.getKey())
                writer.write(l.initailForm.word + " ");
            long textsCount = e.getValue().indexesOfTexts.stream().distinct().count();
            writer.write("]   count:" + e.getValue().count + "  textsCount:" + textsCount + "  stability:" + e.getValue().stability + "\n");
        }
        writer.close();
    }
}
