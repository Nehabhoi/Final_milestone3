package rankedretrieval;

public interface RetrievalStrategy {
        double getWQT(double N, double dft);
        double getWDT(double tftd, int docID);
        double getLD(int docId);

    }