package frequency_dictionary;

import java.util.ArrayList;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WordForm wordForm = (WordForm) o;
        if (word != null && wordForm.word != null) {
            return word.equals(wordForm.word);
        }
        return grammemes.containsAll(wordForm.grammemes) || wordForm.grammemes.containsAll(grammemes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(word, grammemes);
    }
}
