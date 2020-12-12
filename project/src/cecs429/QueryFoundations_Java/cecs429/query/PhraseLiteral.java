package cecs429.QueryFoundations_Java.cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.TokenProcessor;
import utility.ApplicationLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a phrase literal consisting of one or more terms that must occur
 * in sequence.
 */
public class PhraseLiteral implements Query {
	// The list of individual terms in the phrase.
	private List<String> mTerms = new ArrayList<>();
	private ApplicationLogger LoggerObj = ApplicationLogger.getInstance();
	/**
	 * Constructs a PhraseLiteral with the given individual phrase terms.
	 */
	public PhraseLiteral(List<String> terms,TokenProcessor processor) {
		for (String term : terms) {
			List<String> list = processor.processToken(term);
			mTerms.add(list.get(list.size() - 1));
		}
	}

	/**
	 * Constructs a PhraseLiteral given a string with one or more individual terms
	 * separated by spaces.
	 */
	public PhraseLiteral(String terms,TokenProcessor processor) {
		List<String> termList = Arrays.asList(terms.split(" "));
		for (String term : termList) {
			if(!term.contains("*")){
			List<String> list = processor.processToken(term);
			mTerms.add(list.get(list.size() - 1)); }
			else { mTerms.add(term);}
		}
	}

	@Override
	public List<Posting> getPostings(Index index) {
		// new york university(3)
		// new york-1
		List<Posting> PhraseQueryResult = new ArrayList<>();
		List<List<Posting>> AllTermPosting = new ArrayList<>();
		for (String term : mTerms) {

			if(term.contains("*")) {
				WildcardLiteralQuery wildcardQueryObj = new WildcardLiteralQuery(term);
				AllTermPosting.add(wildcardQueryObj.getPostings(index));
			}else {

				AllTermPosting.add(index.getPostings(term));
			}
		}
		PhraseQueryResult.addAll(AllTermPosting.get(0));
		for (int i = 1; i < AllTermPosting.size(); i++) {
			List<Posting> FirstTermPosting = AllTermPosting.get(i);
			List<Posting> SecondTermPosting = new ArrayList<>(PhraseQueryResult);
			PhraseQueryResult.clear();
			int j = 0;
			int k = 0;
			while (j < FirstTermPosting.size() && k < SecondTermPosting.size()) {
				Posting FirsttermPostingObj = FirstTermPosting.get(j);
				Posting SecondTermPostingObj = SecondTermPosting.get(k);
				if (FirsttermPostingObj.getDocumentId() == SecondTermPostingObj.getDocumentId()) {
					List<Integer> PositionOfFirstObj = FirsttermPostingObj.getPosition();
					List<Integer> PositionOfSecondObj = SecondTermPostingObj.getPosition();
					List<Integer> TempResultOfPosition = new ArrayList<>();
					int c = 0;
					int d = 0;
					while (c < PositionOfFirstObj.size() && d < PositionOfSecondObj.size()) {
						int pos2 = PositionOfSecondObj.get(d);
						int pos1 = PositionOfFirstObj.get(c);
						//System.out.println("--" + pos1 + "--" + pos2);
						//LoggerObj.insterLog("PhraseLiteral.getPostings: --" + pos1 + "--" + pos2, Level.INFO);
						if (pos2 < pos1 && (pos1 - pos2) == 1) {

							TempResultOfPosition.add(pos1);
							c++;
							d++;
						} else if (pos1 == pos2) {
							c++;
							d++;
						} else if (pos1 < pos2) {
							c++;
						} else {
							d++;
						}
					}
					if (TempResultOfPosition.size() > 0) {
						PhraseQueryResult.add(new Posting(FirsttermPostingObj.getDocumentId(), TempResultOfPosition));
						//LoggerObj.insterLog("PhraseLiteral.getPostings: TempResultOfPosition"+TempResultOfPosition, Level.INFO);
						TempResultOfPosition.clear();
					}
					j++;
					k++;
				} else if (FirsttermPostingObj.getDocumentId() < SecondTermPostingObj.getDocumentId()) {
					j++;
				} else {
					k++;
				}
			}
			//LoggerObj.insterLog("PhraseLiteral.getPostings: PhraseQueryResult"+PhraseQueryResult, Level.INFO);
		}

		return PhraseQueryResult;
		// TODO: program this method. Retrieve the postings for the individual terms in
		// the phrase,
		// and positional merge them together.
	}

	@Override
	public String toString() {
		return "\"" + String.join(" ", mTerms) + "\"";
	}

	@Override
	public boolean isPostive() {
		return true;
	}
}