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

public class SolrSearch {
	
	

	public static void main(String[] args) {

		try {

			List<String> content = Files.readAllLines(Paths.get("./framework/meta.txt"));
			for (String string : content) {
				String x = string;
				System.out.println(x);
			}
			
			String urlString = "http://localhost:8983/solr/gettingstarted";
			SolrClient solr = new HttpSolrClient.Builder(urlString).build();
			

			SolrQuery query = new SolrQuery();

			query.setQuery("messages.message:/.*[Ii]t appears.*/");
			query.setFields("_number", "messages.id", "messages.message");
			query.setStart(0);
			
			QueryResponse response = solr.query(query);
			
			SolrDocumentList results = response.getResults();
			
			long numCodeReviewsFound = results.getNumFound();
			System.out.println(numCodeReviewsFound);
			
			for (int i = 0; i < results.size(); ++i) {
				SolrDocument d = results.get(i);

				@SuppressWarnings("unchecked")
				List<String> dm = ((List<String>) d.getFieldValue("messages.message"));
				
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
}
