/**
 * 
 */
package fbrecommender;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;



/**
 * This class creates Indexes and documents in Lucene
 * @author SatNam621
 *
 */

public class IndexCreator {

	IndexWriter mIndexWriter;

	IndexWriterConfig mIndexWriterConfig;

	Directory indexDirectory;
	String[] fields_array = {"pageId","pageDesc"};
	
	Analyzer analyzer = null;

	public IndexWriter getIndexWriterInstance(){
		return mIndexWriter;
	}

	public void createIndex (ArrayList<Document> documentList,String outputFilePath){

		//Directory indexDirectory = null;
		try {

			File indexFile = new File(outputFilePath);
			if(!indexFile.exists()) {
				indexFile.createNewFile();
			}


			if(!checkIfInstanceExists()){
				indexDirectory = new RAMDirectory();
				analyzer =  new StandardAnalyzer(Version.LUCENE_45);
				mIndexWriterConfig = new IndexWriterConfig(Version.LUCENE_45, analyzer);
				mIndexWriterConfig.setOpenMode(OpenMode.CREATE);
				mIndexWriterConfig.setRAMBufferSizeMB(1024);
				//mIndexWriter.forceMerge(12);// costly operation
				mIndexWriter = new IndexWriter(indexDirectory, mIndexWriterConfig);
			} 

			if (mIndexWriter.getConfig().getOpenMode() == OpenMode.CREATE) {
				// New index, so we just add the document (no old document can be there):					
                for (Document document : documentList)
				 {
                	mIndexWriter.addDocument(document);
				 }

			}	

		}
		catch (IOException e) {

			e.printStackTrace();
		}	
		finally{

		}

	}

	private boolean checkIfInstanceExists(){
		if(null != mIndexWriter && null != mIndexWriterConfig){
			return true;
		}
		return false;
	}

	
	protected  void commitIndexWriter(){
		try {
			mIndexWriter.commit();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	/*protected void search(ArrayList<Document> pageData, ArrayList<Document> queryData){
		 DirectoryReader ireader = DirectoryReader.open(indexDirectory);
		    IndexSearcher isearcher = new IndexSearcher(ireader);
		    // Parse a simple query that searches for "text":
		    QueryParser parser = new QueryParser(Version.LUCENE_45, "fieldname", analyzer);
		    Query query = parser.parse("text");
		    ScoreDoc[] hits = isearcher.search(query, null, 1000).scoreDocs;
		    assertEquals(1, hits.length);
		    // Iterate through the results:
		    for (int i = 0; i < hits.length; i++) {
		      Document hitDoc = isearcher.doc(hits[i].doc);
		      assertEquals("This is the text to be indexed.", hitDoc.get("fieldname"));
		    }
		    ireader.close();
		    directory.close();
	}*/
}
