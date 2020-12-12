package cecs429.QueryFoundations_Java.cecs429.query;
import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.TokenProcessor;

import java.util.List;

/**
 * A TermLiteral represents a single term in a subquery.
 */
public class TermLiteral implements Query {
	private String mTerm;
	private TokenProcessor processor;
	
	public TermLiteral(String term,TokenProcessor processor) {
		mTerm = term;
		this.processor=processor;
	}
	
	public String getTerm() {
		return mTerm;
		
	}
	
	@Override
	public List<Posting> getPostings(Index index) {
		List<String> result=processor.processToken(mTerm);
		return index.getPostings(result.get(0));
		//return index.getPostings(mTerm);
	}
	
	@Override
	public String toString() {
		return mTerm;
	}
	@Override
	public boolean isPostive()
	{
		return true;
	}
}
