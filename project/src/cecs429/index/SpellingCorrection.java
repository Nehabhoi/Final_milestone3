package cecs429.index;

import utility.ApplicationLogger;
import utility.Ngrams;

import java.util.*;
import java.util.logging.Level;

public class SpellingCorrection {
    private ApplicationLogger LoggerObj = ApplicationLogger.getInstance();
    private DiskPositionalIndex indexObj = null;
    private void setDiskPositionalIndex(DiskPositionalIndex index){
        indexObj =index;
    }
    public String spellingCorrection(DiskPositionalIndex index, String query, float tough) {
        this.setDiskPositionalIndex(index);
        KGramDiskIndex  kGramDiskIndex = indexObj.getkGramDiskIndex();
        String termWithDoller = '$' + query + '$';
        TreeSet<String> listOfTerms = new TreeSet<String>();
        Set<String> queryGrams = Ngrams.ngrams(3, termWithDoller);

        // get all terms in kgram index which has one or two grams from query
        for (String gram : queryGrams) {
            List<String> wordsList =kGramDiskIndex.getWordList(gram);
            if(wordsList!=null && wordsList.size() > 0 ){
                listOfTerms.addAll(wordsList);
            }
			/*if (this.kgramindex.map.containsKey(gram)) {
				listOfTerms.addAll(this.kgramindex.map.get(gram));
			}*/
        }

        LoggerObj.insterLog("InvertedIndex.spellingCorrection: kgrams "+listOfTerms, Level.INFO);

        // get terms which has value of jaccavd greater than or equal to tough
        TreeSet<String> listOfActualTerm = new TreeSet<String>();
        for (String term : listOfTerms) {
            Set<String> termGrams = Ngrams.ngrams(3, '$' + term + '$');
            float val = calculateJaccavd(queryGrams, termGrams);
            LoggerObj.insterLog("InvertedIndex.spellingCorrection: "+term + ":jaccavd val:  "+val, Level.INFO);
            if (val >= tough) {
                listOfActualTerm.add(term);
            }
        }
        LoggerObj.insterLog("InvertedIndex.spellingCorrection: terms witch are greater than threshold: "+ listOfActualTerm, Level.INFO);

        // if only one term return that as correct spelled term
        if (listOfActualTerm.size() == 0) {
            LoggerObj.insterLog("InvertedIndex.spellingCorrection:  Jaccavd value is below threshold", Level.WARNING);
            return null;
        }else if (listOfActualTerm.size() == 1) {
            return listOfActualTerm.pollFirst();
        } else {
            // if more than one term find edit distance and return the term which has
            // minimum edit distance
            return termWithMinEditDistance(query, listOfActualTerm);
        }
    }

    private String termWithMinEditDistance(String query, TreeSet<String> listOfActualTerm) {

        // find edit distance for each term
        HashMap<String, Integer> termEditDistanceMap = new HashMap<String, Integer>();
        for (String termInList : listOfActualTerm) {
            // calculate edit distance
            int editDistance = calculateEditDistance(query, termInList);
            LoggerObj.insterLog("InvertedIndex.termWithMinEditDistance: "+ termInList + " : "+editDistance, Level.INFO);
            termEditDistanceMap.put(termInList, editDistance);
        }

        // sort based on edit distance
        HashMap<String, Integer> termEditDistanceMapSorted = sortByValue(termEditDistanceMap);

        // get minimum edit distance term
        List<String> listOFCorrectSpelledTerms = new ArrayList<String>();
        int min = termEditDistanceMapSorted.entrySet().iterator().next().getValue();
        Iterator<Map.Entry<String, Integer>> itr = termEditDistanceMapSorted.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, Integer> entry = itr.next();
            if (entry.getValue() == min) {
                listOFCorrectSpelledTerms.add(entry.getKey());
            } else {
                break;
            }
        }

        if (listOFCorrectSpelledTerms.size() == 1) {
            return listOFCorrectSpelledTerms.get(0);
        } else {
            // find the most frequent term between all the term
            LoggerObj.insterLog("InvertedIndex.termWithMinEditDistance: terms with same edit distance: "+listOFCorrectSpelledTerms, Level.INFO);
            return findMostFrequentTerm(listOFCorrectSpelledTerms);
        }

    }

    private String findMostFrequentTerm(List<String> listOfTerms) {
        //create hasmap with key as document frequency and value as a list of string
        TreeMap<Integer,List<String>> tf_term_map  = new TreeMap<>();
        for(String term:listOfTerms) {
            int documentFrequency = this.indexObj.getPostings(term).size();
            if(!tf_term_map.containsKey(documentFrequency)) {
                tf_term_map.put(documentFrequency, new ArrayList<String>());
            }
            List<String> termsList = tf_term_map.get(documentFrequency);
            termsList.add(term);
            tf_term_map.put(documentFrequency, termsList);
        }

        //get the least key's value
        LoggerObj.insterLog("InvertedIndex.findMostFrequentTerm: term with document Frequency:\n"+ tf_term_map, Level.INFO);
        List<String> mostFrequentTerms = tf_term_map.get(tf_term_map.lastKey());

        Collections.sort(mostFrequentTerms);
        LoggerObj.insterLog("InvertedIndex.findMostFrequentTerm: sorted terms: "+ mostFrequentTerms , Level.INFO);

        // return first term
        return mostFrequentTerms.get(0);
    }

    private int calculateEditDistance(String query, String term) {
        return EditDistance.editDistDP(query, term, query.length(), term.length());
    }

    private float calculateJaccavd(Set<String> queryGrams, Set<String> termGrams) {
        // get number of grams in query
        int NumberOfGramsInQuery = queryGrams.size();
        LoggerObj.insterLog("InvertedIndex.calculateJaccavd: query kgram: "+ queryGrams, Level.INFO);
        Set<String> queryGramsTemp = new TreeSet<String>();
        queryGramsTemp.addAll(queryGrams);

        // get number of grams in term
        int NumberOfGramsInterm = termGrams.size();
        LoggerObj.insterLog("InvertedIndex.calculateJaccavd: term kgram: "+termGrams , Level.INFO);

        // intersection of grams in both term
        queryGramsTemp.retainAll(termGrams);
        int intersection = queryGramsTemp.size();
        LoggerObj.insterLog("InvertedIndex.calculateJaccavd: intersection:"+ queryGramsTemp , Level.INFO);

        // Union of grams in both term
        int union = NumberOfGramsInQuery + NumberOfGramsInterm - intersection;

        return ((float) intersection / (float) union);
    }

    // function to sort hashmap by values
    private static HashMap<String, Integer> sortByValue(HashMap<String, Integer> hm) {
        // Create a list from elements of HashMap
        List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(hm.entrySet());

        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        // put data from sorted list to hashmap
        HashMap<String, Integer> temp = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }
}
