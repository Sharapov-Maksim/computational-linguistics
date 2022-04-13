package concordance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

public class Util {
    public static List<String> tokenize(String text) {
        List<String> basedTokens = Arrays.stream(text.replace("\n", "").toLowerCase().split("[ \"()?:!'-]")).
                filter((x) -> x.length() > 0).collect(Collectors.toList());
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
}
