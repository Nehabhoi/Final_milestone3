package cecs429.QueryFoundations_Java.cecs429.query;

import cecs429.index.Index;

import cecs429.index.Posting;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An AndQuery composes other Query objects and merges their postings in an
 * intersection-like operation.
 */
public class AndQuery implements Query {
	private List<Query> mChildren;
	private boolean type;

	public AndQuery(Iterable<Query> children) {
		type = true;
		mChildren = new ArrayList<>();
		for (Query q : children)
			mChildren.add(q);
	}

	@Override
	public List<Posting> getPostings(Index index) {
		// TODO: program the merge for an AndQuery, by gathering the postings of the
		// composed QueryComponents and
		// intersecting the resulting postings.
		List<Posting> andQueryResult = new ArrayList<>();
		// collecting all not query components
		List<Query> negativeComponents = new ArrayList<>();

		for (Query currentQueryTerm : mChildren) {
			if (currentQueryTerm.isPostive()) {
				// if it is positive then we will do and merge
				if (andQueryResult.isEmpty()) {
					andQueryResult = currentQueryTerm.getPostings(index);
					continue;
				}
				List<Posting> currentQuerytermPostingList = currentQueryTerm.getPostings(index);
				List<Posting> secondTermPostingList = new ArrayList<Posting>(andQueryResult);
				andQueryResult.clear();
				int i = 0, j = 0;
				while (i < secondTermPostingList.size() && j < currentQuerytermPostingList.size()) {
					if (secondTermPostingList.get(i).getDocumentId() == currentQuerytermPostingList.get(j)
							.getDocumentId()) {
						andQueryResult.add(secondTermPostingList.get(i));
						i++;
						j++;
					} else {
						if (secondTermPostingList.get(i).getDocumentId() > currentQuerytermPostingList.get(j)
								.getDocumentId()) {
							j++;
						} else {
							i++;
						}
					}
				}
			} else {
				negativeComponents.add(currentQueryTerm);
			}
		}
		// now with negative components we will do "and not" merge
		for (Query currentQueryTerm : negativeComponents) {
			List<Posting> currentQuerytermPostingList = currentQueryTerm.getPostings(index);
			List<Posting> secondTermPostingList = new ArrayList<Posting>(andQueryResult);
			andQueryResult.clear();
			int i = 0, j = 0;
			while (i < secondTermPostingList.size() && j < currentQuerytermPostingList.size()) {
				if (secondTermPostingList.get(i).getDocumentId() == currentQuerytermPostingList.get(j)
						.getDocumentId()) {
					i++;
					j++;
				} else {
					if (secondTermPostingList.get(i).getDocumentId() > currentQuerytermPostingList.get(j)
							.getDocumentId()) {
						j++;
					} else {
						andQueryResult.add(secondTermPostingList.get(i));
						i++;
					}
				}
			}
			while (i < secondTermPostingList.size()) {
				andQueryResult.add(secondTermPostingList.get(i));
				i++;
			}

		}

		return andQueryResult;
	}

	@Override
	public String toString() {
		return String.join(" ", mChildren.stream().map(c -> c.toString()).collect(Collectors.toList()));
	}

	@Override
	public boolean isPostive() {
		return type;
	}
}