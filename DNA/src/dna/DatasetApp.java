package dna;

import java.util.Date;
import java.util.List;


public class DatasetApp {
	public static void main(String[] args) {

		System.out.println( "Started..." );
		System.out.println( new Date() );
		String file1 = "/Users/rockyrock/Desktop/s0.dna";
		
		DataAccess dataAccess = new DataAccess("sqlite", file1 );
		List<Document> documentsList = dataAccess.getDocuments();
		
		int counter = 0;
		
		System.out.println( documentsList.size() );
		
//		for (Document document : documentsList) {
//			if ( counter > 15 )
//				dataAccess.removeDocument(document.getId());
//			counter++;
//		}
		dataAccess.closeFile();
	}
}
