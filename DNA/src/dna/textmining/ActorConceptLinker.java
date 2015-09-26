package dna.textmining;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
				0.6, 0.2, 0.2, 0, true, true, true);
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
	private boolean addATNegatives;
	private boolean addTTNegatives;
	private boolean createDataset;
	private SimpleDataset trainDataset;
	private SimpleDataset testDataset;
	private SimpleDataset valiDataset;

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
	 * @param addATNegatives should the Actor-Token negative samples be used?
	 * @param addTTNegatives should the Token-Token negative samples be used?
	 * @param createDataset should the dataset be created from scratch or just an existing one from disk?
	 */
	public ActorConceptLinker(List<String> dnaFiles, List<DNAFeature> features, DNAClassifier classifier,
			int windowSize, double trainSetSize, double testSetSize, 
			double validationSetSize, int seed, boolean addATNegatives, boolean addTTNegatives,
			boolean createDataset) {

		this.dnaFiles = dnaFiles;
		this.features = features;
		this.windowSize = windowSize;
		this.addATNegatives = addATNegatives;
		this.addTTNegatives = addTTNegatives;
		this.clf = classifier;
		trainTestValDocsIds  = 
				DNATextMiner.getTrainTestValidateSplit(dnaFiles, trainSetSize, 
						testSetSize, validationSetSize, seed);

		this.acDataProcessor = new ActorConceptDataPreprocessor(new StanfordDNATokenizer());

		String trainDatasetFileName = "ActorConceptTrainDS.csv";
		String testDatasetFileName = "ActorConceptTestDS.csv";
		String valiDatasetFileName = "ActorConceptValDS.csv";
		
		if (createDataset) {
			
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
		
			this.trainDataset = 
					getSamples(trainTestValDocsIds.get("train"), addATNegatives, addTTNegatives);
			this.testDataset = 
					getSamples(trainTestValDocsIds.get("test"), addATNegatives, addTTNegatives);
			this.valiDataset = 
					getSamples(trainTestValDocsIds.get("validate"), addATNegatives, addTTNegatives);
			
			ARFFExporter trainDatasetExporter = 
					new ARFFExporter(trainDatasetFileName, this.trainDataset);
			ARFFExporter testDatasetExporter = 
					new ARFFExporter(testDatasetFileName, this.testDataset);
			ARFFExporter valDatasetExporter = 
					new ARFFExporter(valiDatasetFileName, this.valiDataset);
		}
		else {
			ARFFImporter trainDatasetImporter = 
					new ARFFImporter(trainDatasetFileName);
			this.trainDataset = trainDatasetImporter.asSimpleDataset();
			
			ARFFImporter testDatasetImporter = 
					new ARFFImporter(testDatasetFileName);
			this.testDataset = 
					testDatasetImporter.asSimpleDataset();
			
			ARFFImporter valiDatasetImporter = 
					new ARFFImporter(valiDatasetFileName);
			this.valiDataset = valiDatasetImporter.asSimpleDataset();
		}
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
		SimpleDataset trainDataset = this.trainDataset;
		
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
		SimpleDataset simpleDataset = this.testDataset;
		
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
		SimpleDataset simpleDataset = this.valiDataset;
		
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
	 * @param addATNegatives should the Actor-Token negative samples be added as well?
	 * @param addTTNegatives should the Token-Token negative samples be added as well?
	 * @return
	 */
	private SimpleDataset getSamples( Set<Integer> docsIds, 
			boolean addATNegatives, boolean addTTNegatives ) {

		List<SparseVector> positiveSamples = new ArrayList<SparseVector>();
		List<SparseVector> negativeSamples = new ArrayList<SparseVector>();
		
		for ( int docID : docsIds ) {
			List<DNAToken> docTokens = acMapper.getFromDocIdToDocTokens().get(docID);
			Map<Integer, Set<Integer>> acLinks = acMapper.getFromDocIdToActorsConceptsLinks().get(docID);
			List<SparseVector> windowVectors = getWindowVectors(docTokens);
			Set<Integer> allConceptTokensIndicesInTheDoc = 
					new HashSet<Integer>();
			
			int numDocPositiveSamples = 0;

			//Generating the positive samples
			for ( int actorTokenIndex : acLinks.keySet() ) {
				SparseVector actorTokenWindowVector = windowVectors.get(actorTokenIndex);
				Set<Integer> conceptTokensIndices = acLinks.get(actorTokenIndex);
				allConceptTokensIndicesInTheDoc.addAll(conceptTokensIndices);
				
				for ( int conceptTokenIndex : conceptTokensIndices ) {
					SparseVector conceptTokenWindowVector = windowVectors.get(conceptTokenIndex);
					SparseVector concatinatedVector = actorTokenWindowVector.getACopy();
					concatinatedVector.addAll(conceptTokenWindowVector);
					double distanceFeature = 
							Math.abs( 
									docTokens.get(actorTokenIndex).getStart_position() -
									docTokens.get(conceptTokenIndex).getStart_position()
							);
					concatinatedVector.add(distanceFeature);
					positiveSamples.add(concatinatedVector);
					numDocPositiveSamples++;
				}
			}
			
			//Generating the Actor-Concept negative samples
			for ( int actorTokenIndex : acLinks.keySet() ) {
				SparseVector actorTokenWindowVector = windowVectors.get(actorTokenIndex);
				Set<Integer> actorConceptTokensIndices = acLinks.get(actorTokenIndex);
				
				for ( int candidateConceptTokenIndex : allConceptTokensIndicesInTheDoc ) {
					
					if ( !actorConceptTokensIndices.contains( candidateConceptTokenIndex ) ) {
						SparseVector candidateConceptTokenWindowVector = 
								windowVectors.get(candidateConceptTokenIndex);
						SparseVector concatinatedVector = actorTokenWindowVector.getACopy();
						concatinatedVector.addAll(candidateConceptTokenWindowVector);
//						System.err.printf( "- Actor: %s, Concept: %s\n", docTokens.get(actorTokenIndex).getText(),
//								docTokens.get(candidateConceptTokenIndex).getText());
						
						double distanceFeature = 
								Math.abs( 
										docTokens.get(actorTokenIndex).getStart_position() -
										docTokens.get(candidateConceptTokenIndex).getStart_position()
								);
						concatinatedVector.add(distanceFeature);
						negativeSamples.add(concatinatedVector);
					}
					
				}
			}
			
			List<Integer> actorsTokenIndicesSelectionList = new ArrayList<Integer>();
			List<Integer> normalTokenIndicesSelectionList = new ArrayList<Integer>();
			
			for (int i = 0; i < docTokens.size(); i++) {
				DNAToken token = docTokens.get(i);
				if (token.getLabel().equals("Normal"))
					normalTokenIndicesSelectionList.add(i);
				else if(token.getLabel().equals("Actor"))
					actorsTokenIndicesSelectionList.add(i);
			}
			
			
			if ( addATNegatives && 
					normalTokenIndicesSelectionList.size() > 0 &&
					actorsTokenIndicesSelectionList.size() > 0 ) {
				Random actorsRandomGen = new Random(0);
				Random normalTokensRandomGen = new Random(1);
				for ( int j = 0; j < numDocPositiveSamples/2; j++ ) {
					int actorTokenIndex = actorsRandomGen.nextInt(actorsTokenIndicesSelectionList.size());
					int normalTokenIndex = 
							normalTokensRandomGen.nextInt(normalTokenIndicesSelectionList.size());
					
					SparseVector actorTokenWindowVector = windowVectors.get(actorTokenIndex);
					SparseVector normalTokenWindowVector = 
							windowVectors.get(normalTokenIndex);
					SparseVector concatinatedVector = actorTokenWindowVector.getACopy();
					concatinatedVector.addAll(normalTokenWindowVector);
					double distanceFeature = 
							Math.abs( 
									docTokens.get(actorTokenIndex).getStart_position() -
									docTokens.get(normalTokenIndex).getStart_position()
							);
					concatinatedVector.add(distanceFeature);
					negativeSamples.add(concatinatedVector);
				}
			}
			
			if ( addTTNegatives && normalTokenIndicesSelectionList.size() > 0 ) {
				Random normalTokensRandomGen1 = new Random(0);
				Random normalTokensRandomGen2 = new Random(1);
				for ( int j = 0; j < numDocPositiveSamples/2; j++ ) {
					int normalTokenIndex1 = normalTokensRandomGen1.nextInt(normalTokenIndicesSelectionList.size());
					int normalTokenIndex2 = 
							normalTokensRandomGen2.nextInt(normalTokenIndicesSelectionList.size());
					
					SparseVector normalTokenWindowVector1 = windowVectors.get(normalTokenIndex1);
					SparseVector normalTokenWindowVector2 = windowVectors.get(normalTokenIndex2);
					SparseVector concatinatedVector = normalTokenWindowVector1.getACopy();
					concatinatedVector.addAll(normalTokenWindowVector2);
					double distanceFeature = 
							Math.abs( 
									docTokens.get(normalTokenIndex1).getStart_position() -
									docTokens.get(normalTokenIndex2).getStart_position()
							);
					concatinatedVector.add(distanceFeature);
					negativeSamples.add(concatinatedVector);
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














