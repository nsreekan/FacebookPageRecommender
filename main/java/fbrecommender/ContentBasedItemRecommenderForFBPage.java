/**
 * 
 */
package fbrecommender;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.IRStatistics;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderIRStatsEvaluator;
import org.apache.mahout.cf.taste.impl.eval.GenericRecommenderIRStatsEvaluator;
import org.apache.mahout.cf.taste.impl.model.GenericBooleanPrefDataModel;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.recommender.CachingRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.TanimotoCoefficientSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.ItemBasedRecommender;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;

/**
 * This class implements some of the Mahout taste APIs 
 * to bring in content based filtering
 * @author SatNam621
 *
 */
public class ContentBasedItemRecommenderForFBPage {

	protected IRStatistics getItemBasedonContentRecommendations(Long userId, 
			PageRecommenderFB main, final FBJsonFeedParser parser){
		IRStatistics stats = null;
		try {
			DataModel model = new FileDataModel(new File("../data/Rating.csv"));
			model =  new GenericBooleanPrefDataModel(model);
			ArrayList<Long> likedByUser = (ArrayList<Long>) parser.getUserItems();

			//ItemSimilarity itemSimilarity = new ContentBasedItemSimilarity(parser,likedByUser,parser.getPages()) ;

			ItemSimilarity itemSimilarity = new ContentBasedItemDescSimilarity(parser,likedByUser,parser.getPages()) ;
			ItemBasedRecommender recommender =
					new GenericItemBasedRecommender(model, itemSimilarity);
			final Recommender cachingRecommender = new CachingRecommender(recommender);


			List<RecommendedItem> recommendations =
					cachingRecommender.recommend(userId, 50);

			RecommenderIRStatsEvaluator evaluator =
					new GenericRecommenderIRStatsEvaluator();

			RecommenderBuilder builder = new RecommenderBuilder() {
				public Recommender buildRecommender(DataModel testModel) {

					/*Specifies the Similarity algorithm*/
					ItemSimilarity itemSimilarity = new LogLikelihoodSimilarity(testModel);

					/*Initalizing the recommender */

					ItemBasedRecommender recommender =new GenericItemBasedRecommender(testModel, itemSimilarity);

					return recommender;					 
				}
			};
			stats = evaluator.evaluate(builder, null, model, null, 10,Double.NaN,0.7);

			System.out.print(stats);

			System.out.println("The size of the recommendations are "+ recommendations.size());

			main.showRecommendationsToUserCB("../data/ContentBasedDesc.txt","Based on the type of pages you liked ! ",recommendations);
			System.out.println(recommendations);
		}



		catch (IOException e) {

			e.printStackTrace();
		} catch (TasteException e) {

			e.printStackTrace();
		}

		return stats;

	}

}
