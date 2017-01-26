package solr.main;

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

import solr.utils.Const;
import solr.utils.Utils;

public class SolrSearchIC {

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

		// the path for the framework file
		String frameworkPath = Const.DIR_FRAMEWORK + framework + Const._TXT;

		System.out.println("Searching for framework: " + frameworkPath);

		try {
			// call the Solr Cloud URL
			SolrClient solr = new HttpSolrClient.Builder(Const.URL_SORL).build();

			// create the Solr query string base
			String solrQuery = Const.FILES_COMMENTS_MESSAGE + Const.TWO_DOTS + Const.DOUBLE_QUOTES;

			// read the framework file
			List<String> features = Files.readAllLines(Paths.get(frameworkPath));

			StringBuffer sbFeaturesOutput = new StringBuffer();

			StringBuffer sbFeaturesIDs = new StringBuffer();

			List<String> featuresIDs = new ArrayList<String>();
			
			String codeReviewID = "";

			// iterate over each feature
			for (String feature : features) {

				String solrQueryString = solrQuery + feature + Const.DOUBLE_QUOTES;

				System.out.println(solrQueryString);

				int numTotalHits = Const._0;

				System.out.println("Searching for feature: " + solrQueryString);

				// add the feature to the string buffer
				sbFeaturesOutput.append(feature + Const.SEMICOLON);

				// create the query object
				SolrQuery query = new SolrQuery();

				// set the query
				query.setQuery(solrQueryString);

				// set the fields to be returned from the json
				query.setFields(Const.ID, Const.FILES_COMMENTS_MESSAGE, Const.FILES_COMMENTS_AUTHOR_ID,
						Const.FILES_COMMENTS_PATCH_SET, Const.FILES_COMMENTS_ID);

				int pageNum = 1;
				int numItemsPerPage = Const._20000;
				int sumRead = numItemsPerPage;
				query.setStart((pageNum - 1) * numItemsPerPage);
				query.setRows(numItemsPerPage);

				// call the query
				QueryResponse response = solr.query(query);

				// get the results
				SolrDocumentList results = response.getResults();

				// count the number of code reviews with the feature
				long numCodeReviewsFound = results.getNumFound();

				System.out.println("Number of inline comments => " + numCodeReviewsFound);

				boolean pagination = false;
				if (numCodeReviewsFound > results.size()) {
					pagination = true;
				}

				// iterate over each code review
				for (int i = 0; i < results.size(); ++i) {

					// create the Solr code review object
					SolrDocument codeReview = results.get(i);

					// get the files.comments.message field
					List<String> messages = ((List<String>) codeReview.getFieldValue(Const.FILES_COMMENTS_MESSAGE));

					// get the files.comments.author._account_id field
					List<Long> authorsID = ((List<Long>) codeReview.getFieldValue(Const.FILES_COMMENTS_AUTHOR_ID));

					List<String> messagesID = ((List<String>) codeReview.getFieldValue(Const.FILES_COMMENTS_ID));

					codeReviewID = (String) codeReview.getFieldValue(Const.ID);

					int numMessagesFound = Const._0;

					// iterate over the messages.message object to count the
					// number of features within it
					for (int j = 0; j < messages.size(); j++) {

						if (!Utils.isBot(authorsID.get(j))) {

							Pattern pattern = Pattern.compile(feature.toLowerCase());

							Matcher matcher = pattern.matcher(messages.get(j).toLowerCase());

							while (matcher.find()) {
								numMessagesFound++;
								if (!featuresIDs.contains(messagesID.get(j))) {
									featuresIDs.add(messagesID.get(j));
									String url = Const.URL_GERRIT + codeReviewID;
									int x = j + 1;
									sbFeaturesIDs.append(feature + Const.SEMICOLON + Const.SPACE + codeReviewID 
											+ Const.SEMICOLON + Const.SPACE + x + Const.SEMICOLON
											+ Const.SPACE +  url + Const.NEW_LINE);
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

					// iterate over each code review
					for (int i = 0; i < results.size(); ++i) {

						// create the Solr code review object
						SolrDocument codeReview = results.get(i);

						// get the files.comments.message field
						List<String> messages = ((List<String>) codeReview.getFieldValue(Const.FILES_COMMENTS_MESSAGE));

						// get the files.comments.author._account_id field
						List<Long> authorsID = ((List<Long>) codeReview.getFieldValue(Const.FILES_COMMENTS_AUTHOR_ID));

						List<String> messagesID = ((List<String>) codeReview.getFieldValue(Const.FILES_COMMENTS_ID));

						int numMessagesFound = Const._0;

						// iterate over the messages.message object to count the
						// number of features within it
						for (int j = 0; j < messages.size(); j++) {

							if (!Utils.isBot(authorsID.get(j))) {

								Pattern pattern = Pattern.compile(feature.toLowerCase());

								Matcher matcher = pattern.matcher(messages.get(j).toLowerCase());

								while (matcher.find()) {
									numMessagesFound++;
									if (!featuresIDs.contains(messagesID.get(j))) {
										featuresIDs.add(messagesID.get(j));
										String url = Const.URL_GERRIT + codeReviewID;
										int x = j + 1;
										sbFeaturesIDs.append(feature + Const.SEMICOLON + Const.SPACE + codeReviewID 
												+ Const.SEMICOLON + Const.SPACE + x + Const.SEMICOLON
												+ Const.SPACE +  url + Const.NEW_LINE);
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
					new FileOutputStream(Const.DIR_FRAMEWORK + Const._IC  + framework + Const._IC 
							+ Const._OUT + Const._CSV), Const._UTF_8))) {
				writer.write(sbFeaturesOutput.toString());
			} catch (Exception e) {
				System.out.println(e);
			}

			try (Writer writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(Const.DIR_FRAMEWORK + Const._IC  + framework + Const._IC 
							+ Const._ID + Const._TXT), Const._UTF_8))) {
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
		String framework = "hypotheticals";

		// count the occurrences
		countFeaturesOccurrences(framework);
	}
}
