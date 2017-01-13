package solr.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * <h1>HtmlRequests</h1>
 * 
 * The HtmlRequests class contains all methods to do HTML requests used in the
 * program.
 *
 * @author Felipe Ebert
 * @version 0.1
 * @since 2016-11-25
 */
public class HtmlRequests {

	/**
	 * This method does the HTTP requests.
	 * 
	 * @param targetURL
	 *            The URL to be requested.
	 *            
	 * @return String The HTML as a string.
	 */
	public static List<String> excuteSolrRequest(String targetURL) {

		// creates the HTTP connection
		HttpURLConnection connection = null;

		try {
			// create the URL object
			URL url = new URL(targetURL);

			// set the connection parameters
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Content-Language", "en-US");
			connection.setUseCaches(false);
			connection.setDoOutput(true);

			// get HTML response
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			
			List<String> response = new ArrayList<String>();
			String line;
			while ((line = rd.readLine()) != null) {
				if (!line.equals("\"_number\"")) {
					response.add(line);
				}
			}
			rd.close();
			return response;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (connection != null) {

				// close the connection
				connection.disconnect();
			}
		}
	}
}
