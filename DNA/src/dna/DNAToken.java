package dna;

import dna.features.SparseVector;

public class DNAToken {
	
	//Add features array, and label
	private String text;
	private int start_position;
	private int end_position;
	//either a positive class token or a negative class token
	private String label;
	private SparseVector features;
	//the position of the token within a document (1st token or 2nd token ... etc)
	private int index;
	//the id of the token in the document collection. (-1 if not available, e.g. classifying a new document from outside the app)
	private int id; 
	//The ID of the document (per a single dna database file) that this token belongs to. (-1 if not available, e.g. classifying a new document from outside the app)
	private int docId;
	//This id is used to identify documents when using many dna files, i.e. to avoid duplicates.
	//in other words this is to identify the document from the entire documents collection. (-1 if not available, e.g. classifying a new document from outside the app)
	private int internalDocId;
	//if the token has been classified then this indicates the probability that this token belongs to the positive class.
	//if it hasn't been classifier, then it will be -1;
	private double prediction_probability;
	
	public DNAToken() {
		features = new SparseVector();
		id = -1;
		docId = -1;
		internalDocId = -1;
		prediction_probability = -1;
	}
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public int getStart_position() {
		return start_position;
	}
	public void setStart_position(int start_position) {
		this.start_position = start_position;
	}
	public int getEnd_position() {
		return end_position;
	}
	public void setEnd_position(int end_position) {
		this.end_position = end_position;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public SparseVector getFeatures() {
		return features;
	}

	public void setFeatures(SparseVector features) {
		this.features = features;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * @return The ID of the document that this token belongs to.
	 */
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
	
	public String toString() {
		return getText();
	}

	public double getPrediction_probability() {
		return prediction_probability;
	}

	public void setPrediction_probability(double prediction_probability) {
		this.prediction_probability = prediction_probability;
	}
	
	

}
