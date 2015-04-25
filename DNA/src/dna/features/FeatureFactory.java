package dna.features;

import java.util.List;

import dna.DNAFeature;
import dna.DNAToken;

/**
 * Extract features for the tokens.
 * 
 */
public class FeatureFactory {

	private List<DNAToken> tokens;
	private int totalNumberOfFeatures;
	private List<DNAFeature> features;
	private Vocabulary vocab;
	
	public FeatureFactory(List<DNAToken> tokens, List<DNAFeature> features) {
		this.tokens = tokens;
		this.features = features;
		this.totalNumberOfFeatures = 0;
		this.vocab = new Vocabulary(tokens);
		for ( DNAFeature f : features ) {
			if ( f instanceof VocabularyDependent )
				((VocabularyDependent) f).setVocab(vocab);
		}
		
		for ( DNAFeature f : this.features ) {
			this.totalNumberOfFeatures += f.numberOfFeatures();
		}
		
	}
	
	/**
	 * Generates the features for the tokens.
	 * @return the tokens with added features to them.
	 */
	public List<DNAToken> generateFeatures() {
		
		for (DNAFeature feature : features) {
			tokens = feature.buildFeature(tokens);
		}
		
		return tokens;
	}
	
	public int getTotalNumberOfFeatures() {
		return totalNumberOfFeatures;
	}
	

}















