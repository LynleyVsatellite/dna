package dna.textmining;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class ARFFExporter {
	
	private BufferedWriter bw;
	
	public ARFFExporter(String filename, int numberOfFeatures) {
		try {
			bw = new BufferedWriter( new FileWriter(filename) );
			
			bw.write( "@relation dataset\n" );
			for ( int i = 0; i < numberOfFeatures; i++ ) {
				bw.write( "@attribute " + i + " numeric\n" );
			}
			bw.write( "@attribute " + numberOfFeatures + " numeric\n" );//class attribute
			bw.write( "@data\n" );
			bw.flush();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void append(double[] array, int label) {
		try {
			bw.write( "{" );
			for ( int i = 0; i < array.length; i++ ) {
				if ( array[i] != 0 )
					bw.write( i + " " + array[i] + ", " );
			}
			
			bw.write( array.length + " " + label + "}\n" );
			
			bw.flush();
			 
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void close() {
		try {
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
