package solr.analysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class RebuildJsonICFiles {

	public static void rebuildJsonICFiles() {

		try {

			List<File> filesInFolder = new ArrayList<>();

			filesInFolder.addAll(Files.walk(Paths.get("C:/Users/Felipe/Documents/code-reviews/inline-comments/"))
					.filter(Files::isRegularFile).map(Path::toFile).collect(Collectors.toList()));

			for (File file : filesInFolder) {

				int codeReviewID = Integer.parseInt(file.getName().substring(0, file.getName().indexOf("-")));

				String content = new String(Files.readAllBytes(file.toPath()));

				JsonObject jsonIC = (JsonObject) new JsonParser().parse(content);

				Set<Entry<String, JsonElement>> files = jsonIC.entrySet();

				int fileCounter = 1;

				for (Iterator<Entry<String, JsonElement>> iterator = files.iterator(); iterator.hasNext();) {

					Entry<String, JsonElement> entry = (Entry<String, JsonElement>) iterator.next();

					JsonObject newJsonIC = new JsonObject();

					newJsonIC.addProperty("id", codeReviewID + "." + fileCounter);
					
					String fileName = entry.getKey();
					
					newJsonIC.addProperty("file", fileName);

					JsonArray comments = entry.getValue().getAsJsonArray();

					JsonArray newComments = new JsonArray();

					for (JsonElement comment : comments) {

						JsonObject newComment = new JsonObject();

						JsonObject jsonObj = comment.getAsJsonObject();

						JsonObject author = (JsonObject) jsonObj.get("author");

						author.remove("avatars");

						newComment.add("author", author);

						JsonElement patchSet = jsonObj.get("patch_set");

						newComment.add("patch_set", patchSet);

						JsonElement id = jsonObj.get("id");

						newComment.add("id", id);

						if (jsonObj.get("line") != null) {
							newComment.add("line", jsonObj.get("line"));
						} else {
							newComment.addProperty("line", -1);
						}

						JsonObject range = new JsonObject();

						if (jsonObj.get("range") != null) {

							range = jsonObj.getAsJsonObject("range");

						} else {

							range.addProperty("start_line", -1);

							range.addProperty("start_character", -1);

							range.addProperty("end_line", -1);

							range.addProperty("end_character", -1);
						}

						newComment.add("range", range);

						if (jsonObj.get("in_reply_to") != null) {

							newComment.add("in_reply_to", jsonObj.get("in_reply_to"));

						} else {

							newComment.addProperty("in_reply_to", "0");
						}

						JsonElement updated = jsonObj.get("updated");

						newComment.add("updated", updated);

						JsonElement message = jsonObj.get("message");

						newComment.add("message", message);

						newComments.add(newComment);
					}

					newJsonIC.add("comments", newComments);

					Gson gson = new GsonBuilder().setPrettyPrinting().create();
					
					String json = gson.toJson(newJsonIC);
					
					String filePath = file.getAbsolutePath().replace(codeReviewID + "-", codeReviewID + "-" + fileCounter + "-");
					
					try (Writer writer = new BufferedWriter(
							new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8"))) {
						writer.write(json);
					} catch (Exception e) {
						System.out.println(e);
					}
					
					file.delete();

					fileCounter = fileCounter + 1;
				}

				System.out.println("finished: " + file.getName());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("finished all!!!");
	}

	public static void main(String[] args) {

		rebuildJsonICFiles();
	}
}