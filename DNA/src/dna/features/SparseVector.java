package dna.features;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dna.textmining.ListUtils;

/**
 * An implementation of a sparse vector to hold the feature vectors.
 * 
 */

//TODO add put() to make creating a sparse vector with couple elements fast

public class SparseVector implements Iterable<Double> {
	
	private Map<Integer, Double> ST;
	private int currentIndex;
	
	/**
	 * 
	 * @param size the size of the vector. 
	 * Note that this corresponds to the total number of values (sparse and non-sparse) in this vector.
	 * @param indices the indices of the non-zero elements.
	 * @param values the non-zero elements. Note that {@code values} and {@code indices} must have the same length.
	 */
	public SparseVector( int size, int[] indices, double[] values ) {
		ST = new LinkedHashMap<Integer, Double>();
		currentIndex = size;
		
		if ( indices.length != values.length )
			throw new RuntimeException( "The number of values and indices is not the same!" );
		
		if ( size < values.length )
			throw new RuntimeException( "The size of the vector is less than the number of non-sparse values!" );
		
		for ( int i = 0; i < values.length; i++ ) {
			ST.put(indices[i], values[i]);
		}
	}
	
	public SparseVector( int size ) {
		this(size, new int[0], new double[0]);
	}
	
	public SparseVector() {
		this(0);
	}
	
	public void add( double value ) {
		if (value != 0.0)
			ST.put(currentIndex, value);
		currentIndex++;
	}
	
	public void addAll(SparseVector vector) {
		for ( double value : vector ) {
			add(value);
		}
	}
	
	public void addAll(double[] list) {
		for ( double value : list ) {
			add(value);
		}
	}
	
	public void addAll(List<Double> list) {
		for ( double value : list ) {
			add(value);
		}
	}

	public int size() {
		return currentIndex;
	}

	public boolean isEmpty() {
		if (currentIndex == 0) 
			return true;
		else 
			return false;
	}

	public double[] toArray() {
		double[] array = new double[ size() ];
		for ( int i = 0; i < size(); i++ ) {
			if ( ST.containsKey(i) ) {
				array[i] = ST.get(i);
			}
		}
		return array;
	}

	public void clear() {
		currentIndex = 0;
		ST.clear();
	}

	public Double get(int index) {
		if (index < 0 || index >= currentIndex)
			throw new RuntimeException("Illegal index");
		if (ST.containsKey(index))
			return ST.get(index);
		else
			return 0.0;
	}

	public void remove(int index) {
		if (index < 0 || index >= currentIndex)
			throw new RuntimeException("Illegal index");
		
		if (ST.containsKey(index)) 
			ST.remove(index);
		for ( int i = index+1; i < size(); i++ ) {
			if ( ST.containsKey(i) ) {
				Double value = ST.get(i);
				ST.put(i-1, value);
				ST.remove(i);
			}
				
		}
		
		currentIndex--;
	}

	public Iterator<Double> iterator() {
		return new SparseVectorIterator(this);
	}
	
	private class SparseVectorIterator implements Iterator<Double> {
		
		SparseVector vector;
		int position;
		
		SparseVectorIterator(SparseVector vector) {
			this.vector = vector;
			position = 0;
		}

		public boolean hasNext() {
			if (position < vector.size())
				return true;
			else
				return false;
		}

		public Double next() {
			return vector.get(position++);
		}

		public void remove() {
			vector.remove(position);
		}
		
	}
	
	/**
	 * Returns the indices of the non-sparse values.
	 * @return
	 */
	public int[] getIndices() {
		return ListUtils.asIntArray(ST.keySet());
	}

	/**
	 * Returns the non-sparse values.
	 * @return
	 */
	public double[] getNonSparseValues() {
		double[] v = new double[ ST.size() ];
		int i = 0;
		for ( int key : ST.keySet() ) {
			v[i++] = ST.get(key);
		}
		
		return v;
	}

	public Map<Integer, Double> getHoldingHashTable() {
		return ST;
	}

	public void setHoldingHashTable(Map<Integer, Double> hashTable) {
		ST = hashTable;
	}
	
	public void setCurrentIndex( int currentIndex ) {
		this.currentIndex = currentIndex;
	}
	
	public SparseVector getACopy() {
		SparseVector copy = new SparseVector();
		for ( double value : this ) {
			copy.add(value);
		}
		return copy;
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[ ");
		for ( int i = 0; i < size(); i++ ) {
			if ( i == size() - 1 ) 
				buffer.append( get(i) + " " );
			else
				buffer.append( get(i) + ", " );
		}
		buffer.append("]");
		return buffer.toString();
	}

}
