package models;

import concordance.Util;
import frequency_dictionary.Dictionary;
import frequency_dictionary.Lemma;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Model {
    static ArrayList<Model> models;
    static HashMap<String, Model> elementNameToModel = new HashMap<>();
    static HashMap<String, ArrayList<Lemma>> elementNameToSynonyms = new HashMap<>();
    static HashMap<Lemma, String> synonymToElementName = new HashMap<>();
    public ArrayList<ModelElement> modelElements = new ArrayList<>();
    
    
    public static class ModelElement {
        public String name;
        public ArrayList<Lemma> synonyms;

        public ModelElement(String name, ArrayList<Lemma> synonyms){
            this.name = name;
            this.synonyms = synonyms;
        }
    }

    public static ArrayList<Model> loadModels (String pathToModels, String pathToElements, Dictionary dict) {
        HashMap<String, ArrayList<Lemma>> elements = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(pathToElements))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.length() < 2)
                    continue;
                String[] parts = line.split(":");
                String name = parts[0];
                List<String> synonymsStr = Util.tokenize(parts[1]);
                var syns = dict.lemmatizeTokens(synonymsStr);
                elements.put(name, syns);
                elementNameToSynonyms.put(name, syns);
                for (var s : syns)
                    synonymToElementName.put(s, name);
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
                List<String> elems = Util.tokenize(line);
                for (String elementName : elems){
                    ModelElement modelElement = new ModelElement(elementName, elements.get(elementName));
                    model.modelElements.add(modelElement);
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
            if (synonymToElementName.containsKey(l)) {
                isElementsInSentence.put(synonymToElementName.get(l), true);
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
