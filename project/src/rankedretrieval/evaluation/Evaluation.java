package rankedretrieval.evaluation;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.*;
import cecs429.text.AdvanceTokenProcessor;
import cecs429.text.EnglishTokenStream;
import edu.csulb.DiskApplication;
import rankedretrieval.RankedRetrieval;
import rankedretrieval.RetrievalStrategy;
import utility.ApplicationLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;

public class Evaluation {
    public ApplicationLogger LoggerObj = ApplicationLogger.getInstance();
    public String corpusPath = null;
    public String queryPath;
    public String queryRelevancePath;
    public AdvanceTokenProcessor processor = new AdvanceTokenProcessor();
    public DiskPositionalIndex index;
    public DirectoryCorpus corpus;
    public RetrievalStrategy retrievalStrategy = null;

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_PURPLE = "\u001B[35m";


    public Evaluation(String corpusPath ,DiskPositionalIndex index){
        DiskApplication diskApplication=new DiskApplication();
        this.corpusPath = corpusPath;
        java.nio.file.Path path = Paths.get(corpusPath);
        corpus = new DirectoryCorpus(path);
        corpus.loadTextFile(".txt");
        corpus.loadJsonDirectory(".json");
        this.index = index;
        queryPath = corpusPath + "/relevance/queries";
        queryRelevancePath = corpusPath + "/relevance/qrel";
    }

    //Get results for a query with Inexact ranked retrieval
    public List<PostingAccumulator> getQueryResults(String query, RetrievalStrategy retrievalStrategy) throws IOException {
        String result = "";
        RankedRetrieval rankedRetrieval = new RankedRetrieval(query, corpusPath, corpus.getCorpusSize());
        rankedRetrieval.setNumberOfDocuments(50);
        List<PostingAccumulator> rankedResults = new ArrayList<>();
        rankedResults = rankedRetrieval.getPostings(index, processor, retrievalStrategy);
        for (PostingAccumulator p : rankedResults) {
            Posting posting = p.getPosting();
            String s = corpus.getDocument(posting.getDocumentId()).getTitle() + " Accum value - " + p.getAccumulator();
            result += s + "\n";
        }
        return rankedResults;
    }

    private Set<Integer> getRelevanceIntegerList(String relevantDocuments){
        Set<Integer> relevantList = new HashSet<>();
        for (String s : relevantDocuments.split(" ")) {
            s.trim();
            if (!s.equalsIgnoreCase(""))
                relevantList.add(Integer.parseInt(s));
        }
        return relevantList;
    }

    private long getQueryTime(String query, RetrievalStrategy retrievalStrategy) throws IOException {
        List<PostingAccumulator> results = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        results = getQueryResults(query,retrievalStrategy);
        long endTime = System.currentTimeMillis();
        long queryTime = (endTime - startTime);
        return queryTime;
    }

    private void drawPRCurveForaQuery(String title,List<Double> precisionList, List<Double> recallList){
        final Plotting plot = new Plotting(title, recallList, precisionList);
        plot.pack();
        plot.setVisible(true);
    }

    private double getAveraePrecisionForAQuery(String title,List<PostingAccumulator> results,Set<Integer> relevantList, Boolean isPrint){
        String documentLists = "";
        List<Double> precisionList = new ArrayList<>();
        List<Double> recallList = new ArrayList<>();
        double precision=0,recall=0;
        double AP = 0;
        double numberOfRelevantDocuments = 0;
        double numberOfReturnedDocuments = 0;
        int index = 0;
        for (PostingAccumulator p : results) {
            index++;
            numberOfReturnedDocuments++;
            Posting posting = p.getPosting();
            int documentId = posting.getDocumentId();
            //Add something in Document to get name of the document
            String documentName = corpus.getDocument(documentId).getTitle();
            documentName = documentName.replaceFirst("[.][^.]+$", "");
            int documentNumber = Integer.parseInt(documentName);
            if (relevantList.contains(documentNumber)) {
                numberOfRelevantDocuments++;
                documentLists += documentNumber + " ";
                precision = numberOfRelevantDocuments / numberOfReturnedDocuments;
                recall = numberOfRelevantDocuments / relevantList.size();
                AP += precision;
                if(isPrint){
                    double precisionAtI = (double)(numberOfRelevantDocuments/index);
                    double recallAtI = (double)(numberOfRelevantDocuments/50);
                    System.out.println(ANSI_GREEN + "Relevant: "+ corpus.getDocument(documentId).getTitle()+ " at index "+ index + " Precision at i: "+  precisionAtI + " Recall at i: " + recallAtI +ANSI_RESET);
                }
            }else{
                if(isPrint) {
                    double precisionAtI = (double)(numberOfRelevantDocuments/index);
                    double recallAtI = (double)(numberOfRelevantDocuments/50);
                    System.out.println(ANSI_PURPLE + "Not Relevant: " + corpus.getDocument(documentId).getTitle() + " at index " + index + " Precision at i: " + precisionAtI + " Recall at i: " + recallAtI +ANSI_RESET);
                }
            }

            //Draw precision recall for first query
            if(isPrint) {
                precisionList.add(precision);
                recallList.add(recall);
            }
        }
        AP = AP / (double) relevantList.size();
        if(isPrint){
            drawPRCurveForaQuery(title,precisionList,recallList);
        }
        return AP;
    }

    public void getResultForAllQuery(String title, RetrievalStrategy retrievalStrategy) throws IOException {
            Scanner sc1 = new Scanner(new File(queryPath));
            Scanner sc2 = new Scanner(new File(queryRelevancePath));

            String query = "";
            String relevantDocuments = "";


            double MAP = 0;
            double MRT = 0;
            double numberOfQueries = 0;

            System.out.println("Query Execution started!");
            while (sc1.hasNextLine()) {

                numberOfQueries++;
                query = sc1.nextLine();
                relevantDocuments = sc2.nextLine();

                //get the relevance integer from string
                Set<Integer> relevantList = getRelevanceIntegerList(relevantDocuments);

                // get query time
                List<PostingAccumulator> results = new ArrayList<>();
                long startTime = System.currentTimeMillis();
                results = getQueryResults(query,retrievalStrategy);
                long endTime = System.currentTimeMillis();
                long queryTime = (endTime - startTime);

                // calculating AP for a query
                double AP = getAveraePrecisionForAQuery(title,results, relevantList,false);
                System.out.println("Average precision for this query ["+query+" ]:" +  AP);
                MAP += AP;
                MRT += queryTime;
            }
            System.out.println("Query Execution Completed!");
            // get MRT MAP Throughput for all query
            double throughput = numberOfQueries  / (MRT / 1000);
            MAP = MAP / numberOfQueries;
            MRT = MRT / numberOfQueries;
            System.out.println("Results for All Query:");
            System.out.println("MAP = " + MAP);
            System.out.println("Throughput = " + throughput+" queries/Sec");
            System.out.println("MRT = " + MRT + " miliseconds\n");
            sc1.close();
            sc2.close();
        }


    public void getResultforFirstQuery(String title, RetrievalStrategy retrievalStrategy, Boolean draw)throws IOException {
        Scanner sc1 = new Scanner(new File(queryPath));
        Scanner sc2 = new Scanner(new File(queryRelevancePath));
        String query = sc1.nextLine();
        String relevantDocuments = sc2.nextLine();
        //get the relevance integer from string
        Set<Integer> relevantList = getRelevanceIntegerList(relevantDocuments);
        // get query time for 30 iteration
        List<PostingAccumulator> results = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        System.out.println("Query Execution started!");
        for(int i=0; i<30;i++) {
            results = getQueryResults(query, retrievalStrategy);
        }
        System.out.println("Query Execution completed!");
        long endTime = System.currentTimeMillis();
        double queryTime = (endTime - startTime);

        //get evaluation measure calculation
        double AP = getAveraePrecisionForAQuery(title, results, relevantList,draw);
        double MRT = queryTime / 30;
        double throughput = 30  / (MRT / 1000);
        System.out.println("Results for 30 iteration:");
        System.out.println("Average precision for this query ["+query+" ]:" +  AP);
        System.out.println("MRT = " + MRT + " miliseconds");
        System.out.println("Throughput = " + throughput +" queries/Sec\n");
        sc1.close();
        sc2.close();
    }
}


