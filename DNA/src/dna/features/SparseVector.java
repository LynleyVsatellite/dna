package dna.features;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * An implementation of a sparse vector to hold the feature vectors.
 * 
 */

public class SparseVector implements Iterable<Double> {
	
	private Map<Integer, Double> ST;
	private int currentIndex;
	
	public SparseVector( int size ) {
		ST = new HashMap<Integer, Double>();
		currentIndex = size;
	}
	
	public SparseVector() {
		this(0);
	}
	
	public void add( double value ) {
		if (value != 0.0)
			ST.put(currentIndex, value);
		currentIndex++;
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


}
