package concordance;

import frequency_dictionary.Dictionary;
import frequency_dictionary.Lemma;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Concordance {
    int n;
    String phrase;
    List<String> phraseTokens;
    List<Lemma> normalizedPhraseLemmas;
    List<String> corporaTokens;
    List<Lemma> normalizedCorporaLemmas;
    ArrayList<Integer> matchIndexes = null;
    HashMap<Context, Statistics> leftContexts = new HashMap<>();
    HashMap<Context, Statistics> rightContexts = new HashMap<>();

    public static class Statistics {
        public Integer count = null;
        public ArrayList<Integer> indexes = new ArrayList<>();

        public Statistics() {};

        public Statistics(int count, int firstIdx) {
            this.count = count;
            this.indexes.add(firstIdx);
        }
    }

    public class Context {
        public final ArrayList<Lemma> lemmas;

        public Context(ArrayList<Lemma> lemmas) {
            this.lemmas = lemmas;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Context context = (Context) o;
            if (this.lemmas.size() != context.lemmas.size()) return false;
            for (int i = 0; i < this.lemmas.size(); i++){
                if (!this.lemmas.get(i).equals(context.lemmas.get(i)))
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return Objects.hash(lemmas);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (Lemma l : lemmas) {
                sb.append(l.initailForm.word).append(" ");
            }
            sb.append("]");
            return sb.toString();
        }
    }

    public Concordance(String phrase, String corpora, int n, Dictionary dict){
        this.n = n;
        this.phrase = phrase;
        this.phraseTokens = Util.tokenize(phrase);
        this.corporaTokens = Util.tokenize(corpora);
        this.normalizedPhraseLemmas = dict.lemmatizeTokens(this.phraseTokens);
        this.normalizedCorporaLemmas = dict.lemmatizeTokens(this.corporaTokens);

        computeConcordances(n);
    }

    public Concordance(List<Lemma> phrase, List<Lemma> corpora, int n, Dictionary dict, ArrayList<Integer> matchIndexes){
        this.n = n;
        this.normalizedPhraseLemmas = phrase;
        this.normalizedCorporaLemmas = corpora;
        this.matchIndexes = matchIndexes;
        computeConcordances(n);
    }

    private void computeConcordances(int n) {
        // find phrase matches
        if (matchIndexes == null) {
            this.matchIndexes = new ArrayList<>();
            for (int i = 0; i < normalizedCorporaLemmas.size() - normalizedPhraseLemmas.size(); i++) {
                boolean match = true;
                for (int j = 0; j < normalizedPhraseLemmas.size(); j++) {
                    Lemma pl = normalizedPhraseLemmas.get(j);
                    Lemma cl = normalizedCorporaLemmas.get(i + j);
                    if (!pl.equals(cl)) {
                        match = false;
                        break;
                    }
                }
                if (match)
                    matchIndexes.add(i);
            }
        }
        // finding left contexts
        for (Integer i : matchIndexes){
            int startIdx = Math.max(i - n, 0);
            int endIdx = i + normalizedPhraseLemmas.size();
            for (; startIdx < i; startIdx++){
                ArrayList<Lemma> contextLemmas = new ArrayList<>();
                for (int j = startIdx; j<endIdx; j++) {
                    contextLemmas.add(normalizedCorporaLemmas.get(j));
                }
                Context c = new Context(contextLemmas);
                if (leftContexts.get(c) == null) {
                    leftContexts.put(c, new Statistics(1, i));
                }
                else {
                    leftContexts.get(c).count ++;
                }
            }
        }

        // right contexts
        for (Integer i : matchIndexes){
            int startIdx = i;
            int endIdx = Math.min(i + normalizedPhraseLemmas.size() + n, normalizedCorporaLemmas.size());
            for (int endIdx2 = i + normalizedPhraseLemmas.size(); endIdx2 <= endIdx; endIdx2++){
                ArrayList<Lemma> contextLemmas = new ArrayList<>();
                for (int j = startIdx; j<endIdx2; j++) {
                    contextLemmas.add(normalizedCorporaLemmas.get(j));
                }
                Context c = new Context(contextLemmas);
                if (rightContexts.get(c) == null) {
                    rightContexts.put(c, new Statistics(1, i));
                }
                else {
                    rightContexts.get(c).count ++;
                }
            }
        }

    }

    public ArrayList<Integer> getMatchIndexes() {
        return matchIndexes;
    }

    public void printStatisticsLeft(String path) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));
        var entries = leftContexts.entrySet().stream().sorted((e1, e2) -> -1 * e1.getValue().count.compareTo(e2.getValue().count)).toList();
        for (Map.Entry<Context,Statistics> e : entries) {
            writer.write(e.getKey().lemmas + ": " + e.getValue().count + "\n");
        }
        writer.close();
    }

    public void printStatisticsRight(String path) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));
        var entries = rightContexts.entrySet().stream().sorted((e1, e2) -> -1 * e1.getValue().count.compareTo(e2.getValue().count)).toList();
        for (Map.Entry<Context,Statistics> e : entries) {
            writer.write(e.getKey() + ": " + e.getValue().count + "\n");
        }
        writer.close();
    }

    public void printStatistics(String path, Integer threshold) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));
        final Integer t;
        if (threshold != null && threshold >= 0)
            t = threshold;
        else
            t = 0;
        var entries = leftContexts.entrySet().stream().filter(e -> e.getValue().count >= t).
                sorted((e1, e2) -> -1 * e1.getValue().count.compareTo(e2.getValue().count)).toList();

        for (Map.Entry<Context,Statistics> e : entries) {
            writer.write("l: " + e.getKey() + ": " + e.getValue().count + "\n");
        }
        entries = rightContexts.entrySet().stream().filter(e -> e.getValue().count >= t).
                sorted((e1, e2) -> -1 * e1.getValue().count.compareTo(e2.getValue().count)).toList();
        for (Map.Entry<Context,Statistics> e : entries) {
            writer.write("r: " + e.getKey() + ": " + e.getValue().count + "\n");
        }
        writer.close();
    }

    public HashMap<Context, Statistics> getLeftContexts() {
        return leftContexts;
    }

    public HashMap<Context, Statistics> getRightContexts() {
        return rightContexts;
    }
}
