package frequency_dictionary;

import java.util.ArrayList;

public class WordForm {
    public String word = null;
    //TODO replacing String with frequency_dictionary.Grammeme is possible
    public ArrayList<String> grammemes = new ArrayList<String>();

    public WordForm(){}

    public WordForm(String word){
        this.word = word;
    }

    @Override
    public String toString() {
        return "{'" + word + '\'' +
                ": grammemes:" + grammemes + '}';
    }
}
