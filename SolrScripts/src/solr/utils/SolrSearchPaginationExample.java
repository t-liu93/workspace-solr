package solr.utils;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.SortClause;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CursorMarkParams;

public class SolrSearchPaginationExample {

	public static void paginationSolrTest() {

		try {

			SolrClient solr = new HttpSolrClient.Builder(Const.URL_SORL).build();
			
			SolrQuery query = new SolrQuery();

			query.setQuery("message:\"can\"");
			
			query.setRows(3300);
			
			query.setSort(SortClause.asc("id"));

			query.setFields(Const.ID, Const.MESSAGE);

			String cursorMark = CursorMarkParams.CURSOR_MARK_START;

			boolean done = false;

			while (!done) {

				query.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);

				QueryResponse response = solr.query(query);

				String nextCursorMark = response.getNextCursorMark();

				SolrDocumentList results = response.getResults();

				System.out.println("Number of results => " + results.size());

				if (cursorMark.equals(nextCursorMark)) {
					done = true;
				}
				
				cursorMark = nextCursorMark;
			}
		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
		}

		System.out.println("Done...");
	}

	public static void main(String[] args) {

		paginationSolrTest();
	}
}
