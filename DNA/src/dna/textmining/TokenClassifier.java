package dna.textmining;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import weka.classifiers.Classifier;
import weka.classifiers.functions.Logistic;
import dna.DNAToken;
import dna.features.SparseVector;

//TODO Also add the feature of the previous prediction! Make sure to modify the number of feature space and featureNames!


public class TokenClassifier implements Serializable {
	
	private static final long serialVersionUID = -8585609561306124520L;
	
	private GeneralClassifier clf;
	private Classifier wekaClf;
	private Dataset dataset;
	private boolean isTrained;
	public static final String POSITIVE_CLASS = "P"; 
	public static final String NEGATIVE_CLASS = "N"; 
	private int windowSize;
	private LinkedHashSet<String> featuresNames;
//	private Map<Integer, DNAToken> tokenIdToToken;
	/**
	 * Creates a TokenClassifier with a default Logistic Regression classifier and window size of 1 token
	 *  (i.e one token before the current token and one after).
	 * @param dataset
	 */
	public TokenClassifier(Dataset dataset) {
		this(dataset, new Logistic(), 1);
	}
	
	/**
	 * 
	 * @param dataset
	 * @param wekaClf
	 * @param windowSize
	 */
	public TokenClassifier(Dataset dataset, Classifier wekaClf, int windowSize) {
		
		this.isTrained = false;
		this.dataset = dataset;
		this.wekaClf = wekaClf;
		this.windowSize = windowSize;
		LinkedHashSet<String> classes = new LinkedHashSet<String>();
//		tokenIdToToken = new HashMap<Integer, DNAToken>();
		classes.add( POSITIVE_CLASS );
		classes.add( NEGATIVE_CLASS );
		featuresNames = new LinkedHashSet<String>();
		
		for ( Integer i = 0; i < dataset.getFeatureSpaceSize() * windowSize; i++ )
			featuresNames.add( i.toString() );
//		
//		for (DNAToken token : dataset.getTrainingSet()) {
//			if ( tokenIdToToken.containsKey( token.getId() ) )
//				throw new RuntimeException( "Two tokens have the same ID!!!" );
//			else
//				tokenIdToToken.put(token.getId(), token);
//		}
//		for (DNAToken token : dataset.getTestSet()) {
//			if ( tokenIdToToken.containsKey( token.getId() ) )
//				throw new RuntimeException( "Two tokens have the same ID!!!" );
//			else
//				tokenIdToToken.put(token.getId(), token);
//		}
//		for (DNAToken token : dataset.getValidationSet()) {
//			if ( tokenIdToToken.containsKey( token.getId() ) )
//				throw new RuntimeException( "Two tokens have the same ID!!!" );
//			else
//				tokenIdToToken.put(token.getId(), token);
//		}
		
		clf = new GeneralClassifier(wekaClf, featuresNames, classes, true);
		
	}
	
	public double classify(String text) {
		if (isTrained) {
			return 0.0;
		}
		else {
			throw new RuntimeException( "The classifier is not trained!" );
		}
	}
	
	private List<SparseVector> getWindowVectors( List<DNAToken> docTokens ) {
		List<SparseVector> vectors = new ArrayList<SparseVector>();

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
		}
		
		//TODO Also add the feature of the previous prediction! Make sure to modify the number of feature space and featureNames!
		
		return vectors;
	}
	
	public void train() {
		List<DNAToken> trainSet = dataset.getTrainingSet();
		List<List<DNAToken>> docs = fromTokensToDocs(trainSet);
		
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
			}
			
			clf.updateClassifier();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private Map<String, Double> getWekaVector(SparseVector vector) {
		Map<String, Double> row = new HashMap<String, Double>();
		
		if ( vector.size() != featuresNames.size() )
			throw new RuntimeException( "The size of the feature vector is different from the number of feautres!" );
		else {

			int i = 0; 
			for ( String featureName : featuresNames ) {
				row.put(featureName, vector.get(i));
				i++;
			}
			
		}
		
		return row;
	}

	public void test() {
		List<DNAToken> testSet = dataset.getTestSet();
		//TODO throw an expcetion if the test set size is zero! The same for the validation set!
		try {
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
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
}

























