package dna.textmining;

import java.util.ArrayList;
import java.util.List;

import weka.classifiers.Classifier;
import weka.classifiers.functions.SimpleLogistic;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;

/**
 * Testing WEKA's performance.
 *
 */

public class TestClassifier {
	
	public static void main(String[] args) throws Exception {
		test();
		
	}
	
	public static void test() {
		System.out.println( "Buidling dataset" );
		int numb_features = 20000;
		FastVector features = new FastVector(numb_features + 1);
		List<String> plainFeatures = new ArrayList<String>();
		for ( Integer i = 0; i < numb_features; i++ ) {
			features.addElement( new Attribute(i.toString()) );
			plainFeatures.add(i.toString());
		}
		
		FastVector classesValues = new FastVector( 2 );
		classesValues.addElement("positive");
		classesValues.addElement("negative");
		features.addElement( new Attribute("Class", classesValues) );
		
		Instances data = new Instances("", features, 100);
		data.setClassIndex(data.numAttributes()-1);
		int number_of_samples = 10000;
		
		for ( int i = 0; i < number_of_samples; i++ ) {
			double vals[] = new double[numb_features];
			vals[5] = 1;
			vals[9] = 1;
			vals[1000] = 1;
			vals[15000] = 1;
			Instance instance = new Instance( 1.0, vals );
			Instance sparseInstance = new SparseInstance(instance);
			sparseInstance.setDataset(data);
			data.add(sparseInstance);
		}
		
		
		
		Classifier classifier = new SimpleLogistic();
		try {
			System.out.println( "Building classifier" );
			classifier.buildClassifier(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
