package solr.analysis;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import solr.utils.Const;

import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;
import java.io.FileOutputStream;

public class XMLReader {

	public static void main(String[] args) {

		try {

			FileOutputStream out = new FileOutputStream("./results-gc/set-training/training-set.xls");
			Workbook wb = new HSSFWorkbook();
			Sheet s = wb.createSheet();
			wb.setSheetName(Const._0, Const.HEDGES);
			Row r = null;
			Cell c = null;
			int lineCounter = 1;
			r = s.createRow(Const._0);
			c = r.createCell(Const._0);
			c.setCellValue(Const.ID);
			c = r.createCell(Const._1);
			c.setCellValue(Const.CONFUSION);
			c = r.createCell(Const._2);
			c.setCellValue(Const.REASONING);
			c = r.createCell(Const._3);
			c.setCellValue(Const.COMMENT);
			
			File fXmlFile = new File("./results-gc/set-training/training-set.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName("example");

			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;
					String id = eElement.getElementsByTagName("id").item(0).getTextContent();
					String confusion = eElement.getElementsByTagName("confusion").item(0).getTextContent();
					confusion = confusion.replaceAll("!", "");
					String reasoning = eElement.getElementsByTagName("reasoning").item(0).getTextContent();
					String message = eElement.getElementsByTagName("message").item(0).getTextContent();
					
					r = s.createRow(lineCounter);
					c = r.createCell(Const._0);
					c.setCellValue(id);
					c = r.createCell(Const._1);
					c.setCellValue(confusion);
					c = r.createCell(Const._2);
					c.setCellValue(reasoning);
					c = r.createCell(Const._3);
					c.setCellValue(message);
					lineCounter = lineCounter + 1;
				}
			}
			
			wb.write(out);
			out.close();
			wb.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Done...");
	}
}
