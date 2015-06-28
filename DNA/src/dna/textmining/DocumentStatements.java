package dna.textmining;

import java.util.ArrayList;
import java.util.List;

/**
 * This class encapsulates the ApprovedStatements of a specific document.
 *
 */
public class DocumentStatements {
	
	private List<ApprovedStatement> statements;
	private int docId;
	private int internalDocId;
	
	public DocumentStatements() {
		statements = new ArrayList<ApprovedStatement>();
	}
	
	public void add(ApprovedStatement statementTokens) {
		statements.add( statementTokens );
	}
	
	public List<ApprovedStatement> getStatements() {
		return statements;
	}
	public void setStatements(List<ApprovedStatement> statements) {
		this.statements = statements;
	}
	public int getDocId() {
		return docId;
	}
	public void setDocId(int docId) {
		this.docId = docId;
	}
	public int getInternalDocId() {
		return internalDocId;
	}
	public void setInternalDocId(int internalDocId) {
		this.internalDocId = internalDocId;
	}

}
