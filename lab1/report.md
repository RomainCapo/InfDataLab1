# Report T-InfData Labo1
* Capocasale Romain
* Selajdin Bilali
* Date : 12.10.2020
* Course : T-InfData

## Chapter 2.1
### 1 and 2)
* StrinField : A field that is indexed but not tokenized (the entire String value is indexed as a single token). No term frequency or positional informations.
* LongPoint : A field that indexes long values for efficient range filtering and sorting.
* TextField : A field that is indexed and tokenized, without term vectors. Not stored.

### 3)
It seems that the StandardAnalyser (https://lucene.apache.org/core/7_3_1/core/org/apache/lucene/analysis/standard/StandardAnalyzer.html) class proceeds to a suppresion of stop words in the query. 

Lucene provides a default stop words dictionary if no stop words list is provided in the StandardAnalyser class constructor.

### 4)
In the Lucene library, the following classes are available for stemming: 
* PorterStemFilter
* PorterStemmer
* EnglishAnalyzer 

 None of these classes are used in the command line demo. The stemming is not perform.

### 5)
In the demo example the search is case-insentive. 

Indeed the StandardAnalyser class uses a LowerCaseFilter which converts all the words of the query into lower case. 
    
### 6)  
The removal of stop words takes place first. Indeed there is no interest into perform the stemming on the stop words to delete them later.
Note that if you change the processing order of stemming and stop word deletion the result may be different.

## Chapter 3.1
### 1
    
A term vector saves all the information related to a term, include the Term value, frequencies, positions.

It is a per-document inverted index and provides functionality to find all the term information in the document according to document identifiant.

Definition from : https://medium.com/@Alibaba_Cloud/analysis-of-lucene-basic-concepts-5ff5d8b90a53

### 2
To store term vector : 
```
fieldType.setIndexOptions(IndexOptions.DOCS);
fieldType.setStoreTermVectorOffsets(true);
fieldType.setStoreTermVectors(true);
```

To read the term vector : 
* getTermVector(int docID, String field) -> get one term vector
* getTermVectors(int docID) -> get all term vectors

### 3
* Size without term vector : 669 Ko
* Size with term vector : 1.35 Mo

It can be seen that the (memory) size of the index is larger with the term vector.


CACMIndexer.java
```java
@Override
public void onNewDocument(Long id, String authors, String title, String summary) {

	Document doc = new Document();

	doc.add(new StoredField("id", id));
	
	for (String author : authors.split(";")) {
		doc.add(new StringField("authors", author, Field.Store.YES));

	}
	
	doc.add(new TextField("title", title, Field.Store.YES));

	if (summary != null) {
		FieldType fieldType = new FieldType();
		fieldType.setIndexOptions(IndexOptions.DOCS);	
		fieldType.setStoreTermVectorOffsets(true);// Store Offset
		fieldType.setStoreTermVectors(true);// Store term vector

		doc.add(new Field("summary", summary, fieldType));
	}
		try {
			this.indexWriter.addDocument(doc);

		} catch (IOException e) {
			e.printStackTrace();
		}


}
```

## Chapter 3.2
### 2 and 3)
* WhitespaceAnalyzer() -> Separation of text from spaces
	* The number of indexed documents and indexed terms : 3203 / 34272
	* The number of indexed terms in the summary field : 26'821
	* The top 10 frequent terms of the summary field in the index : of, the, is, a, and, to, in, for, The, are
	* The size of the index on disk : 1.42 Mo
	* The required time for indexing : 1135.0 ms
	
* EnglishAnalyzer() -> Analyzer for English and stop word removing
	* The number of indexed documents and indexed terms : 3203 / 23010
	* The number of indexed terms in the summary field : 16724
	* The top 10 frequent terms of the summary field in the index : us, which, comput, program, system, present, describ, paper, method, can
	* The size of the index on disk : 1.09 Mo
	* The required time for indexing : 1483.0 ms
	
* ShingleAnalyzerWrapper() -> 1-gram and 2-gram
	* The number of indexed documents and indexed terms : 3203 / 113908
	* The number of indexed terms in the summary field : 95116
	* The top 10 frequent terms of the summary field in the index : the, of, a, is, and, to, in, for, are, ofthe
	* The size of the index on disk : 3.14 Mo
	* The required time for indexing : 1657.0 ms
	
* ShingleAnalyzerWrapper() -> 1-gram and 3-gram
	* The number of indexed documents and indexed terms : 3203 / 158363
	* The number of indexed terms in the summary field : 137620
	* The top 10 frequent terms of the summary field in the index : the, of, a, is, and, to, in, for, are, this
	* The size of the index on disk : 7.3 Mo
	* The required time for indexing : 1936.0 ms
	
* StopAnalyzer() -> Letter tokenization, Lower case filter and stop word removal
	* The number of indexed documents and indexed terms : 3203 / 24663
	* The number of indexed terms in the summary field : 18342
	* The top 10 frequent terms of the summary field in the index :  system, computer, paper, presented, time, method, program, data, algorithm, discussed 
	* The size of the index on disk : 1.05 Mo
	* The required time for indexing : 926.0 ms

Main.java
```java
public static void main(String[] args) throws IOException {

		// 1.1. create an analyzer
		double beforeTime = System.currentTimeMillis();
		Analyzer analyser = getAnalyzer("stop");
		
		//Similarity similarity = new ClassicSimilarity();
		Similarity similarity = new MySimilarity();
		
		CACMIndexer indexer = new CACMIndexer(analyser, similarity);
		indexer.openIndex();
		CACMParser parser = new CACMParser("documents/cacm.txt", indexer);
		parser.startParsing();
		indexer.finalizeIndex();
		
		double timeTaken = System.currentTimeMillis() - beforeTime;
		System.out.println("Time taken in ms : " + timeTaken);

		QueriesPerformer queriesPerformer = new QueriesPerformer(analyser, similarity);

		// Section "Reading Index"
		readingIndex(queriesPerformer);

		// Section "Searching"
		searching(queriesPerformer);

		queriesPerformer.close();
		

	}
	
	...
	
private static Analyzer getAnalyzer(String analyzerName) throws IOException {

		if(analyzerName == "stop") {
			Path path = FileSystems.getDefault().getPath("common_words.txt");
			return new StopAnalyzer(path);
		} else if(analyzerName == "whitespace") {
			return new WhitespaceAnalyzer();
		} else if(analyzerName == "english") {
			return new EnglishAnalyzer();
		} else if(analyzerName == "shingle12") {
			return new ShingleAnalyzerWrapper(new StandardAnalyzer() , 2, 2, "", true, true, "");

		} else if(analyzerName == "shingle13") {
			return new ShingleAnalyzerWrapper(new StandardAnalyzer() , 3, 3, "", true, true, "");

		}
		return new StandardAnalyzer();
	}
```

### 4)
1. The two Singhle analyzer take more place on the disk. This is due to the fact that these analysers store the same word several times. The indexing time is also longer. 

2. Only the English analyzer and the Stop analyzer remove stop words. For the English analyzer it is even possible to specify its own stop word list. We notice that in the other analysers words like: the, of, is, a are stored and are even very frequent.

3. Analysers using stop words take up less disk space as stop words are not stored. The two analyzers that take up the least storage are the English Analyzer and the Stop Analyzer, two analyzers that remove stop words. 

## Chapter 3.3
### 1
1. "" (No author) : 84

### 2
1. algorithm : 1008
2. comput : 392
3. program : 307
4. system : 280
5. gener : 169
6. method : 156
7. languag : 144
8. function : 142
9. us : 123
10. data : 110

QueriesPerformer.java
```java
public void printTopRankingTerms(String field, int numTerms) {
		
		try {
			DocFreqComparator cmp = new HighFreqTerms.DocFreqComparator();
			TermStats[] highFreqTerms;
			highFreqTerms = HighFreqTerms.getHighFreqTerms(indexReader, numTerms, field, cmp);
			System.out.println("Top ranking terms for field ["  + field +"] are: ");
			
			int i = 0;
			for(TermStats ts : highFreqTerms)
			{
				i++;
				System.out.println(i + ") " + ts.termtext.utf8ToString() + " : " + ts.docFreq);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
```

## Chapter 3.4
### Information Retrival
```
1516: Automatic Data Compression (3.6460404)
3134: The Use of Normal Multiplication Tablesfor Information Storage and Retrieval (3.6460404)
2870: A Lattice Model of Secure Information Flow (3.483682)
1267: Performance of Systems Used for Data TransmissionTransfer Rate of Information Bits -An ASA TutorialStandard (3.284971)
1652: A Code for Non-numeric Information ProcessingApplications in Online Systems (3.284971)
1032: Theoretical Considerations in Information Retrieval Systems (3.0287883)
1194: Establishment of the ACM Repository and Principlesof the IR System Applied to its Operation (3.0287883)
1457: Data Manipulation and Programming Problemsin Automatic Information Retrieval (3.0287883)
1745: A Position Paper on Computing and Communications (3.0287883)
2307: Dynamic Document Processing (3.0287883)
```

### Information AND Retrival
No result

### Information AND Retrival NOT Database
```
3134: The Use of Normal Multiplication Tablesfor Information Storage and Retrieval (6.855922)
1032: Theoretical Considerations in Information Retrieval Systems (6.6731195)
1457: Data Manipulation and Programming Problemsin Automatic Information Retrieval (6.6731195)
891: Everyman's Information Retrieval System (6.312051)
1699: Experimental Evaluation of InformationRetrieval Through a Teletypewriter (6.312051)
2307: Dynamic Document Processing (6.2386703)
1527: A Grammar Base Question Answering Procedure (5.8776007)
1652: A Code for Non-numeric Information ProcessingApplications in Online Systems (5.7521563)
1681: Easy English,a Language for InformationRetrieval Through a Remote Typewriter Console (5.6947985)
2990: Effective Information Retrieval Using Term Accuracy (5.6947985)
```

### Info*
```
222: Coding Isomorphisms (1.0)
272: A Storage Allocation Scheme for ALGOL 60 (1.0)
396: Automation of Program  Debugging (1.0)
397: A Card Format for Reference Files in Information Processing (1.0)
409: CL-1, An Environment for a Compiler (1.0)
440: Record Linkage (1.0)
483: On the Nonexistence of a Phrase Structure Grammar for ALGOL 60 (1.0)
616: An Information Algebra - Phase I Report-LanguageStructure Group of the CODASYL Development Committee (1.0)
644: A String Language for Symbol Manipulation Based on ALGOL 60 (1.0)
655: COMIT as an IR Language (1.0)
```

### "Information Retrieval"~5
```
891: Everyman's Information Retrieval System (5.877601)
1457: Data Manipulation and Programming Problemsin Automatic Information Retrieval (5.877601)
1699: Experimental Evaluation of InformationRetrieval Through a Teletypewriter (5.877601)
2307: Dynamic Document Processing (5.3131714)
1652: A Code for Non-numeric Information ProcessingApplications in Online Systems (4.517652)
1935: Randomized Binary Search Technique (4.517652)
2451: Design of Tree Structures for Efficient Querying (4.517652)
2516: Hierarchical Storage in Information Retrieval (4.517652)
2519: On the Problem of Communicating Complex Information (4.517652)
2795: Sentence Paraphrasing from a Conceptual Base (4.517652)
```

Main.java
```java
private static void searching(QueriesPerformer queriesPerformer) {
		// Example
		queriesPerformer.query("query compiler");
		queriesPerformer.query("\"Information Retrival\"");
		queriesPerformer.query("Information Retrival");
		queriesPerformer.query("Information Retrieval -Database");
		queriesPerformer.query("Info*");
		queriesPerformer.query("\"Information Retrieval\"~5");
	}
```

QueryPerformer.java
```java
	public void query(String q) {
		final int NB_SEARCH = 10;
		// 2.1 create query parser
		System.out.println("Searching for " + q);
		QueryParser parser = new QueryParser("summary", analyzer);
		try {
			Query query = parser.parse(q);
			ScoreDoc[] scores = indexSearcher.search(query, NB_SEARCH).scoreDocs;
			for(ScoreDoc d : scores) {
				Document doc = indexSearcher.doc(d.doc);
				System.out.println(doc.get("id") + ": " + doc.get("title") + " (" + d.score + ")");
			}
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
```

## Chapter 3.5

* With MySimilarity class :
```
2990: Effective Information Retrieval Using Term Accuracy (5.1278267)
1976: Multi-attribute Retrieval with Combined Indexes (4.620015)
2716: Optimizing the Performance of a Relational Algebra Database Interface (4.2597175)
2722: Multidimensional Binary Search Trees Used for Associative Searching (4.2597175)
2728: Consecutive Storage of Relevant Records with Redundancy (4.2597175)
2534: Design and Implementation of a Diagnostic Compiler for PL/I (3.7561734)
637: A NELIAC-Generated 7090-1401 Compiler (3.5419197)
1215: Some Techniques Used in the ALCOR ILLINOIS 7090 (3.5419197)
2652: Reduction of Compilation Costs Through Language Contraction (3.5419197)
2897: A Case Study of a New Code Generation Technique for Compilers (3.5419197)
```

* With ClassicSimilarity class :
```
2990: Effective Information Retrieval Using Term Accuracy (1.4831469)
1215: Some Techniques Used in the ALCOR ILLINOIS 7090 (1.40438)
2728: Consecutive Storage of Relevant Records with Redundancy (1.3196394)
718: An Experiment in Automatic Verification of Programs (1.3136772)
1122: A Note on Some Compiling Algorithms (1.3136772)
3189: An Algebraic Compiler for the FORTRAN Assembly Program (1.1203077)
799: Design of a Separable Transition-Diagram Compiler* (1.087828)
205: Macro Instruction Extensions of Compiler Languages (1.0305332)
2716: Optimizing the Performance of a Relational Algebra Database Interface (1.0221883)
2652: Reduction of Compilation Costs Through Language Contraction (0.99304664)
```

MySimilarity.java
```java
public class MySimilarity extends ClassicSimilarity {

	@Override
	public float tf(float freq) {
		return (float) (1.0 + Math.log10(freq));

	}

	@Override
	public float idf(long docFreq, long numDocs) {
		return (float) (Math.log10(numDocs/(float)docFreq+1)+1);

	}
	
	@Override
	public float lengthNorm(int numTerms) {
		return 1;
	}
}


```