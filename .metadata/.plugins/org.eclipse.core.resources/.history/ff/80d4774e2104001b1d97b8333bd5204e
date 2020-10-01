# Report
## Chapter 2
### 1 and 2)
    StrinField -> A field that is indexed but not tokenized (the entire String value is indexed as a single token).
    LongPoint -> A field that indexes long values for efficient range filtering and sorting.
    TextField -> A field that is indexed and tokenized, without term vectors.

### 3)
    It seems that the StandardAnalyser (https://lucene.apache.org/core/7_3_1/core/org/apache/lucene/analysis/standard/StandardAnalyzer.html) class proceeds to a suppresion of stop words in the query. 
    Lucene provides a default stop words dictionary if no stop words list is provided in the StandardAnalyser class constructor.

### 4)
    In the Lucene library, the following classes are available for stemming: 
        * PorterStemFilter
        * PorterStemmer

    None of these classes are used in the command line demo. The stemming is not perform.

### 5)
    In the demo example the search is case-insentive. 
    Indeed the StandardAnalyser class uses a LowerCaseFilter which converts all the words of the query into lower case. 
    
### 6)  
    The removal of stop words takes place first. Indeed there is no interest ito perform the stemming on the stop words to delete them later.