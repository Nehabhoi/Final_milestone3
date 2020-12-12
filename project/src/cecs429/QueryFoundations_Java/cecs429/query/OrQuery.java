package cecs429.QueryFoundations_Java.cecs429.query;

import cecs429.index.Index;
import java.util.ArrayList;
import cecs429.index.Posting;

import java.util.List;
import java.util.stream.Collectors;

/**
 * An OrQuery composes other Query objects and merges their postings with a
 * union-type operation.
 */
public class OrQuery implements Query {
	// The components of the Or query.
	private List<Query> mChildren;
	private boolean type;

	public OrQuery(Iterable<Query> children) {
		type = true;
		mChildren = new ArrayList<>();
		for (Query q : children)
			mChildren.add(q);
	}

	@Override
	public List<Posting> getPostings(Index index) {
		// TODO: program the merge for an OrQuery, by gathering the postings of the
		// composed QueryComponents and
		// unioning the resulting postings.
		List<Posting> orQueryresult = new ArrayList();

		for (Query currentQueryTerm : mChildren) {
			List<Posting> currentQuertytermPosting = currentQueryTerm.getPostings(index);
			if (orQueryresult.isEmpty()) {
				orQueryresult.addAll(currentQuertytermPosting);
				continue;
			}
			List<Posting> secondQueryTerm = new ArrayList<Posting>(orQueryresult);

			orQueryresult.clear();
			int i = 0, j = 0;

			while (i < secondQueryTerm.size() && j < currentQuertytermPosting.size()) {
				if (secondQueryTerm.get(i).getDocumentId() == currentQuertytermPosting.get(j).getDocumentId()) {
					orQueryresult.add(secondQueryTerm.get(i));
					i++;
					j++;
				} else {
					if (secondQueryTerm.get(i).getDocumentId() > currentQuertytermPosting.get(j).getDocumentId()) {
						orQueryresult.add(currentQuertytermPosting.get(j));
						j++;
					} else {
						orQueryresult.add(secondQueryTerm.get(i));
						i++;
					}
				}
			}
			while (i < secondQueryTerm.size()) {
				orQueryresult.add(secondQueryTerm.get(i));
				i++;
			}
			while (j < currentQuertytermPosting.size()) {
				orQueryresult.add(currentQuertytermPosting.get(j));
				j++;
			}
		}
		return orQueryresult;

	}

	@Override
	public String toString() {
		// Returns a string of the form "[SUBQUERY] + [SUBQUERY] + [SUBQUERY]"
		return "(" + String.join(" + ", mChildren.stream().map(c -> c.toString()).collect(Collectors.toList())) + " )";
	}

	@Override
	public boolean isPostive() {
		return type;
	}
}