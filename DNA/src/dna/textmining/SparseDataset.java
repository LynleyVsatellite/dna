package dna.textmining;

import java.util.List;

import dna.features.SparseVector;

public interface SparseDataset {

	public List<SparseVector> getX();
	public double[] getY();
//	public List<SparseVector> getX_test();
//	public double[] getY_test();
//	public List<SparseVector> getX_val();
//	public double[] getY_val();
	
}
