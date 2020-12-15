package edu.csulb;

import cecs429.QueryFoundations_Java.cecs429.query.BooleanQueryParser;
import cecs429.QueryFoundations_Java.cecs429.query.Query;
import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.*;
import cecs429.text.AdvanceTokenProcessor;
import cecs429.text.EnglishTokenStream;
import org.tartarus.snowball.ext.PorterStemmer;
import rankedretrieval.*;
import rankedretrieval.evaluation.VariantRankingFormulasEvaluation;
import rankedretrieval.evaluation.VocabularyEliminationEvaluation;
import utility.ApplicationLogger;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;

public class DiskApplication {
	private static ApplicationLogger LoggerObj = ApplicationLogger.getInstance();
	private static SpellingCorrection SpellingCorrectionObj = new SpellingCorrection();
	private static java.nio.file.Path path = null;
	private static DirectoryCorpus corpus = null;
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static void main(String[] args) throws IOException {
		Scanner sc = new Scanner(System.in);
		String typeOfOperation = "1";
		while(!typeOfOperation.equals("4")){
			System.out.println(ANSI_BLUE+"***** MENU *****"+ANSI_RESET);
			System.out.println(ANSI_BLUE+"Select 1 for creating the index"+ANSI_RESET);
			System.out.println(ANSI_BLUE+"Select 2 for executing query"+ANSI_RESET);
			System.out.println(ANSI_BLUE+"Select 3 for Precision-Recall Evaluation"+ANSI_RESET);
			System.out.println(ANSI_BLUE+"Select 4 for quit"+ANSI_RESET);
			typeOfOperation = sc.nextLine();
			switch(typeOfOperation){
				case "1":
					buildIndex(sc);
					break;
				case "2":
					executeQuery(sc);
					break;
				case "3":
					evaluateSearchEngine(sc);
				case "4":
					System.out.println("exiting Application!\n");
					break;
				default:
					System.out.println("Select correct option");
					break;
			}
		}
	}

	public static void buildIndex(Scanner sc){
		System.out.println("Enter path");
		String filePath = sc.nextLine();
		path = Paths.get(filePath);
		corpus = new DirectoryCorpus(path);
		corpus.loadTextFile(".txt");
		corpus.loadJsonDirectory(".json");

		DiskPositionalIndex index = (DiskPositionalIndex) indexCorpus(path,corpus);
		System.out.println("Indexing done");
		executeQuery(index, sc);
	}

	public static void executeQuery(Scanner sc){
		System.out.println("Enter path");
		String filePath = sc.nextLine();
		path = Paths.get(filePath);
		DiskPositionalIndex index = new DiskPositionalIndex(filePath);
		KGramDiskIndex kGramDiskIndex=new KGramDiskIndex(filePath);
		index.setKGramDiskIndex(kGramDiskIndex);
		executeQuery(index, sc);
	}

	public static void evaluateSearchEngine(Scanner sc) throws IOException {
		System.out.println("Enter test corpus path:");
		String corpusPath = sc.nextLine();
		java.nio.file.Path path = Paths.get(corpusPath);
		DirectoryCorpus corpus = new DirectoryCorpus(path);
		corpus.loadTextFile(".txt");
		corpus.loadJsonDirectory(".json");
		DiskPositionalIndex index = (DiskPositionalIndex) indexCorpus(path,corpus);
		System.out.println("Indexing done");
		System.out.println("Corpus Size: "+ corpus.getCorpusSize());

		String typeOfEvaluation = "1";
		while (!typeOfEvaluation.equals("3")) {
				System.out.println(ANSI_CYAN+"***** EVALUATION MENU *****"+ANSI_RESET);
				System.out.println(ANSI_CYAN+"Select 1 for Variant ranking formulas"+ANSI_RESET);
				System.out.println(ANSI_CYAN+"Select 2 for Vocabulary elimination"+ANSI_RESET);
				System.out.println(ANSI_CYAN+"Select 3 for quit\n"+ANSI_RESET );
				typeOfEvaluation = sc.nextLine();
				switch (typeOfEvaluation){
					case "1":
						VariantRankingFormulasEvaluation variantRankingFormulasEvaluationObj = new VariantRankingFormulasEvaluation(corpusPath,index);
						String option = "1";
						while (!option.equals("3")) {
							System.out.println(ANSI_YELLOW+"***** VARIANT RANKING FORMULA MENU *****"+ANSI_RESET);
							System.out.println(ANSI_YELLOW+"Select 1 to execute all 225 queries"+ANSI_RESET);
							System.out.println(ANSI_YELLOW+"Select 2 to execute first query"+ANSI_RESET);
							System.out.println(ANSI_YELLOW+"Select 3 for quit\n"+ANSI_RESET);
							option = sc.nextLine();
							switch (option){
								case "1":
									variantRankingFormulasEvaluationObj.executeVariantRankingFormulasEvaluationALlQueries();
									break;
								case "2":
									variantRankingFormulasEvaluationObj.executeVariantRankingFormulasEvaluationFirstQuery();
									break;
								case "3":
									System.out.println("Exiting variant rankin formula\n");
									break;
								default:
									System.out.println("Select correct option");
									break;
							}
						}
						break;
					case "2":
						VocabularyEliminationEvaluation vocabularyEliminationEvaluationObj = new VocabularyEliminationEvaluation(corpusPath,index, 1.4f);
						String option2 = "1";
						while (!option2.equals("3")) {
							System.out.println(ANSI_YELLOW+"***** VOCABULARY ELIMINATION MENU *****"+ANSI_RESET);
							System.out.println(ANSI_YELLOW+"Select 1 to execute all 225 queries"+ANSI_RESET);
							System.out.println(ANSI_YELLOW+"Select 2 to execute first query"+ANSI_RESET);
							System.out.println(ANSI_YELLOW+"Select 3 for quit\n"+ANSI_RESET);
							option2 = sc.nextLine();
							switch (option2){
								case "1":
									vocabularyEliminationEvaluationObj.executeVocabularyEliminationEvaluationALlQueries();
									break;
								case "2":
									vocabularyEliminationEvaluationObj.setWQTThreshold(1.0f);
									vocabularyEliminationEvaluationObj.executeVocabularyEliminationEvaluationFirstQuery();
									vocabularyEliminationEvaluationObj.setWQTThreshold(1.1f);
									vocabularyEliminationEvaluationObj.executeVocabularyEliminationEvaluationFirstQuery();
									vocabularyEliminationEvaluationObj.setWQTThreshold(1.2f);
									vocabularyEliminationEvaluationObj.executeVocabularyEliminationEvaluationFirstQuery();
									vocabularyEliminationEvaluationObj.setWQTThreshold(1.3f);
									vocabularyEliminationEvaluationObj.executeVocabularyEliminationEvaluationFirstQuery();
									vocabularyEliminationEvaluationObj.setWQTThreshold(1.4f);
									vocabularyEliminationEvaluationObj.executeVocabularyEliminationEvaluationFirstQuery();
									vocabularyEliminationEvaluationObj.setWQTThreshold(1.5f);
									vocabularyEliminationEvaluationObj.executeVocabularyEliminationEvaluationFirstQuery();
									vocabularyEliminationEvaluationObj.setWQTThreshold(1.7f);
									vocabularyEliminationEvaluationObj.executeVocabularyEliminationEvaluationFirstQuery();
									vocabularyEliminationEvaluationObj.setWQTThreshold(2.0f);
									vocabularyEliminationEvaluationObj.executeVocabularyEliminationEvaluationFirstQuery();
									vocabularyEliminationEvaluationObj.setWQTThreshold(2.5f);
									vocabularyEliminationEvaluationObj.executeVocabularyEliminationEvaluationFirstQuery();
									break;
								case "3":
									System.out.println("Exiting vocabulary elimination\n");
									break;
								default:
									System.out.println("Select correct option");
									break;
							}
						}
						break;
					case "3":
						System.out.println("Exiting Search Engine evaluation\n");
						break;
					default:
						System.out.println("Select correct option");
						break;
				}
		}
	}

	public static void executeQuery(DiskPositionalIndex index, Scanner sc){
		String typeOfParser = "1";
		while (!typeOfParser.equals("3")) {
			System.out.println(ANSI_CYAN+"***** QUERY EXECUTION MENU *****"+ANSI_RESET);
			System.out.println(ANSI_CYAN+"Select 1 for Booelan Query Parser"+ANSI_RESET);
			System.out.println(ANSI_CYAN+"Select 2 for Ranked Query Parser"+ANSI_RESET);
			System.out.println(ANSI_CYAN+"Select 3 for quit\n"+ANSI_RESET);
			typeOfParser = sc.nextLine();
			switch(typeOfParser){
				case "1":
					booleanParser(index,sc);
					break;
				case "2":
					rankedQueryParser(index);
					break;
				case "3":
					System.out.println("exiting query execution!\n");
					break;
				default:
					System.out.println("Select correct option");
					break;
			}
		}
	}
	public static void booleanParser(DiskPositionalIndex index, Scanner sc){
		System.out.println("Enter Query");
		String query = sc.nextLine();
		if (query.equals(":q")) {
			specialQueryParser(index, query,sc);
		} else if (query.charAt(0) == ':') {
			specialQueryParser(index, query,sc);
		} else {
			BooleanQueryParser b = new BooleanQueryParser();
			Query q = b.parseQuery(query);
			if (q.getPostings(index) == null) {
				System.out.println("No results found");
				String spellCorrectedQuery = SpellingCorrectionObj.spellingCorrection(index,query, 1.4f);
				if (spellCorrectedQuery != null) {
					Query q1 = b.parseQuery(spellCorrectedQuery);
					System.out.println("Spell Corrected Query: " + spellCorrectedQuery);
					int count = 0;
					for (Posting p : q1.getPostings(index)) {
						System.out.println("Document " + corpus.getDocument(p.getDocumentId()).getTitle() + " id:"
								+ p.getDocumentId());
						count++;
					}
					System.out.println("count from disk : " + count);
				}
			} else {
				int count = 0;
				for (Posting p : q.getPostings(index)) {
					System.out.println("Document " + corpus.getDocument(p.getDocumentId()).getTitle() + " id:"
							+p.getDocumentId()+": "+corpus.getDocument(p.getDocumentId()).gettitteOfDocuement());
					count++;
				}
				System.out.println("count from disk : " + count+"\n");
			}
		}

	}

	public static void rankedQueryParser(DiskPositionalIndex index){
		String result = null;
		RetrievalStrategy retrievalStrategy = null;
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter Query");
		String query = sc.nextLine();
		System.out.println("Enter retrieval strategy");
		System.out.println("Enter 1 for Default \nEnter 2 for Okapi \nEnter 3 for Wacky \nEnter 4 for Tf_tdf");
		String retrieval=sc.nextLine();

		switch(retrieval) {
			case "1":
				retrievalStrategy = new DefaultRankedRetrieval(index);
				break;
			case "2":
				retrievalStrategy=new Okapi(index);
				break;
			case "3":
				retrievalStrategy=new Wacky(index);
				break;
			case "4":
				retrievalStrategy=new Tf_tdf(index);
				break;
		}
		List<PostingAccumulator> rankedRetrievalPostings=new ArrayList<>();
		RankedRetrieval rankedRetrieval = new RankedRetrieval(query, path.toString(),(double) corpus.getCorpusSize());
		AdvanceTokenProcessor processor = new AdvanceTokenProcessor();
		rankedRetrieval.setNumberOfDocuments(50);
		try {
			if(rankedRetrieval.checkForTerms( index,  processor)){
				String newQuery = rankedRetrieval.getSpellCorrectedQuery(index,processor);
				System.out.println("Do you want to search for: " + newQuery);
				System.out.println("Enter 1 to rank corrected query: ");
				System.out.println("Enter 2 to rank old query: ");
				String queryOption=sc.nextLine();
				switch(queryOption){
					case "1":
						RankedRetrieval rankedRetrievalNew = new RankedRetrieval(newQuery, path.toString(), corpus.getCorpusSize());
						rankedRetrievalNew.setNumberOfDocuments(50);
						rankedRetrievalPostings = rankedRetrievalNew.getPostings(index, processor, retrievalStrategy);
						printRankResults(rankedRetrievalPostings,newQuery);
						break;
					case "2":
						rankedRetrievalPostings = rankedRetrieval.getPostings(index, processor, retrievalStrategy);
						printRankResults(rankedRetrievalPostings,query);
						break;
					default:
						System.out.println("Selected option 2 by default");
						rankedRetrievalPostings = rankedRetrieval.getPostings(index, processor, retrievalStrategy);
						printRankResults(rankedRetrievalPostings,query);
						break;
				}
			}
			else{
				rankedRetrievalPostings = rankedRetrieval.getPostings(index, processor, retrievalStrategy);
				printRankResults(rankedRetrievalPostings,query);
			}
		} catch (IOException ex) {
			LoggerObj.insterLog("DiskApplication.rankedQueryParser: "+ ex.toString(), Level.WARNING);
		}
	}

	public static void printRankResults(List<PostingAccumulator> rankedRetrievalPostings,String query){
		String result = null;
		int j = 0;
		for (PostingAccumulator p : rankedRetrievalPostings) {
			j++;
			Posting posting = p.getPosting();

			String s = corpus.getDocument(posting.getDocumentId()).getTitle() +corpus.getDocument(posting.getDocumentId()).gettitteOfDocuement()+" Accum value - " + p.getAccumulator();
			result += s +"\n";
			System.out.println(j + ")" + corpus.getDocument(posting.getDocumentId()).getTitle()  +
					": "+corpus.getDocument(posting.getDocumentId()).gettitteOfDocuement()+" Accum value - " + p.getAccumulator());
		}
		if (j == 0) {
			System.out.println("No Match Found for query \"" + query + "\".\n");
		}
	}

	public static void specialQueryParser(DiskPositionalIndex index,String query,Scanner sc) {
		List<String> result = new ArrayList<>();
		if (query.equals(":q")) {
			index = null;
			corpus = null;
			path = null;
			System.out.println("Exiting Application!");
		} else if (query.equals(":vocab")) {
			if (index == null) {
				String res = "index does not contain any data!";
				System.out.println(res);
			} else {
				result.addAll(index.getVocabulary());
				int count = 0;
				for (String s : result){
					System.out.print(s + "\t");
					count = count + 1;
				}
				System.out.println("\nTotal Vocab terms: "+ count);
			}
		} else {
			String[] queryTerms = query.split(" ");
			if (queryTerms[0].equals(":stem")) {
				PorterStemmer stemmer = new PorterStemmer();
				stemmer.setCurrent(queryTerms[1]);
				stemmer.stem();
				String res = stemmer.getCurrent();
				System.out.println(res);
			} else if (queryTerms[0].equals(":index")) {
				buildIndex(sc);
				String res = "Index created for selected directory!";
				System.out.println(res);
			}
		}

	}

	public static Index indexCorpus(Path path, DocumentCorpus corpus){

		InvertedIndex invertedIndexObj = new InvertedIndex();
		KGramIndex kgm = new KGramIndex();
		DiskPositionalIndex diskPositionalIndex = null;
		KGramDiskIndex kGramDiskIndex=null;

		List<Double> ListOfLd = new ArrayList<Double>();
		List<Double> docLength = new ArrayList<>();
		List<Double> avgTermFrequency=new ArrayList<>();
		List<Double> docByteSize = new ArrayList<Double>();
		for (Document documement : corpus.getDocuments()) {
			int pos = 0;
			HashMap<String, Integer> termFrequencyMap = new HashMap<>();;
			EnglishTokenStream tokenStream = new EnglishTokenStream(documement.getContent());
			for (String token : tokenStream.getTokens()) {
				AdvanceTokenProcessor processor = new AdvanceTokenProcessor();
				List<String> res = processor.processToken(token);
				Set<String> setobj = new HashSet<>(res);
				for (String term : setobj) {
					invertedIndexObj.addTerm(term, documement.getId(), pos);
					kgm.addTerm(term);
						if (!termFrequencyMap.containsKey(term))
							termFrequencyMap.put(term, 1);
						else {
							termFrequencyMap.put(term, (termFrequencyMap.get(term) + 1));
						}
				}

				pos = pos + 1;
			}
			docLength.add((double)pos);
			docByteSize.add(documement.getSize());
			Iterator termFrequencyMapItr = termFrequencyMap.entrySet().iterator();
			double w_td_square = 0;
			double averageTermFrequency = 0;
			while (termFrequencyMapItr.hasNext()) {
				Map.Entry mapElement = (Map.Entry)termFrequencyMapItr.next();
				int tf_td = (int)mapElement.getValue();
				averageTermFrequency+=(int)mapElement.getValue();
				double w_td = 1 + Math.log(tf_td);
				w_td_square = w_td_square + (w_td * w_td);
			}
			ListOfLd.add(Math.sqrt(w_td_square));
			avgTermFrequency.add(averageTermFrequency/termFrequencyMap.keySet().size());
		}
		invertedIndexObj.setKgramindex(kgm);
		DiskIndexWriter diskIndexWriterObj = new DiskIndexWriter();
		try {
			LoggerObj.insterLog("DiskApplication.indexCorpus: size-"+docByteSize.size(), Level.INFO);
			diskIndexWriterObj.writeDocumentWeight(ListOfLd, path,avgTermFrequency,docLength,docByteSize);
			LoggerObj.insterLog("DiskApplication.indexCorpus: Writing on disk", Level.INFO);
			diskIndexWriterObj.writeIndex(invertedIndexObj, path);
			LoggerObj.insterLog("DiskApplication.indexCorpus: Index on disk created", Level.INFO);
			diskPositionalIndex = new DiskPositionalIndex(path.toString());

			//write Kgrams index to the disk
			LoggerObj.insterLog("DiskApplication.indexCorpus: Writing Kgram to disk", Level.INFO);
			kgm.writeIndex(path);
			LoggerObj.insterLog("DiskApplication.indexCorpus: Kgram on disk done", Level.INFO);
			invertedIndexObj.setKgramindex(kgm);
			kGramDiskIndex=new KGramDiskIndex(path.toString());
			diskPositionalIndex.setKGramDiskIndex(kGramDiskIndex);
		} catch (IOException e) {
			LoggerObj.insterLog("DiskApplication.indexCorpus: "+ e.toString(), Level.WARNING);
		}
		return diskPositionalIndex;
	}


}