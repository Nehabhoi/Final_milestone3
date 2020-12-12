package cecs429.index;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import utility.ApplicationLogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

public class KGramDiskIndex {
    private String path;
    private RandomAccessFile kgram;
    private static ApplicationLogger LoggerObj = ApplicationLogger.getInstance();

    public KGramDiskIndex(String path) {
        try {
            this.path = path;
            kgram = new RandomAccessFile(new File(path, "/index/kgram.bin"), "r");
        } catch (FileNotFoundException ex) {
            System.out.println(ex.toString());
        }
    }
    public List<String> getWordList(String term)
    {
        List<String> result=new ArrayList<>();
        DB db = DBMaker.fileDB("file1.db").readOnly().make();
        ConcurrentMap<String,Long> diskmap= db.hashMap("diskmap", Serializer.STRING, Serializer.LONG).open();

        if(!diskmap.containsKey(term)) return null;
        long term_position = (long) diskmap.get(term);
        db.close();
        if(term_position==-1) return result;
        try{
            kgram.seek(term_position);
            int legnth_of_words= kgram.readInt();
            for(int i=0;i<legnth_of_words;i++)
            {
                int length_eachofword= kgram.readInt();
                byte[] buffer_of_words=new byte[length_eachofword];
                kgram.read(buffer_of_words, 0, buffer_of_words.length);
                String word=new String(buffer_of_words, StandardCharsets.UTF_8);
                result.add(word);
            }
        }
        catch(Exception e)
        {
            LoggerObj.insterLog("KGramDiskIndex.getWordList: "+ e.toString(), Level.WARNING);
        }

        return result;
    }
}
