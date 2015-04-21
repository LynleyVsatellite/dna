package dna;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import dna.features.Vocabulary;


public class DNATextMiner {

	public static void main(String[] args) {
		System.out.println( "Started..." );
		String file1 = "/Users/rockyrock/Desktop/steffi.dna";
		String file2 = "/Users/rockyrock/Desktop/okt.dna";
		List<String> files = new ArrayList<String>();
		files.add(file1);
		files.add(file2);
		String classLabel = "Person";
		DNATextMiner textMiner = new DNATextMiner( new StanfordDNATokenizer() );
		textMiner.exportToCSV(files, classLabel, "trainset.csv", 0.5, 0.5, 0.0, 1);
		System.out.println( new Date() );
	}
	
	private DNATokenizer tokenzier;
	
	/**
	 * @param tokenzier the tokenizer that shall be used to tokenize the text.
	 */
	public DNATextMiner(DNATokenizer tokenzier) {
		this.tokenzier = tokenzier;
	}
	
	/**
	 * Exports data of the dna file into a CSV file to be used for training.
	 * @param files a list of paths to dna files.
	 * @param classLabel the named entity that shall be used as a positive class for training. 
	 * the value of this parameter is either "Person", "Organization" or "Concept".
	 * For example, the tokens of the statements in the dna file that are highlighted as "Person" 
	 * will be given the positive class label, while the rest of the tokens will have the negative
	 * class label.
	 * @param path where to save the file
	 * @param trainSetSize the percentage [0.0 , 1.0] of the documents to be used as training data.
	 * @param testSetSize the percentage [0.0 , 1.0] of the documents to be used as testing data.
	 * @param validationSetSize the percentage [0.0 , 1.0] of the documents to be used as validation data.
	 * 
	 */
	public void exportToCSV(List<String> files, String classLabel, String path,
			double trainSetSize, double testSetSize, double validationSetSize, int seed) {
		
		Random random = new Random(seed);
		List<Integer> docsIds = new ArrayList<Integer>();
		int internalDocId = 0;
		
		for ( String filePath : files ) {
			DataAccess dataAccess = new DataAccess("sqlite", filePath );
			List<Document> documentsList = dataAccess.getDocuments();
			for (Document doc : documentsList) {
				docsIds.add( internalDocId );
				internalDocId++;
			}
			dataAccess.closeFile();
		}
		
		//Sample some documents to be used as training, testing, validation.
		int numbTrainDocs = (int) (docsIds.size() * trainSetSize);
		int numbTestDocs  = (int) (docsIds.size() * testSetSize);
		Set<Integer> trainingDocsIds = new HashSet<Integer>();
		Set<Integer> testingDocsIds = new HashSet<Integer>();
		Set<Integer> validationDocsIds = new HashSet<Integer>();
		Collections.shuffle(docsIds, random);
		
		for( int i = 0; i < numbTrainDocs; i++ ) {
			trainingDocsIds.add( docsIds.get(i) );
		}
		
		//In case we only want to use a training/testing split without a validation set.
		if ( validationSetSize > 0.0 ) {
			for( int i = 0; i < numbTestDocs; i++ ) {
				int index = i + numbTrainDocs;
				testingDocsIds.add( docsIds.get( index ) );
			}
			
			for( int i = numbTrainDocs+numbTestDocs; i < docsIds.size(); i++ ) {
				validationDocsIds.add( docsIds.get(i) );
			}
		}
		else {
			for( int i = numbTrainDocs; i < docsIds.size(); i++ ) {
				testingDocsIds.add( docsIds.get( i ) );
			}
		}
		
		List<DNAToken> tokens = extract_data(files, classLabel);
		Map<String, Set<Integer>> trainTestValDocsIds = new HashMap<String, Set<Integer>>();
		trainTestValDocsIds.put("train", trainingDocsIds);
		trainTestValDocsIds.put("test", testingDocsIds);
		trainTestValDocsIds.put("validate", validationDocsIds);
		
		Vocabulary.buildVocabularyFile(tokens, trainTestValDocsIds);
		toCSVFile(tokens, trainTestValDocsIds, path);
	}
	
	/**
	 * Takes as input a a list of paths to dna files, then it tokenizes the text of the documents and assign a label for each token.
	 * Also the features of each token is generated.
	 * 
	 * @param files a list of paths to dna files.
	 * @param classLabel classLabel the named entity that shall be used as a positive class for training. 
	 * the value of this parameter is either "Person", "Organization" or "Concept".
	 * For example, the tokens of the statements in the dna file that are highlighted as "Person" 
	 * will be given the positive class label, while the rest of the tokens will have the negative
	 * class label.
	 * @return a list that contains the tokens.
	 */
	public List<DNAToken> getTokens(List<String> files, String classLabel) {
		return extract_data(files, classLabel);
	}
	
	/**
	 * The method that does all the preprocessing for the dna file and generates the tokens 
	 * and assign their labels.
	 * 
	 * @param files a list of paths to dna files.
	 * @param classLabel the named entity that shall be used as a positive class for training. 
	 * the value of this parameter is either "Person", "Organization" or "Concept".
	 * For example, the tokens of the statements in the dna file that are highlighted as "Person" 
	 * will be given the positive class label, while the rest of the tokens will have the negative
	 * class label.
	 * @return a list that contains the tokens.
	 */
	private List<DNAToken> extract_data( List<String> files, String classLabel ) {
		
		List<DNAToken> allTokens = new ArrayList<DNAToken>();
		int internalDocId = 0;
		int counter = 0;
		
		for ( String filePath : files ) {
		
			DataAccess dataAccess = new DataAccess("sqlite", filePath );
			List<Document> documentsList = dataAccess.getDocuments();
			
			for (Document document : documentsList) {
				List<DNAToken> docTokens = new ArrayList<DNAToken>();
				List<SidebarStatement> statements = 
						dataAccess.getStatementsPerDocumentId(document.getId());
				
				HashMap<Integer, Integer> statements_positions = new HashMap<Integer, Integer>();
				
				String docString = document.getText();
				
				//Store statements start and end positions
				for (SidebarStatement st : statements) {
					if (st.getType().equals(classLabel)) {
						
						if ( !statements_positions.containsKey( st.getStart() ) ) {
							statements_positions.put(st.getStart(), st.getStop());
						}
						else {
							//Store the statement with the larger range
							if ( statements_positions.get( st.getStart() ) < st.getStop() ) {
								statements_positions.put( st.getStart(),  st.getStop());
							}
						}
					}
				}
				
				//Remove the short highlighted statements inside a larger statement,
				//i.e. just select the wider statement to avoid redundant tokens.
				statements_positions = removeInnerStatements( statements_positions );
				
				StringBuffer normalText = new StringBuffer();
				StringBuffer statementText = new StringBuffer();
				int normalTextStartPosition = 0;
				//buffer statements and normal text and then tokenize them
				for( int index = 0; index < docString.length(); index++ ) {
					if ( statements_positions.containsKey(index) ) {
						
						//tokenize and flush the normal text and clear its buffer.
						List<DNAToken> temp_tokens = getTokenzier().tokenize(normalTextStartPosition,
								normalText.toString());
						temp_tokens = giveLabels(temp_tokens, "N");
						docTokens.addAll( temp_tokens );
						normalText = new StringBuffer();
						
						//tokenize and flush the statement text and then clear its buffer.
						int start_pos = index;
						int end_pos = statements_positions.get(start_pos);
						index = end_pos-1;// update index to continue buffering normal text after statement
						statementText.append( docString.substring( start_pos, end_pos ) );
						
						temp_tokens = getTokenzier().tokenize(start_pos, statementText.toString());
						temp_tokens = giveLabels(temp_tokens, "P");
						docTokens.addAll( temp_tokens );
						statementText = new StringBuffer();
						normalTextStartPosition = end_pos;
					}
					else {
						normalText.append( docString.charAt(index) );
					}
				}
				
				if ( normalText.length() > 0 ) {
					List<DNAToken> temp_tokens = getTokenzier().tokenize(normalTextStartPosition,
							normalText.toString());
					temp_tokens = giveLabels(temp_tokens, "N");
					docTokens.addAll( temp_tokens );
					normalText = new StringBuffer();
				}
				
				for ( int i = 0; i < docTokens.size(); i++ ) {
					DNAToken tok = docTokens.get(i);
					tok.setDocId( document.getId() );
					tok.setInternalDocId(internalDocId);
					tok.setId(i);
				}
				internalDocId++;
				allTokens.addAll(docTokens);
				
				System.out.println( "Processed doc: " + counter );
				counter++;
//				if(counter == 2) break;
			}
			
			dataAccess.closeFile();
//			break;
		}
		
//		FeatureFactory featFact = new FeatureFactory(allTokens);
//		allTokens = featFact.addFeatures();
//		
//		if(exportToCSV)
//			toCSVFile(allTokens, featFact.getNumberOfFeatures(), files.get(0));
		
		return allTokens;
	}
	
	/**
	 * It checks if a word is from a specific class (Person, Organization, Concept)
	 * @param wStart the start caret position of the word in the text.
	 * @param wStop the end caret position of the word in the text.
	 * @param ranges an array that contains the start and end position for every highlighted class/statement in a document.
	 * @return true if the word is in one of the ranges of the respective class.
	 */
	private static boolean isFromType( int wStart, int wStop, List<int[]> ranges ) {
		
		boolean isFromType = false;
		
		for (int[] range : ranges) {
			
			int pStart = range[0];
			int pStop = range[1];
			
			if ( (wStart >= pStart) && (wStop <= pStop) ) {
				isFromType = true;
				break;
			}
			
		}
		
		return isFromType;
		
	}
	
	/**
	 * Removes the statements that fall in a larger statement range. In other words,
	 * if a statement is highlighted and is part of a wider highlighted statement from the same category,
	 * then only the wider statement is used. For example, the statement "Mr.X" can be highlighted 
	 * as a Person and the statement "The minister of defense Mr.X" is also highlighted as a statement.
	 * This methods removes the extra "Mr.X" statement because its tokens are redundant.
	 * @param statements_positions the keys of this hash table are the start caret positions
	 * of the statements in the document text, while the values are the end positions.
	 * @return the statement positions with the redundant statements removed.
	 */
	private static HashMap<Integer, Integer> removeInnerStatements( HashMap<Integer, Integer> 
		statements_positions ) {
		ArrayList<Integer> tobe_removed = new ArrayList<Integer>();
		
		for (Integer start : statements_positions.keySet()) {
			int end = statements_positions.get(start);
			
			for (Integer temp_start : statements_positions.keySet()) {
				int temp_end = statements_positions.get(temp_start);
				
				if ( ( start >= temp_start && end <= temp_end) && ( start != temp_start || end !=temp_end ) ) {
					tobe_removed.add(start);
				}
				
			}
			
		}
		
		for (int key : tobe_removed) {
			statements_positions.remove(key);
		}
		
//		System.out.println(statements_positions);
		
		return statements_positions;
	}

	public DNATokenizer getTokenzier() {
		return tokenzier;
	}

	public void setTokenzier(DNATokenizer tokenzier) {
		this.tokenzier = tokenzier;
	}
	
	private static List<DNAToken> giveLabels( List<DNAToken> tokens, String label ) {
		
		for (DNAToken token : tokens) {
			token.setLabel(label);
		}
		
		return tokens;
	}

	public static void test() {
		String file = "/Users/rockyrock/Desktop/file.dna";
		DataAccess dataAccess = new DataAccess("sqlite", file );
		ArrayList<Document> documentsList = dataAccess.getDocuments();
		
		for (Document document : documentsList) {
			List<SidebarStatement> statements = 
					dataAccess.getStatementsPerDocumentId(document.getId());
			String docString = document.getText();
			
			for (SidebarStatement st : statements) {
				if (st.getType().equals("Person")) {
					String txt = docString.substring(st.getStart(), st.getStop());
					System.out.println(txt);
				}
			}
			
			break;
		}
	}
	
	/**
	 * Exports the tokens of the documents into a CSV file that contains the features
	 * for each token and its label to be used for machine learning algorithm.
	 * @param tokens the tokens of the documents.
	 * @param trainTestValDocsIds a map that contains 3 keys ("train", "test", "validate"). Each
	 * key map to a set that contains the IDs of the documents that are used for training, testing, and validation.
	 * @param path where to save the CSV file.
	 */
	private static void toCSVFile(List<DNAToken> tokens, 
			Map<String, Set<Integer>> trainTestValDocsIds, String path) {
		FeatureFactory featFact = new FeatureFactory(tokens);
		tokens = featFact.addFeatures();
		int numberOfFeatures = featFact.getTotalNumberOfFeatures();
		
		Set<Integer> trainDocsIDs = trainTestValDocsIds.get("train");
		Set<Integer> testDocsIDs = trainTestValDocsIds.get("test");
		Set<Integer> valDocsIDs = trainTestValDocsIds.get("validate");
		
		System.out.println("Saving as CSV file ...");
//		File oldFile = new File(path);
//		File csvFile = new File( oldFile.getAbsoluteFile() +  ".csv" );
		File csvFile = new File( path );
		System.out.println(csvFile.getAbsolutePath());
		if (!csvFile.exists()) {
			try {
				csvFile.createNewFile();
				
				FileWriter fw = new FileWriter(csvFile.getAbsoluteFile());
				
				BufferedWriter bw = new BufferedWriter(fw);
				
				//Write header
				bw.write("token,id,docId,internalDocId,start_position,end_position,");
				
				for (int i = 0; i < numberOfFeatures; i++) {
					bw.write("f"+i+",");
				}
				
				bw.write("label,dataset\n");
				
				for (DNAToken tok : tokens) {
					bw.write(tok.getText() + "," + tok.getId() + 
							"," + tok.getDocId() + "," + tok.getInternalDocId() + "," + tok.getStart_position() +
							"," + tok.getEnd_position() + ",");
					
					if (tok.getFeatures().size() != numberOfFeatures)
						throw new RuntimeException("The token's feature vector size is different from the total number of features!");
					
					for (Double f : tok.getFeatures()) {
						bw.write(f.toString() + ",");
					}
					
					bw.write(tok.getLabel() + ",");
					
					if( trainDocsIDs.contains( tok.getInternalDocId() ) )
						bw.write("train" + "\n");
					if( testDocsIDs.contains( tok.getInternalDocId() ) )
						bw.write("test" + "\n");
					if( valDocsIDs.contains( tok.getInternalDocId() ) )
						bw.write("validate" + "\n");
					
				}
				
				bw.close();
				System.out.println("Done.");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			System.err.println("CSV file exists!");
		}
		
	}
	
}











