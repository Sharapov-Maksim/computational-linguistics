package search;

import frequency_dictionary.Lemma;
import models.Model;

import java.util.ArrayList;
import java.util.List;

public class Descriptor {
    public String name;
    ArrayList<Model.NGramm> synset = new ArrayList<>(); // Synonyms set

    ArrayList<Descriptor> general = new ArrayList<>();

    // TODO do we need special?
    ArrayList<Descriptor> special = new ArrayList<>();

    Double IDF;
    Integer count = 0;

    public Descriptor (String name, List<Lemma> synset) {
        this.name = name;
        for (Lemma l : synset) {
            this.synset.add(new Model.NGramm(new ArrayList<>(List.of(l))));
        }
    }

    public Descriptor (String name, ArrayList<Model.NGramm> synset) {
        this.name = name;
        this.synset.addAll(synset);
    }

    public Descriptor (String name) {
        this.name = name;
    }

    public void addSynset(ArrayList<Model.NGramm> synset) {
        this.synset.addAll(synset);
    }
}
