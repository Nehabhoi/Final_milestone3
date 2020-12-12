package rankedretrieval.evaluation;


import cecs429.index.DiskPositionalIndex;
import cecs429.index.Posting;
import cecs429.index.PostingAccumulator;
import rankedretrieval.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class VocabularyEliminationEvaluation extends Evaluation {
    float wqtThreshold;
    public VocabularyEliminationEvaluation(String corpusPath, DiskPositionalIndex index, float wqtThreshold){
        super(corpusPath,index);
        this.corpusPath = corpusPath;
        this.wqtThreshold = wqtThreshold;
    }
    public void setWQTThreshold(float wqtThreshold){
        this.wqtThreshold = wqtThreshold;
    }

    public void executeVocabularyEliminationEvaluationALlQueries() throws IOException {
        retrievalStrategy = new DefaultRankedRetrieval(this.index);
        System.out.println("Default Retrieval Strategy with wqt threshold "+wqtThreshold +"\n");
        getResultForAllQuery("Default Retrieval",retrievalStrategy);
    }

    public void executeVocabularyEliminationEvaluationFirstQuery() throws IOException {
        retrievalStrategy = new DefaultRankedRetrieval(this.index);
        System.out.println("Default Retrieval Strategy with wqt threshold "+wqtThreshold +"\n");
        getResultforFirstQuery("Default Retrieval",retrievalStrategy,false);
    }

    @Override
    public List<PostingAccumulator> getQueryResults(String query,RetrievalStrategy retrievalStrategy) throws IOException {
        String result = "";
        RankedRetrieval rankedRetrieval = new RankedRetrieval(query, corpusPath, corpus.getCorpusSize());
        rankedRetrieval.setNumberOfDocuments(50);
        List<PostingAccumulator> rankedResults = new ArrayList<>();
        rankedResults = rankedRetrieval.getPostings(index, processor, retrievalStrategy,wqtThreshold);
        for (PostingAccumulator p : rankedResults) {
            Posting posting = p.getPosting();
            String s = corpus.getDocument(posting.getDocumentId()).getTitle() + " Accum value - " + p.getAccumulator();
            result += s + "\n";
        }
        return rankedResults;
    }

}
