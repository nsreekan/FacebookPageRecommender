/**
 * 
 */
package fbrecommender;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.apache.mahout.cf.taste.eval.IRStatistics;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;




/**
 * This class focusses on calling the recommendation methods and also
 * evaluating these methods and providing recommendations and the reasons for it
 * @author SatNam621
 *
 */
public class PageRecommenderFB {
	
	FBJsonFeedParser parser = new FBJsonFeedParser();

	public static void main(String args[]){
		PageRecommenderFB recommender =  new PageRecommenderFB();
		recommender.getRecommendations(args[0]);
		
	}
	
	protected void getRecommendations(String fileName){
		parser.parseFiles(fileName);
		IRStatistics stats = null;
		Long startTime = System.currentTimeMillis();
		UserBasedRecommenderForFbPage userBasedRecommender =  new UserBasedRecommenderForFbPage();
		stats = userBasedRecommender.getUserBasedRecommendations(parser.getUserId(),this);
		Long endTime = System.currentTimeMillis();
		Long time_run = endTime-startTime;
		writeToLogFile("../data/EvalData.txt","User Based CBF",stats,time_run);
		
		startTime = System.currentTimeMillis();
		ItembasedRecommenderForFBPage itemBasedRecommender =  new ItembasedRecommenderForFBPage();
		stats = itemBasedRecommender.getItemBasedRecommendations(parser.getUserId(),this);
		endTime = System.currentTimeMillis();
		time_run = endTime-startTime;
		writeToLogFile("../data/EvalData.txt","Item Based CBF",stats,time_run);
		
		startTime = System.currentTimeMillis();
		SVDBasedRecommenderForFBPage svdRecommenderForFBPage = new SVDBasedRecommenderForFBPage();
		stats = svdRecommenderForFBPage.getSVDBasedRecommendations(parser.getUserId(),this);
		endTime = System.currentTimeMillis();
		time_run = endTime-startTime;
		writeToLogFile("../data/EvalData.txt","SVD",stats,time_run);
		
		startTime = System.currentTimeMillis();
		KClusteringRecommenderForFBPage clusteredRecommenderForFBPage = new KClusteringRecommenderForFBPage();
		clusteredRecommenderForFBPage.getUserBasedRecommendations(parser.getUserId(), 
				this, 5);
		endTime = System.currentTimeMillis();
		time_run = startTime-endTime;
		writeToLogFile("../data/EvalData.txt","UserClusters",stats,time_run);

		startTime = System.currentTimeMillis();
		ContentBasedItemRecommenderForFBPage cbIR = new ContentBasedItemRecommenderForFBPage();
		stats = cbIR.getItemBasedonContentRecommendations(parser.getUserId(), this, parser);
		endTime = System.currentTimeMillis();
		time_run = endTime-startTime;
		writeToLogFile("../data/EvalData.txt","Content Based",stats,time_run);
		
	}

	
	protected void showRecommendationsToUser(String fileName,String title,
			List<RecommendedItem> recommendations) {
		LinkedHashMap<Long, Page> pages = parser.getPages();
		LinkedHashMap<Long, String> userMap = parser.getUsers();
		List<Long> pageIds = new ArrayList<Long> ();
		for (RecommendedItem scoredItem: recommendations){
			pageIds.add(scoredItem.getItemID());
		}
		HashMap <String, ArrayList<Long>> pagesByCategory = getPagesByCategory(pageIds);
		StringBuilder builder = new StringBuilder();
		builder.append("Here is the list of recommended Items \n");
		builder.append(title+"\n");
		/*Set<String> keySet =pagesByCategory.keySet();
		for(String category: keySet){
			builder.append("Categories "+ category +": \n");
			ArrayList<Long> ids = pagesByCategory.get(category);
			for (Long id: ids){
				Page page = pages.get(id);
				LinkedHashMap<Long, ArrayList<Long>> friendsWhoLikedThePage = parser.getPageLikedBy();
				List<Long> userIds = friendsWhoLikedThePage.get(id);
				StringBuilder friendsString = new StringBuilder();
				for(Long userId: userIds){
					friendsString.append(userMap.get(userId) +" ");
				}
				builder.append(page.getName()+ "\t" + "Also Liked By " + friendsString.toString()+"\n");
			}
		}*/
		for (Long id: pageIds){
			Page page = pages.get(id);
			LinkedHashMap<Long, ArrayList<Long>> friendsWhoLikedThePage = parser.getPageLikedBy();
			List<Long> userIds = friendsWhoLikedThePage.get(id);
			StringBuilder friendsString = new StringBuilder();
			for(Long userId: userIds){
				friendsString.append(userMap.get(userId) +" ");
			}
			builder.append(page.getName()+ "\t" + "Also Liked By " + friendsString.toString()+"\n");
		}
		writeToLogFile(fileName,builder.toString());
	}
	
	public void writeToLogFile(String string, String results) {

		PrintWriter writer;
		try {
			writer = new PrintWriter(string, "UTF-8");
			writer.print(results);
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			
			e.printStackTrace();
		}
	}

	protected void showRecommendationsToUserCB(String fileName,String title,
			List<RecommendedItem> recommendations) {
		
		LinkedHashMap<Long, String> userMap = parser.getUsers();
		List<Long> pageIds = new ArrayList<Long> ();
		for (RecommendedItem scoredItem: recommendations){
			pageIds.add(scoredItem.getItemID());
		}
		LinkedHashMap<Long, Page> pages = parser.getPages();
		HashMap <String, ArrayList<Long>> pagesByCategory = getPagesByCategory(pageIds);
		StringBuilder builder = new StringBuilder();
		builder.append("Here is the list of recommended Items \n");
		builder.append(title+"\n");
		Set<String> keySet =pagesByCategory.keySet();
		HashMap <String, ArrayList<Long>> userPagesByCategory = getPagesByCategory(parser.getUserItems());
		for(String category: keySet){
			builder.append("Categories "+ category +": \n");
			ArrayList<Long> ids = pagesByCategory.get(category);
			ArrayList<Long> userPageIds = userPagesByCategory.get(category);
			StringBuilder userPagebuilder = new StringBuilder();
			for(Long userPage: userPageIds){
				Page page = pages.get(userPage);
				userPagebuilder.append(page.getName()+"\t");
			}
			builder.append("Because you liked pages "+userPagebuilder.toString()+"\n");
			//+ "\t" + "because you liked " + category+"\n"
			for (Long id: ids){
				Page page = pages.get(id);
				
				builder.append(page.getName()+"\n");
			}
		}
		writeToLogFile(fileName, builder.toString());
	}

	private HashMap <String, ArrayList<Long>> getPagesByCategory(List<Long> pageIds) {
		LinkedHashMap<Long, Page> pages = parser.getPages();
		HashMap <String, ArrayList<Long>> pagesByCategory = new HashMap <String, ArrayList<Long>>();
		for(Long pageId : pageIds){
			Page page= pages.get(pageId);
			String category = page.getCategory();
			if(pagesByCategory.containsKey(category)){
				ArrayList<Long> idList = pagesByCategory.get(category);
				idList.add(pageId);
			} else {
				ArrayList<Long> idList = new ArrayList<Long>();
				idList.add(pageId);
				pagesByCategory.put(category, idList);
			}
			
		}
		return pagesByCategory;
	}
	
	public  void writeToLogFile(String path, String title,IRStatistics stats,Long time_run){
		FileOutputStream stream = null;
		try {
			stream = new FileOutputStream(path, true);
			stream.write((title +"\n").getBytes());
			stream.write(("No of users friends "+(parser.getUsers().size()-1)+"\n").getBytes());
			stream.write(("No of users likes "+parser.getUserItems().size()+"\n").getBytes());
			stream.write(("Precision \t" + stats.getPrecision()+"\n").getBytes());
			stream.write(("Recall \t" + stats.getRecall()+"\n").getBytes());
			stream.write(("F1 measure \t" + stats.getF1Measure()+"\n").getBytes());
			stream.write(("Time executed \t" + time_run+"\n").getBytes());

		}catch(Exception e){
			
		}finally {
		    try {
				stream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
