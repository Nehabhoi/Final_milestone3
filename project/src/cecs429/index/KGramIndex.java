package cecs429.index;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

public class KGramIndex {
	public HashMap<String,TreeSet<String>> map;
	public KGramIndex()
	{
		map=new HashMap<>();
	}
	// will return all words containing that K-gram
	public List<String> getListOfWords(String key)
	{
		if(map.containsKey(key))
		{
			List<String> li=new ArrayList<>();
			li.addAll(map.get(key));
			return li;
		}
		else return new ArrayList<>();
	}
	// add the term to map
	public void addTerm(String term) {
		String termWithDoller = '$' + term + '$';
		Set<String> ngramList = new TreeSet<String>(); 
		ngramList.addAll(ngrams(3, termWithDoller));
		ngramList.addAll(ngrams(2, termWithDoller));
		ngramList.addAll(ngrams(1, term));
		for(String gram: ngramList) {
			if(!map.containsKey(gram)) map.put(gram,new TreeSet<String>());
			TreeSet<String> li=map.get(gram);
			li.add(term);
			map.put(gram, li);
		}

	}
	public void writeIndex(Path corpusPath) throws IOException {
		File yourFile = new File(corpusPath+"/index/kgram.bin");
		yourFile.getParentFile().mkdirs();
		yourFile.createNewFile();
		RandomAccessFile writer = new RandomAccessFile(yourFile, "rw");
		DB db = DBMaker.fileDB("file1.db").make();
		ConcurrentMap<String,Long> diskmap = db.hashMap("diskmap", Serializer.STRING, Serializer.LONG).createOrOpen();

		long vocab_position=0;
		for(String key: map.keySet())
		{
			Set<String> listOfwords=map.get(key);
			vocab_position = writer.getFilePointer();
			diskmap.put(key,vocab_position);
			int length_listOfwords=listOfwords.size();
			writer.writeInt(length_listOfwords);
			for(String str:listOfwords)
			{
				byte arr[] = str.getBytes("UTF-8");
				int lengthofeachword=arr.length;
				writer.writeInt(lengthofeachword);
				writer.write(arr,0,arr.length);
			}
		}
		db.close();
		writer.close();

	}

	public Set<String> ngrams(int n, String str) {
		Set<String> ngrams = new TreeSet<String>(); 
	    for (int i = 0; i < str.length() - n + 1; i++)
	        ngrams.add(str.substring(i, i + n));
	    return ngrams;
	  }
}
