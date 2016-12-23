package solr.main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

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
	 * save the results in a csv file.
	 * 
	 * @param framework
	 */
	public static void countFeaturesOccurrences(String framework) {
		try {

			List<String> content = Files.readAllLines(Paths.get(framework));

			for (String string : content) {
				String x = string;
				System.out.println(x);
			}

			SolrClient solr = new HttpSolrClient.Builder(Constants.URL_SORL).build();

			SolrQuery query = new SolrQuery();

			query.setQuery("messages.message:/.*[Ii]t appears.*/");
			query.setFields(Constants._NUMBER, Constants.MESSAGES_ID, Constants.MESSAGES_MESSAGE);
			query.setStart(0);

			QueryResponse response = solr.query(query);

			SolrDocumentList results = response.getResults();

			long numCodeReviewsFound = results.getNumFound();
			System.out.println(numCodeReviewsFound);

			for (int i = 0; i < results.size(); ++i) {
				SolrDocument d = results.get(i);

				@SuppressWarnings("unchecked")
				List<String> dm = ((List<String>) d.getFieldValue(Constants.MESSAGES_MESSAGE));

				for (String string : dm) {
					if (string.contains("like a reasonable")) {
						String s = string;
						System.out.println(s);
					}
				}

				System.out.println(dm);

				System.out.println(d);
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
