package solr.main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import solr.utils.Constants;

public class SolrSearch {

	/**
	 * This method counts the occurrences of each feature in the framework and
	 * save the results in a csv file in the same framework directory in the
	 * pattern "framework-out.csv".
	 * 
	 * Note: Solr API does not provide the number of features within a code
	 * review, that's why we need to do call contains in the messages.message
	 * object.
	 * 
	 * @param framework
	 */
	public static void countFeaturesOccurrences(String framework) {

		System.out.println("Searching for framework: " + framework);

		try {
			// call the Solr Cloud URL
			SolrClient solr = new HttpSolrClient.Builder(Constants.URL_SORL).build();

			// create the Solr query string base 
			String solrQuery = Constants.MESSAGES_MESSAGE + Constants.TWO_DOTS + Constants.SLASH;

			// read the framework file
			List<String> features = Files.readAllLines(Paths.get(framework));

			StringBuffer sb = new StringBuffer();

			// iterate over each feature
			for (String feature : features) {

				System.out.println("Searching for feature: " + feature);

				// add the feature to the string buffer
				sb.append(feature + Constants.COMMA);

				// create the query object
				SolrQuery query = new SolrQuery();

				// set the query
				query.setQuery(solrQuery + feature + Constants.SLASH);

				// set the fields to be returned from the json
				query.setFields(Constants._NUMBER, Constants.MESSAGES_ID, Constants.MESSAGES_MESSAGE);

				// call the query
				QueryResponse response = solr.query(query);

				// get the results
				SolrDocumentList results = response.getResults();

				// count the number of code reviews with the feature
				long numCodeReviewsFound = results.getNumFound();

				System.out.println("Number of code reviews: " + numCodeReviewsFound);

				// iterate over each code review
				for (int i = 0; i < results.size(); ++i) {

					// create the Solr code review object
					SolrDocument codeReview = results.get(i);

					@SuppressWarnings("unchecked")
					// get the messages.message field
					List<String> messages = ((List<String>) codeReview.getFieldValue(Constants.MESSAGES_MESSAGE));

					int countMatches = StringUtils.countMatches(messages.toString(), feature);
					System.out.println(countMatches);

					// iterate over the messages.message object to count the
					// number of features within it
					for (String string : messages) {
						if (string.contains("like a reasonable")) {
							String s = string;
							System.out.println(s);
						}
					}
				}
			}
		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This is the main method which makes use of all Solr methods.
	 * 
	 * @param args
	 *            Unused.
	 * 
	 */
	public static void main(String[] args) {

		// the path for the framework file
		String framework = "./framework/meta.txt";

		// count the occurrences
		countFeaturesOccurrences(framework);
	}
}
