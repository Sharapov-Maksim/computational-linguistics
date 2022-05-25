package search;

import frequency_dictionary.Dictionary;
import frequency_dictionary.Lemma;
import models.Model;

import javax.json.*;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static concordance.Util.*;

public class SearchSystem {
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

        List<String> texts = splitTexts(corpora);
        for (String text : texts) {
            Text textObj = new Text(text);
            textCollection.add(textObj);
            for (Descriptor descriptor : descriptors.values()) {
                if (textObj.containsDescriptor(descriptor))
                    descriptor.count++;
            }
        }
        for (Descriptor descriptor : descriptors.values()) {
            descriptor.IDF = Math.log(1. * textCollection.size() / descriptor.count);
        }
        var descvals = new ArrayList<>(descriptors.values());
        for (Text text : textCollection)
            text.calculateVector(descvals);
    }
}
