package solr.tests;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class ReplaceNumberTest {

	public static String replaceNames(String str) {

		String nameListPath = "C:/Users/febert/Documents/genderComputer/nameLists/";

		HashSet<String> nameList = readNameListFiles(nameListPath);

		String[] words = str.split(" ");
		
		for (int i = 0; i < words.length; i++) {
			if (nameList.contains(words[i])) {
				str = str.replaceAll(words[i], "@USERNAME");
			}
		}
		
		return str;
	}

	public static HashSet<String> readNameListFiles(String filePath) {
		HashSet<String> set = new HashSet<String>();
		try {
			List<File> filesInFolder = new ArrayList<>();
			filesInFolder.addAll(Files.walk(Paths.get(filePath)).filter(Files::isRegularFile).map(Path::toFile)
					.collect(Collectors.toList()));
			for (File file : filesInFolder) {
				List<String> list = Files.readAllLines(file.toPath());
				for (String line : list) {
					String[] array = line.split(";");
					set.add(array[0]);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return set;
	}

	public static void main(String[] args) {

		String str = "Patch Do Set NUMBER: Alan  SureÃ¢â‚¬Â¦ Do you have any existing Felipe Jessica Mike";

		str = replaceNames(str);

		System.out.println(str);

	}
}
