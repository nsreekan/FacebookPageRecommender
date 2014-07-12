/**
 * 
 */
package fbrecommender;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.IRStatistics;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderIRStatsEvaluator;
import org.apache.mahout.cf.taste.impl.eval.GenericRecommenderIRStatsEvaluator;
import org.apache.mahout.cf.taste.impl.model.GenericBooleanPrefDataModel;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.recommender.CachingRecommender;
import org.apache.mahout.cf.taste.impl.recommender.svd.SVDRecommender;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.ItemBasedRecommender;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;

/**
 * This implements the SVD based Facebook recommender 
 * @author SatNam621
 *
 */
public class SVDBasedRecommenderForFBPage {
	protected IRStatistics getSVDBasedRecommendations(Long userId, PageRecommenderFB main) {
		IRStatistics stats = null;
		try {
			DataModel model = new FileDataModel(new File("../data/Rating.csv"));
			model =  new GenericBooleanPrefDataModel(model);
			ItemSimilarity itemSimilarity = new LogLikelihoodSimilarity(model);
			
			Recommender recommender = 
					 new SVDRecommender(model, 30, 10); 

			final Recommender cachingRecommender = new CachingRecommender(recommender);


			List<RecommendedItem> recommendations =
					cachingRecommender.recommend(userId, 50);

			RecommenderIRStatsEvaluator evaluator =
					new GenericRecommenderIRStatsEvaluator();
			
			
			RecommenderBuilder builder = new RecommenderBuilder() {
				public Recommender buildRecommender(DataModel testModel) {
					Recommender recommender = null;
	                
	                 
	                  /*Initalizing the recommender */
	                  try {
						recommender = 
								 new SVDRecommender(testModel, 30, 10);
						
					} catch (TasteException e) {
					
						e.printStackTrace();
					}
	                  return recommender;		 
				}
			};
			 stats =
					evaluator.evaluate(builder, null, model, null, 10,0.01,0.7);
			
			System.out.print(stats);
			
			System.out.println("The size of the recommendations are "+ recommendations.size());
			
			main.showRecommendationsToUser("../data/ItemsBasedSVD.txt","Based on the pages you liked ! ",recommendations);
			System.out.println(recommendations);
			
		} catch (IOException e) {

			e.printStackTrace();
		} catch (TasteException e) {

			e.printStackTrace();
		}
      return stats;
	}
}
