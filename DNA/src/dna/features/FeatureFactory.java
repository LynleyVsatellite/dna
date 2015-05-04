package dna.features;

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
	
	/**
	 * 
	 * @param tokens the tokens of the documents collection
	 * @param features the features to be used for the tokens
	 * @param trainTestValDocsIds holds the internalDocId/s of the documents that are used for training, testing, and validation.
	 */
	public FeatureFactory(List<DNAToken> tokens, List<DNAFeature> features, 
			Map<String, Set<Integer>> trainTestValDocsIds) {
		this.tokens = tokens;
		this.features = features;
		this.totalNumberOfFeatures = 0;
		this.vocab = new Vocabulary(tokens, trainTestValDocsIds);
		
		for ( DNAFeature f : features ) {
			if ( f instanceof VocabularyDependent )
				((VocabularyDependent) f).setVocab(vocab);
		}
		
		for ( int j = 0; j < features.size(); j++ ) {
			DNAFeature f = features.get(j);
			this.totalNumberOfFeatures += f.numberOfFeatures();
		}
		
		
	}
	
	/**
	 * Generates the features for the original tokens that this FeatureFactory is based on.
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
	
	/**
	 * This gives the size of the feature space NOT the number of DNAFeature/s used!
	 * @return
	 */
	public int getTotalNumberOfFeatures() {
		return totalNumberOfFeatures;
	}

	

}















