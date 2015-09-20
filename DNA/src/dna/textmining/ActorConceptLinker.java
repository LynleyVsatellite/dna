package dna.textmining;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dna.ActorConceptDataPreprocessor;
import dna.DNAFeature;
import dna.DNATextMiner;
import dna.DNAToken;
import dna.StanfordDNATokenizer;
import dna.features.FeatureFactory;
import dna.features.HasCapitalLetterDNAFeature;
import dna.features.SparseVector;
import ml.classification.LogisticRegression;

/**
 * This class links Actors to Concepts. In other words, it gives the concept/s that each actor is talking about.
 */
public class ActorConceptLinker {
	
	public static void main(String[] args) {
		System.out.println( "+++ Started +++" );
		String file1 = "/Users/rakandirbas/Desktop/dna_files/s0.dna";
//		String file1 = "/Users/rakandirbas/Desktop/dna_files/s0-mini.dna";
		List<String> files = new ArrayList<String>();
		files.add(file1);
		
		List<DNAFeature> features = new ArrayList<DNAFeature>();
		features.add( new HasCapitalLetterDNAFeature() );
		
		int regularizationType = 0;
		double lambda = 0;
		ml.classification.Classifier lamlOrigClf = new LogisticRegression(regularizationType, lambda);
		
		LAMLClassifier lamlClf = new LAMLClassifier(lamlOrigClf);
		
		ActorConceptLinker acLinker = new ActorConceptLinker(files, features, lamlClf, 0, 
				0.6, 0.2, 0.2, 0);
		acLinker.train();
		acLinker.test();
		System.out.println( "+++ Done +++" );
		
	}

	private List<String> dnaFiles;
	private List<DNAFeature> features;
	private int windowSize;
	private Map<String, Set<Integer>> trainTestValDocsIds;
	private ActorConceptDataPreprocessor acDataProcessor;
	private ActorConceptMapper acMapper;
	private FeatureFactory featureFactory;
	private DNAClassifier clf;

	/**
	 * 
	 * @param dnaFiles the files to use to create a training/testing/validation datasets.
	 * @param features the features to be used for the classifier.
	 * @param classifier the classifier to be used.
	 * @param windowSize the number of tokens before and after the current tokens to be used when 
	 * constructing the feature vector for the current token.
	 * @param trainSetSize the size of the training dataset.
	 * @param testSetSize the size of the testing dataset.
	 * @param validationSetSize the size of the validation dataset.
	 * @param seed the seed to be used when sampling the dna files while creating the datasets.
	 */
	public ActorConceptLinker(List<String> dnaFiles, List<DNAFeature> features, DNAClassifier classifier,
			int windowSize, double trainSetSize, double testSetSize, 
			double validationSetSize, int seed) {

		this.dnaFiles = dnaFiles;
		this.features = features;
		this.windowSize = windowSize;
		this.clf = classifier;
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
		featureFactory.generateFeatures(allTokens);//The commented code does the same task
		//but I didn't remove it because I'm not yet sure why I did it that way.
		//TODO Maybe generateFeatures() should be called at a doc level or so. To be checked later.
//		for ( Integer docId : acMapper.getFromDocIdToActorsConceptsLinks().keySet() ) {
//			List<DNAToken> docTokens = acMapper.getFromDocIdToDocTokens().get(docId);
//			featureFactory.generateFeatures(docTokens);
//
//		}

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
		
		SimpleDataset trainDataset = getSamples(trainTestValDocsIds.get("train"));
		List<SparseVector> X = trainDataset.getX();
		double[] y = trainDataset.getY();
		
		for ( int i = 0; i < X.size(); i++ ) {
			String classValue = "";
			if ( y[i] == 0 ) {
				classValue = clf.NEGATIVE_CLASS;
			}
			else {
				classValue = clf.POSITIVE_CLASS;
			}
			clf.updateData(X.get(i), classValue);
		}
		clf.updateClassifier();
		System.out.println( "Done training" );
		
		//Generate the window feature vectors only for the training samples!

	}

	/**
	 * Tests the classifier using the test dataset.
	 */
	public void test() {
		SimpleDataset simpleDataset = getSamples(trainTestValDocsIds.get("test"));
		List<SparseVector> X = simpleDataset.getX();
		double[] y_test = simpleDataset.getY();
		double[] preds = new double[ y_test.length ];

		for ( int i = 0; i < X.size(); i++ ) {
			SparseVector vector = X.get(i);
			String classValue = clf.classifyInstance(vector);
			
			if ( classValue.equals(clf.NEGATIVE_CLASS) ) {
				preds[i] = 0;
			}
			else {
				preds[i] = 1;
			}
		}
		
		StatsUtils.printStats(y_test, preds);
		
	}

	/**
	 * Validates the classifier using the validation dataset.
	 */
	public void validate() {
		SimpleDataset simpleDataset = getSamples(trainTestValDocsIds.get("validate"));
		List<SparseVector> X = simpleDataset.getX();
		double[] y_test = simpleDataset.getY();
		double[] preds = new double[ y_test.length ];

		for ( int i = 0; i < X.size(); i++ ) {
			SparseVector vector = X.get(i);
			String classValue = clf.classifyInstance(vector);
			
			if ( classValue.equals(clf.NEGATIVE_CLASS) ) {
				preds[i] = 0;
			}
			else {
				preds[i] = 1;
			}
		}
		
		StatsUtils.printStats(y_test, preds);
	}
	
	/**
	 * Generates a set of samples to be used as either training, testing or a validation set.
	 * @param docsIds a set that contains the internal docIds of the documents from which the samples
	 * 	will be generated.
	 * @return
	 */
	private SimpleDataset getSamples( Set<Integer> docsIds ) {

		List<SparseVector> positiveSamples = new ArrayList<SparseVector>();
		List<SparseVector> negativeSamples = new ArrayList<SparseVector>();
		
		for ( int docID : docsIds ) {
			List<DNAToken> docTokens = acMapper.getFromDocIdToDocTokens().get(docID);
			Map<Integer, Set<Integer>> acLinks = acMapper.getFromDocIdToActorsConceptsLinks().get(docID);
			List<SparseVector> windowVectors = getWindowVectors(docTokens);
			Set<Integer> allConceptTokensIndicesInTheDoc = 
					new HashSet<Integer>();
			

			//Generating the positive samples
			for ( int actorTokenIndex : acLinks.keySet() ) {
				SparseVector actorTokenWindowVector = windowVectors.get(actorTokenIndex);
				Set<Integer> conceptTokensIndices = acLinks.get(actorTokenIndex);
				allConceptTokensIndicesInTheDoc.addAll(conceptTokensIndices);
				
				for ( int conceptTokenIndex : conceptTokensIndices ) {
					SparseVector conceptTokenWindowVector = windowVectors.get(conceptTokenIndex);
					SparseVector concatinatedVector = actorTokenWindowVector.getACopy();
					concatinatedVector.addAll(conceptTokenWindowVector);
					positiveSamples.add(concatinatedVector);
				}
			}
			
			//Generating the negative samples
			for ( int actorTokenIndex : acLinks.keySet() ) {
				SparseVector actorTokenWindowVector = windowVectors.get(actorTokenIndex);
				Set<Integer> actorConceptTokensIndices = acLinks.get(actorTokenIndex);
				
				for ( int candidateConceptTokenIndex : allConceptTokensIndicesInTheDoc ) {
					
					if ( !actorConceptTokensIndices.contains( candidateConceptTokenIndex ) ) {
						SparseVector candidateConceptTokenWindowVector = 
								windowVectors.get(candidateConceptTokenIndex);
						SparseVector concatinatedVector = actorTokenWindowVector.getACopy();
						concatinatedVector.addAll(candidateConceptTokenWindowVector);
						System.err.printf( "- Actor: %s, Concept: %s\n", docTokens.get(actorTokenIndex).getText(),
								docTokens.get(candidateConceptTokenIndex).getText());
						negativeSamples.add(concatinatedVector);
					}
					
				}
			}
			
		}
		
		double[] y = new double[ positiveSamples.size() + negativeSamples.size() ];
		
		for ( int i = 0; i < positiveSamples.size(); i++ ) {
			y[i] = 1;
		}
		
		positiveSamples.addAll(negativeSamples);
		SimpleDataset dataset = new SimpleDataset(positiveSamples, y);
		
		return dataset;
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














