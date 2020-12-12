package cecs429.text;

import org.tartarus.snowball.ext.PorterStemmer;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class AdvanceTokenProcessor implements TokenProcessor {

    private List<String> processedTokens;
    PorterStemmer stemmer = new PorterStemmer();
    @Override
    public List<String> processToken(String token) {

        List<String> temp = new ArrayList<>();
        processedTokens = new ArrayList<>();
        String pToken_1 = token.replaceAll("^[^\\sa-zA-Z0-9]+|[^a-zA-Z0-9\\s]+$", "");
        String pToken_2 = pToken_1.replaceAll("\'+|\"+", "");
        String pToken_3 = pToken_2.toLowerCase();
        if (pToken_3.contains("-")) {
            temp.add(pToken_3.replaceAll("-", ""));
            StringTokenizer st = new StringTokenizer(pToken_3, "-");
            while (st.hasMoreTokens()) {
                String s = st.nextToken();
                if(!s.trim().equals(""))
                    temp.add(s);
            }

        } else {
            temp.add(pToken_3);
        }
        for (String str : temp) {
            stemmer.setCurrent(str);
            stemmer.stem();
            processedTokens.add(stemmer.getCurrent());
        }
        return processedTokens;
    }
}

