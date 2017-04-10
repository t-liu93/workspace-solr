package solr.utils;

public class Tuple<L, R> {

	private L feature;
	private R commentID;

	public void setLeft(L left) {
		this.feature = left;
	}

	public void setRight(R right) {
		this.commentID = right;
	}

	public L getLeft() {
		return feature;
	}

	public R getRight() {
		return commentID;
	}

	@Override
	public int hashCode() {
		return feature.hashCode() ^ commentID.hashCode();
	}

	@Override
	public String toString() {
		return feature + Const.SEMICOLON + commentID;
	}
}
