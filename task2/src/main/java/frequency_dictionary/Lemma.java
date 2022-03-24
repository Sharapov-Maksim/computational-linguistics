package frequency_dictionary;

public class Lemma {
    public Integer id = null;
    public WordForm initailForm = null;

    public Lemma () {}

    public Lemma (int id) {
        this.id = id;
    }

    public Lemma (Integer id, WordForm wf) {
        this.id = id;
        this.initailForm = wf;
    }

    @Override
    public String toString() {
        return "Lemma{" + initailForm + '}';
    }
}
