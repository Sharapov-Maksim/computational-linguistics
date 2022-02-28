import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Dictionary {
    ArrayList<Grammeme> grammemes = new ArrayList<>();
    ArrayList<Lemma> lemmata = new ArrayList<>();
    HashMap<String, PossibleLemmas> lemmatizationMapping = new HashMap<>();

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
                        // Grammeme internal fields
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
    }


}
