/**
 * 
 */
package fbrecommender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.grouplens.lenskit.scored.ScoredId;

/**
 * This class is the starting point for the FBPageRecommender
 * where different methods currently are uncommented for performance
 * uncomment if that method needs to run
 * @author SatNam621
 *
 */
public class FBPageRecommender {
	FBJsonFeedParser parser = new FBJsonFeedParser();
	PageRecommenderFB rec = new PageRecommenderFB();
	
	public static void main(String args[]){
		
		FBPageRecommender recommender = new FBPageRecommender();
	    recommender.parser.parseFiles(args[0]);
	  //  recommender.rec.
	    
	    /*GlobalItemRecommenderForFBPage globalItemRecommender = new GlobalItemRecommenderForFBPage();
		globalItemRecommender.recommendByItemSimilarityGlobally(args[0],recommender.parser.getUserItems(),recommender);
		
		*/
		/*ItemItemRecommenderForFBpage itemItemRecommender = new ItemItemRecommenderForFBpage();
		itemItemRecommender.recommendByItemSimilarity(args[0], recommender.parser.getUserId(),recommender);*/
		
		/*UserUserRecommenderForFBPage userUserRecommender = new UserUserRecommenderForFBPage();
		userUserRecommender.recommendByUserSimilarity(args[0], recommender.parser.getUserId(),recommender);
		*/
		/*SVDForFBPageRecommeder svgRecommender = new SVDForFBPageRecommeder();
		svgRecommender.recommendBySVD(args[0], recommender.parser.getUserId(),recommender);*/
		
	}

	protected void showRecommendationsToUser(String title,
			List<ScoredId> similarItems) {
		LinkedHashMap<Long, Page> pages = parser.getPages();
		LinkedHashMap<Long, String> userMap = parser.getUsers();
		List<Long> pageIds = new ArrayList<Long> ();
		for (ScoredId scoredItem: similarItems){
			pageIds.add(scoredItem.getId());
		}
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
		StringBuilder builder = new StringBuilder();
		builder.append("Here is the list of recommended Items \n");
		builder.append(title+"\n");
		Set<String> keySet =pagesByCategory.keySet();
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
		}
		System.out.println(builder.toString());
	}

	
}
