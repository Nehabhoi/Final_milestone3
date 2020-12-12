package cecs429.index;

public class EditDistance {
	static int min(int num1, int num2, int num3) {
		if (num1 <= num2 && num1 <= num3)
			return num1;
		if (num2 <= num1 && num2 <= num3)
			return num2;
		else
			return num3;
	}

	static int editDistDP(String str1, String str2, int str1Length, int str2Length) {
		// Create a table to store results of subproblems
		int dp[][] = new int[str1Length + 1][str2Length + 1];

		// Fill d[][] in bottom up manner
		for (int i = 0; i <= str1Length; i++) {
			for (int j = 0; j <= str2Length; j++) {
				// If first string is empty, only option is to insert all characters of second string
				if (i == 0)
					dp[i][j] = j; // Min. operations = j

				// If second string is empty, only option is to remove all characters of second string
				else if (j == 0)
					dp[i][j] = i; // Min. operations = i

				// If last characters are same, ignore last char and recur for remaining string
				else if (str1.charAt(i - 1) == str2.charAt(j - 1))
					dp[i][j] = dp[i - 1][j - 1];

				// If the last character is different, consider all possibilities and find the minimum
				else
					dp[i][j] = 1 + min(dp[i][j - 1], // Insert
							dp[i - 1][j], // Remove
							dp[i - 1][j - 1]); // Replace
			}
		}

		return dp[str1Length][str2Length];
	}
}
