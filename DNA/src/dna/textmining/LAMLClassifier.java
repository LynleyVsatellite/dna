package dna.textmining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import la.matrix.Matrix;
import la.matrix.SparseMatrix;
import dna.features.SparseVector;

public class LAMLClassifier extends DNAClassifier {
	
	private boolean upToDate;
	private ml.classification.Classifier clf;
	private List<SparseVector> data;
	private List<Integer> labels;
	private boolean allowUpdate;
	
	public LAMLClassifier(ml.classification.Classifier clf) {
		this.clf = clf;
		data = new ArrayList<SparseVector>();
		labels = new ArrayList<Integer>();
		allowUpdate = true;
	}


	@Override
	public void updateData(SparseVector vec, String classValue) {
		if (allowUpdate) {
			data.add(vec);
			if ( classValue.equals( DNAClassifier.POSITIVE_CLASS ) )
				labels.add(1);
			else
				labels.add(0);
			upToDate = false;
		}
		else {
			throw new RuntimeException( "You can't change the data after training the classifier. You need to create a new classifier." );
		}
		
	}

	public void updateClassifier() {
		if ( !upToDate ) {
			Matrix M = toLAMLSparseMatrix( data );
			clf.feedData(M);
			clf.feedLabels(ListUtils.asIntArray(labels));
			clf.train();
			upToDate = true;
			allowUpdate = false;
			data.clear();//to free the memory!
			labels.clear();
		}
	}

	@Override
	public String classifyInstance(SparseVector vec) {
		
		if ( !upToDate ) {
			updateClassifier();
		}
		
		List<SparseVector> X_test = new ArrayList<SparseVector>();
		X_test.add(vec);
		Matrix M = toLAMLSparseMatrix( X_test );
		int[] lamlPreds = clf.predict( M );
		
		if (lamlPreds.length > 1)
			throw new RuntimeException( "The size of the prediction array is more than 1!!" );
		
		if ( lamlPreds[0] == 0 )
			return DNAClassifier.NEGATIVE_CLASS;
		else if ( lamlPreds[0] == 1 )
			return DNAClassifier.POSITIVE_CLASS;
		else 
			throw new RuntimeException( "The classification result is strange!" );
		
	}

	@Override
	public Map<String, Double> distributionForInstance(SparseVector vec) {
		Map<String, Double> result = new HashMap<String, Double>();
		if ( !upToDate ) {
			updateClassifier();
		}
		
		List<SparseVector> X_test = new ArrayList<SparseVector>();
		X_test.add(vec);
		Matrix M = toLAMLSparseMatrix( X_test );
		double[][] probs = clf.predictLabelScoreMatrix(M).getData();
		probs = toStandardProbsArray(probs);
		if ( probs.length > 2 )
			throw new RuntimeException( "The size of the probs array is more than 2!!" );
		
		if ( probs[0].length > 1 || probs[1].length > 1 )
			throw new RuntimeException( "More than two samples are classified!!" );
		
		result.put(POSITIVE_CLASS, probs[1][0]);
		result.put(NEGATIVE_CLASS, probs[0][0]);
		
		return result;
	}

	@Override
	public void save() {
		// TODO Auto-generated method stub
	}

	
	public static Matrix toLAMLSparseMatrix( List<SparseVector> data ) {
		if ( data.size() == 0 || data.get(0).size() == 0 ) 
			throw new RuntimeException( "The dataset is emtpy!" );
		
		int numRows = data.size();
		int numColumns = data.get(0).size();
		List<Integer> rInd = new ArrayList<Integer>();
		List<Integer> cInd = new ArrayList<Integer>();
		List<Double> vals = new ArrayList<Double>();

		for ( int i = 0; i < data.size(); i++ ) {
			SparseVector v = data.get(i);
			for (int j = 0; j < v.getIndices().length; j++ ) {
				rInd.add(i);
			}
			
			cInd.addAll( ListUtils.asList( v.getIndices() ) );
			vals.addAll( ListUtils.asList( v.getNonSparseValues() ) );
		}
		Matrix m = new SparseMatrix( ListUtils.asIntArray( rInd ), 
				ListUtils.asIntArray( cInd ), ListUtils.asDoubleArray(vals), numRows, numColumns );
		return m;
		
	}
	
	public static double[][] toStandardProbsArray( double[][] lamlScores ) {

		double[][] x = MathUtils.transformMatrix(lamlScores);
		double[] temp = x[1];
		x[1] = x[0];
		x[0] = temp;
		
		return x;
	}
	
}
