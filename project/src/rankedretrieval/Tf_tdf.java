package rankedretrieval;

import cecs429.index.DiskPositionalIndex;
import utility.ApplicationLogger;

import java.io.IOException;
import java.util.logging.Level;

public class Tf_tdf implements RetrievalStrategy{
	DiskPositionalIndex diskIndex;
    private static ApplicationLogger LoggerObj = ApplicationLogger.getInstance();

    public Tf_tdf(DiskPositionalIndex index)
    {

        diskIndex = index;
    }

    @Override
    public double getWQT(double N, double dft) {
        double idft = Math.log(N/dft);
        return idft;
    }

    @Override
    public double getWDT(double tftd, int docID) {
        return tftd;
    }

    @Override
    public double getLD(int docId) {
        double LD=0;
        try {
            LD = diskIndex.getDocWeightsLD(docId);
        } catch (IOException e) {
            LoggerObj.insterLog("Tf_tdf.getLD: " + e.toString(), Level.WARNING);
        }
        return LD;
    }
}
