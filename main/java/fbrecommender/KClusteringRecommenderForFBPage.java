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
import org.apache.mahout.cf.taste.impl.recommender.ClusterSimilarity;
import org.apache.mahout.cf.taste.impl.recommender.FarthestNeighborClusterSimilarity;
import org.apache.mahout.cf.taste.impl.recommender.GenericBooleanPrefUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.NearestNeighborClusterSimilarity;
import org.apache.mahout.cf.taste.impl.recommender.TreeClusteringRecommender;
import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.TanimotoCoefficientSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

/**
 * This class represents the clustering method for FBpage, where
 * users with similar likes are grouped together
 * @author SatNam621
 *
 */
public class KClusteringRecommenderForFBPage {
	protected IRStatistics getUserBasedRecommendations(Long userId, PageRecommenderFB main,final int kSize) {
		IRStatistics stats = null;
		try {
			DataModel model = new FileDataModel(new File("../data/Rating.csv"));
			model =  new GenericBooleanPrefDataModel(model);
			UserSimilarity userSimilarity = new TanimotoCoefficientSimilarity(model);

			ClusterSimilarity clusterSimilarity = new NearestNeighborClusterSimilarity(userSimilarity);

			Recommender recommender = new TreeClusteringRecommender(model, clusterSimilarity, kSize);
			final Recommender cachingRecommender = new CachingRecommender(recommender);


			List<RecommendedItem> recommendations =
					cachingRecommender.recommend(userId, 50);

			RecommenderIRStatsEvaluator evaluator =
					new GenericRecommenderIRStatsEvaluator();

			RecommenderBuilder builder = new RecommenderBuilder() {
				public Recommender buildRecommender(DataModel testModel) {

					Recommender recommender = null;

					try {
						testModel =  new GenericBooleanPrefDataModel(testModel);

						UserSimilarity userSimilarity = new TanimotoCoefficientSimilarity(testModel);

						ClusterSimilarity clusterSimilarity = new NearestNeighborClusterSimilarity(userSimilarity);

						recommender = new TreeClusteringRecommender(testModel, clusterSimilarity, kSize);
					} catch (TasteException e) {
						
						e.printStackTrace();
					}
					return recommender;
				}
			};
			stats =
					evaluator.evaluate(builder, null, model, null, 10,0.0,0.7);
			

			System.out.print(stats);

			System.out.println("The size of the recommendations are "+ recommendations.size());

			main.showRecommendationsToUser("../data/FriendsBasedCluster.txt","Based on Your Friends ! ",recommendations);
			
			System.out.print(stats.getF1Measure());
			System.out.print(stats.getFallOut());
			

		} catch (IOException e) {

			e.printStackTrace();
		} catch (TasteException e) {

			e.printStackTrace();
		}
		return stats;

	}

}
