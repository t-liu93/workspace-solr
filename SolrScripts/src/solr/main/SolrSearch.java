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
	@SuppressWarnings("unchecked")
	public static void countFeaturesOccurrences(String framework) {

		// the path for the framework file
		String frameworkPath = Constants.DIR_FRAMEWORK + framework + Constants._TXT;

		System.out.println("Searching for framework: " + frameworkPath);

		try {
			// call the Solr Cloud URL
			SolrClient solr = new HttpSolrClient.Builder(Constants.URL_SORL).build();

			// create the Solr query string base
			String solrQuery = Constants.MESSAGES_MESSAGE + Constants.TWO_DOTS + Constants.DOUBLE_QUOTES;

			// read the framework file
			List<String> features = Files.readAllLines(Paths.get(frameworkPath));

			StringBuffer sbFeaturesOutput = new StringBuffer();
			
			StringBuffer sbFeaturesIDs = new StringBuffer();
			
			List<String> featuresIDs = new ArrayList<String>();
			
			Long _number = (long) 0;

			// iterate over each feature
			for (String feature : features) {

				// Escaping Special Characters:
				// The current list special characters are:
				// + - && || ! ( ) { } [ ] ^ " ~ * ? : \
				// To escape these character use the \ before the character.

				// "/.*[^a-zA-Z0-9][Ss]ort[ \t\n]of.*/"


				String solrQueryString = solrQuery + feature + Constants.DOUBLE_QUOTES;
				
				System.out.println(solrQueryString);

				int numTotalHits = Constants._0;

				System.out.println("Searching for feature: " + solrQueryString);

				// add the feature to the string buffer
				sbFeaturesOutput.append(feature + Constants.SEMICOLON);

				// create the query object
				SolrQuery query = new SolrQuery();

				// set the query
				query.setQuery(solrQueryString);

				// set the fields to be returned from the json
				query.setFields(Constants._NUMBER, Constants.MESSAGES_MESSAGE, Constants.MESSAGES_AUTHOR_ID, Constants.MESSAGES_ID);

				int pageNum = 1;
				int numItemsPerPage = Constants._20000;
				int sumRead = numItemsPerPage;
				query.setStart((pageNum - 1) * numItemsPerPage);
				query.setRows(numItemsPerPage);

				// call the query
				QueryResponse response = solr.query(query);

				// get the results
				SolrDocumentList results = response.getResults();

				// count the number of code reviews with the feature
				long numCodeReviewsFound = results.getNumFound();

				System.out.println("Number of code reviews => " + numCodeReviewsFound);

				boolean pagination = false;
				if (numCodeReviewsFound > results.size()) {
					pagination = true;
				}

				// iterate over each code review
				for (int i = 0; i < results.size(); ++i) {

					// create the Solr code review object
					SolrDocument codeReview = results.get(i);

					// get the messages.message field
					List<String> messages = ((List<String>) codeReview.getFieldValue(Constants.MESSAGES_MESSAGE));
					
					// get the messages.message field
					List<Long> authorsID = ((List<Long>) codeReview.getFieldValue(Constants.MESSAGES_AUTHOR_ID));
					
					List<String> messagesID = ((List<String>) codeReview.getFieldValue(Constants.MESSAGES_ID));
					
					_number = ((List<Long>) codeReview.getFieldValue(Constants._NUMBER)).get(0);

					int numMessagesFound = Constants._0;

					// iterate over the messages.message object to count the
					// number of features within it
					for (int j = 0; j < messages.size(); j++) {
						
						if (!isBot(authorsID.get(j))) {
							
							Pattern pattern = Pattern.compile(feature.toLowerCase());
							
							Matcher matcher = pattern.matcher(messages.get(j).toLowerCase());
							
							while (matcher.find()) {
								numMessagesFound++;
								if (!featuresIDs.contains(messagesID.get(j))) {
									featuresIDs.add(messagesID.get(j));
									sbFeaturesIDs.append(_number + ";" + messagesID.get(j) + Constants.NEW_LINE);
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

						// get the messages.message field
						List<String> messages = ((List<String>) codeReview.getFieldValue(Constants.MESSAGES_MESSAGE));

						// get the messages.message field
						List<Long> authorsID = ((List<Long>) codeReview.getFieldValue(Constants.MESSAGES_AUTHOR_ID));
						
						List<String> messagesID = ((List<String>) codeReview.getFieldValue(Constants.MESSAGES_ID));
						
						int numMessagesFound = Constants._0;

						// iterate over the messages.message object to count the
						// number of features within it
						for (int j = 0; j < messages.size(); j++) {
							
							if (!isBot(authorsID.get(j))) {
							
								Pattern pattern = Pattern.compile(feature.toLowerCase());
	
								Matcher matcher = pattern.matcher(messages.get(j).toLowerCase());
	
								while (matcher.find()) {
									numMessagesFound++;
									if (!featuresIDs.contains(messagesID.get(j))) {
										featuresIDs.add(messagesID.get(j));
										sbFeaturesIDs.append(_number + ";" + messagesID.get(j) + Constants.NEW_LINE);
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
				sbFeaturesOutput.append(numTotalHits + Constants.NEW_LINE);

				System.out.println("Number of general messages => " + numTotalHits);
				System.out.println("===========================");
			}

			try (Writer writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(Constants.DIR_FRAMEWORK + framework + Constants._OUT + Constants._CSV),
					Constants._UTF_8))) {
				writer.write(sbFeaturesOutput.toString());
			} catch (Exception e) {
				System.out.println(e);
			}
			
			try (Writer writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(Constants.DIR_FRAMEWORK + framework + Constants._ID + Constants._TXT),
					Constants._UTF_8))) {
				writer.write(sbFeaturesIDs.toString());
			} catch (Exception e) {
				System.out.println(e);
			}
			
			
			System.out.println("Total of general messages: " + featuresIDs.size());

		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
		}

		System.out.println("Done...");
		
	}

	private static boolean isBot(Long authorID) {
		
		boolean isBot = false;
		
		if (authorID == Constants.ANDROID_BOT_TREEHUGGER
				|| authorID == Constants.ANDROID_BOT_DECKARD
				|| authorID == Constants.ANDROID_BOT_ANONYMOUS
				|| authorID == Constants.ANDROID_BOT_BIONIC
				|| authorID == Constants.ANDROID_BOT_ANDROID_MERGER
				|| authorID == Constants.ANDROID_BOT_ANDROID_DEVTOOLS
				|| authorID == Constants.ANDROID_BOT_GERRIT) {
			isBot = true;
		} 
		
		return isBot;
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
		String framework = "meta";

		// count the occurrences
		countFeaturesOccurrences(framework);
	}
}
