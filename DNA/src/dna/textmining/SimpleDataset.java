package dna.textmining;

import java.util.List;

import dna.features.SparseVector;

public class SimpleDataset implements SparseDataset {
	
	/**
	 * The samples.
	 */
	private List<SparseVector> X;

	/**
	 * The groundtruth for the samples
	 */
	private double[] y;

	public SimpleDataset(List<SparseVector> X, double[] y) {
		this.X = X;
		this.y = y;
	}
	
	@Override
	public List<SparseVector> getX() {
		return X;
	}

	@Override
	public double[] getY() {
		return y;
	}

}
