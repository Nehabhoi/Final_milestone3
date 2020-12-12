package cecs429.index;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import utility.ApplicationLogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class DiskPositionalIndex implements Index {
	private String path;
	private RandomAccessFile docWeights;
	private RandomAccessFile postings;
	private KGramDiskIndex kGramDiskIndex = null;
	private static ApplicationLogger LoggerObj = ApplicationLogger.getInstance();

	public DiskPositionalIndex(String path) {
		try {
			this.path = path;
			postings = new RandomAccessFile(new File(path, "/index/postings.bin"), "r");
			docWeights = new RandomAccessFile(new File(path, "/index/docWeights.bin"), "r");
		} catch (FileNotFoundException ex) {
			LoggerObj.insterLog("DiskPositionalIndex.DiskPositionalIndex: "+ ex.toString(), Level.WARNING);
		}
	}

	@Override
	public List<Posting> getPostings(String term) {
		DB db = DBMaker.fileDB("file.db").readOnly().make();
		ConcurrentMap<String,Long> map= db.hashMap("map", Serializer.STRING, Serializer.LONG).open();

		if(!map.containsKey(term)) return null;
		long term_position = (long) map.get(term);
		db.close();
		List<Posting> posting_list = new ArrayList<>();
		if (term_position == -1) {
			return posting_list;
		}
		try {
			postings.seek(term_position);
		} catch (Exception ex) {
			LoggerObj.insterLog("DiskPositionalIndex.getPostings: "+ ex.toString(), Level.WARNING);
		}

		byte[] byteBuffer = new byte[4];
		try {
			postings.read(byteBuffer, 0, byteBuffer.length);
			ByteBuffer wrapped = ByteBuffer.wrap(byteBuffer);
			int dft = wrapped.getInt();
			int prev = 0;
			int doc_id;
			for (int i = 0; i < dft; i++) {
				postings.read(byteBuffer, 0, byteBuffer.length);
				doc_id = prev + ByteBuffer.wrap(byteBuffer).getInt();
				prev = ByteBuffer.wrap(byteBuffer).getInt();
				postings.read(byteBuffer, 0, byteBuffer.length);
				int tftd = ByteBuffer.wrap(byteBuffer).getInt();
				int position = 0;
				List<Integer> position_list = new ArrayList();
				for (int j = 0; j < tftd; j++) {
					postings.read(byteBuffer, 0, byteBuffer.length);
					position = position + ByteBuffer.wrap(byteBuffer).getInt();
					position_list.add(position);
				}

				Posting p = new Posting(doc_id, position_list);
				posting_list.add(p);
			}

		} catch (Exception ex) {
			LoggerObj.insterLog("DiskPositionalIndex.getPostings: "+ ex.toString(), Level.WARNING);
		}
		return posting_list;

	}
	public List<Posting> getPostingsWithoutPositions(String term) {
		DB db = DBMaker.fileDB("file.db").readOnly().make();
		ConcurrentMap<String,Long> map= db.hashMap("map", Serializer.STRING, Serializer.LONG).open();
		long term_position = map.get(term);
		db.close();
		List<Posting> posting_list = new ArrayList<>();
		if (term_position == -1) {
			return posting_list;
		}
		try {
			postings.seek(term_position);
		} catch (IOException ex) {
			System.out.println(ex);
		}
		byte[] byteBuffer = new byte[4];
		try {
			ByteBuffer wrapped = ByteBuffer.wrap(byteBuffer);
			int dft = wrapped.getInt();
			int prev = 0;
			int doc_id;
			for (int i = 0; i < dft; i++) {
				postings.read(byteBuffer, 0, byteBuffer.length);
				doc_id = prev + ByteBuffer.wrap(byteBuffer).getInt();
				prev = ByteBuffer.wrap(byteBuffer).getInt();
				postings.read(byteBuffer, 0, byteBuffer.length);
				int tftd = ByteBuffer.wrap(byteBuffer).getInt();

				int position = tftd * byteBuffer.length;
				postings.skipBytes(position);
				Posting p = new Posting(doc_id, tftd);
				posting_list.add(p);
			}

		} catch (IOException ex) {
			System.out.println(ex);
		}

		return posting_list;
	}
	@Override
	public List<String> getVocabulary() {
		DB db = DBMaker.fileDB("file.db").readOnly().make();
		ConcurrentMap<String,Long> map= db.hashMap("map", Serializer.STRING, Serializer.LONG).open();
		List<String> vocab =  new ArrayList<String>();
		vocab.addAll(map.keySet());
		Collections.sort(vocab);
		return vocab;
	}

	@Override
	public List<String> getfirstNVocabulary(int N) {
		DB db = DBMaker.fileDB("file.db").readOnly().make();
		ConcurrentMap<String,Long> map= db.hashMap("map", Serializer.STRING, Serializer.LONG).open();
		List<String> vocab =  new ArrayList<String>();
		vocab.addAll(map.keySet());
		Collections.sort(vocab);
		List<String> firstNVocabulary = vocab.stream().limit(N).collect(Collectors.toList());
		return firstNVocabulary;
	}

	@Override
	public KGramIndex getKgramindex() {
		throw new UnsupportedOperationException("Sorry not implemented yet");
	}

	public double getAvgTftd(int docID) throws IOException {
		long weighPosition = (docID) * 8 * 4 + 24;
		return readAndReturnValue(weighPosition);
	}

	public double getByteSize(int docID) throws IOException {
		long weighPosition = (docID) * 8 * 4 + 16;
		return readAndReturnValue(weighPosition);
	}

	public double getDocWeightsLD(int docID) throws IOException {
		long weighPosition = docID * (8 * 4);
		return readAndReturnValue(weighPosition);
	}

	public double getDocLength(int docID) throws IOException {
		long weighPosition = (docID) * 8 * 4 + 8;
		return readAndReturnValue(weighPosition);
	}

	public double getDocLengthA() throws IOException {
		long length = docWeights.length() - 8;
		return readAndReturnValue(length);
	}

	public double readAndReturnValue(long weighPosition) throws IOException {
		docWeights.seek(weighPosition);
		byte[] byteBuffer = new byte[8];
		docWeights.read(byteBuffer, 0, byteBuffer.length);
		ByteBuffer wrapped = ByteBuffer.wrap(byteBuffer);
		return wrapped.getDouble();
	}

	public void setKGramDiskIndex( KGramDiskIndex kGramDiskIndex){this.kGramDiskIndex = kGramDiskIndex;}
	public KGramDiskIndex getkGramDiskIndex(){return kGramDiskIndex;}
}
