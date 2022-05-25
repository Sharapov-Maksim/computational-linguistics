package concordance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

public class Util {
    public static List<String> tokenize(String text) {
        List<String> basedTokens = Arrays.stream(text.replace("\n", "").toLowerCase().split("[ \\[\\]\"()?:!'-]")).
                filter((x) -> x.length() > 0).toList();
        // "." and "," are tokens
        List<String> tokensWithDotsCommas = new ArrayList<>();
        for (String baseToken : basedTokens){
            StringTokenizer st = new StringTokenizer(baseToken, "[.,]", true);
            while (st.hasMoreTokens()){
                tokensWithDotsCommas.add(st.nextToken());
            }
        }
        return tokensWithDotsCommas;
    }

    public static List<String> tokenizeWithoutDots(String text) {
        return Arrays.stream(text.replace("\n", "").split("[ \\[\\]\"()?:!'-.,]")).
                filter((x) -> x.length() > 0).toList();
    }

    public static List<String> tokenizeBy(String text, String regex) {
        return Arrays.stream(text.replace("\n", "").split(regex)).
                filter((x) -> x.length() > 0).toList();
    }

    public static List<String> splitTexts(String corpora) {
        List<String> basedTexts = Arrays.stream(corpora.toLowerCase().split("[\n]")).
                filter((x) -> x.length() > 0).toList();

        assert (basedTexts.size() % 2 == 0);
        ArrayList<String> texts = new ArrayList<>();
        for (int i = 0; i < basedTexts.size()-1; i+=2){
            texts.add(basedTexts.get(i) + '\n' + basedTexts.get(i+1));
        }
        return texts;
    }

    public static List<String> splitOnSentences(String corpora) {
        return Arrays.stream(corpora.split("[\n.!?]")).
                filter((x) -> x.length() > 0).toList();
    }

    public static List<List<String>> splitSentencesTokens(List<String> sentences) {
        List<List<String>> textsTokens = new ArrayList<>();
        for (String t : sentences) {
            textsTokens.add(tokenize(t));
        }
        return textsTokens;
    }


    public static List<List<String>> splitTextsTokens(String corpora) {
        List<String> texts = splitTexts(corpora);
        List<List<String>> textsTokens = new ArrayList<>();
        for (String t : texts) {
            textsTokens.add(tokenize(t));
        }
        return textsTokens;
    }
}
