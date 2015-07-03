package dna.textmining;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dna.ActorConceptDataPreprocessor;
import dna.DNAFeature;
import dna.DNATextMiner;
import dna.DNAToken;
import dna.StanfordDNATokenizer;
import dna.features.FeatureFactory;

/**
 * This class links Actors to Concepts. In other words, it gives the concept/s that each actor is talking about.
 */
public class ActorConceptLinker {
	
	private List<String> dnaFiles;
	private List<DNAFeature> features;
	private int windowSize;
	private Map<String, Set<Integer>> trainTestValDocsIds;
	private ActorConceptDataPreprocessor acDataProcessor;
	private ActorConceptMapper acMapper;
	private FeatureFactory featureFactory;
	
	/**
	 * 
	 * @param dnaFiles the files to use to create a training/testing/validation datasets.
	 * @param features the features to be used for the classifier.
	 * @param windowSize the number of tokens before and after the current tokens to be used when 
	 * constructing the feature vector for the current token.
	 * @param trainSetSize the size of the training dataset.
	 * @param testSetSize the size of the testing dataset.
	 * @param validationSetSize the size of the validation dataset.
	 * @param seed the seed to be used when sampling the dna files while creating the datasets.
	 */
	public ActorConceptLinker(List<String> dnaFiles, List<DNAFeature> features, 
			int windowSize, double trainSetSize, double testSetSize, 
			double validationSetSize, int seed) {
		//Sample the documents from DNATextMiner.getSplit
		//Build the feature vectors for the tokens
		//Build the window features for the tokens
		//Build the X,y dataset format
		//Build train, test, validate methods to train a DNAClassifier
		
		this.dnaFiles = dnaFiles;
		this.features = features;
		this.windowSize = windowSize;
		trainTestValDocsIds  = 
				DNATextMiner.getTrainTestValidateSplit(dnaFiles, trainSetSize, 
						testSetSize, validationSetSize, seed);
		
		this.acDataProcessor = new ActorConceptDataPreprocessor(new StanfordDNATokenizer());
		List<File> files = new ArrayList<File>();
		for ( String filePath : dnaFiles ) {
			files.add( new File( filePath ) );
		}
		
		System.out.println( "Generalizing the DNA files for the ActorConcept Linker" );
		this.acMapper = acDataProcessor.generalizeData(files);
		System.out.println( "Done generalizing the DNA files." );
		
		List<DNAToken> allTokens = new ArrayList<DNAToken>();
		
		featureFactory = new FeatureFactory(allTokens, features, trainTestValDocsIds);
		
	}

	/**
	 * This method gives the concepts that each actor is talking about. It accepts a list 
	 * of tokens of a document, where the actor tokens have the label 'Actor' and the concept tokens
	 * have the label 'Concept'.
	 * @param documentTokens The tokens of a document.
	 * @return A map that has as keys the indices of the Actor tokens in the {@link documentTokens}, and as values
	 * lists of the indices of the concept tokens that each Actor token is referring to in the text.
	 */
	public Map<Integer, List<Integer>> linkActorsToConcept(List<DNAToken> documentTokens) {
		return null;
	}
	
	/**
	 * Trains the classifier using the generated training dataset.
	 */
	public void train() {
		//build the feature vectors for the tokens
		//build the window feature vectors
		//train
		
		for ( Integer docId : acMapper.getFromDocIdToActorsConceptsLinks().keySet() ) {
			List<DNAToken> docTokens = acMapper.getFromDocIdToDocTokens().get(docId);
			Map<Integer, List<Integer>> acLinks = acMapper.getFromDocIdToActorsConceptsLinks().get(docId);
			for ( int actorTokenIndex : acLinks.keySet() ) {
				DNAToken actorToken = docTokens.get(actorTokenIndex);
				List<DNAToken> singleActorTokenList = new ArrayList<DNAToken>();
				singleActorTokenList.add( actorToken );
				
				for (DNAFeature feature : features) {
					feature.buildFeature(singleActorTokenList);
				}
				
				System.out.println( "Linked to the following concept tokens:" );
				for( int conceptTokenIndex : acLinks.get(actorTokenIndex) ) {
					System.out.print("[" + docTokens.get(conceptTokenIndex) + "]");
				}
				System.out.println("\n");
			}
			
		}
	}
	
	/**
	 * Tests the classifier using the test dataset.
	 */
	public void test() {
		//build the feature vectors for the tokens
		//build the window feature vectors
		//test
	}
	
	/**
	 * Validates the classifier using the validation dataset.
	 */
	public void validate() {
		//build the feature vectors for the tokens
		//build the window feature vectors
		//validate
	}
	
}














