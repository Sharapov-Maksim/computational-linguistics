package ngramms;

import concordance.Concordance;
import concordance.Util;
import frequency_dictionary.Dictionary;
import frequency_dictionary.Lemma;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class NGramms {
    private final Dictionary dictionary;
    //private final String corpora;
    private int n;
    private double threshold;
    private List<Lemma> normalizedCorporaLemmas;
    private final HashMap<List<Lemma>, Statistics> ngrammsStats = new HashMap<>();
    private List<Map.Entry<List<Lemma>, Statistics>> filteredByStability;

    public static class Statistics {
        public Integer count = null;
        public ArrayList<Integer> indexes = new ArrayList<>();
        public Double stability = null;
        public Integer maxExtensionFrequency = null;
        public Statistics() {};

        public Statistics(int count, int firstIdx) {
            this.count = count;
            this.indexes.add(firstIdx);
        }
    }

    public NGramms(String corpora, int n, double threshold, Dictionary dict){
        //this.corpora = corpora;
        this.dictionary = dict;
        this.n = n;
        this.threshold = threshold;

        // Tokenize and normalize
        // TODO возможно придётся переписать на набор текстов... надеюсь нет...
        List<String> tokens = Util.tokenize(corpora);
        normalizedCorporaLemmas = dict.lemmatizeTokens(tokens);

        // Store all n-gramms
        for (int i = 0; i < normalizedCorporaLemmas.size()-n+1; i++){
            List<Lemma> ngramm = normalizedCorporaLemmas.subList(i, i+n);
            Statistics stats = ngrammsStats.get(ngramm);
            if (stats == null) {
                ngrammsStats.put(ngramm, new Statistics(1, i));
            }
            else {
                stats.count ++;
                stats.indexes.add(i);
            }
        }

        filterStability();
    }

    private void filterStability() {
        System.out.println("Filtering by stability with threshold: " + threshold + "...");
        var entries = ngrammsStats.entrySet().stream().
                sorted((e1, e2) -> -1 * e1.getValue().count.compareTo(e2.getValue().count)).toList();

        this.filteredByStability = new ArrayList<>();
        for (Map.Entry<List<Lemma>, Statistics> entry : entries){
            if (entry.getValue().count < 2)
                continue;
            Concordance concordance = new Concordance(entry.getKey(), this.normalizedCorporaLemmas, 1, dictionary, entry.getValue().indexes);
            var axn = concordance.getLeftContexts().entrySet().stream().filter(e -> e.getKey().lemmas.size() >= n+1).
                    max((e1, e2) -> e1.getValue().count.compareTo(e2.getValue().count));
            var xnb = concordance.getRightContexts().entrySet().stream().filter(e -> e.getKey().lemmas.size() >= n+1).
                    max(Comparator.comparing(e -> e.getValue().count));
            if (axn.isPresent() && xnb.isPresent()){
                Integer fax = axn.get().getValue().count;
                Integer fxb = xnb.get().getValue().count;
                entry.getValue().stability = Double.max((1. * fax / entry.getValue().count), (1. * fxb / entry.getValue().count));
                entry.getValue().maxExtensionFrequency = Integer.max(fax, fxb);
                if (entry.getValue().stability <= threshold){
                    filteredByStability.add(entry);
                }
            }
            else
                System.out.println("Couldn`t find max freq");
        }
    }


    public void printOverallStatistics(String path) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));
        var entries = ngrammsStats.entrySet().stream().sorted((e1, e2) -> -1 * e1.getValue().count.compareTo(e2.getValue().count)).toList();
        for (Map.Entry<List<Lemma>, Statistics> e : entries) {
            for (Lemma l : e.getKey())
                writer.write(l.initailForm.word + " ");
            writer.write(": " + e.getValue().count + "\n");
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
            writer.write("]   count:" + e.getValue().count + "  max extensions:" + e.getValue().maxExtensionFrequency + "  stability:" + e.getValue().stability + "\n");
        }
        writer.close();
    }
}
