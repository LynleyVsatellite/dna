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
import dna.features.SparseVector;

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

		//Getting all the tokens in the collections to be able to generate the features
		//using the FeatureFactory
		List<DNAToken> allTokens = new ArrayList<DNAToken>();
		for ( int docID : acMapper.getFromDocIdToDocTokens().keySet() ) {
			List<DNAToken> docTokens = acMapper.getFromDocIdToDocTokens().get(docID);
			allTokens.addAll(docTokens);
		}
		featureFactory = new FeatureFactory(allTokens, features, trainTestValDocsIds);

		//Building the feature vectors for the tokens.
		for ( Integer docId : acMapper.getFromDocIdToActorsConceptsLinks().keySet() ) {
			List<DNAToken> docTokens = acMapper.getFromDocIdToDocTokens().get(docId);
			featureFactory.generateFeatures(docTokens);

		}//Is it generating the features for all the samples? i.e. train/validate/test?!

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
		//build the window feature vectors
		//train
		
		//Genereate the window feature vectors only for the training samples!

	}

	/**
	 * Tests the classifier using the test dataset.
	 */
	public void test() {
		//build the window feature vectors
		//test
	}

	/**
	 * Validates the classifier using the validation dataset.
	 */
	public void validate() {
		//build the window feature vectors
		//validate
	}
	
	/**
	 * Generates a set of samples to be used as either training, testing or a validation set.
	 * @param docsIds a set that contains the internal docIds of the documents from which the samples
	 * 	will be generated.
	 * @return
	 */
	private SimpleDataset getSamples( Set<Integer> docsIds ) {

		for ( int docID : docsIds ) {
			List<DNAToken> docTokens = acMapper.getFromDocIdToDocTokens().get(docID);
			Map<Integer, List<Integer>> acLinks = acMapper.getFromDocIdToActorsConceptsLinks().get(docID);
			List<SparseVector> windowVectors = getWindowVectors(docTokens);
			
			//Now generate the samples! i.e which one is positive and which one is negative!
		}
		
		return null;
	}
	
	/**
	 * For each token in the document, concatenates the token's feature vector with the 
	 * feature vectors of the nearby tokens. 
	 * The number of nearby tokens to be used is specified by the {@link #windowSize} field.
	 * @param docTokens the tokens of the document whose tokens will have a window feature vector.
	 * @return a list that contains the feature vectors of all the tokens in the document.
	 */
	private List<SparseVector> getWindowVectors( List<DNAToken> docTokens ) {
		List<SparseVector> windowVectors = new ArrayList<SparseVector>();
		
		for ( int i = 0; i < docTokens.size(); i++  ) {
			DNAToken token = docTokens.get(i);
			List<SparseVector> previousTokensVectors = new ArrayList<SparseVector>();
			List<SparseVector> afterTokensVectors = new ArrayList<SparseVector>();

			for ( int j = i-1; ( j >= 0 ) && ( previousTokensVectors.size() < windowSize ); j-- ) {
				previousTokensVectors.add( docTokens.get(j).getFeatures() );
			}
			
			for ( int j = i+1; ( j < docTokens.size() ) && ( afterTokensVectors.size() < windowSize ); j++ ) {
				afterTokensVectors.add( docTokens.get(j).getFeatures() );
			}
			
			while ( previousTokensVectors.size() < windowSize ) {
				previousTokensVectors.add( new SparseVector( featureFactory.getTotalNumberOfFeatures() ) );
			}
			
			while ( afterTokensVectors.size() < windowSize ) {
				afterTokensVectors.add( new SparseVector( featureFactory.getTotalNumberOfFeatures() ) );
			}
			
			SparseVector windowVector = new SparseVector();
			for ( int c = previousTokensVectors.size()-1; c >= 0; c-- ) {
				SparseVector tempVec =  previousTokensVectors.get(c);
				windowVector.addAll( tempVec.toArray() );
			}
			
			windowVector.addAll( token.getFeatures().toArray() );
			
			for ( SparseVector tempVec : afterTokensVectors ) {
				windowVector.addAll( tempVec.toArray() );
			}
			
			windowVectors.add(windowVector);
			
		}
		
		return windowVectors;
	}
	
}














