package rankedretrieval;

import cecs429.index.DiskPositionalIndex;
import cecs429.index.Posting;
import cecs429.index.PostingAccumulator;
import cecs429.index.SpellingCorrection;
import cecs429.text.TokenProcessor;
import utility.ApplicationLogger;
import cecs429.QueryFoundations_Java.cecs429.query.WildcardLiteralQuery;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.logging.Level;

public class RankedRetrieval {
    private String queryTerms[];
    private double corpuSize;
    private String path;
    private static ApplicationLogger LoggerObj = ApplicationLogger.getInstance();
    private List<String> wildcardTerms = new ArrayList<>();
    private List<String> notPresentTerms = new ArrayList<>();
    private List<String> stringterms = new ArrayList<>();
    private int numberOfdocuments = 10;


    public RankedRetrieval(String query, double size) {
        queryTerms = query.split(" ");
        corpuSize = size;
    }

    public RankedRetrieval(String query, String path, double size) {
        path = path;
        queryTerms = query.split(" ");
        corpuSize = size;
    }

    public void processQuery(DiskPositionalIndex index,TokenProcessor processor){
        stringterms=new ArrayList<>();
        for (int i = 0; i < queryTerms.length; i++) {
            if (queryTerms[i].contains("*")) {
                wildcardTerms.add(queryTerms[i]);
            } else {
                List<String> s = new ArrayList(processor.processToken(queryTerms[i]));
                if(s.size() > 0){
                    stringterms.add(s.get(0));
                }
            }
        }
        for (String term : stringterms) {
            if (index.getPostings(term) == null || index.getPostings(term).size() == 0) {
                notPresentTerms.add(term);
            }
        }
    }

    public boolean checkForTerms(DiskPositionalIndex index,TokenProcessor processor){
        processQuery(index,processor);
        if(notPresentTerms.size()>0){
            return  true;
        }
        return false;
    }

    private HashMap<String, String> findSpellCorrectedTerm(DiskPositionalIndex index){
        HashMap<String, String> mapTerm = new HashMap<>();
        SpellingCorrection SpellingCorrectionObj = new SpellingCorrection();
        for (String term : notPresentTerms) {
            String spellCorrectedQuery = SpellingCorrectionObj.spellingCorrection(index,term, 0.01f);
            mapTerm.put(term,spellCorrectedQuery);
        }
        return mapTerm;
    }

    public String getSpellCorrectedQuery(DiskPositionalIndex index, TokenProcessor processor) {
        HashMap<String, String> mapTerm = findSpellCorrectedTerm(index);
        String result = "";
        for (int i = 0; i < queryTerms.length; i++) {
            if (queryTerms[i].contains("*")) {
                result = result + queryTerms[i] + " ";
            } else {
                List<String> terms = new ArrayList(processor.processToken(queryTerms[i]));
                String newQueryTerm = null;
                for (String str : terms) {
                    if (mapTerm.containsKey(str)) {
                        newQueryTerm = mapTerm.get(str);
                        break;
                    }
                }
                if(newQueryTerm != null){
                    result = result + newQueryTerm + " ";
                }else{
                    result = result + processor.processToken(queryTerms[i]).get(0) + " ";
                }

            }
        }
        return result.substring(0, result.length() - 1);
    }

    public List<PostingAccumulator> getPostings(DiskPositionalIndex index, TokenProcessor processor, RetrievalStrategy retrieval_strategy) throws IOException {
        HashMap<Integer, PostingAccumulator> map = new HashMap<>();
        double N = (double) corpuSize;
        processQuery(index,processor);
        for (String term : stringterms) {
            if (index.getPostings(term) == null || index.getPostings(term).size() == 0) {
                LoggerObj.insterLog("RankedRetrieval.getPostings: Could not find term " + term, Level.INFO);
            } else {
                List<Posting> postings = index.getPostings(term);
                double dft = postings.size();
                double wqt = retrieval_strategy.getWQT(N, dft);
                for (Posting p : postings) {
                    double tftd = p.getPosition().size();
                    double wdt = retrieval_strategy.getWDT(tftd, p.getDocumentId());

                    double increment = wdt * wqt;
                    if (map.containsKey(p.getDocumentId())) {
                        PostingAccumulator postingaccumulator = map.get(p.getDocumentId());
                        double Ad = postingaccumulator.getAccumulator() + increment;
                        postingaccumulator.setAccumulator(Ad);
                        map.put(p.getDocumentId(), postingaccumulator);
                    } else {
                        map.put(p.getDocumentId(), new PostingAccumulator(p, increment));
                    }
                }
            }

        }
        if (wildcardTerms != null || wildcardTerms.size() > 0) {
            for (String term : wildcardTerms) {
                WildcardLiteralQuery wildcardLiteralQuery = new WildcardLiteralQuery(term);
                List<Posting> postings = wildcardLiteralQuery.getPostings(index);
                double dft = postings.size();
                double wqt = retrieval_strategy.getWQT(N, dft);
                for (Posting p : postings) {
                    double tftd = p.getPosition().size();
                    double wdt = retrieval_strategy.getWDT(tftd, p.getDocumentId());

                    double increment = wdt * wqt;
                    if (map.containsKey(p.getDocumentId())) {
                        PostingAccumulator postingaccumulator = map.get(p.getDocumentId());
                        double Ad = postingaccumulator.getAccumulator() + increment;
                        postingaccumulator.setAccumulator(Ad);
                        map.put(p.getDocumentId(), postingaccumulator);
                    } else {
                        map.put(p.getDocumentId(), new PostingAccumulator(p, increment));
                    }
                }
            }
        }
        List<PostingAccumulator> results = getTopNDocuments( map,  retrieval_strategy);
        return results;
    }

    // getPosting for vocabulary elimination
    public List<PostingAccumulator> getPostings(DiskPositionalIndex index, TokenProcessor processor, RetrievalStrategy retrieval_strategy,float wqtThreshold) throws IOException {
        HashMap<Integer, PostingAccumulator> map = new HashMap<>();
        double N = (double) corpuSize;
        processQuery(index,processor);
        for (String term : stringterms) {
            if (index.getPostings(term) == null || index.getPostings(term).size() == 0) {
                LoggerObj.insterLog("RankedRetrieval.getPostings: Could not find term " + term, Level.INFO);
            } else {
                List<Posting> postings = index.getPostings(term);
                double dft = postings.size();
                double wqt = retrieval_strategy.getWQT(N, dft);
                if(wqt >= wqtThreshold){
                    for (Posting p : postings) {
                        double tftd = p.getPosition().size();
                        double wdt = retrieval_strategy.getWDT(tftd, p.getDocumentId());

                        double increment = wdt * wqt;
                        if (map.containsKey(p.getDocumentId())) {
                            PostingAccumulator postingaccumulator = map.get(p.getDocumentId());
                            double Ad = postingaccumulator.getAccumulator() + increment;
                            postingaccumulator.setAccumulator(Ad);
                            map.put(p.getDocumentId(), postingaccumulator);
                        } else {
                            map.put(p.getDocumentId(), new PostingAccumulator(p, increment));
                        }
                    }
                }
            }

        }
        if (wildcardTerms != null || wildcardTerms.size() > 0) {
            for (String term : wildcardTerms) {
                WildcardLiteralQuery wildcardLiteralQuery = new WildcardLiteralQuery(term);
                List<Posting> postings = wildcardLiteralQuery.getPostings(index);
                double dft = postings.size();
                double wqt = retrieval_strategy.getWQT(N, dft);
                if(wqt >= wqtThreshold){
                    for (Posting p : postings) {
                        double tftd = p.getPosition().size();
                        double wdt = retrieval_strategy.getWDT(tftd, p.getDocumentId());

                        double increment = wdt * wqt;
                        if (map.containsKey(p.getDocumentId())) {
                            PostingAccumulator postingaccumulator = map.get(p.getDocumentId());
                            double Ad = postingaccumulator.getAccumulator() + increment;
                            postingaccumulator.setAccumulator(Ad);
                            map.put(p.getDocumentId(), postingaccumulator);
                        } else {
                            map.put(p.getDocumentId(), new PostingAccumulator(p, increment));
                        }
                    }
                }
            }
        }
        List<PostingAccumulator> results = getTopNDocuments( map,  retrieval_strategy);
        return results;
    }

    private List<PostingAccumulator> getTopNDocuments(HashMap<Integer, PostingAccumulator> map, RetrievalStrategy retrieval_strategy){
        PriorityQueue<PostingAccumulator> priorityQueue = new PriorityQueue<>();

        for (HashMap.Entry<Integer, PostingAccumulator> entry : map.entrySet()) {
            double LD = retrieval_strategy.getLD(entry.getKey());
            //LoggerObj.insterLog("RankedRetrieval.getTopNDocuments: LD" + LD, Level.INFO);
            //LoggerObj.insterLog("RankedRetrieval.getTopNDocuments: " + entry.getValue().getPosting().getDocumentId(), Level.INFO);
            PostingAccumulator p = entry.getValue();
            Double Ad;
            if (p.getAccumulator() != 0) {
                Ad = p.getAccumulator() / LD;
                p.setAccumulator(Ad);
            }
            priorityQueue.add(p);
        }
        List<PostingAccumulator> results = new ArrayList<>();
        int size = priorityQueue.size();
        int i = 0;
        while (i < numberOfdocuments && i < size) {
            results.add(priorityQueue.poll());
            i++;
        }
        return results;
    }

    public void setNumberOfDocuments(int n){
        this.numberOfdocuments = n;
    }

    public int getNumberOfDocuments(){
        return this.numberOfdocuments;
    }
}