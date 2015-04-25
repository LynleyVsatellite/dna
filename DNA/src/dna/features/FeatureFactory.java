package dna.features;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	private List<String> featuresNames;
	
	public FeatureFactory(List<DNAToken> tokens, List<DNAFeature> features, 
			Map<String, Set<Integer>> trainTestValDocsIds) {
		this.tokens = tokens;
		this.features = features;
		this.totalNumberOfFeatures = 0;
		this.vocab = new Vocabulary(tokens, trainTestValDocsIds);
		this.featuresNames = new ArrayList<String>();
		
		for ( DNAFeature f : features ) {
			if ( f instanceof VocabularyDependent )
				((VocabularyDependent) f).setVocab(vocab);
		}
		
		for ( int j = 0; j < features.size(); j++ ) {
			DNAFeature f = features.get(j);
			for( int i = 0; i < f.numberOfFeatures(); i++ ) {
				featuresNames.add( j + "_" + i );
			}
			this.totalNumberOfFeatures += f.numberOfFeatures();
		}
		
		if( featuresNames.size() != totalNumberOfFeatures)
			throw new RuntimeException("The number of features names is not the same as the number of features!");
		
	}
	
	/**
	 * Generates the features for the original tokens that this FeatureFactory is based on.
	 * @return the tokens with added features to them.
	 */
	public void generateFeatures() {
		generateFeatures(tokens);
	}
	
	/**
	 * Generates the features for new tokens based on the features specified for this FeatureFactory.
	 * @param tokens
	 */
	public void generateFeatures(List<DNAToken> tokens) {
		
		for (DNAFeature feature : features) {
			feature.buildFeature(tokens);
		}
		
	}
	
	public int getTotalNumberOfFeatures() {
		return totalNumberOfFeatures;
	}

	public List<String> getFeaturesNames() {
		return featuresNames;
	}
	

}















