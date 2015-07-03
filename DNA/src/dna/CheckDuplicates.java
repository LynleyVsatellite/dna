package dna;

import java.util.ArrayList;
import java.util.List;


/**
 * This class checks for duplicate annotated documents. 
 *
 */
public class CheckDuplicates {

	public static void main(String[] args) {
		
		System.out.println( "Started..." );
		
		String file1 = "train_data/steffi.dna";
		String file2 = "train_data/okt.dna";

		List<Document> file1Docs = new ArrayList<Document>();
		List<Document> file2Docs = new ArrayList<Document>();

		DataAccess dataAccess = new DataAccess("sqlite", file1 );
		List<Document> documentsList = dataAccess.getDocuments();

		for (Document document : documentsList) {
//			List<SidebarStatement> statements = 
//					dataAccess.getStatementsPerDocumentId(document.getId());
//
//			for (SidebarStatement st : statements) {
//
//			}

			file1Docs.add( document );
			
		}
		dataAccess.closeFile();
		
		dataAccess = new DataAccess("sqlite", file2 );
		documentsList = dataAccess.getDocuments();

		for (Document document : documentsList) {
			file2Docs.add( document );
			
		}
		dataAccess.closeFile();
		
		System.out.println( "Checking for duplicates..." );
		int counter = 0;
		for ( Document docFile1 : file1Docs ) {
			for ( Document docFile2 : file2Docs ) {
				if ( docFile1.getText().equals(docFile2.getText()) )
					System.out.printf( "%d - %s\n", counter++, docFile1.getTitle() );
			}
		}
		
	}

}















