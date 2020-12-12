package cecs429.documents;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;

public class JsonFileDocument implements FileDocument {
    private int mDocumentId;
    private Path mFilePath;
    private String tittle;
    private String titteOfDocuement;
    private static double size;

    public JsonFileDocument(int id, Path absoluteFilePath) {
        mDocumentId = id;
        mFilePath = absoluteFilePath;
    }

    @Override
    public Path getFilePath() {
        return mFilePath;
    }

    @Override
    public int getId() {
        return mDocumentId;
    }

    @Override
    public Reader getContent() {
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader(mFilePath.toString()));
            JSONObject jsonObject = (JSONObject) obj;
            String body = (String) jsonObject.get("body");
            titteOfDocuement = (String) jsonObject.get("title");
            return new StringReader(body);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void setSize(double size_d) {
        size = size_d;
    }

    @Override
    public double getSize() {
        return size;
    }

    @Override
    public String getTitle() {
        return mFilePath.getFileName().toString();
    }

    public String gettitteOfDocuement() {
        return titteOfDocuement;
    }

    public static FileDocument loadJsonFileDocument(Path absolutePath, int documentId) {
        setSize(absolutePath.toFile().length());
        return new JsonFileDocument(documentId, absolutePath);
    }
}
