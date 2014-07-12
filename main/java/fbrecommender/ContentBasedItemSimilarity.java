/**
 * 
 */
package fbrecommender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;

/**
 * This content based method uses the categories of the data to match the 
 * documents that are similar to each other
 * @author SatNam621
 *
 */
public class ContentBasedItemSimilarity implements ItemSimilarity{
	
	FBJsonFeedParser parser;
	ArrayList<Long> pagesLikedByUser;
	ArrayList<String> categories= new ArrayList<String>();
	
	public ContentBasedItemSimilarity(FBJsonFeedParser parser, ArrayList<Long> pagesLikedByUser, 
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
		return;
	}

	/**
	 * This method uses the category of the page to matach documents
	 */
	@Override
	public double itemSimilarity(long arg0, long arg1) throws TasteException {
		//	TanimotoCoefficientSimilarity itemSimilarity = new TanimotoCoefficientSimilarity();
		double similarity = 0.0;
		Page page0 = parser.getPages().get(arg0);
		if(categories.contains(page0.category)){
			Page page1 = parser.getPages().get(arg0);
			Page page2 = parser.getPages().get(arg1);
			if(page1.getCategory().equals(page2.getCategory())){
				similarity = 1.0;
			}
		}
		return similarity;
	}

	/**
	 * itemSimilarities computes similarities between pages by giving 
	 * higher scores for documents that match and lesser for documents
	 * that do not match
	 */
	@Override
	public double[] itemSimilarities(long arg0, long[] arg1)
			throws TasteException {
		double[] similarity = new double[arg1.length];
		Page page0 = parser.getPages().get(arg0);
		int i=0;
		if(categories.contains(page0.category)){
			for(long pageId: arg1){
				Page page = parser.getPages().get(pageId);				

				if(page0.getCategory().equals(page.getCategory())){
					double simMeasure = 0.0; 
					simMeasure += 0.1;
					similarity[i] = simMeasure;
				}
				i++;
			}
		}
		return similarity;
	}

}
