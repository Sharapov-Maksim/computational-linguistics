package frequency_dictionary;

public class Lemma {
    public Integer id = null;
    public WordForm initailForm = null;

    public Lemma () {}

    public Lemma (int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "frequency_dictionary.Lemma{" + initailForm + '}';
    }
}
