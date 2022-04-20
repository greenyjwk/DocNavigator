package com.company;

import java.io.*;
import java.util.*;

/**
 * ISTE-612-Group1 Project
 * April 20 2022
 */

public class InvertedIndex {
    ArrayList<String> termDictionary;
    HashMap<String, ArrayList<Doc>> docLists;
    File filesList[];
    HashMap<Integer, String> fileNames;
    HashSet<String> stopwordsList;

    //for inverted Index
    private HashMap<String, LinkedHashSet<Doc>> termList;

    /**
     * Construct a positional index
     *
     * @param path List of input strings or file names
     */
    public InvertedIndex(String path) {
        this.termList = new HashMap<>();

        File directoryPath = new File(path);

        //List of all files and directories
        filesList = directoryPath.listFiles();

        File stopwordsPath = new File("stopwords");

        // Stop List Process
        File stopFilesList[] = stopwordsPath.listFiles();

        fileNames = new HashMap<>();
        stopwordsList = stopListCreater(stopFilesList[0]);
        termDictionary = new ArrayList<>();
        docLists = new HashMap<>();

        for (int i = 0; i < filesList.length; i++) {
            int docId = i;
//            System.out.println(filesList[i].getName());
            // ********* Read single file *********
            String singleDoc = new String();
            try (BufferedReader br = new BufferedReader(new FileReader(filesList[i]))) {
                String line;
                while ((line = br.readLine()) != null) singleDoc += line;
            } catch (IOException e) {
                e.printStackTrace();
            }
            // ********* Read single file *********

            // ********* Tokenizing *********
            ArrayList<String> tokenList = tokenizer(singleDoc);
            // ********* Tokenizing *********


            // ********* Removing stop words *********
            for (int j = 0; j < tokenList.size(); j++)
                if (this.stopwordsList.contains(tokenList.get(j))) tokenList.remove(j);
            // ********* Removing stop words *********


            // ********* converting lower case *********
            ArrayList<String> lowercaseList = new ArrayList<>();
            for (int j = 0; j < tokenList.size(); j++) lowercaseList.add(tokenList.get(j).toLowerCase());
            // ********* converting lower case *********


            // ********* Porter's Stemmer *********
            ArrayList<String> tokensAfterStemmed = PortersStemmer(lowercaseList);
            // ********* Porter's Stemmer *********


            // Removing nulls
            while (tokensAfterStemmed.remove(null)) {
            }
            // Removing nulls


            // Building invereted index
            for (int tokenIndex = 0; tokenIndex < tokensAfterStemmed.size(); tokenIndex++) {
                String word = tokensAfterStemmed.get(tokenIndex);
                if (!termList.containsKey(word)) {
                    LinkedHashSet<Doc> docSet = new LinkedHashSet<>();
                    Doc doc = new Doc(docId);
                    docSet.add(doc);
                    termList.put(word, docSet);
                } else {
                    LinkedHashSet<Doc> set = termList.get(word);

                    Doc docTemp = new Doc(docId);
                    set.add(docTemp);
                    termList.put(word, set); // Adding a doc from the list
                }
            }
            fileNames.put(docId, filesList[docId].getName());
        }
    }


    /**
     * Return string representation of a positional index
     */
    public String toString() {
        String matrixString = new String();
        ArrayList<Doc> docList;
        for (int i = 0; i < termDictionary.size(); i++) {
            matrixString += String.format("%-15s", termDictionary.get(i));
            docList = docLists.get(i);
            for (int j = 0; j < docList.size(); j++) matrixString += docList.get(j) + "\t";
            matrixString += "\n";
        }
        return matrixString;
    }


    /**
     * Adopts the Porter Stemmer
     *
     * @param tokenList Strings of the single document
     */
    public ArrayList<String> PortersStemmer(ArrayList<String> tokenList) {
        Stemmer stemmer = new Stemmer();
        ArrayList<String> tokensAfterStemmed = new ArrayList<>();
        for (String token : tokenList) {
            char[] charArray = token.toCharArray();
            stemmer.add(charArray, token.length());
            stemmer.stem();
            String temp = stemmer.toString();
            tokensAfterStemmed.add(temp);
        }
        return tokensAfterStemmed;
    }
    /** Gets the strings that has been through porter's stemmer.
     * @return A string representing the employee’s last name.
     */


    /**
     * Tokenizes the single document into array of tokenization
     *
     * @param doc file that contains stop words
     */
    public static ArrayList<String> tokenizer(String doc) {
        StringTokenizer st1 = new StringTokenizer(doc, " ,.:;?![]()'%$#!/+-*\"\'");
        ArrayList<String> tokens = new ArrayList<>();
        while (st1.hasMoreTokens()) tokens.add(st1.nextToken());
        return tokens;
    }

    /**
     * Creates a list that the stop words
     *
     * @param stopwordsFile file that contains stop words
     */
    public HashSet<String> stopListCreater(File stopwordsFile) {
        HashSet<String> stopList = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(stopwordsFile))) {
            String line;
            while ((line = br.readLine()) != null) stopList.add(line);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stopList;
    }


    /**
     * Multiple keywords search with condition And, print out the document IDs that contain the serach keyword
     *
     * @param queryParam Two words that will be searched
     */
    public ArrayList<Doc> Search(String[] queryParam) {

        ArrayList<String> queryUpdated = new ArrayList<>();
        for (int i = 0; i < queryParam.length; i++) queryUpdated.add(queryParam[i].toLowerCase());
        ArrayList<String> AfterStemmed = PortersStemmer(queryUpdated);
        String queryKeywords[] = AfterStemmed.toArray(new String[AfterStemmed.size()]);

        HashSet<Integer> set = new HashSet<>();
        boolean check = true;
        for (String keyword : queryKeywords) {
            if (termList.containsKey(keyword)) {
                if (check) {
                    check = false;
                    (termList.get(keyword)).forEach(Doc -> {
                        set.add(Doc.docId);
                    });
                } else {
                    HashSet<Integer> set2 = new HashSet<>();
                    (termList.get(keyword)).forEach(Doc -> {
                        set2.add(Doc.docId);
                    });
                    set.retainAll(set2);
                }
            } else {
                System.out.println("Missing keyword\n\n");
                return new ArrayList<>();
            }
        }

        ArrayList<Doc> searchResult = new ArrayList<>();
        for (int docId : set) searchResult.add(new Doc(docId));

        //Need to check
        return searchResult;
    }
}

/**
 * Document class that contains the document id and the position list
 */
class Doc {
    public int docId;
    ArrayList<Integer> positionList;

    public Doc(int did) {
        docId = did;
        positionList = new ArrayList<Integer>();
    }

    public Doc(int did, int position) {
        docId = did;
        positionList = new ArrayList<Integer>();
        positionList.add(new Integer(position));
    }

    public void insertPosition(int position) {
        positionList.add(new Integer(position));
    }

    public int getID() {
        return this.docId;
    }

    public String toString() {
        String docIdString = "" + docId + ":<";
        for (Integer pos : positionList)
            docIdString += pos + ",";
        docIdString = docIdString.substring(0, docIdString.length() - 1) + ">";
        return docIdString;
    }
}

class docListComp implements Comparator<ArrayList<Integer>> {
    @Override
    public int compare(ArrayList<Integer> e1, ArrayList<Integer> e2) {
        if (e1.size() < e2.size()) return -1;
        else if (e1.size() > e2.size()) return 1;
        else return 0;
    }
}