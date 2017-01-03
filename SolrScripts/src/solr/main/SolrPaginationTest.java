package solr.main;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

import solr.utils.Constants;

public class SolrPaginationTest {

	public static void main(String[] args) {

		try {
			SolrClient solr = new HttpSolrClient.Builder(Constants.URL_SORL).build();

			String solrQuery = Constants.MESSAGES_MESSAGE + Constants.TWO_DOTS + Constants.SLASH;

			SolrQuery query = new SolrQuery();

			query.setQuery(solrQuery + ".*[Vv]iew.*" + Constants.SLASH);

			query.setFields(Constants._NUMBER, Constants.MESSAGES_ID, Constants.MESSAGES_MESSAGE);

			//===========
			int pageNum = 1;
			int numItemsPerPage = Constants._20000;
			int sumRead = numItemsPerPage;
			query.setStart((pageNum  - 1) * numItemsPerPage );
			query.setRows(numItemsPerPage);
			//===========
			
			QueryResponse response = solr.query(query);

			SolrDocumentList results = response.getResults();

			long numCodeReviewsFound = results.getNumFound();

			System.out.println("Number of code reviews => " + numCodeReviewsFound);
			
			boolean pagination = false;
			if (numCodeReviewsFound > results.size()) {
				pagination = true;
			}
			
//			for (int i = 0; i < results.size(); ++i) {
//
//				SolrDocument codeReview = results.get(i);
//
//				@SuppressWarnings("unchecked")
//				List<String> messages = ((List<String>) codeReview.getFieldValue(Constants.MESSAGES_MESSAGE));
//
//			}
			
			while (pagination) {
				
				if (sumRead >= numCodeReviewsFound) {
					break;
				}
				
				pageNum++;
				query.setStart((pageNum  - 1) * numItemsPerPage );
				response = solr.query(query);
				results = response.getResults();

				sumRead = sumRead + results.size();
				System.out.println("Number of sum read => " + sumRead);
			}

		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
		}
	}
}
