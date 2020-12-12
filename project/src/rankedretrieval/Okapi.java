package rankedretrieval;

import cecs429.index.DiskPositionalIndex;
import utility.ApplicationLogger;

import java.io.IOException;
import java.util.logging.Level;

public class Okapi implements RetrievalStrategy{
	DiskPositionalIndex diskIndex;
    private static ApplicationLogger LoggerObj = ApplicationLogger.getInstance();

    public Okapi(DiskPositionalIndex diskPositionalIndex) {
        diskIndex =diskPositionalIndex;
    }

    @Override
    public double getWQT(double N, double dft) {
        double a = 0.1;
        double b = Math.log((N-dft+0.5)/(dft+0.5));
        double wqt = Math.max(a, b);
        return wqt;
    }

    @Override
    public double getWDT(double tftd, int docID) {
        double doclength = 0,doclengthA = 0;
            try {
				doclength = diskIndex.getDocLength(docID);
			} catch (IOException e) {
                LoggerObj.insterLog("Okapi.getWDT: " + e.toString(), Level.WARNING);
			}
            //LoggerObj.insterLog("Okapi.getWDT: doc len" + doclength, Level.INFO);
            try {
				doclengthA = diskIndex.getDocLengthA();
			} catch (IOException e) {
                LoggerObj.insterLog("Okapi.getWDT: " + e.toString(), Level.WARNING);
			}

        double numerator = 2.2 * tftd;
        double denominator = (1.2*(0.25 + (0.75)*(doclength/doclengthA))) + tftd;
        double wdt = numerator/denominator;
        return wdt;
    }

    @Override
    public double getLD(int docId) {
        return 1;
    }
}
