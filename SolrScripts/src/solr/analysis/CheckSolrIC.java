package solr.analysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class CheckSolrIC {

	public static void printJsonICIDs() {

		List<String> icIDs = new ArrayList<String>();

		try {

			List<File> filesInFolder = new ArrayList<>();

			filesInFolder.addAll(Files.walk(Paths.get("C:/Users/Felipe/Documents/code-reviews/tmp"))
					.filter(Files::isRegularFile).map(Path::toFile).collect(Collectors.toList()));

			for (File fileIC : filesInFolder) {

				String content = new String(Files.readAllBytes(fileIC.toPath()));

				JsonObject jsonIC = (JsonObject) new JsonParser().parse(content);

				Set<Entry<String, JsonElement>> files = jsonIC.entrySet();

				for (Iterator<Entry<String, JsonElement>> iterator = files.iterator(); iterator.hasNext();) {

					Entry<String, JsonElement> file = (Entry<String, JsonElement>) iterator.next();

					JsonArray comments = file.getValue().getAsJsonArray();

					for (JsonElement comment : comments) {

						icIDs.add(comment.getAsJsonObject().get("id").getAsString());
					}
				}
			}
			
			try (BufferedWriter bw = new BufferedWriter(new FileWriter("./results/inline-comments-id-list.txt"))) {

				for (String id : icIDs) {
					bw.write(id + "\n");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("finished all!!!");
	}

	public static void main(String[] args) {

	}
}
