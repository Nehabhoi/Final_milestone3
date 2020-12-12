package cecs429.QueryFoundations_Java.cecs429.query;

import cecs429.index.Index;
import cecs429.index.KGramDiskIndex;
import cecs429.index.Posting;
import utility.ApplicationLogger;

import java.util.ArrayList;
import java.util.List;


public class WildcardLiteralQuery implements Query {
	private String term;
	private ApplicationLogger LoggerObj = ApplicationLogger.getInstance();

	public WildcardLiteralQuery(String term) {
		this.term = term;
		
	}
	public List<Posting> getPostings(Index index) {
		// term= "$red*a*d$"
		String newTerm = "$" + term + "$";
		//KGramIndex kgramindex = index.getKgramindex();
		KGramDiskIndex kGramDiskIndex = index.getkGramDiskIndex();
		String[] query = newTerm.split("\\*");
		// query[]={$red,a,$d}
		List<String> kGrams = new ArrayList<>();
		// creating largest grams of query
		for (String q : query) {
			if (!q.equals("$")) {
				if (q.length() >= 3) {
					for (int i = 0; i <= q.length() - 3; i++) {
						String three_gram = q.substring(i, i + 3);
						kGrams.add(three_gram);
					}
				} else if (q.length() == 2) {
					for (int i = 0; i <= q.length() - 2; i++) {
						String two_gram = q.substring(i, i + 2);
						kGrams.add(two_gram);
					}
				} else if (q.length() == 1) {
					kGrams.add(q);
				}
			}
		}
		// intersect grams
		List<String> result_grams = new ArrayList<>();
		List<List<String>> combine_gram_words = new ArrayList<>();
		for (String eachGram : kGrams) {
			List<String> TermOfKgramlist = new ArrayList<>();
			//TermOfKgramlist.addAll(kgramindex.getListOfWords(eachGram));

			TermOfKgramlist.addAll(kGramDiskIndex.getWordList(eachGram));
			if (TermOfKgramlist.size() > 0)
				combine_gram_words.add(TermOfKgramlist);
		}
		if (combine_gram_words.size() > 0)
			result_grams.addAll(combine_gram_words.get(0));
		for (int i = 1; i < combine_gram_words.size(); i++) {
			List<String> TermOfFirstGram = combine_gram_words.get(i);
			List<String> TermOfSecondGram = new ArrayList<>(result_grams);
			result_grams.clear();
			int k = 0;
			int j = 0;
			while (k < TermOfFirstGram.size() && j < TermOfSecondGram.size()) {
				String FirstWord = TermOfFirstGram.get(k);
				String SeconWord = TermOfSecondGram.get(j);
				if (FirstWord.equals(SeconWord)) {
					result_grams.add(FirstWord);
					k++;
					j++;
				} else if (FirstWord.compareTo(SeconWord) < 0) {
					k++;
				} else {
					j++;
				}

			}
		}
		// intersect done
		// filtering step
		List<String> filtered = new ArrayList<>();
		for (String s : result_grams) {
		//if(Pattern.compile(term).matcher(s).matches())
			filtered.add(s);
			// LoggerObj.insterLog("WildcardLiteralQuery.getPostings: kgrams "+s, Level.INFO);
		}
		// or query of postings
		List<List<Posting>> allTermPostings = new ArrayList<>();
		List<Posting> OrQueryResult = new ArrayList<>();
		for (String s : filtered) {
			List<Posting> indiviualPosting = index.getPostings(s);
			allTermPostings.add(indiviualPosting);
		}
		if (allTermPostings != null) {
			OrQueryResult.addAll(allTermPostings.get(0));
			for (int i = 1; i < allTermPostings.size(); i++) {
				List<Posting> curr = allTermPostings.get(i);
				List<Posting> ans = new ArrayList<>(OrQueryResult);
				OrQueryResult.clear();
				int j = 0;
				int k = 0;
				while (j < curr.size() && k < ans.size()) {
					if (curr.get(j).getDocumentId() == ans.get(k).getDocumentId()) {
						List<Integer> li1=curr.get(j).getPosition();
						List<Integer> li2=ans.get(k).getPosition();
						List<Integer> result=new ArrayList<>();
						int p1=0;
						int p2=0;
						while(p1<li1.size()&& p2<li2.size())
						{
							int m=li1.get(p1);
							int n=li2.get(p2);
							if(m==n)
							{
								result.add(m);
								p1++;
								p2++;
							}
							else if(m<n)
							{
								result.add(m);
								p1++;
								
							}
							else
							{
								result.add(n);
								p2++;
							}
						}
						while(p1<li1.size())
						{
							result.add(li1.get(p1));
							p1++;
						}
						while(p2<li2.size())
						{
							result.add(li2.get(p2));
							p2++;
						}
						OrQueryResult.add(new Posting(curr.get(j).getDocumentId(),result));
						j++;
						k++;
					} else if (curr.get(j).getDocumentId() < ans.get(k).getDocumentId()) {
						OrQueryResult.add(new Posting(curr.get(j).getDocumentId(),curr.get(j).getPosition()));
						j++;
					} else {
						OrQueryResult.add(new Posting(ans.get(k).getDocumentId(),ans.get(k).getPosition()));
						k++;
					}
				}
				while (j < curr.size()) {
					OrQueryResult.add(new Posting(curr.get(j).getDocumentId(),curr.get(j).getPosition()));
					j++;
				}
				while (k < ans.size()) {
					OrQueryResult.add(new Posting(ans.get(k).getDocumentId(),ans.get(k).getPosition()));
					k++;
				}

			}
		}
		return OrQueryResult;

	}

	public boolean isPostive() {
		return true;
	}
}
