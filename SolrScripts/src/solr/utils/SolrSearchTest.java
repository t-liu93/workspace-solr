package solr.utils;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.SortClause;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

public class SolrSearchTest {

	public static void paginationSolrTest(int start) {

		try {

			SolrClient solr = new HttpSolrClient.Builder("http://localhost:8983/solr/gettingstarted").build();

			SolrQuery query = new SolrQuery();

			query.setQuery("message:\"can\"");

			query.setRows(3);

			query.setSort(SortClause.asc("id"));

			query.setStart(start);

			query.setFields("id", "message");

			int counter = 0;

			while (counter < start) {

				QueryResponse response = solr.query(query);

				SolrDocumentList results = response.getResults();

				for (int i = 0; i < results.size(); i++) {
					
					SolrDocument codeReview = results.get(i);
					
					String id = (String) codeReview.getFieldValue("id");
					
					System.out.println("id: " + id);
				}

				counter = counter + 1;
			}

		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
		}

		System.out.println("Done...");
	}

	public static void main(String[] args) {

		int start = 0;

		paginationSolrTest(start);
	}
}
