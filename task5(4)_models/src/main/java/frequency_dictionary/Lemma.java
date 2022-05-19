package frequency_dictionary;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Lemma lemma = (Lemma) o;
        return Objects.equals(initailForm, lemma.initailForm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
