package cecs429.index;

import java.util.*;
import java.util.stream.Collectors;

public class InvertedIndex implements Index {

	private HashMap<String, List<Posting>> termPostingMap;
	private KGramIndex kgramindex=null;

	public InvertedIndex() {
		termPostingMap = new HashMap<>();

	}

	// for special query to get first N vocab
	public List<String> getfirstNVocabulary(int n){
		ArrayList<String> vocab=new ArrayList<>(termPostingMap.keySet());
		Collections.sort(vocab);
		List<String> firstNVocabulary = vocab.stream().limit(n).collect(Collectors.toList());
		return firstNVocabulary;
	}
	// to add term with postion in map
	public void addTerm(String term, int documentId,int pos) {

		//check if the term is present in the map
		if (!termPostingMap.containsKey(term)) {
			termPostingMap.put(term, new ArrayList<>());
		}
		List<Posting> postingList = termPostingMap.get(term);
		// check if the documentId is present in the posting list
		if (postingList.isEmpty() || postingList.get(postingList.size() - 1).getDocumentId() != documentId)
		{
			Posting p=new Posting(documentId);
			p.setPostion(pos);
			postingList.add(p);
		}
		else {
			Posting p=postingList.get(postingList.size() - 1);
			p.setPostion(pos);
			postingList.set(postingList.size() - 1,p);
		}

		termPostingMap.put(term, postingList);

	}


	public List<Posting> getPostings(String term) {
		return termPostingMap.get(term);
	}

	public List<String> getVocabulary() {
		//return new ArrayList<>(termPostingMap.keySet());
		ArrayList<String> vocab=new ArrayList<>(termPostingMap.keySet());
		Collections.sort(vocab);
		return vocab;
	}

	public void setKgramindex(KGramIndex kgramindex) {
		this.kgramindex = kgramindex;
	}
	public KGramIndex getKgramindex() {
		return kgramindex;
	}



	public List<Posting> getPostingsWithoutPositions(String term){
		throw new UnsupportedOperationException("Sorry not implemented yet");
	}
	public KGramDiskIndex getkGramDiskIndex(){
		throw new UnsupportedOperationException("Sorry not implemented yet");
	}
}
