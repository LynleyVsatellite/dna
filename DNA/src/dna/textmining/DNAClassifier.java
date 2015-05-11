package dna.textmining;

import java.util.Map;

public abstract class DNAClassifier {
	
	public static final String POSITIVE_CLASS = "P"; 
	public static final String NEGATIVE_CLASS = "N"; 
	
	public abstract void updateData( double[] vec, String classValue );
	public abstract void updateClassifier();
	public abstract String classifyInstance( double[] vec );
	public abstract Map<String, Double> distributionForInstance(double[] vec);
}
