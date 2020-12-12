package cecs429.index;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import utility.ApplicationLogger;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

public class DiskIndexWriter {
    private static ApplicationLogger LoggerObj = ApplicationLogger.getInstance();

    public DiskIndexWriter() {
    }

    public void writeIndex(Index index, Path corpusPath) throws IOException {
        File file = new File(corpusPath + "/index/postings.bin");
        file.getParentFile().mkdirs();
        file.createNewFile();
        RandomAccessFile writer = new RandomAccessFile(file, "rw");
        DB db = DBMaker.fileDB("file.db").make();
        ConcurrentMap<String, Long> map = db.hashMap("map", Serializer.STRING, Serializer.LONG).createOrOpen();
        Long vocab_position;
        int previousDocId = 0;
        for (String str : index.getVocabulary()) {
            List<Posting> postings = index.getPostings(str);
            int dft = postings.size();
            byte[] hex_dft = ByteBuffer.allocate(4).putInt(dft).array();
            vocab_position = writer.getFilePointer();
            map.put(str, vocab_position);
            writer.write(hex_dft, 0, hex_dft.length);
            previousDocId  = 0;
            int id = 0;
            for (Posting p : postings) {
                id = p.getDocumentId();
                id = id - previousDocId ;
                previousDocId  = id;
                byte[] hex_id = ByteBuffer.allocate(4).putInt(id).array();
                writer.write(hex_id, 0, hex_id.length);
                List<Integer> positions = p.getPosition();
                int tftd = positions.size();
                byte[] hex_tftd = ByteBuffer.allocate(4).putInt(tftd).array();
                writer.write(hex_tftd, 0, hex_tftd.length);
                int previousPositionId = 0;
                for (int position : positions) {
                    position = position - previousPositionId;
                    previousPositionId = position;
                    byte[] hex_position = ByteBuffer.allocate(4).putInt(position).array();
                    writer.write(hex_position, 0, hex_position.length);
                }
            }
        }
        writer.close();
        db.close();

    }


    public void writeDocumentWeight(List<Double> ListOfLd, Path corpusPath, List<Double> avgTermFrequency, List<Double>
            docLength, List<Double> docByteSize) throws IOException {
        // create docWeights.bin file
        File file = new File(corpusPath + "/index/docWeights.bin");
        RandomAccessFile weightsFile = new RandomAccessFile(file, "rw");
        for (int docId = 0; docId < docByteSize.size(); docId++) {

            byte[] docWeightByte = ByteBuffer.allocate(8).putDouble(ListOfLd.get(docId)).array();
            weightsFile.write(docWeightByte, 0, docWeightByte.length);

            double length = docLength.get(docId);
            byte[] docLengthByte = ByteBuffer.allocate(8).putDouble(length).array();
            weightsFile.write(docLengthByte, 0, docLengthByte.length);

            double byteSize = docByteSize.get(docId);
            byte[] docSizeByte = ByteBuffer.allocate(8).putDouble(byteSize).array();
            weightsFile.write(docSizeByte, 0, docSizeByte.length);


            byte[] aveTfByte = ByteBuffer.allocate(8).putDouble(avgTermFrequency.get(docId)).array();
            weightsFile.write(aveTfByte, 0, aveTfByte.length);

        }

        double avgDocumentLength = 0;
        for (double dLength : docLength) {
            avgDocumentLength  += dLength;
        }
        double size=docLength.size();
        avgDocumentLength  /= size;
        byte[] aveDocLengthByte = ByteBuffer.allocate(8).putDouble(avgDocumentLength).array();
        weightsFile.write(aveDocLengthByte, 0, aveDocLengthByte.length);

        weightsFile.close();
    }


}