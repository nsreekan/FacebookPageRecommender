/**
 * 
 */
package fbrecommender;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.commons.lang.ArrayUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.LongField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;

/**
 * This class uses the page description data and builds a content based 
 * recommender system. It uses the Apache Lucene to do match page data
 * @author SatNam621
 *
 */
public class ContentBasedItemDescSimilarity implements ItemSimilarity{

	FBJsonFeedParser parser;
	ArrayList<Long> pagesLikedByUser;
	ArrayList<String> categories= new ArrayList<String>();

	public static final FieldType TYPE_STORED = new FieldType();
	static {
		TYPE_STORED.setIndexed(true);
		TYPE_STORED.setTokenized(true);
		TYPE_STORED.setStored(true);
		TYPE_STORED.setStoreTermVectors(true);
		TYPE_STORED.setStoreTermVectorPositions(true);
		TYPE_STORED.freeze();
	}

	public ContentBasedItemDescSimilarity(FBJsonFeedParser parser, ArrayList<Long> pagesLikedByUser, 
			LinkedHashMap<Long, Page> pageMap) {
		this.parser = parser;

		this.pagesLikedByUser = pagesLikedByUser;
		for(Long pageId : pagesLikedByUser){
			Page page = pageMap.get(pageId);
			if(!categories.contains(page.getCategory())){
				categories.add(page.getCategory());
			}
		}
	}


	@Override
	public void refresh(Collection<Refreshable> arg0) {
		// TODO Auto-generated method stub

	}



	@Override
	/**
	 * This method retrieves the page description for the pages mentioned as
	 * arguments and computes the similarities
	 */
	public double[] itemSimilarities(long arg0, long[] arg1)
			throws TasteException {
        Page page0 = parser.getPages().get(arg0);        
		if(categories.contains(page0.category)){
			Double [] values =buildDocumentDescData(arg0, arg1);
			if(values == null){
				return new double[arg1.length];
			}
			double[] similarities =  ArrayUtils.toPrimitive(values);
			return similarities;
		}else {
			return new double[arg1.length];
		}

	}

	/**
	 * This method builds the document description as document and removes stop words
	 * computes the score of the query document with all the pages.
	 * @param query page description of the page liked by the user
	 * @param pages all pages liked by users friends
	 * @return scores of similarities.
	 */
	protected Double[] buildDocumentDescData(long query,long[] pages){

		StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_45);
		Directory index = new RAMDirectory();

		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_45, analyzer);
		LinkedHashMap<Long,Double> pageScore = new LinkedHashMap<Long,Double>();
		IndexWriter writer;
		try {
			writer= new IndexWriter(index, config);
			for(long pageId: pages){
				Page page = parser.getPages().get(pageId);
				addDoc(writer, page.pageDesc, pageId);
				pageScore.put(pageId,  0.0);
			}
			writer.close();
			Page page = parser.getPages().get(query);
			String queryDesc = page.pageDesc;
			queryDesc = queryDesc.replaceAll("[^A-Za-z0-9 ]", "");
			queryDesc = queryDesc.replace (":","").replace(" with ", " ").replace(" given ", " ").
					replace(" when ", " ").replace(" can ", " ").
					replace(" there "," ").replace(" from ", " ").replace(" this ", " ");
			if("".equals(queryDesc.trim())){
				return null;
			}
			searchDoc(index,buildQuery(queryDesc),pageScore);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pageScore.values().toArray(new Double[pageScore.values().size()]);
	}

	/**
	 * This method searches the document with Lucene inbuilt method
	 * @param index the directory of the files
	 * @param query the page data like dby the user
	 * @param pageScore ranking score
	 */
	private void searchDoc(Directory index, Query query, LinkedHashMap<Long, Double> pageScore) {

		try {
			int hitsPerPage = pageScore.size();
			IndexReader reader = IndexReader.open(index);
			IndexSearcher searcher = new IndexSearcher(reader);
			TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
			searcher.search(query, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;
			for(int i=0;i<hits.length;++i) {
				int docId = hits[i].doc;
				Document d = searcher.doc(docId);
				pageScore.put(Long.valueOf(d.get("pageId")), (double)hits[i].score);
			}    
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



/**
 * Adds pages to the repository 
 * @param w is the index writer instance that writes to the repository
 * @param title is the title of the document
 * @param id is the document id
 * @throws IOException
 */
	private void addDoc(IndexWriter w, String title, long id) throws IOException {
		Document doc = new Document();
		doc.add(new Field("pageDesc", title, TYPE_STORED));
		LongField field = new LongField("pageId", id, Field.Store.YES);
		field.setLongValue(id);
		doc.add(field);
		w.addDocument(doc);
	}
/**
 * This method uses the lucene method and build a query format for every 
 * page liked by the user
 * @param queryDesc
 * @return query instance 
 */
	protected Query buildQuery(String queryDesc){

		Query query = null;

		try {
			StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_45);
			StandardQueryParser parser = new StandardQueryParser(analyzer);
			/*queryDesc = queryDesc.trim().replace("!","").replaceAll("/", " ").replace("?", "").replace(":", " ");*/

			query = parser.parse(queryDesc, "pageDesc");
		} catch (QueryNodeException e) {

			System.out.println(queryDesc);
			e.printStackTrace();
		}
		return query;	

	}


	@Override
	public double itemSimilarity(long arg0, long arg1) throws TasteException {
		// TODO Auto-generated method stub
		return 0;
	}

}
