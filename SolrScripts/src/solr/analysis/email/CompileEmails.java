package solr.analysis.email;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import solr.utils.Const;

public class CompileEmails {

	public static Map<String, String> extractEmails(String file) throws IOException {

		List<String> emails = Files.readAllLines(Paths.get(file));

		Map<String, String> map = new HashMap<String, String>();

		for (String email : emails) {

			String[] array = email.split("###");

			map.put(array[0], array[1].trim());
		}
		return map;
	}

	public static void compileEmails() {

		String emailsDir = Const.EMAILS + Const.SLASH;

		String fileGCNoConfusion = emailsDir + Const.EMAIL_LIST + Const._GC + Const._TXT;

		String fileICNoConfusion = emailsDir + Const.EMAIL_LIST + Const._IC + Const._TXT;

		String fileGCConfusion = emailsDir + Const.EMAIL_LIST + Const._GC + Const.DASH + Const.CONFUSION + Const._TXT;

		String fileICConfusion = emailsDir + Const.EMAIL_LIST + Const._IC + Const.DASH + Const.CONFUSION + Const._TXT;

		try {

			Map<String, String> mapGCNoConfusion = extractEmails(fileGCNoConfusion);

			Map<String, String> mapGCConfusion = extractEmails(fileGCConfusion);

			Map<String, String> mapICNoConfusion = extractEmails(fileICNoConfusion);

			Map<String, String> mapICConfusion = extractEmails(fileICConfusion);

			for (String key : mapICConfusion.keySet()) {
				if (!mapGCConfusion.containsKey(key)) {
					mapGCConfusion.put(key, mapICConfusion.get(key));
				}
			}

			for (String key : mapICNoConfusion.keySet()) {
				if (!mapGCNoConfusion.containsKey(key)) {
					mapGCNoConfusion.put(key, mapICNoConfusion.get(key));
				}
			}

			for (String key : mapGCConfusion.keySet()) {
				if (mapGCNoConfusion.containsKey(key)) {
					System.out.println("removed: " + key);
					mapGCNoConfusion.remove(key);
				}
			}

			String fileNoConfusion = emailsDir + Const.EMAIL_LIST + Const._TXT;

			try (Writer writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(fileNoConfusion), Const._UTF_8))) {

				for (String key : mapGCNoConfusion.keySet()) {

					writer.write(mapGCNoConfusion.get(key) + Const.NEW_LINE);
				}

			} catch (Exception e) {
				System.out.println(e);
			}

			String fileConfusion = emailsDir + Const.EMAIL_LIST + Const.DASH + Const.CONFUSION + Const._TXT;

			try (Writer writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(fileConfusion), Const._UTF_8))) {

				for (String key : mapGCConfusion.keySet()) {

					writer.write(mapGCConfusion.get(key) + Const.NEW_LINE);
				}

			} catch (Exception e) {
				System.out.println(e);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Done with compileEmails...");

	}

	public static void main(String[] args) {

		compileEmails();
	}
}
