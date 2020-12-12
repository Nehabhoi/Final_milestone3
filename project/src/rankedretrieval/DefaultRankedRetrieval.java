package rankedretrieval;

import cecs429.index.DiskPositionalIndex;
import utility.ApplicationLogger;

import java.io.IOException;
import java.util.logging.Level;

public class DefaultRankedRetrieval implements RetrievalStrategy {
    DiskPositionalIndex mIndex;
    private static ApplicationLogger LoggerObj = ApplicationLogger.getInstance();

    public DefaultRankedRetrieval(DiskPositionalIndex index) {
        mIndex = index;
    }


    @Override
    public double getWQT(double N, double dft) {
        double wqt = Math.log(1 + (N / dft));
        return wqt;
    }

    @Override
    public double getWDT(double tftd, int docID) {
        double wdt = 1 + Math.log(tftd);
        return wdt;
    }

    @Override
    public double getLD(int docId) {
        double LD = 0;
        try {
            LD = mIndex.getDocWeightsLD(docId);
        } catch (IOException e) {
            LoggerObj.insterLog("DefaultRankedRetrieval.getLD: Exception in getLD: " + e.toString(), Level.WARNING);
        }
        return LD;
    }
}
