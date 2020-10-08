package ch.heigvd.iict.dmg.labo1.queries;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.misc.HighFreqTerms;
import org.apache.lucene.misc.HighFreqTerms.DocFreqComparator;
import org.apache.lucene.misc.TermStats;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
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
		Path path = FileSystems.getDefault().getPath("index");
		Directory dir;
		try {
			dir = FSDirectory.open(path);
			this.indexReader = DirectoryReader.open(dir);
			this.indexSearcher = new IndexSearcher(indexReader);
			if (similarity != null)
				this.indexSearcher.setSimilarity(similarity);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void printTopRankingTerms(String field, int numTerms) {
		// TODO student
		// This methods print the top ranking term for a field.
		// See "Reading Index".

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
		try {
			final int NB_SEARCH = 10;
			Query query = new QueryBuilder(this.analyzer).createBooleanQuery("summary", q);

			TopDocs topDocs = indexSearcher.search(query, NB_SEARCH);
			for(ScoreDoc topDoc: topDocs.scoreDocs) {
				Document doc = indexSearcher.doc(topDoc.doc);
				System.out.println(doc.get("id") + ") " + doc.get("title") + " [score=" + topDoc.score + "]");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Searching for [" + q + "]");
	}

	public void close() {
		if (this.indexReader != null)
			try {
				this.indexReader.close();
			} catch (IOException e) {
				/* BEST EFFORT */ }
	}

}
