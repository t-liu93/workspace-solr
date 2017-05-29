package solr.main;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import java.io.File;
import java.io.IOException;

public class BuildCSVFromExcel {

	private static final String EXCEL_FILE_LOCATION = "C:\\Users\\febert\\Downloads\\verifying-set.xls";

	public static void main(String[] args) {

		Workbook workbook = null;

		try {

			workbook = Workbook.getWorkbook(new File(EXCEL_FILE_LOCATION));

			Sheet sheet = workbook.getSheet(0);

			for (int i = 0; i < 400; i++) {

				Cell cell1 = sheet.getCell(1, i);

				String comment = cell1.getContents();

				comment = comment.replaceAll("\n", " ");
				
				comment = comment.replaceAll("\"", "");

				Cell cell2 = sheet.getCell(0, i);

				String label = cell2.getContents();

				System.out.println(label + ",\"" + comment + "\"");

			}

		} catch (IOException | BiffException e) {
			e.printStackTrace();
		} finally {
			if (workbook != null) {
				workbook.close();
			}
		}
	}
}
