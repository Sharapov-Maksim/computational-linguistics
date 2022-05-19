package frequency_dictionary;

import concordance.Util;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Dictionary {
    public ArrayList<Grammeme> grammemes = new ArrayList<>();
    public ArrayList<Lemma> lemmata = new ArrayList<>();
    public HashMap<String, PossibleLemmas> lemmatizationMapping = new HashMap<>();

    public static class PossibleLemmas {
        ArrayList<WordForm> wordForms = new ArrayList<>(); // possible word forms match that token
        ArrayList<Lemma> possibleLemmas = new ArrayList<>(); // corresponding lemmas for that word forms

        public PossibleLemmas() {}

        @Override
        public String toString() {
            return "{" +
                    "wordForms=" + wordForms +
                    ", possibleLemmas=" + getNormalizedLemmas() +
                    '}';
        }

        public ArrayList<Lemma> getNormalizedLemmas() {
            ArrayList<Lemma> res = new ArrayList<>();
            for (Lemma l : possibleLemmas) {
                if (!res.contains(l)) {
                    res.add(l);
                }
            }
            return res;
        }
    }

    /**
     * Construct dictionary from XML file, that can be downloaded from http://opencorpora.org/dict.php
     * @param pathToXML path to XML file containing dictionary
     */
    public Dictionary (String pathToXML) throws IOException, XMLStreamException {
        XMLInputFactory streamFactory = XMLInputFactory.newInstance();
        XMLStreamReader reader = streamFactory.createXMLStreamReader(new FileInputStream(pathToXML), "utf-8");

        // Objects to fill from xml
        Grammeme current_grammeme = new Grammeme();
        Lemma current_lemma = new Lemma();
        WordForm current_form = new WordForm();

        for (; reader.hasNext(); reader.next()) {
            int eventType = reader.getEventType();
            switch (eventType) {
                case XMLStreamConstants.START_ELEMENT -> {
                    switch (reader.getLocalName()) {
                        case "dictionary" -> {
                            System.out.println("Parsing started");
                        }
                        case "grammemes", "restrictions", "lemmata", "restr" -> {}

                        // <grammeme>: <name/> <alias/> <description/> </grammeme>
                        case "grammeme" -> {
                            current_grammeme = new Grammeme();
                        }
                        // frequency_dictionary.Grammeme internal fields
                        // <name>POST</name>
                        case "name" -> {
                            reader.next();
                            if (reader.getEventType() == XMLStreamConstants.CHARACTERS)
                                current_grammeme.name = reader.getText().trim();
                            else throw new IllegalStateException("Tag with no text inside");
                        }
                        // <alias>ЧР</alias>
                        case "alias" -> {
                            reader.next();
                            if (reader.getEventType() == XMLStreamConstants.CHARACTERS)
                                current_grammeme.alias = reader.getText().trim();
                            else throw new IllegalStateException("Tag with no text inside");
                        }
                        // <description>часть речи</description>
                        case "description" -> {
                            reader.next();
                            if (reader.getEventType() == XMLStreamConstants.CHARACTERS)
                                current_grammeme.description = reader.getText().trim();
                            else throw new IllegalStateException("Tag with no text inside");
                        }

                        // <lemma id rev>:  <l><g/><g/>...<g/></l>  <f><g/>...<g/></f>  </lemma>
                        case "lemma" -> {
                            current_lemma = new Lemma();
                            current_lemma.id = Integer.valueOf(reader.getAttributeValue(0));
                        }
                        // lemma internal structure
                        // <l t="ёж"><g v="NOUN"/><g v="anim"/><g v="masc"/></l>
                        case "l" -> { // lemma normal form
                            current_form = new WordForm();
                            if (reader.getAttributeCount() == 1 && reader.getAttributeLocalName(0).equals("t")) {
                                current_lemma.initailForm = current_form;
                                current_form.word = reader.getAttributeValue(0);
                            }
                            else throw new IllegalStateException("Error handling <l> tag");
                        }
                        // <f t="ежу"><g v="sing"/><g v="datv"/></f>
                        case "f" -> {
                            current_form = new WordForm();
                            if (reader.getAttributeCount() == 1 && reader.getAttributeLocalName(0).equals("t"))
                                current_form.word = reader.getAttributeValue(0);
                            else throw new IllegalStateException("Error handling <f> tag");
                        }
                        case "g" -> {
                            if (reader.getAttributeCount() == 1 && reader.getAttributeLocalName(0).equals("v"))
                                current_form.grammemes.add(reader.getAttributeValue(0));
                            else throw new IllegalStateException("Error handling <g> tag");
                        }

                    }
                }
                case XMLStreamConstants.END_ELEMENT -> {
                    switch (reader.getLocalName()) {
                        case "grammeme" -> {
                            this.grammemes.add(current_grammeme);
                        }
                        case "lemma" -> {
                            this.lemmata.add(current_lemma);
                        }
                        case "l", "f" -> {
                            if (current_form.word == null)
                                throw new NullPointerException("Null in word field after finishing parse wordform");
                            // Add/update word form mapping to lemmas
                            PossibleLemmas pl = lemmatizationMapping.get(current_form.word);
                            if (pl != null) {
                                pl.wordForms.add(current_form);
                                pl.possibleLemmas.add(current_lemma);
                            }
                            else {
                                PossibleLemmas pos = new PossibleLemmas();
                                pos.wordForms.add(current_form);
                                pos.possibleLemmas.add(current_lemma);
                                lemmatizationMapping.put(current_form.word, pos);
                            }
                        }
                        case "g", "left", "right", "alias", "description", "name",
                                "grammemes", "restrictions", "lemmata" -> {}
                        case "dictionary" -> {
                            System.out.println("Parsing complete");
                            System.out.println("Grammemes: " + grammemes.size());
                            System.out.println("Lemmas: " + lemmata.size());
                        }
                    }
                }
            }
        }

        // Adding "." and "," to dictionary
        WordForm dotw = new WordForm(".");
        WordForm commaw = new WordForm(",");
        Lemma dot = new Lemma(666666, dotw);
        Lemma comma = new Lemma(666667, commaw);
        lemmata.add(dot);
        lemmata.add(comma);
        PossibleLemmas pdot = new PossibleLemmas();
        pdot.possibleLemmas.add(dot);
        pdot.wordForms.add(dotw);
        lemmatizationMapping.put(".", pdot);
        PossibleLemmas pcomma = new PossibleLemmas();
        pcomma.possibleLemmas.add(comma);
        pcomma.wordForms.add(commaw);
        lemmatizationMapping.put(",", pcomma);
    }

    public ArrayList<Lemma> getPossibleLemmas (String word) {
        var res = lemmatizationMapping.get(word);
        if (res == null) {
            // add new word to dictionary
            WordForm wf = new WordForm(word);
            Lemma lemma = new Lemma(null, wf);
            lemmata.add(lemma);
            PossibleLemmas pl = new PossibleLemmas();
            pl.possibleLemmas.add(lemma);
            pl.wordForms.add(wf);
            lemmatizationMapping.put(word, pl);
            return pl.possibleLemmas;
        }
        return res.possibleLemmas;
    }

    public Lemma lemmatizeWord (String word) {
        if (getPossibleLemmas(word).size() == 0) {
            System.out.println(word);
        }
        return getPossibleLemmas(word).get(0);
    }

    public ArrayList<Lemma> lemmatizeTokens (List<String> tokens) {
        ArrayList<Lemma> res = new ArrayList<>();
        for (String t : tokens) {
            if (t.startsWith("<")){
                WordForm wordForm = new WordForm(null);
                wordForm.grammemes = new ArrayList<>(Util.tokenizeBy(t.substring(1,t.length()-1),";"));
                Lemma l = new Lemma(-1, wordForm);
                res.add(l);
            }
            else
                res.add(lemmatizeWord(t));
        }
        return res;
    }

    public ArrayList<ArrayList<Lemma>> lemmatizeTextTokens (List<List<String>> textTokens) {
        ArrayList<ArrayList<Lemma>> res = new ArrayList<>();
        for (List<String> text : textTokens) {
            ArrayList<Lemma> lemmas = new ArrayList<>();
            for (String w : text) {
                lemmas.add(lemmatizeWord(w));
            }
            res.add(lemmas);
        }
        return res;
    }
}
