/**
 * 
 */
package fbrecommender;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

/**
 * This class is the PageDescriptionParser, creates a Lucene compliant document
 * and puts into the directory where it can be looked for
 * @author SatNam621
 *
 */
public class PageDescriptionParser {

	IndexCreator idx_creator = new IndexCreator();
	ArrayList<Document> pageData = new ArrayList<Document>();
	HashMap<Long,Query> queryData = new HashMap<Long,Query>();
	public static final FieldType TYPE_STORED = new FieldType();
	static {
		TYPE_STORED.setIndexed(true);
		TYPE_STORED.setTokenized(true);
		TYPE_STORED.setStored(true);
		TYPE_STORED.setStoreTermVectors(true);
		TYPE_STORED.setStoreTermVectorPositions(true);
		TYPE_STORED.freeze();
	}

	public static void main(String a[]){

		//parser.idx_creator.search(parser.pageData,parser.queryData);
	}

	protected void parsePageDescData(String filePath, boolean isQuery){

		BufferedReader bufferedReader = null;
		InputStreamReader fileReader= null;
		try {
			long documentId = 0;

			String documentDesc = null;
			InputStream is = new FileInputStream(filePath);
			fileReader= new InputStreamReader(is,Charset.forName("UTF-16"));	
			bufferedReader = new BufferedReader(fileReader);
			String line = null; 
			while((line = bufferedReader.readLine()) != null){


				switch(line.trim()) { 	

				case("<DOCID>") :    // the document id
					line = bufferedReader.readLine();
				documentId  = new Long(line);								
				break;
				case ("<DESC>"):    // document title
					while(((line = bufferedReader.readLine()) != null) && !line.equals("</DOCID>"))
					{

						documentDesc += line;
					}


				case("</DOCID>") :    
					if(!isQuery){
						if(line != null || !line.equals(""))
						{
							Document document = new Document();
							LongField pageId = new LongField("pageId", documentId, Field.Store.NO);
							Field pageDesc = new Field("pageDesc",documentDesc, TYPE_STORED);
							document.add(pageId);
							document.add(pageDesc);

							pageData.add(document);

						}
					}
					else {
						queryData.put(documentId, buildQuery(documentDesc));
					}
				break;										
				}
			}
			if(!isQuery)
			{
				idx_creator.createIndex(pageData, "../data/indexFiles");
			}


		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}	 
		finally {
			try {
				fileReader.close();
				bufferedReader.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


	}


	
	
	protected Query buildQuery(String queryDesc){

		Query query = null;

		try {
			StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_45);
			StandardQueryParser parser = new StandardQueryParser(analyzer);
			queryDesc = queryDesc.replaceAll("/", " ").replace("?", "");
			queryDesc = queryDesc.replace(" with ", " ").replace(" given ", " ").replace(" when ", " ").replace(" can ", " ").replace(" there "," ").replace(" from ", " ");
			query = parser.parse(queryDesc, "documentDesc");
		} catch (QueryNodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return query;	

	}

}
