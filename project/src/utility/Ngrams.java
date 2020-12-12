package utility;

import java.util.Set;
import java.util.TreeSet;

public class Ngrams {
    public static Set<String> ngrams(int n, String str) {
        Set<String> ngrams = new TreeSet<String>();
        for (int i = 0; i < str.length() - n + 1; i++)
            ngrams.add(str.substring(i, i + n));
        return ngrams;
    }
}
