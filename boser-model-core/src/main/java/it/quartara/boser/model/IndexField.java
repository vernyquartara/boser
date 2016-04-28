package it.quartara.boser.model;

public enum IndexField {

	URL,
	TITLE,
	CONTENT,
	DIGEST;

	@Override
	public String toString() {
		String fieldName = super.toString();
		return fieldName.toLowerCase();
	}

	
}
