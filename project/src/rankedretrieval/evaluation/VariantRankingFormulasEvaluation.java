package rankedretrieval.evaluation;
import cecs429.index.*;
import rankedretrieval.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class VariantRankingFormulasEvaluation extends Evaluation {
    private static Boolean draw=true;


    public VariantRankingFormulasEvaluation(String corpusPath,DiskPositionalIndex index){
        super(corpusPath,index);
        this.corpusPath = corpusPath;
    }

    public void executeVariantRankingFormulasEvaluationALlQueries() throws IOException {
        executeDefaultStrategyALL();
        executeOKAPIStrategyALL();
        executeTfTdfStrategyALL();
        executeWackyStrategyALL();
    }

    public void executeVariantRankingFormulasEvaluationFirstQuery() throws IOException {
        executeDefaultStrategy();
        executeOKAPIStrategy();
        executeTfTdfStrategy();
        executeWackyStrategy();
    }

    public void executeDefaultStrategyALL() throws IOException {
        retrievalStrategy = new DefaultRankedRetrieval(this.index);
        System.out.println("Default Retrieval Strategy");
        getResultForAllQuery("Default Ranking",retrievalStrategy);
    }

    public void executeOKAPIStrategyALL() throws IOException {
        retrievalStrategy = new Okapi(index);
        System.out.println("OkapiBM25 Retrieval Strategy");
        getResultForAllQuery("OkapiBM25 Ranking",retrievalStrategy);
    }

    public void executeTfTdfStrategyALL() throws IOException {
        retrievalStrategy = new Tf_tdf(index);
        System.out.println("TfTdf Retrieval Strategy");
        getResultForAllQuery("Tf_Idf Ranking",retrievalStrategy);
    }

    public void executeWackyStrategyALL() throws IOException {
        retrievalStrategy = new Wacky(index);
        System.out.println("Wacky Retrieval Strategy");
        getResultForAllQuery("Wacky Ranking",retrievalStrategy);
    }


    public void executeDefaultStrategy() throws IOException {
        retrievalStrategy = new DefaultRankedRetrieval(this.index);
        System.out.println("Default Retrieval Strategy");
        getResultforFirstQuery("Default Ranking",retrievalStrategy, draw);
    }

    public void executeOKAPIStrategy() throws IOException {
        retrievalStrategy = new Okapi(index);
        System.out.println("OkapiBM25 Retrieval Strategy");
        getResultforFirstQuery("OkapiBM25 Ranking",retrievalStrategy, draw);
    }

    public void executeTfTdfStrategy() throws IOException {
        retrievalStrategy = new Tf_tdf(index);
        System.out.println("TfTdf Retrieval Strategy");
        getResultforFirstQuery("Tf_Idf Ranking",retrievalStrategy, draw);
    }

    public void executeWackyStrategy() throws IOException {
        retrievalStrategy = new Wacky(index);
        System.out.println("Wacky Retrieval Strategy");
        getResultforFirstQuery("Wacky Ranking",retrievalStrategy, draw);
    }
}
