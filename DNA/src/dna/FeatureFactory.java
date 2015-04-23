package dna;

import java.util.ArrayList;
import java.util.List;

import dna.features.AllCapitalizedDNAFeature;
import dna.features.CoreNLPDNAFeature;
import dna.features.HasCapitalLetterDNAFeature;
import dna.features.HasWeirdCharDNAFeature;
import dna.features.IsANumberDNAFeature;
import dna.features.NGramDNAFeature;
import dna.features.NumberOfCharsDNAFeature;
import dna.features.WordDNAFeature;

/**
 * Extract features for the tokens.
 * 
 */
public class FeatureFactory {

	private List<DNAToken> tokens;
	private int totalNumberOfFeatures;
	private List<DNAFeature> features;
	
	public FeatureFactory(List<DNAToken> tokens) {
		this.tokens = tokens;
		this.features = new ArrayList<DNAFeature>();
		this.totalNumberOfFeatures = 0;

		features.add( new HasCapitalLetterDNAFeature() );
		features.add( new CoreNLPDNAFeature() );
		features.add( new HasWeirdCharDNAFeature() );
		features.add( new AllCapitalizedDNAFeature() );
		features.add( new IsANumberDNAFeature() );
//		features.add( new NERDNAFeature() );
		features.add( new NumberOfCharsDNAFeature() );
//		features.add( new POSDNAFeature() );
		features.add( new WordDNAFeature() );
		features.add( new NGramDNAFeature(3) );
		
		for ( DNAFeature f : this.features ) {
			this.totalNumberOfFeatures += f.numberOfFeatures();
		}
		
	}
	
	/**
	 * Generates the features for the tokens.
	 * @return the tokens with added features to them.
	 */
	public List<DNAToken> addFeatures() {
		
		for (DNAFeature feature : features) {
			tokens = feature.buildFeature(tokens);
		}
		
		return tokens;
	}
	
	public int getTotalNumberOfFeatures() {
		return totalNumberOfFeatures;
	}
	

}















