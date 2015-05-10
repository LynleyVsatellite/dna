package dna.textmining;

import java.util.Map;

public abstract class DNAClassifier {
	
	public static final String POSITIVE_CLASS = "P"; 
	public static final String NEGATIVE_CLASS = "N"; 
	
	public abstract void updateData( Map<String, Double> row, String classValue );
	public abstract void updateClassifier();
	public abstract String classifyInstance( Map<String, Double> row );
	public abstract Map<String, Double> distributionForInstance(Map<String, Double> row);
}
