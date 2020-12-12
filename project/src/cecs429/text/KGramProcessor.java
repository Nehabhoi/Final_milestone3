package cecs429.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class KGramProcessor {
	public List<String> processToken(String token) {
		if (token.equals("--"))
			return new ArrayList<>();
		boolean flag = false;
		for (char c : token.toCharArray()) {
			if (Character.isLetterOrDigit(c))
				flag = true;
		}
		if (flag == false)
			return new ArrayList<>();
		// System.out.println(token);
		List<String> tokens = new ArrayList<String>();
		int findex, lindex;

		for (findex = 0; findex < token.length(); findex++) {
			if (isAlphabet(token.charAt(findex))) {
				break;
			}
		}
		for (lindex = token.length() - 1; lindex >= 0; lindex--) {
			if (isAlphabet(token.charAt(lindex))) {
				break;
			}
		}
		if (findex - 1 == lindex + 1) {
			token = "";
		} else {
			token = token.substring(findex, lindex + 1);
		}
		token = token.replaceAll("['|\"]", "").toLowerCase();
		tokens.addAll(Arrays.asList(token.split("-")));
		tokens.add(token.replaceAll("-", ""));

		return tokens;

	}

	public static boolean isAlphabet(char c) {
		if ((Character.isLetterOrDigit(c)))
			return true;
		else
			return false;
	}
}
