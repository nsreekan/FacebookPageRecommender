import org.grouplens.lenskit.RatingPredictor
import org.grouplens.lenskit.eval.data.crossfold.RandomOrder
import org.grouplens.lenskit.eval.metrics.predict.CoveragePredictMetric
import org.grouplens.lenskit.eval.metrics.predict.MAEPredictMetric
import org.grouplens.lenskit.eval.metrics.predict.NDCGPredictMetric
import org.grouplens.lenskit.eval.metrics.predict.RMSEPredictMetric
import org.grouplens.lenskit.knn.CosineSimilarity
import org.grouplens.lenskit.knn.Similarity
import org.grouplens.lenskit.knn.item.ItemItemRatingPredictor
import org.grouplens.lenskit.knn.params.NeighborhoodSize
import org.grouplens.lenskit.knn.params.UserSimilarity
import org.grouplens.lenskit.knn.params.ItemSimilarity
import org.grouplens.lenskit.knn.user.UserUserRatingPredictor
import org.grouplens.lenskit.norm.BaselineSubtractingNormalizer
import org.grouplens.lenskit.norm.MeanVarianceNormalizer
import org.grouplens.lenskit.norm.VectorNormalizer
import org.grouplens.lenskit.params.NormalizerBaseline
import org.grouplens.lenskit.params.PredictNormalizer
import org.grouplens.lenskit.params.UserVectorNormalizer
import org.grouplens.lenskit.params.Damping
import org.grouplens.lenskit.slopeone.SlopeOneModel
import org.grouplens.lenskit.slopeone.SlopeOneRatingPredictor
import org.grouplens.lenskit.slopeone.WeightedSlopeOneRatingPredictor
import org.grouplens.lenskit.svd.FunkSVDRatingPredictor
import org.grouplens.lenskit.svd.params.FeatureCount
import org.grouplens.lenskit.svd.params.IterationCount
import org.grouplens.lenskit.baseline.*
import org.grouplens.lenskit.knn.params.ModelSize

def baselines = [GlobalMeanPredictor, UserMeanPredictor, ItemMeanPredictor, ItemUserMeanPredictor]

def buildDir = System.getProperty("project.build.directory", ".")

def ml100k = crossfold("ml-100k") {
	source csvfile("${buildDir}/ml-100k/u.data") {
		delimiter "\t"
		domain {
			minimum 1.0
			maximum 5.0
			precision 1.0
		}
	}
	order RandomOrder
	holdout 10
	partitions 5
	train "${buildDir}/ml-100k.train.%d.csv"
	test "${buildDir}/ml-100k.test.%d.csv"
}

trainTest {
	depends ml100k
	dataset ml100k

	output "${buildDir}/eval-results.csv"
	predictOutput "${buildDir}/eval-preds.csv"

	metric CoveragePredictMetric
	metric MAEPredictMetric
	metric RMSEPredictMetric
	metric NDCGPredictMetric

	for (bl in baselines) {
		algorithm(bl.simpleName.replaceFirst(/Predictor$/, "")) {
			bind RatingPredictor to BaselineRatingPredictor
			bind BaselinePredictor to bl
		}
	}

	algorithm("UserUser") {
		bind RatingPredictor to UserUserRatingPredictor
		bind VectorNormalizer withQualifier PredictNormalizer to MeanVarianceNormalizer
		bind BaselinePredictor to ItemUserMeanPredictor
		bind BaselinePredictor withQualifier NormalizerBaseline to UserMeanPredictor
		bind VectorNormalizer withQualifier UserVectorNormalizer to BaselineSubtractingNormalizer
		bind Similarity withQualifier UserSimilarity to CosineSimilarity
		within (UserSimilarity, Similarity) bind Double withQualifier Damping to 100.0d
		bind Integer withQualifier NeighborhoodSize to 30
//        setComponent(RatingPredictor, UserUserRatingPredictor)
//        setComnponent(PredictNormalizer, VectorNormalizer, MeanVarianceNormalizer)
//        setComponent(BaselinePredictor, ItemUserMeanPredictor)
//        setComponent(NormalizerBaseline, BaselinePredictor, UserMeanPredictor)
//        setComponent(UserVectorNormalizer, VectorNormalizer, BaselineSubtractingNormalizer)
//        setComponent(UserSimilarity, Similarity, CosineSimilarity)
//        set(SimilarityDamping, 100)
//        set(NeighborhoodSize, 30)
	}

	algorithm("ItemItem") {
		bind RatingPredictor to ItemItemRatingPredictor
		bind BaselinePredictor to ItemUserMeanPredictor
		bind BaselinePredictor withQualifier NormalizerBaseline to UserMeanPredictor
		bind VectorNormalizer withQualifier UserVectorNormalizer to BaselineSubtractingNormalizer
		within (ItemSimilarity, Similarity) bind Double withQualifier Damping to 100.0d
		bind Integer withQualifier NeighborhoodSize to 30
//        setComponent(RatingPredictor, ItemItemRatingPredictor)
//        setComponent(BaselinePredictor, ItemUserMeanPredictor)
//        setComponent(UserVectorNormalizer, VectorNormalizer, BaselineSubtractingNormalizer)
//        set(SimilarityDamping, 100)
//        set(NeighborhoodSize, 30);
	}
	
	algorithm("WeightedSlopeOne") {
		bind BaselinePredictor withQualifier NormalizerBaseline to GlobalMeanPredictor
		bind VectorNormalizer withQualifier UserVectorNormalizer to BaselineSubtractingNormalizer
		bind RatingPredictor to WeightedSlopeOneRatingPredictor
		bind BaselinePredictor to ItemUserMeanPredictor
		within SlopeOneModel bind Integer withQualifier Damping to 0
//        setComponent(NormalizerBaseline, BaselinePredictor, GlobalMeanPredictor)
//        setComponent(UserVectorNormalizer, VectorNormalizer, BaselineSubtractingNormalizer)
//        setComponent(RatingPredictor, WeightedSlopeOneRatingPredictor)
//        setComponent(BaselinePredictor, ItemUserMeanPredictor)
//        set(DeviationDamping, 0)
	}

	algorithm("SlopeOne") {
		bind BaselinePredictor withQualifier NormalizerBaseline to GlobalMeanPredictor
		bind VectorNormalizer withQualifier UserVectorNormalizer to BaselineSubtractingNormalizer
		bind RatingPredictor to SlopeOneRatingPredictor
		bind BaselinePredictor to ItemUserMeanPredictor
		within SlopeOneModel bind Integer withQualifier Damping to 0
//        setComponent(NormalizerBaseline, BaselinePredictor, GlobalMeanPredictor)
//        setComponent(UserVectorNormalizer, VectorNormalizer, BaselineSubtractingNormalizer)
//        setComponent(RatingPredictor, SlopeOneRatingPredictor)
//        setComponent(BaselinePredictor, ItemUserMeanPredictor)
//        set(DeviationDamping, 0)
	}

	algorithm("FunkSVD") {
		bind RatingPredictor to FunkSVDRatingPredictor
		bind BaselinePredictor to ItemUserMeanPredictor
		bind Integer withQualifier FeatureCount to 30
		bind Integer withQualifier IterationCount to 100
//        setComponent(RatingPredictor, FunkSVDRatingPredictor)
//        setComponent(BaselinePredictor, ItemUserMeanPredictor)
//        set(FeatureCount, 30)
//        set(IterationCount, 100)
	}
}