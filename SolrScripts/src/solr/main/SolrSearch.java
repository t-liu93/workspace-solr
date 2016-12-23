package solr.main;

import java.io.IOException;

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
			
			String urlString = "http://localhost:8983/solr/gettingstarted";
			SolrClient solr = new HttpSolrClient.Builder(urlString).build();

			SolrQuery query = new SolrQuery();
			
			// double check it
			query.setRequestHandler("/spellCheckCompRH");
			
			query.setQuery("messages.message:/.*it appears.*/");
			query.setFields("id", "_number");
			query.setStart(0);
			
			QueryResponse response = solr.query(query);
			SolrDocumentList results = response.getResults();
			for (int i = 0; i < results.size(); ++i) {
				SolrDocument d = results.get(i);
				System.out.println(d);
			}
			
		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
		}
	}
}
