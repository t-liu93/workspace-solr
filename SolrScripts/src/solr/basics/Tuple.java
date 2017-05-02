package solr.basics;

import solr.utils.Const;

public class Tuple {

	private String feature;

	private String commentID;

	public Tuple(String feature, String commentID) {
		this.feature = feature;
		this.commentID = commentID;
	}

	public void setFeature(String feature) {
		this.feature = feature;
	}

	public void setRCommentID(String commentID) {
		this.commentID = commentID;
	}

	public String getFeature() {
		return feature;
	}

	public String getCommentID() {
		return commentID;
	}

	@Override
	public int hashCode() {
		return commentID.hashCode();
	}

	@Override
	public String toString() {
		return feature + Const.SEMICOLON + commentID;
	}

	@Override
	public boolean equals(Object obj) {
		// TODO check this method! It is failing for inline comments IDs!!!! 
		return (hashCode() == obj.hashCode());
	}
}
