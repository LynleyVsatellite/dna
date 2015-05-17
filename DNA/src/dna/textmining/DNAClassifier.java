package dna.textmining;

import java.io.Serializable;
import java.util.Map;

import dna.features.SparseVector;

public abstract class DNAClassifier implements Serializable {
	
	private static final long serialVersionUID = 7663068090586568448L;
	public static final String POSITIVE_CLASS = "P"; 
	public static final String NEGATIVE_CLASS = "N"; 
	
	public abstract void updateData( SparseVector sparseVector, String classValue );
	public abstract void updateClassifier();
	public abstract String classifyInstance( SparseVector sparseVector );
	public abstract Map<String, Double> distributionForInstance( SparseVector sparseVector );
	public abstract void save();
}
