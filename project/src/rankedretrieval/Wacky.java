package rankedretrieval;

import cecs429.index.DiskPositionalIndex;
import utility.ApplicationLogger;

import java.io.IOException;
import java.util.logging.Level;

public class Wacky implements RetrievalStrategy {
	DiskPositionalIndex diskIndex;
    private static ApplicationLogger LoggerObj = ApplicationLogger.getInstance();

    public Wacky(DiskPositionalIndex diskPositionalIndex) {
        diskIndex =diskPositionalIndex;
    }

    @Override
    public double getWQT(double N, double dft) {
        double a = Math.log((N-dft)/dft);
        double wqt = Math.max(0, a);
        return wqt;
    }

    @Override
    public double getWDT(double tftd, int docID) {
        double avgTftd =0;
        try {
			avgTftd = diskIndex.getAvgTftd(docID);
		} catch (IOException e) {
            LoggerObj.insterLog("Wacky.getWDT: " + e.toString(), Level.WARNING);
		}
        double numerator = 1 + Math.log(tftd);
        double denominator = 1 + Math.log(avgTftd);
        double wdt = numerator/denominator;
        return wdt;
    }

    @Override
    public double getLD(int docId) {
        double byteSize = 0;
        try {
			byteSize = diskIndex.getByteSize(docId);
		} catch (IOException e) {
            LoggerObj.insterLog("Wacky.getLD: " + e.toString(), Level.WARNING);
		}
        double LD = Math.sqrt(byteSize);
        return LD;
    }
}
