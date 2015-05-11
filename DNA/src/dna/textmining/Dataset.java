package dna.textmining;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dna.DNAFeature;
import dna.DNAToken;
import dna.features.FeatureFactory;

/**
 * The dataset that is used to hold training data, testing data and validation data. 
 *
 */
public class Dataset implements Serializable {

	private static final long serialVersionUID = -1506597078873587149L;
	private FeatureFactory featureFactory;
	private List<DNAToken> trainingSet;
	private List<DNAToken> testSet;
	private List<DNAToken> validationSet;
	private List<DNAToken> tokens;
	private List<DNAFeature> features;
	//Holds the internal ids of the documents that are used for:
	//training, testing and validation.
	Map<String, Set<Integer>> trainTestValDocsIds;
	
	/**
	 * @param tokens the tokens of the documents collection
	 * @param features the features to be used for the tokens
	 * @param trainTestValDocsIds holds the internalDocId/s of the documents that are used for training, testing, and validation.
	 */
	public Dataset( List<DNAToken> tokens, List<DNAFeature> features, 
			Map<String, Set<Integer>> trainTestValDocsIds) {
		this.tokens = tokens;
		this.features = features;
		this.trainTestValDocsIds = trainTestValDocsIds;
		
		featureFactory = new FeatureFactory(tokens, features, trainTestValDocsIds);
		trainingSet = new ArrayList<DNAToken>();
		testSet = new ArrayList<DNAToken>();
		validationSet = new ArrayList<DNAToken>();
		
		System.out.println( "Building features ..." );
		featureFactory.generateFeatures();
		System.out.println( "Done building features ..." );
		
		Set<Integer> trainDocsIDs = trainTestValDocsIds.get("train");
		Set<Integer> testDocsIDs = trainTestValDocsIds.get("test");
		Set<Integer> valDocsIDs = trainTestValDocsIds.get("validate");
		
		for ( DNAToken token : tokens ) {
			int docID = token.getInternalDocId();
			
			if ( trainDocsIDs.contains(docID) )
				trainingSet.add(token);
			else if ( testDocsIDs.contains(docID) )
				testSet.add(token);
			else if ( valDocsIDs.contains(docID) )
				validationSet.add(token);
			else 
				throw new RuntimeException( "The token's document does not belong to any of the train/test/val docs!!!" );
		}
	}
	
	/**
	 * The size of the feature space NOT the number of DNAFeature/s used!
	 * @return
	 */
	public int getFeatureSpaceSize() {
		return featureFactory.getTotalNumberOfFeatures();
	}

	/**
	 * Returns the tokens of the documents used for training.
	 * @return
	 */
	public List<DNAToken> getTrainingSet() {
		return trainingSet;
	}

	/**
	 * Returns the tokens of the documents used for testing.
	 * @return
	 */
	public List<DNAToken> getTestSet() {
		return testSet;
	}

	/**
	 * Returns the tokens of the documents used for validation.
	 * @return
	 */
	public List<DNAToken> getValidationSet() {
		return validationSet;
	}
	
	
}












