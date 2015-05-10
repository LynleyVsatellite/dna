package dna.textmining;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import weka.classifiers.Classifier;
import weka.classifiers.evaluation.TwoClassStats;
import weka.classifiers.functions.SimpleLogistic;
import dna.DNAToken;
import dna.DNATokenizer;
import dna.features.SparseVector;

//TODO Also add the feature of the previous prediction! Make sure to modify the number of feature space and featureNames!


public class TokenClassifier implements Serializable {
	
	private static final long serialVersionUID = -8585609561306124520L;
	
	private DNAClassifier clf;
	private Classifier wekaClf;
	private Dataset dataset;
	private boolean isTrained;
	public static final String POSITIVE_CLASS = DNAClassifier.POSITIVE_CLASS; 
	public static final String NEGATIVE_CLASS = DNAClassifier.NEGATIVE_CLASS; 
	private int windowSize;
	private LinkedHashSet<String> featuresNames;
	/**
	 * Creates a TokenClassifier with a default Logistic Regression classifier and window size of 1 token
	 *  (i.e one token before the current token and one after).
	 * @param dataset
	 */
	public TokenClassifier(Dataset dataset) {
		this(dataset, new SimpleLogistic(), 1);
	}
	
	/**
	 * 
	 * @param dataset the dataset that holds the training, testing and validation data.
	 * @param wekaClf Weka classifier
	 * @param windowSize the number of tokens before and after the 
	 * 	current token to be used for the current token's feature vector. 
	 * 	So 1 means one token before the current token and one token after the current token.
	 * 	And 2 means two tokens before and two tokens after. If 1 is chosen, then the size of a sample's
	 * 	feature vector is 3 * the size of a token's feature vector (because: previous token + current token + next token).
	 */
	public TokenClassifier(Dataset dataset, Classifier wekaClf, int windowSize) {
		
		this.isTrained = false;
		this.dataset = dataset;
		this.wekaClf = wekaClf;
		this.windowSize = windowSize;
		LinkedHashSet<String> classes = new LinkedHashSet<String>();
		classes.add( POSITIVE_CLASS );
		classes.add( NEGATIVE_CLASS );
		featuresNames = new LinkedHashSet<String>();
		
		for ( Integer i = 0; i < dataset.getFeatureSpaceSize() * (2*windowSize+1); i++ )
			featuresNames.add( i.toString() );
		
		clf = new WekaClassifier(wekaClf, featuresNames, classes, true);
		
	}
	
	/**
	 * 
	 * @param dataset the dataset that holds the training, testing and validation data.
	 * @param clf a @DNAClassifier to be used.
	 * @param windowSize the number of tokens before and after the 
	 * 	current token to be used for the current token's feature vector. 
	 * 	So 1 means one token before the current token and one token after the current token.
	 * 	And 2 means two tokens before and two tokens after. If 1 is chosen, then the size of a sample's
	 * 	feature vector is 3 * the size of a token's feature vector (because: previous token + current token + next token).
	 */
	public TokenClassifier(Dataset dataset, DNAClassifier clf, int windowSize) {
		
		this.isTrained = false;
		this.dataset = dataset;
		this.windowSize = windowSize;
		LinkedHashSet<String> classes = new LinkedHashSet<String>();
		classes.add( POSITIVE_CLASS );
		classes.add( NEGATIVE_CLASS );
		featuresNames = new LinkedHashSet<String>();
		
		for ( Integer i = 0; i < dataset.getFeatureSpaceSize() * (2*windowSize+1); i++ )
			featuresNames.add( i.toString() );
		
		this.clf = clf;
		
	}
	
	/**
	 * Classifies the tokens as either they belong to the positive class (named entity) or not.
	 * @param text the text of a document.
	 * @param tokenzier the tokenzier that was used to create the training tokens/data.
	 * @return
	 */
	public List<DNAToken> classify(String text, DNATokenizer tokenzier) {
		List<DNAToken> tokens = tokenzier.tokenize(0, text);
		for ( int i = 0; i < tokens.size(); i++ ) {
			DNAToken tok = tokens.get(i);
			tok.setIndex(i);
		}
		if (isTrained) {
			classify(tokens);
			return tokens;
		}
		else {
			throw new RuntimeException( "The classifier is not trained!" );
		}
	}
	
	/**
	 * Classifies the tokens of a document
	 * @param tokens the tokens of a document
	 */
	public void classify(List<DNAToken> tokens) {
		if (isTrained) {
			List<SparseVector> windowVectors = getWindowVectors(tokens);
			int i = 0;
			for ( SparseVector vector : windowVectors ) {
				Map<String, Double> row = getWekaVector(vector);
				DNAToken token = tokens.get(i++);
				
				try {
					String clfResult = clf.classifyInstance(row);
					token.setLabel(clfResult);
					double positive_pred_prob = clf.distributionForInstance(row).get( TokenClassifier.POSITIVE_CLASS );
					token.setPrediction_probability( positive_pred_prob );
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		else {
			throw new RuntimeException( "The classifier is not trained!" );
		}
	}
	
	/**
	 * Constructs a feature vector based on a window around the current token.
	 * @param docTokens the tokens of a document.
	 * @return
	 */
	private List<SparseVector> getWindowVectors( List<DNAToken> docTokens ) {
		List<SparseVector> vectors = new ArrayList<SparseVector>();
//		System.out.println( "Getting window vectors ... " );
//		System.out.println( "Doc tokens: " + docTokens.size() );
		int counter = 0;
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
				previousTokensVectors.add( new SparseVector( dataset.getFeatureSpaceSize() ) );
			}
			
			while ( afterTokensVectors.size() < windowSize ) {
				afterTokensVectors.add( new SparseVector( dataset.getFeatureSpaceSize() ) );
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
			
			vectors.add(windowVector);
//			System.out.println( "Token " + counter++ );
			
		}
		
		return vectors;
	}
	
	/**
	 * Trains the classifier with the training data.
	 */
	public void train() {
		System.out.println( "Started training ..." );
		List<DNAToken> trainSet = dataset.getTrainingSet();
		System.out.println( "Number of training samples: " + trainSet.size() );
		List<List<DNAToken>> docs = fromTokensToDocs(trainSet);
		
		System.out.println( "Number of docs: " + docs.size() );
		int counter = 0;
		
		try {
			for (List<DNAToken> docTokens : docs) {
				List<SparseVector> windowVectors = getWindowVectors(docTokens);
				int i = 0;
				for ( SparseVector vector : windowVectors ) {
					//construct the feature vector for weka
					Map<String, Double> row = getWekaVector(vector);
					DNAToken token = docTokens.get(i++);
					String classValue = "";
					if ( token.getLabel().equals( NEGATIVE_CLASS ) )
						classValue = NEGATIVE_CLASS;
					else if ( token.getLabel().equals( POSITIVE_CLASS ) )
						classValue = POSITIVE_CLASS;
					else
						throw new RuntimeException( "Unknown class label!!" );
					clf.updateData(row, classValue);
					
				}
				System.out.println("Added the samples from document " + counter++);
			}
			
			clf.updateClassifier();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		isTrained = true;
	}
	
	/**
	 * Converts a SparseVector to a format appropriate for use by the GeneralClassifier.
	 * @param vector a sparse feature vector.
	 * @return
	 */
	private Map<String, Double> getWekaVector(SparseVector vector) {
		Map<String, Double> row = new HashMap<String, Double>();
		
		if ( vector.size() != featuresNames.size() )
			throw new RuntimeException( "The size of the feature vector is different from the number of features!" );
		else {

			int i = 0; 
			for ( String featureName : featuresNames ) {
				row.put(featureName, vector.get(i));
				i++;
			}
			
		}
		
		return row;
	}

	/**
	 * Tests the classifier with the testing data and output performance statistics
	 */
	public void test() {
		List<DNAToken> set = dataset.getTestSet();
		int tp = 0;
		int fp = 0;
		int tn = 0;
		int fn = 0;
		if ( set.size() == 0 ) {
			throw new RuntimeException( "The test set size is zero!" );
		}
		else if (!isTrained) {
			throw new RuntimeException( "The classifier is not trained yet to be tested!" );
		}
		else {
			try {
				List<List<DNAToken>> docs = fromTokensToDocs(set);
				for (List<DNAToken> docTokens : docs) {
					List<SparseVector> windowVectors = getWindowVectors(docTokens);
					int i = 0;
					for ( SparseVector vector : windowVectors ) {
						//construct the feature vector for weka
						Map<String, Double> row = getWekaVector(vector);
						DNAToken token = docTokens.get(i++);
						String classValue = "";
						if ( token.getLabel().equals( NEGATIVE_CLASS ) )
							classValue = NEGATIVE_CLASS;
						else if ( token.getLabel().equals( POSITIVE_CLASS ) )
							classValue = POSITIVE_CLASS;
						else
							throw new RuntimeException( "Unknown class label!!" );
						
						String clfResult = clf.classifyInstance(row);
						if ( classValue.equals( POSITIVE_CLASS ) && clfResult.equals( POSITIVE_CLASS ) ) 
							tp++;
						else if ( classValue.equals( POSITIVE_CLASS ) && clfResult.equals( NEGATIVE_CLASS ) ) 
							fn++;
						else if ( classValue.equals( NEGATIVE_CLASS ) && clfResult.equals( POSITIVE_CLASS ) ) 
							fp++;
						else if ( classValue.equals( NEGATIVE_CLASS ) && clfResult.equals( NEGATIVE_CLASS ) ) 
							tn++;
						else {
							throw new RuntimeException( "Something is wrong while computing performance stats!" );
						}
					}
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		TwoClassStats stats = new TwoClassStats(tp, fp, tn, fn);
		System.out.println( "++++++ Testing stats ++++++" );
		System.out.printf( "Recal: %f, Precision: %f, F1-measure: %f\n", stats.getRecall(), stats.getPrecision(), stats.getFMeasure() );
		System.out.printf( "TP: %d, TN: %d, FP: %d, FN: %d\n", tp, tn, fp, fn );
	}
	
	/**
	 * Tests the classifier with the validation data and output performance statistics
	 */
	public void validate() {
		List<DNAToken> set = dataset.getValidationSet();
		int tp = 0;
		int fp = 0;
		int tn = 0;
		int fn = 0;
		if ( set.size() == 0 ) {
			throw new RuntimeException( "The validation set size is zero!" );
		}
		else if (!isTrained) {
			throw new RuntimeException( "The classifier is not trained yet to be tested!" );
		}
		else {
			try {
				List<List<DNAToken>> docs = fromTokensToDocs(set);
				for (List<DNAToken> docTokens : docs) {
					List<SparseVector> windowVectors = getWindowVectors(docTokens);
					int i = 0;
					for ( SparseVector vector : windowVectors ) {
						//construct the feature vector for weka
						Map<String, Double> row = getWekaVector(vector);
						DNAToken token = docTokens.get(i++);
						String classValue = "";
						if ( token.getLabel().equals( NEGATIVE_CLASS ) )
							classValue = NEGATIVE_CLASS;
						else if ( token.getLabel().equals( POSITIVE_CLASS ) )
							classValue = POSITIVE_CLASS;
						else
							throw new RuntimeException( "Unknown class label!!" );
						
						String clfResult = clf.classifyInstance(row);
						if ( classValue.equals( POSITIVE_CLASS ) && clfResult.equals( POSITIVE_CLASS ) ) 
							tp++;
						else if ( classValue.equals( POSITIVE_CLASS ) && clfResult.equals( NEGATIVE_CLASS ) ) 
							fn++;
						else if ( classValue.equals( NEGATIVE_CLASS ) && clfResult.equals( POSITIVE_CLASS ) ) 
							fp++;
						else if ( classValue.equals( NEGATIVE_CLASS ) && clfResult.equals( NEGATIVE_CLASS ) ) 
							tn++;
						else {
							throw new RuntimeException( "Something is wrong while computing performance stats!" );
						}
					}
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		TwoClassStats stats = new TwoClassStats(tp, fp, tn, fn);
		System.out.println( "++++++ Validation stats ++++++" );
		System.out.printf( "Recal: %f, Precision: %f, F1-measure: %f\n", stats.getRecall(), stats.getPrecision(), stats.getFMeasure() );
		System.out.printf( "TP: %d, TN: %d, FP: %d, FN: %d\n", tp, tn, fp, fn );
	}
	
	/**
	 * Organizes all the tokens in the collection into their respective documents.
	 * @param tokens a list of tokens that should be grouped by documents. So the tokens of each document will be grouped in a single list.
	 * @return a list of lists of tokens. Each list represents/contains the tokens of a document.
	 */
	public List<List<DNAToken>> fromTokensToDocs(List<DNAToken> tokens) {
		List<List<DNAToken>> docs = new ArrayList<List<DNAToken>>();
		
		int currentDocID = tokens.get(0).getInternalDocId();
		List<DNAToken> docTokens = new ArrayList<DNAToken>();
		for ( int i = 0; i <  tokens.size(); i++ ) {
			DNAToken token = tokens.get(i);
			if ( token.getInternalDocId() == currentDocID && i != tokens.size()-1 ) {
				docTokens.add(token);
			}
			else {
				if ( i == tokens.size()-1 ) {
					docTokens.add(token);
					docs.add(docTokens);
				}
				else {
					currentDocID = token.getInternalDocId();
					docs.add(docTokens);
					docTokens = new ArrayList<DNAToken>();
					docTokens.add(token);
				}
			}
		}
		
		return docs;
	}
	
	/**
	 * The size of the window feature space
	 * @return
	 */
	public int getWindowFeatureSpaceSize() {
		return dataset.getFeatureSpaceSize() * (2*windowSize+1);
	}
}

























