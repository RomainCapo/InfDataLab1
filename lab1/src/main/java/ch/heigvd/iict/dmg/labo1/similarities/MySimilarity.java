package ch.heigvd.iict.dmg.labo1.similarities;

import org.apache.lucene.search.similarities.ClassicSimilarity;

public class MySimilarity extends ClassicSimilarity {


	public float tf(float freq) {
		return (float) (1.0 + Math.log10(freq));

	}

	public float idf(long docFreq, long numDocs) {
		return (float) (Math.log10(numDocs/docFreq+1)+1);

	}

	public float lengthNorm(int numTerms) {
		return 1;

	}
}
