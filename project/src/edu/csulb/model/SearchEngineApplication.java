package edu.csulb.model;

import java.io.File;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.tartarus.snowball.ext.EnglishStemmer;
import org.tartarus.snowball.ext.PorterStemmer;
import java.io.FileReader;
import java.io.StringReader;
import com.google.gson.Gson;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import cecs429.QueryFoundations_Java.cecs429.query.BooleanQueryParser;
import cecs429.QueryFoundations_Java.cecs429.query.Query;
import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.Index;
import cecs429.index.InvertedIndex;
import cecs429.index.KGramIndex;
import cecs429.index.Posting;
import cecs429.text.AdvanceTokenProcessor;
import cecs429.text.EnglishTokenStream;
import cecs429.text.KGramProcessor;

public class SearchEngineApplication {

	private static SearchEngineApplication single_instance = null;
	private static InvertedIndex index = null;
	private static String directoryName = null;
	private static DirectoryCorpus corpus = null;
	private static boolean resetIndex = true;
	public static List<String> directory;

	private SearchEngineApplication() {
		Initialize();
		System.out.println("Initialize application!");
	}
	
	public static void Initialize() {
		
		directory = new ArrayList<String>();
		directory.add("data1");
		String filePath = "/Users/SuchitraMasters/Desktop/Data";
		java.nio.file.Path path = Paths.get(filePath);
		System.out.println(LocalDateTime.now());
		corpus = new DirectoryCorpus(path);
		corpus.loadTextFile(".txt");
		corpus.loadJsonDirectory(".json");
		index = (InvertedIndex) indexCorpus(corpus);
		System.out.println(LocalDateTime.now());
		resetIndex = false;
	}

	public static SearchEngineApplication getInstance() {
		if (single_instance == null)
			single_instance = new SearchEngineApplication();

		return single_instance;
	}

	public static boolean isSpecialQuery(String query) {
		if (query.startsWith(":", 0))
			return true;
		else
			return false;
	}

	public static SpecialQueryResponse specialQueryParser(String query) {
		List<String> result = new ArrayList<>();
		if (query.equals(":q")) {
			index = null;
			corpus = null;
			directoryName = null;
			resetIndex = true;
			String res = "resetting to null index!";
			result.add(res);
		} else if (query.equals(":vocab")) {
			if (index == null) {
				String res = "index does not contain any data!";
				result.add(res);
			} else {
				result.addAll(index.getfirstNVocabulary(1000));
			}
		} else {

			String[] queryTerms = query.split(" ");
			if (queryTerms[0].equals(":stem")) {
				PorterStemmer stemmer = new PorterStemmer();
				stemmer.setCurrent(queryTerms[1]);
				stemmer.stem();
				String res = stemmer.getCurrent();
				result.add(res);
			} else if (queryTerms[0].equals(":index")) {
				String filePath = "/Users/SuchitraMasters/Desktop/" + queryTerms[1];
				java.nio.file.Path path = Paths.get(filePath);
				DirectoryCorpus corpus = new DirectoryCorpus(path);
				corpus.loadTextFile(".txt");
				corpus.loadJsonDirectory(".json");
				directoryName = queryTerms[1];
				String res = "Index created for selected directory!";
				result.add(res);
			}
		}
		List<SpecialQueryModel> SpecialQueyResponseList = new ArrayList<SpecialQueryModel>();
		for (String element : result) {
			SpecialQueyResponseList.add(new SpecialQueryModel(element));
		}
		SpecialQueryResponse specialQueryResponse = new SpecialQueryResponse(SpecialQueyResponseList);
		return specialQueryResponse;
	}

	public static DirectoryListResponse getDirectoryList() {
		List<DirectoryListModel> directoryResponse = new ArrayList<DirectoryListModel>();
		Iterator<String> iterable = directory.iterator();
		while (iterable.hasNext()) {
			String dirName = iterable.next();
			// generate name value for frontend response
			directoryResponse.add(new DirectoryListModel(dirName, dirName));
		}
		DirectoryListResponse directoryListResponseObj = new DirectoryListResponse(directoryResponse);
		return directoryListResponseObj;
	}

	public static SearchResponse getQueryList(String dir, String query) {
		List<SearchResponseModel> searchResponse = new ArrayList<SearchResponseModel>();
		if(resetIndex) {
			directory = new ArrayList<String>();
			String filePath ="/Users/SuchitraMasters/Desktop/" + dir;
			java.nio.file.Path path = Paths.get(filePath);
			corpus = new DirectoryCorpus(path);
			corpus.loadTextFile(".txt");
			corpus.loadJsonDirectory(".json");
			index = (InvertedIndex) indexCorpus(corpus);
			directoryName = dir;
			if(!directory.contains(dir))
				directory.add(dir);
		}	
		BooleanQueryParser parser = new BooleanQueryParser();
		Query q = parser.parseQuery(query);
		if (q.getPostings(index) == null)
			return null;
		for (Posting p : q.getPostings(index)) {
			System.out.println("Document " + corpus.getDocument(p.getDocumentId()).getTitle());
			Document documentObject = corpus.getDocument(p.getDocumentId());
			searchResponse.add(new SearchResponseModel(documentObject.getId(), documentObject.getTitle(),
					documentObject.gettitteOfDocuement()));
		}
		SearchResponse SearchResponseObj = new SearchResponse(searchResponse);
		return SearchResponseObj;

	}

	private static Index indexCorpus(DocumentCorpus corpus) {

		InvertedIndex invertedIndexObj = new InvertedIndex();
		KGramIndex kgm = new KGramIndex();
		for (Document documement : corpus.getDocuments()) {
			int pos = 0;
			EnglishTokenStream tokenStream = new EnglishTokenStream(documement.getContent());
			for (String token : tokenStream.getTokens()) {
				AdvanceTokenProcessor processor = new AdvanceTokenProcessor();
				List<String> res = processor.processToken(token);
				Set<String> setobj = new HashSet<>(res);
				for (String term : setobj) {
					invertedIndexObj.addTerm(term, documement.getId(), pos);
					kgm.addTerm(term);
				}
				pos = pos + 1;
			}

		}
		invertedIndexObj.setKgramindex(kgm);

		return invertedIndexObj;
	}



	public static ContentModel getDocumentContent(String documentName, String directoryName) {

		String DocumentPath = "/Users/SuchitraMasters/Desktop/"+ directoryName + "/"
				+ documentName;
		java.nio.file.Path path = Paths.get(DocumentPath);
		ContentModel contentModelOBJ = null;
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(new FileReader(path.toString()));

			JSONObject jsonObject = (JSONObject) obj;

			String body = (String) jsonObject.get("body");
			contentModelOBJ = new ContentModel(body);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return contentModelOBJ;
	}

}