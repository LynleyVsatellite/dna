package dna.textmining;

import java.util.List;

import dna.features.SparseVector;

/**
 * A wrapper around a dataset in the format: X, y where X is the design matrix and y is a vector that holds
 * the class label. 
 *
 */
public interface SparseDataset {

	public List<SparseVector> getX();
	public double[] getY();
	
}
