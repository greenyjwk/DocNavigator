package com.company;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

/**
 * ISTE-612-2215 Lab #2
 * Ji Woong Kim
 * March 4 2022
 */

public class PositionalIndex {

    HashSet<String> stopwordsList;
    String[] myDocs;
    ArrayList<String> termDictionary;
    HashMap<String, ArrayList<Doc>> docLists;

    /**
     * Construct a positional index
     * @param fileListParam List of input strings or file names
     * @param stopwordsFile List of stopwords strings
     *
     */
    public PositionalIndex(File[] fileListParam, File stopwordsFile) {

        stopwordsList = stopListCreater(stopwordsFile);
        termDictionary = new ArrayList<String>();
        docLists = new HashMap<>();
        ArrayList<Doc> docList;

        for(int i = 0; i < fileListParam.length; i++){

            // ********* Read single file *********
            String singleDoc = new String();
            try (BufferedReader br = new BufferedReader(new FileReader(fileListParam[i]))) {
                String line;
                while ((line = br.readLine()) != null) singleDoc += line;
            } catch (IOException e) {
                e.printStackTrace();
            }

            // ********* Tokenizing *********
            ArrayList<String> tokenList = tokenizer(singleDoc);
            // ********* Tokenizing *********


            // ********* Removing stop words *********
            for (int j = 0; j < tokenList.size(); j++) if (this.stopwordsList.contains(tokenList.get(j))) tokenList.remove(j);
            // ********* Removing stop words *********


            // ********* Porter's Stemmer *********
            ArrayList<String> tokensAfterStemmed = PortersStemmer(tokenList);
            // ********* Porter's Stemmer *********


            // Removing nulls
            while (tokensAfterStemmed.remove(null)) {
            }
            // ********* Porter's Stemmer *********


            // ********* Conversion ArrayList to Array *********
            String tokens[] = tokensAfterStemmed.toArray(new String[tokensAfterStemmed.size()]);
            // ********* Conversion ArrayList to Array *********

            for(int j = 0; j < tokens.length; j++){

                if(termDictionary.contains(tokens[j])){
                    docList = docLists.get(tokens[j]);
                    boolean check = false;

                    for(Doc doc :docList){
                        if(doc.docId == i){
                            doc.insertPosition(j);
                            check = true;
                        }
                    }
                    if(!check){
                        Doc doc = new Doc(i, j);
                        docList.add(doc);
                    }
                }else{
                    termDictionary.add(tokens[j]);
                    Doc doc = new Doc(i, j);
                    ArrayList<Doc> docIdList = new ArrayList<>();
                    docIdList.add(doc);
                    docLists.put( tokens[j] ,docIdList);
                }
            }
        }
    }


    /**
     * Return string representation of a positional index
     */
    public String toString()
    {
        String matrixString = new String();
        ArrayList<Doc> docList;
        for(int i=0;i<termDictionary.size();i++){
            matrixString += String.format("%-15s", termDictionary.get(i));
            docList = docLists.get(i);
            for(int j=0;j<docList.size();j++)
            {
                matrixString += docList.get(j)+ "\t";
            }
            matrixString += "\n";
        }
        return matrixString;
    }



    /** Adopts the Porter Stemmer
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


    /** Tokenizes the single document into array of tokenization
     * @param doc file that contains stop words
     */
    public static ArrayList<String> tokenizer(String doc) {
        StringTokenizer st1 = new StringTokenizer(doc, " ,.:;?![]()'%$#!/+-*\"\'");
        ArrayList<String> tokens = new ArrayList<>();
        while (st1.hasMoreTokens()) tokens.add(st1.nextToken());
        return tokens;
    }

    /** Creates a list that the stop words
     * @param stopwordsFile file that contains stop words
     */
    public HashSet<String> stopListCreater(File stopwordsFile) {
        HashSet<String> stopList = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(stopwordsFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                stopList.add(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stopList;
    }


    /**
     * Check two postings list that has same doc ID to find the adjacent location
     * @param post1 first postings
     * @param post2 second postings
     * @return merged result of two postings
     */
    public ArrayList<Doc> intersect(ArrayList<Doc> post1, ArrayList<Doc> post2) {
        ArrayList<Doc> intersectList = new ArrayList<>();
        //TASK2: TO BE COMPLETED
        HashSet<Integer> check = new HashSet<>();
        for (int q = 0 ;  q < post1.size() ; q++){
            for(int w = 0 ;  w < post2.size(); w++){
                Doc doc1 = post1.get(q);
                Doc doc2 = post2.get(w);

                if(doc1.docId == doc2.docId){
                    Doc intersectDoc = new Doc(doc2.docId);
                    for (int i = 0; i < doc1.positionList.size(); i++) {
                        for (int e = 0; e < doc2.positionList.size(); e++) {

                            if(doc2.positionList.get(e) - doc1.positionList.get(i) == 1){
                                if(!check.contains(doc1.docId)){
                                    intersectDoc.insertPosition( doc2.positionList.get(e) );
                                    check.add(doc2.docId);
                                }
                            }
                        }
                    }
                    intersectList.add(intersectDoc);
                }
            }
        }
        return intersectList;
    }


    /**
     * Get phrase query result
     * @param queryParam a phrase query that consists of any number of terms in the sequential order
     * @return docIds of documents that contain the phrase
     */
    public ArrayList<Doc> phraseQuery(String[] queryParam)
    {

        ArrayList<String> queryUpdated = new ArrayList<>();
        for(int i = 0 ; i < queryParam.length; i++){
            queryUpdated.add(queryParam[i]);
        }

        ArrayList<String> AfterStemmed = PortersStemmer(queryUpdated);
        String query[] = AfterStemmed.toArray(new String[AfterStemmed.size()]);


        System.out.println("Cehck point*********************");
        for(String t : query){
            System.out.println(t);

        }



        //TASK3: TO BE COMPLETED
        ArrayList<Doc> queryResult = new ArrayList<>();
        if(query.length < 2) {
            System.out.println("Search Keywords Error: Phrase query only affords two keywords search");
            return null;
        }

        if(termDictionary.contains(query[0]) & termDictionary.contains(query[1]) ) {
            ArrayList<Doc> posting1 = docLists.get(query[0]);
            ArrayList<Doc> posting2 = docLists.get(query[1]);
            queryResult = intersect(posting1, posting2);
        }

        for(int i = 1; i < query.length - 1; i++) {
            if (termDictionary.contains(query[i]) & termDictionary.contains(query[i + 1])) {
                ArrayList<Doc> posting1 = queryResult;
                ArrayList<Doc> posting2 = docLists.get(query[i + 1]);
                queryResult = intersect(posting1, posting2);
            } else System.out.println("The words are not searched in the list");
        }
        return queryResult;
    }


    public static void main(String[] args) {
//        String[] docs = {"text warehousing over big data",
//                "dimension data warehouse over big data",
//                "nlp after text mining",
//                "nlp after text classification"};



        File directoryPath = new File("./././DocFolder");
        File stopwordsPath = new File("./././stopwords");

        //List of all files and directories
        File filesList[] = directoryPath.listFiles();

        // Stop List Process
        File stopFilesList[] = stopwordsPath.listFiles();

        // Create PositionalIndex Object
        PositionalIndex pi = new PositionalIndex(filesList, stopFilesList[0]);


        //TASK4: TO BE COMPLETED: design and test phrase queries with 2-5 terms
        System.out.println("\n------------------ Test 1 ------------------");
        String SearchTerm = "opening sequence";
        String[] search = SearchTerm.split(" ");
        ArrayList<Doc> queryResult = pi.phraseQuery(search);

        System.out.println("Search Term: " + SearchTerm);
        for(Doc doc:queryResult) System.out.println("Search Result(Document ID) : " + doc.docId);
        System.out.println("Search Result(ArrayList format): " + queryResult);


        System.out.println("\n------------------ Test 2 ------------------");
        SearchTerm = "apparently assuming";
        search = SearchTerm.split(" ");
        queryResult = pi.phraseQuery(search);
        System.out.println("Search Term: " + SearchTerm);
        for(Doc doc:queryResult) System.out.println("Search Result(Document ID) : " + doc.docId);
        System.out.println("Search Result(ArrayList format): " + queryResult);

    }
}

/**
 *
 * Document class that contains the document id and the position list
 */
class Doc{
    int docId;
    ArrayList<Integer> positionList;
    public Doc(int did)
    {
        docId = did;
        positionList = new ArrayList<Integer>();
    }
    public Doc(int did, int position)
    {
        docId = did;
        positionList = new ArrayList<Integer>();
        positionList.add(new Integer(position));
    }

    public void insertPosition(int position)
    {
        positionList.add(new Integer(position));
    }

    public String toString()
    {
        String docIdString = ""+docId + ":<";
        for(Integer pos:positionList)
            docIdString += pos + ",";
        docIdString = docIdString.substring(0,docIdString.length()-1) + ">";
        return docIdString;
    }
}