package solr.main;

import java.util.ArrayList;
import java.util.List;

public class FeatureResult {

	private String framework;

	private long totalNumCommentsFound;

	private long totalNumFeaturesFound;

	private List<String> listIDs = new ArrayList<String>();

	public String getFramework() {
		return framework;
	}

	public void setFramework(String framework) {
		this.framework = framework;
	}

	public long getTotalNumCommentsFound() {
		return totalNumCommentsFound;
	}

	public void setTotalNumCommentsFound(long totalNumCommentsFound) {
		this.totalNumCommentsFound = totalNumCommentsFound;
	}

	public long getTotalNumFeaturesFound() {
		return totalNumFeaturesFound;
	}

	public void setTotalNumFeaturesFound(long totalNumFeaturesFound) {
		this.totalNumFeaturesFound = totalNumFeaturesFound;
	}

	public List<String> getListIDs() {
		return listIDs;
	}

	public void setListIDs(List<String> listIDs) {
		this.listIDs = listIDs;
	}

}
