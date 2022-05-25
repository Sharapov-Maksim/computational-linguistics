package search;

import concordance.Util;
import frequency_dictionary.Dictionary;
import frequency_dictionary.Lemma;
import models.Model;

import java.util.ArrayList;
import java.util.List;

public class Text {
    public static Dictionary dictionary = null;
    public String rawText;
    public ArrayList<Lemma> lemmas;
    public ArrayList<Double> vector = new ArrayList<>(); // TF-IDF vector

    public Text (String text) {
        this.rawText = text;
        List<String> tokens = Util.tokenize(text);
        if (dictionary == null)
            throw new IllegalStateException("Unset dictionary");
        this.lemmas = dictionary.lemmatizeTokens(tokens);
    }

    public boolean containsDescriptor(Descriptor descriptor){
        for (int i = 0; i < lemmas.size(); i++) {
            for (Model.NGramm nGramm : descriptor.synset) {
                boolean hasNgramm = true;
                if (i + nGramm.lemmas.size() >= lemmas.size())
                    break;
                for (int j = 0; j < nGramm.lemmas.size(); j++) {
                    if (!lemmas.get(i+j).equals(nGramm.lemmas.get(j))){
                        hasNgramm = false;
                        break;
                    }
                }
                if (hasNgramm)
                    return true;
            }
        }
        return false;
    }

    public void calculateVector(List<Descriptor> descriptors) {
        ArrayList<Integer> counts = new ArrayList<>();
        for (Descriptor descriptor : descriptors) {
            int count = 0;
            for (int i = 0; i < lemmas.size(); i++) {
                for (Model.NGramm nGramm : descriptor.synset) {
                    boolean hasNgramm = true;
                    if (i + nGramm.lemmas.size() >= lemmas.size())
                        break;
                    for (int j = 0; j < nGramm.lemmas.size(); j++) {
                        if (!lemmas.get(i+j).equals(nGramm.lemmas.get(j))){
                            hasNgramm = false;
                            break;
                        }
                    }
                    if (hasNgramm)
                        count++;
                }
            }
            counts.add(count);
        }
        vector.clear();
        for (Integer count : counts) {
            vector.add(1. * count / lemmas.size());
        }
    }
}
