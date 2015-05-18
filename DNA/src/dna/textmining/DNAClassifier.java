package dna.textmining;

import java.io.Serializable;
import java.util.Map;

import dna.features.SparseVector;

/**
 * The interface to be implemented if one needs to use another machine learning library.
 * A {@link DNAClassifier} is used by {@link TokenClassifier}. 
 */
public abstract class DNAClassifier implements Serializable {
	
	private static final long serialVersionUID = 7663068090586568448L;
	public static final String POSITIVE_CLASS = "P"; 
	public static final String NEGATIVE_CLASS = "N"; 
	
	/**
	 * Adds the input vector to the training dataset.
	 * @param sparseVector
	 * @param classValue accepts only binary classes. Use {@link DNAClassifier.POSITIVE_CLASS} or {@link DNAClassifier.NEGATIVE_CLASS}.
	 */
	public abstract void updateData( SparseVector sparseVector, String classValue );
	
	/**
	 * Trains the classifier.
	 */
	public abstract void updateClassifier();
	
	/**
	 * Classifies the sample. 
	 * @param sparseVector
	 * @return Either {@link DNAClassifier.POSITIVE_CLASS} or {@link DNAClassifier.NEGATIVE_CLASS}.
	 */
	public abstract String classifyInstance( SparseVector sparseVector );
	
	/**
	 * Returns the probability of the sample being from the positive class or the negative class.
	 * @param sparseVector
	 * @return The returned map should contain two keys: {@link DNAClassifier.POSITIVE_CLASS} and {@link DNAClassifier.NEGATIVE_CLASS}.
	 */
	public abstract Map<String, Double> distributionForInstance( SparseVector sparseVector );
	
	/**
	 * Saves the classifier
	 */
	public abstract void save();
}
