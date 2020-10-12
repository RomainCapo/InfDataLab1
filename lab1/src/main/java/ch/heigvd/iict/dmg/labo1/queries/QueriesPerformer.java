package ch.heigvd.iict.dmg.labo1.queries;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.misc.HighFreqTerms;
import org.apache.lucene.misc.HighFreqTerms.DocFreqComparator;
import org.apache.lucene.misc.TermStats;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.QueryBuilder;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class QueriesPerformer {

	private Analyzer analyzer = null;
	private IndexReader indexReader = null;
	private IndexSearcher indexSearcher = null;

	public QueriesPerformer(Analyzer analyzer, Similarity similarity) {

		this.analyzer = analyzer;

		// 3.1 create index reader
		Path path = FileSystems.getDefault().getPath("EnglishAnalyzer");
		Directory dir;
		try {
			dir = FSDirectory.open(path);
			// 3.1 create index reader
			this.indexReader = DirectoryReader.open(dir);
			// 3.2 create index searcher
			this.indexSearcher = new IndexSearcher(indexReader);
			if (similarity != null)
				this.indexSearcher.setSimilarity(similarity);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void printTopRankingTerms(String field, int numTerms) {

		try {
			DocFreqComparator cmp = new HighFreqTerms.DocFreqComparator();
			TermStats[] highFreqTerms;
			highFreqTerms = HighFreqTerms.getHighFreqTerms(indexReader, numTerms, field, cmp);
			System.out.println("Top ranking terms for field [" + field + "] are: ");

			int i = 0;
			for (TermStats ts : highFreqTerms) {
				i++;
				System.out.println(i + ") " + ts.termtext.utf8ToString() + " : " + ts.docFreq);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

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

	public void close() {
		if (this.indexReader != null)
			try {
				this.indexReader.close();
			} catch (IOException e) {
				/* BEST EFFORT */ }
	}

}
