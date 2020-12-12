package cecs429.documents;

import java.io.Reader;

/**
 * Represents a document in an index.
 */
public interface Document {
	/**
	 * The ID used by the index to represent the document.
	 */
	int getId();
	double getSize();
	/**
	 * Gets a stream over the content of the document.
	 */
	Reader getContent();
	String gettitteOfDocuement();
	/**
	 * The title of the document, for displaying to the user.
	 */
	String getTitle();
}
