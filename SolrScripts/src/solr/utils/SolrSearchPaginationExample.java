package solr.utils;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

public class SolrSearchPaginationExample {

	/**
	 * This method counts the occurrences of each feature in the framework and
	 * save the results in a csv file in the same framework directory in the
	 * pattern "framework-out.csv".
	 * 
	 * Note: Solr API does not provide the number of features within a code
	 * review, that's why we need to do call contains in the files.comments.message
	 * object.
	 * 
	 * @param framework
	 */
	@SuppressWarnings("unchecked")
	public static void countFeaturesOccurrences(String framework) {

		String frameworkPath = Const.DIR_FRAMEWORK + framework + Const._TXT;

		System.out.println("Searching for framework: " + frameworkPath);

		try {
			SolrClient solr = new HttpSolrClient.Builder(Const.URL_SORL).build();

			String solrQuery = Const.COMMENTS_MESSAGE + Const.TWO_DOTS + Const.DOUBLE_QUOTES;

			List<String> features = Files.readAllLines(Paths.get(frameworkPath));

			StringBuffer sbFeaturesOutput = new StringBuffer();

			StringBuffer sbFeaturesIDs = new StringBuffer();

			List<String> featuresIDs = new ArrayList<String>();
			
			String codeReviewID = "";

			for (String feature : features) {

				String solrQueryString = solrQuery + feature + Const.DOUBLE_QUOTES;

				int numTotalHits = Const._0;

				System.out.println("Searching for feature: " + solrQueryString);

				sbFeaturesOutput.append(feature + Const.SEMICOLON);

				SolrQuery query = new SolrQuery();

				query.setQuery(solrQueryString);

				query.setFields(Const.ID, Const.COMMENTS_MESSAGE, Const.COMMENTS_AUTHOR_ID,
						Const.COMMENTS_PATCH_SET, Const.COMMENTS_ID, Const.FILE, 
						Const.COMMENTS_LINE);

				int pageNum = 1;
				int numItemsPerPage = Const._20000;
				int sumRead = numItemsPerPage;
				query.setStart((pageNum - 1) * numItemsPerPage);
				query.setRows(numItemsPerPage);

				QueryResponse response = solr.query(query);

				SolrDocumentList results = response.getResults();

				long numCodeReviewsFound = results.getNumFound();

				System.out.println("Number of inline comments => " + numCodeReviewsFound);

				boolean pagination = false;
				if (numCodeReviewsFound > results.size()) {
					pagination = true;
				}

				for (int i = 0; i < results.size(); ++i) {

					SolrDocument codeReview = results.get(i);

					List<String> messages = ((List<String>) codeReview.getFieldValue(Const.COMMENTS_MESSAGE));

					List<Long> authorsID = ((List<Long>) codeReview.getFieldValue(Const.COMMENTS_AUTHOR_ID));

					List<String> messagesID = ((List<String>) codeReview.getFieldValue(Const.COMMENTS_ID));
					
					List<String> file = ((List<String>) codeReview.getFieldValue(Const.FILE));
					
					List<Long> lines = ((List<Long>) codeReview.getFieldValue(Const.COMMENTS_LINE));
					
					List<Long> patchSets = ((List<Long>) codeReview.getFieldValue(Const.COMMENTS_PATCH_SET));

					codeReviewID = (String) codeReview.getFieldValue(Const.ID);
					
					codeReviewID = codeReviewID.substring(0, codeReviewID.indexOf("."));

					int numMessagesFound = Const._0;

					for (int j = 0; j < messages.size(); j++) {

						if (!Utils.isBot(authorsID.get(j))) {

							Pattern pattern = Pattern.compile(feature.toLowerCase());

							Matcher matcher = pattern.matcher(messages.get(j).toLowerCase());

							while (matcher.find()) {
								numMessagesFound++;
								if (!featuresIDs.contains(messagesID.get(j))) {
									
									featuresIDs.add(messagesID.get(j));
									
									String url = Const.URL_GERRIT + codeReviewID + Const.SLASH + String.valueOf(patchSets.get(j)) 
										+ Const.SLASH + file.get(0);
									
									if (lines.get(j) != -1) {
										
										url = url + Const.AT + String.valueOf(lines.get(j));
									}
									
									sbFeaturesIDs.append(feature + Const.SEMICOLON + Const.SPACE + codeReviewID 
											+ Const.SEMICOLON + Const.SPACE + String.valueOf(patchSets.get(j))
											+ Const.SEMICOLON + Const.SPACE + url + Const.NEW_LINE);
								}
							}
						}
					}

					numTotalHits = numTotalHits + numMessagesFound;
				}

				while (pagination) {

					if (sumRead >= numCodeReviewsFound) {
						break;
					}

					pageNum++;
					query.setStart((pageNum - 1) * numItemsPerPage);
					response = solr.query(query);
					results = response.getResults();

					for (int i = 0; i < results.size(); ++i) {

						SolrDocument codeReview = results.get(i);

						List<String> messages = ((List<String>) codeReview.getFieldValue(Const.COMMENTS_MESSAGE));

						List<Long> authorsID = ((List<Long>) codeReview.getFieldValue(Const.COMMENTS_AUTHOR_ID));

						List<String> messagesID = ((List<String>) codeReview.getFieldValue(Const.COMMENTS_ID));
						
						List<String> file = ((List<String>) codeReview.getFieldValue(Const.FILE));
						
						List<Long> lines = ((List<Long>) codeReview.getFieldValue(Const.COMMENTS_LINE));
						
						List<Long> patchSets = ((List<Long>) codeReview.getFieldValue(Const.COMMENTS_PATCH_SET));

						int numMessagesFound = Const._0;

						for (int j = 0; j < messages.size(); j++) {

							if (!Utils.isBot(authorsID.get(j))) {

								Pattern pattern = Pattern.compile(feature.toLowerCase());

								Matcher matcher = pattern.matcher(messages.get(j).toLowerCase());

								while (matcher.find()) {
									numMessagesFound++;
									if (!featuresIDs.contains(messagesID.get(j))) {
										
										featuresIDs.add(messagesID.get(j));
										
										String url = Const.URL_GERRIT + codeReviewID + Const.SLASH + String.valueOf(patchSets.get(j)) 
											+ Const.SLASH + file.get(0);
									
										if (lines.get(j) != -1) {
											
											url = url + Const.AT + String.valueOf(lines.get(j));
										}
										
										sbFeaturesIDs.append(feature + Const.SEMICOLON + Const.SPACE + codeReviewID 
												+ Const.SEMICOLON + Const.SPACE + String.valueOf(patchSets.get(j))
												+ Const.SEMICOLON + Const.SPACE + url + Const.NEW_LINE);
									}
								}
							}
						}

						numTotalHits = numTotalHits + numMessagesFound;
					}

					sumRead = sumRead + results.size();
					// System.out.println("Number of sum read => " + sumRead);
				}

				// add the number of code reviews to the string buffer
				sbFeaturesOutput.append(numTotalHits + Const.NEW_LINE);

				System.out.println("Number of general messages => " + numTotalHits);
				System.out.println("===========================");
			}

			try (Writer writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(Const.DIR_RESULTS + Const._IC + Const.SLASH + framework +
							Const._OUT + Const._CSV), Const._UTF_8))) {
				writer.write(sbFeaturesOutput.toString());
			} catch (Exception e) {
				System.out.println(e);
			}

			try (Writer writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(Const.DIR_RESULTS + Const._IC + Const.SLASH  + framework +
							Const._ID + Const._TXT), Const._UTF_8))) {
				writer.write(sbFeaturesIDs.toString());
			} catch (Exception e) {
				System.out.println(e);
			}

			System.out.println("Total of inline comments: " + featuresIDs.size());

		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
		}

		System.out.println("Done...");

	}

	/**
	 * This is the main method which makes use of all Solr methods.
	 * 
	 * @param args
	 *            Unused.
	 * 
	 */
	public static void main(String[] args) {

		// the framework
		String framework = "hedges";

		// count the occurrences
		countFeaturesOccurrences(framework);
	}
}
