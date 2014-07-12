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
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.CachingRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericBooleanPrefUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.TanimotoCoefficientSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.apache.mahout.common.RandomUtils;

/**
 * This class represents the user based collaborative filtering 
 * method for providing recommendations
 * @author SatNam621
 *
 */
public class UserBasedRecommenderForFbPage {

	protected IRStatistics getUserBasedRecommendations(Long userId, PageRecommenderFB main) {
		IRStatistics stats = null;
		try {
			DataModel model = new FileDataModel(new File("../data/Rating.csv"));
            model =  new GenericBooleanPrefDataModel(model);
			UserSimilarity userSimilarity = new TanimotoCoefficientSimilarity(model);
			
			UserNeighborhood neighborhood =
					new NearestNUserNeighborhood(20, userSimilarity, model);

			Recommender recommender =
					new GenericBooleanPrefUserBasedRecommender(model, neighborhood, userSimilarity);
			final Recommender cachingRecommender = new CachingRecommender(recommender);


			List<RecommendedItem> recommendations =
					cachingRecommender.recommend(userId, 50);

			RecommenderIRStatsEvaluator evaluator =
					new GenericRecommenderIRStatsEvaluator();
			
			RecommenderBuilder builder = new RecommenderBuilder() {
				public Recommender buildRecommender(DataModel model) {

					//RandomUtils.useTestSeed();
					UserSimilarity userSimilarity = new TanimotoCoefficientSimilarity(model);
					// Optional:
					//userSimilarity.setPreferenceInferrer(new AveragingPreferenceInferrer(model));
					UserNeighborhood neighborhood=null;
					try {
						neighborhood = new NearestNUserNeighborhood(20, userSimilarity, model);
					} catch (TasteException e) {

						e.printStackTrace();
					}

					Recommender recommender =
							new GenericBooleanPrefUserBasedRecommender(model, neighborhood, userSimilarity);
					return recommender;
				}
			};
			stats =
					evaluator.evaluate(builder, null, model, null, 10,0.01,0.7);
			
			System.out.print(stats);
			
			System.out.println("The size of the recommendations are "+ recommendations.size());
			System.out.println(recommendations);
			
			main.showRecommendationsToUser("../data/FriendsBased.txt","Based on Your Friends ! ",recommendations);
			
		} catch (IOException e) {

			e.printStackTrace();
		} catch (TasteException e) {

			e.printStackTrace();
		}
       return stats;
	}

}
