package search;

import frequency_dictionary.Dictionary;
import frequency_dictionary.Lemma;
import models.Model;

import javax.json.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static concordance.Util.*;

public class SearchSystem {
    private static final int MAX_RESULTS = 50;
    private static final Double TRESHOLD = 0.8;
    public ArrayList<Text> textCollection = new ArrayList<>();

    public HashMap<String, Descriptor> descriptors = new HashMap<>();

    public SearchSystem (String descriptorsJSONPath, String corpora, Dictionary dictionary) throws IOException {
        // read descriptors
        JsonReader jsonReader = Json.createReader(new StringReader(Files.readString(Paths.get(descriptorsJSONPath))));
        JsonArray jsonArray = jsonReader.readArray();
        for (JsonValue elem : jsonArray) {
            JsonObject jsonObject = elem.asJsonObject();
            String name = jsonObject.getString("name");

            ArrayList<Model.NGramm> synonyms = new ArrayList<>();
            JsonArray synsetJSON = jsonObject.getJsonArray("synset");
            for (int sid = 0; sid < synsetJSON.size(); sid++) {
                String syn = synsetJSON.getString(sid);
                List<String> gramms = tokenize(syn);
                ArrayList<Lemma> lemmas = dictionary.lemmatizeTokens(gramms);
                synonyms.add(new Model.NGramm(lemmas));
            }

            Descriptor descriptor = descriptors.get(name);
            if (descriptor == null) {
                descriptor = new Descriptor(name, synonyms);
                descriptors.put(name, descriptor);
            }
            else
                descriptor.addSynset(synonyms);

            JsonArray generalJSON = jsonObject.getJsonArray("general");
            if (generalJSON != null)
                for (int gid = 0; gid < generalJSON.size(); gid++) {
                    String generalName = generalJSON.getString(gid);
                    Descriptor generalDescriptor = descriptors.get(generalName);
                    if (generalDescriptor == null) {
                        generalDescriptor = new Descriptor(generalName);
                        descriptors.put(generalName, generalDescriptor);
                    }
                    descriptor.general.add(generalDescriptor);
                    generalDescriptor.special.add(descriptor);
                }

            JsonArray specialJSON = jsonObject.getJsonArray("special");
            if (specialJSON != null)
                for (int sid = 0; sid < specialJSON.size(); sid++) {
                    String specialName = specialJSON.getString(sid);
                    Descriptor specialDescriptor = descriptors.get(specialName);
                    if (specialDescriptor == null) {
                        specialDescriptor = new Descriptor(specialName);
                        descriptors.put(specialName, specialDescriptor);
                    }
                    descriptor.special.add(specialDescriptor);
                    specialDescriptor.general.add(descriptor);
                }
        }

        var descvals = new ArrayList<>(descriptors.values());

        List<String> texts = splitTexts(corpora);
        for (String text : texts) {
            Text textObj = new Text(text);
            textCollection.add(textObj);
        }
        for (Text text : textCollection) {
            text.calculateVectorTF(descvals);
            for (int i = 0; i < descvals.size(); i++) {
                Descriptor descriptor = descvals.get(i);
                if (text.vectorTF.get(i) > 0)
                    descriptor.count++;
            }
        }
        for (Descriptor descriptor : descriptors.values()) {
            if (descriptor.count != 0)
                descriptor.IDF = Math.log(1. * textCollection.size() / descriptor.count);
            else
                descriptor.IDF = Math.log(1. * textCollection.size());
        }

        for (Text text : textCollection)
            text.calculateVector(descvals);

    }

    public void search(String request, String pathToWrite) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(pathToWrite))) {
            bufferedWriter.write("Request: \"" + request + "\"\n");
            var descvals = new ArrayList<>(descriptors.values());
            Text requestObj = new Text(request);
            requestObj.calculateVectorTF(descvals);
            requestObj.calculateVector(descvals);
            bufferedWriter.write("Request vector: " + requestObj.vectorToString(descvals) + "\n");
            ArrayList<Relevance> coss = new ArrayList<>(textCollection.size());
            for (int i = 0; i < textCollection.size(); i++) {
                Text text = textCollection.get(i);
                coss.add(new Relevance(i, requestObj.vector, text.vector));
            }
            var results = coss.stream().sorted((x1, x2) -> x2.cosValue.compareTo(x1.cosValue)).toList();
            for (int i = 0; i < Math.min(MAX_RESULTS, results.size()); i++) {
                if (results.get(i).cosValue < TRESHOLD)
                    break;
                bufferedWriter.newLine();
                int textIdx = results.get(i).textIdx;
                bufferedWriter.write("Result #" + i + "  text #" + textIdx + "  relevance = "+results.get(i).cosValue + "\n");
                bufferedWriter.write("Result vector: " + textCollection.get(textIdx).vectorToString(descvals) + "\n");
                bufferedWriter.write("    " + textCollection.get(textIdx).rawText + "\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public class Relevance {
        public Double cosValue;
        public Integer textIdx;

        public Relevance(int textIdx, ArrayList<Double> requestV, ArrayList<Double> textInCollectionV) {
            if (requestV.isEmpty() || textInCollectionV.isEmpty())
                throw new IllegalArgumentException("Empty vectors");
            if (requestV.size() != textInCollectionV.size())
                throw new IllegalArgumentException("Vectors of different sizes");
            double numerator = 0.;
            double divisor1 = 0.;
            double divisor2 = 0.;
            for (int i = 0; i < requestV.size(); i++) {
                Double ri = requestV.get(i);
                divisor1 += ri*ri;
                Double ti = textInCollectionV.get(i);
                divisor2 += ti*ti;
                numerator += ri * ti;
            }
            divisor1 = Math.sqrt(divisor1);
            divisor2 = Math.sqrt(divisor2);
            this.textIdx = textIdx;
            if (Math.abs(divisor1 * divisor2) < 1E-10)
                cosValue = 0.;
            else
                this.cosValue = numerator / (divisor1 * divisor2);
        }
    }
}
