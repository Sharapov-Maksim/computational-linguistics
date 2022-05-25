package models;

import concordance.Util;
import frequency_dictionary.Dictionary;
import frequency_dictionary.Lemma;

import java.io.*;
import java.util.*;

public class Model {
    static ArrayList<Model> models;
    static HashMap<String, Model> elementNameToModel = new HashMap<>();
    static HashMap<String, ArrayList<Lemma>> elementNameToSynonyms = new HashMap<>();
    static ArrayList<Lemma> synonyms = new ArrayList<>();
    static ArrayList<String> elementNames = new ArrayList<>();
    static HashMap<NGramm, String> ngrammToElementName = new HashMap<>();
    static ArrayList<NGramm> possiblengramms = new ArrayList<>();
    static HashSet<Integer> possibleN = new HashSet<>();
    public ArrayList<ModelElement> modelElements = new ArrayList<>();
    
    
    public static class ModelElement {
        public String name;
        public ArrayList<Lemma> synonyms;
        public NGramm nGramm;
        public ModelElement(String name, ArrayList<Lemma> synonyms, NGramm nGramms){
            this.name = name;
            this.synonyms = synonyms;
            this.nGramm = nGramms;
        }
    }

    public static class NGramm {
        public ArrayList<Lemma> lemmas;

        public NGramm(ArrayList<Lemma> lemmas) {
            this.lemmas = lemmas;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NGramm nGramm = (NGramm) o;
            return Objects.equals(lemmas, nGramm.lemmas);
        }

        @Override
        public int hashCode() {
            return Objects.hash(lemmas);
        }
    }

    public static ArrayList<Model> loadModels (String pathToModels, String pathToElements, Dictionary dict) {
        HashMap<String, ModelElement> elements = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(pathToElements))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.length() < 2)
                    continue;
                String[] parts = line.split(":");
                String name = parts[0];
                if (parts[1].contains("[")) {
                    List<String> ngramElemsStr = Util.tokenizeWithoutDots(parts[1]);
                    NGramm ngram = new NGramm(dict.lemmatizeTokens(ngramElemsStr));
                    ModelElement modelElement = new ModelElement(name, null, ngram);
                    elements.put(name, modelElement);
                    ngrammToElementName.put(ngram, name);
                    possibleN.add(ngram.lemmas.size());
                }
                else {
                    List<String> synonymsStr = Util.tokenizeWithoutDots(parts[1]);
                    var syns = dict.lemmatizeTokens(synonymsStr);
                    ModelElement modelElement = new ModelElement(name, syns, null);
                    elements.put(name, modelElement);
                    elementNameToSynonyms.put(name, syns);
                    for (var s : syns){
                        synonyms.add(s);
                        elementNames.add(name);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArrayList<Model> res = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(pathToModels))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.length() < 2)
                    continue;
                Model model = new Model();
                List<String> elems = Util.tokenizeBy(line, " ");
                for (String elementName : elems){
                    //ModelElement modelElement = new ModelElement(elementName, elements.get(elementName)., null);
                    model.modelElements.add(elements.get(elementName));
                    elementNameToModel.put(elementName, model);
                }
                res.add(model);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        models = res;
        return res;
    }

    public static HashMap<Model, ArrayList<Integer>> findModels(ArrayList<ArrayList<Lemma>> sentencesLemmas) {
        HashMap<Model, ArrayList<Integer>> modelsToSentenceIdx = new HashMap<>();
        for (Model m : models) {
            modelsToSentenceIdx.put(m, new ArrayList<>());
        }
        for (int sentIdx = 0; sentIdx < sentencesLemmas.size(); sentIdx++){
            ArrayList<Lemma> sentence = sentencesLemmas.get(sentIdx);
            HashMap<String, Boolean> elementsInSentence = analyzeSentence(sentence);

            for (Model m : models) {
                boolean isSentenceContainsModel = true;
                for (var elem : m.modelElements) {
                    if (!elementsInSentence.containsKey(elem.name)) {
                        isSentenceContainsModel = false;
                        break;
                    }
                }
                if (isSentenceContainsModel)
                    modelsToSentenceIdx.get(m).add(sentIdx);
            }
        }
        return modelsToSentenceIdx;
    }

    public static HashMap<String, Boolean> analyzeSentence(ArrayList<Lemma> sentence) {
        HashMap<String, Boolean> isElementsInSentence = new HashMap<>();
        for (Lemma l : sentence) {
            int pos = synonyms.indexOf(l);
            if (pos != -1) {
                isElementsInSentence.put(elementNames.get(pos), true);
            }
        }
        for (Integer n : possibleN) {
            for (int start = 0; start + n < sentence.size(); start++) {
                List<Lemma> subList = sentence.subList(start, start+n);
                ArrayList<Lemma> arrayList = new ArrayList<>(subList);
                NGramm nGramm = new NGramm(arrayList);
                if (ngrammToElementName.containsKey(nGramm)) {
                    isElementsInSentence.put(ngrammToElementName.get(nGramm), true);
                }
            }
        }
        return isElementsInSentence;
    }

    public static void printFoundModels (HashMap<Model, ArrayList<Integer>> modelsToSentenceIdxs, List<String> sentences, String path) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(path))) {
            var modelsSentenceIdxs = modelsToSentenceIdxs.entrySet().stream().sorted((e1, e2) ->
                    -1 * Double.compare(1.0*e1.getValue().size() / sentences.size(), 1.0*e2.getValue().size() / sentences.size())).toList();
            bufferedWriter.write("Total amount of sentences: " + sentences.size() + "\n");
            for (var modelSents : modelsSentenceIdxs) {
                Model model = modelSents.getKey();
                ArrayList<Integer> sentenceIdxs = modelSents.getValue();
                bufferedWriter.write(model.toString() + "  count: " + sentenceIdxs.size() + "   frequency: " + 1.0*sentenceIdxs.size()/sentences.size() + "\n");
                for (Integer idx : sentenceIdxs) {
                    bufferedWriter.write("  \"" + sentences.get(idx) + "\"\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Model: ");
        for (var elem : this.modelElements) {
            stringBuilder.append(elem.name).append(" ");
        }
        return stringBuilder.toString();
    }
}
