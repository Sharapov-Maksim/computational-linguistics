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
    public ArrayList<Double> vectorTF = new ArrayList<>(); // TF vector
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

    public void calculateVectorTF(List<Descriptor> descriptors) {
        ArrayList<Integer> counts = new ArrayList<>();
        for (int i = 0; i < descriptors.size(); i++)
            counts.add(0);
        for (int did = 0; did < descriptors.size(); did++) {
            Descriptor descriptor = descriptors.get(did);
            int newcount = 0;
            for (int i = 0; i < lemmas.size(); i++) {
                for (Model.NGramm nGramm : descriptor.synset) {
                    boolean hasNgramm = true;
                    if (i + nGramm.lemmas.size() > lemmas.size())
                        continue;
                    for (int j = 0; j < nGramm.lemmas.size(); j++) {
                        if (!lemmas.get(i+j).equals(nGramm.lemmas.get(j))){
                            hasNgramm = false;
                            break;
                        }
                    }
                    if (hasNgramm) {
                        newcount++;
                        break;
                    }
                }
            }
            counts.set(did, counts.get(did) + newcount);
            for (Descriptor d : descriptor.getAllGeneral()) {
                int gid = descriptors.indexOf(d);
                counts.set(gid, counts.get(gid) + newcount);
            }
        }
        vectorTF.clear();
        for (Integer count : counts) {
            vectorTF.add(1. * count / lemmas.size());
        }
    }

    public void calculateVector(List<Descriptor> descriptors) {
        vector.clear();
        if (vectorTF.isEmpty())
            calculateVectorTF(descriptors);

        for (int i = 0; i < descriptors.size(); i++) {
            Descriptor descriptor = descriptors.get(i);
            Double TF = vectorTF.get(i);
            vector.add(TF * descriptor.IDF);
        }
    }

    public String vectorToString(List<Descriptor> descriptors) {
        StringBuilder stringBuilder = new StringBuilder("[");
        for (int i = 0; i < vector.size(); i++) {
            stringBuilder.append(descriptors.get(i).name).append(" ").append(vector.get(i)).append(", ");
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }
}
