package dna.textmining;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dna.DNAFeature;
import dna.DNAToken;
import dna.features.FeatureFactory;

public class Dataset {

	private FeatureFactory featureFactory;
	private List<DNAToken> trainingSet;
	private List<DNAToken> testSet;
	private List<DNAToken> validationSet;
	private List<DNAToken> tokens;
	private List<DNAFeature> features;
	//Holds the internal ids of the documents that are used for:
	//training, testing and validation.
	Map<String, Set<Integer>> trainTestValDocsIds;
	
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
	
	public int getFeatureSpaceSize() {
		return featureFactory.getTotalNumberOfFeatures();
	}

	public List<DNAToken> getTrainingSet() {
		return trainingSet;
	}

	public List<DNAToken> getTestSet() {
		return testSet;
	}

	public List<DNAToken> getValidationSet() {
		return validationSet;
	}
	
	
}












