package dna.classification;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NominalToBinary;
import weka.filters.unsupervised.attribute.Normalize;

public class GeneralClassifier implements Serializable {

	private static final long serialVersionUID = -7529324492164481302L;
	private Classifier classifier;
	private Instances data;
	private NominalToBinary nominalToBinaryFilter;
	private Normalize normalizeFilter;
	private boolean normalize;
	private boolean upToDate;
	private int numbFeatures;
	private LinkedHashSet<String> classes;

	public GeneralClassifier(Classifier cl, LinkedHashSet<String>features, 
			LinkedHashSet<String> classes, boolean normalize) {
		this.classifier = cl;
		nominalToBinaryFilter = new NominalToBinary();
		this.normalize = normalize;
		normalizeFilter = new Normalize();
		String nameOfDataset = "GeneralClassification";
		
		numbFeatures = features.size();
		this.classes = classes;
		
		FastVector attributes = new FastVector(features.size() + 1);
		for ( String feature : features ) {
			attributes.addElement( new Attribute(feature) );
		}
		
		FastVector classesValues = new FastVector( classes.size() );
		for ( String c : classes ) {
			classesValues.addElement(c);
		}
		
		attributes.addElement( new Attribute("Class", classesValues) );
		data = new Instances(nameOfDataset, attributes, 100);
		data.setClassIndex(data.numAttributes()-1);
		
	}
	
	public Instance makeInstance(Map<String, Double> row, Instances relation)
			throws Exception {
		
		if ( row.size() != numbFeatures ) 
			throw new Exception("The specified row's features are not of" +
					" the same number as in the dataset definition");
		
		Instance instance = new Instance(row.size() + 1);
		Attribute att;
		
		for ( String feature : row.keySet() ) {
			att = relation.attribute(feature);
			instance.setValue(att, row.get(feature));
		}
		
		Instance sparseInstance = new SparseInstance(instance);
		sparseInstance.setDataset(relation);
		
		return sparseInstance;
	}
	
	public void updateData( Map<String, Double> row, String classValue )
			throws Exception {
		Instance instance = makeInstance(row, data);
		instance.setClassValue(classValue);
		data.add(instance);
		upToDate = false;
	}
	
	public void updateClassifier() throws Exception {
		if ( !upToDate ) {
			
			nominalToBinaryFilter.setInputFormat(data);
			nominalToBinaryFilter.setAttributeIndices("last");
			Instances filterdInstances = Filter.useFilter(data, nominalToBinaryFilter); 
			
			if ( normalize ) {
				normalizeFilter.setInputFormat(filterdInstances);
				filterdInstances = Filter.useFilter(filterdInstances, normalizeFilter);
			}
			
			System.err.println("Updating the classifier.");
			classifier.buildClassifier(filterdInstances);
			System.err.println("Done updating the classifier.");
			upToDate = true;
		}
	}
	
	public String classifyInstance( Map<String, Double> row ) throws Exception {
		if ( data.numInstances() == 0 ) {
			throw new Exception( "No classifier available" );
		}
		
		if ( !upToDate ) {
			updateClassifier();
		}
		
		Instances testset = data.stringFreeStructure();
		Instance testInstance = makeInstance(row, testset);
		
		nominalToBinaryFilter.input(testInstance);
		Instance filteredInstance = nominalToBinaryFilter.output();
		
		if ( normalize ) {
			normalizeFilter.input(filteredInstance);
			filteredInstance = normalizeFilter.output();
		}
		
		double predication = classifier.classifyInstance(filteredInstance);
		String classValue = data.classAttribute().value((int)predication);
		return classValue;
	}
	
	public Map<String, Double> distributionForInstance(Map<String, Double> row)
		throws Exception {
		Map<String, Double> classesDists = new LinkedHashMap<String, Double>();
		
		if ( data.numInstances() == 0 ) {
			throw new Exception( "No classifier available" );
		}
		
		if ( !upToDate ) {
			updateClassifier();
		}
		
		Instances testset = data.stringFreeStructure();
		Instance testInstance = makeInstance(row, testset);
		
		nominalToBinaryFilter.input(testInstance);
		Instance filteredInstance = nominalToBinaryFilter.output();
		
		if ( normalize ) {
			normalizeFilter.input(filteredInstance);
			filteredInstance = normalizeFilter.output();
		}
		
		double[] distributions = classifier.distributionForInstance(filteredInstance);
		
		int i = 0;
		for ( String c : classes ) {
			double classDist = distributions[i];
			classesDists.put(c, classDist);
			i++;
		}
		
		return classesDists;
	}
	
	public static void main(String[] args) throws Exception {
		BufferedReader datasetReader = new BufferedReader( new FileReader("datasets/iris.csv") );
		BufferedReader testsetReader = new BufferedReader( new FileReader("datasets/iris-test.csv") );
		String line;
		
		LinkedHashSet<String> features = new LinkedHashSet<String>();
		LinkedHashSet<String> classes = new LinkedHashSet<String>();
		
		features.add("sepallength");
		features.add("sepalwidth");
		features.add("petallength");
		features.add("petalwidth");
		
		classes.add("Iris-setosa");
		classes.add("Iris-versicolor");
		classes.add("Iris-virginica");
		
//		Classifier cl = new LibSVM();
//		
//		cl.setOptions(new String[] {"-B"});
		
		GeneralClassifier classifier = 
				new GeneralClassifier(new J48(), features, classes, false);
		
		while( (line = datasetReader.readLine()) != null ) {
			Map<String, Double> row = new LinkedHashMap<String, Double>();
			String[] values = line.split(",");

			row.put("sepallength", Double.parseDouble(values[0]));
			row.put("sepalwidth", Double.parseDouble(values[1]));
			row.put("petallength", Double.parseDouble(values[2]));
			row.put("petalwidth", Double.parseDouble(values[3]));
			
			classifier.updateData(row, values[4]);
		}
		
		classifier.updateClassifier();
		
		while( (line = testsetReader.readLine()) != null ) {
			Map<String, Double> row = new LinkedHashMap<String, Double>();
			String[] values = line.split(",");

			row.put("sepallength", Double.parseDouble(values[0]));
			row.put("sepalwidth", Double.parseDouble(values[1]));
			row.put("petallength", Double.parseDouble(values[2]));
			row.put("petalwidth", Double.parseDouble(values[3]));
			
			System.out.printf( "True = %s, Predicted = %s\n", values[4], classifier.classifyInstance(row) );
			System.out.println( classifier.distributionForInstance(row) );
		}
		
	}
	
}













