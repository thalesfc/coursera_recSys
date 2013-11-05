package edu.umn.cs.recsys;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nonnull;

import org.grouplens.lenskit.core.LenskitRecommender;
import org.grouplens.lenskit.eval.algorithm.AlgorithmInstance;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.AbstractTestUserMetric;
import org.grouplens.lenskit.eval.metrics.TestUserMetricAccumulator;
import org.grouplens.lenskit.eval.metrics.topn.ItemSelectors;
import org.grouplens.lenskit.eval.traintest.TestUser;
import org.grouplens.lenskit.scored.ScoredId;

import com.google.common.collect.ImmutableList;

import edu.umn.cs.recsys.dao.ItemTagDAO;

/**
 * A metric that measures the tag entropy of the recommended items.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class TagEntropyMetric extends AbstractTestUserMetric {
	private final int listSize;
	private final List<String> columns;

	/**
	 * Construct a new tag entropy metric.
	 * 
	 * @param nitems The number of items to request.
	 */
	public TagEntropyMetric(int nitems) {
		listSize = nitems;
		// initialize column labels with list length
		columns = ImmutableList.of(String.format("TagEntropy@%d", nitems));
	}

	/**
	 * Make a metric accumulator.  Metrics operate with <em>accumulators</em>, which are created
	 * for each algorithm and data set.  The accumulator measures each user's output, and
	 * accumulates the results into a global statistic for the whole evaluation.
	 *
	 * @param algorithm The algorithm being tested.
	 * @param data The data set being tested with.
	 * @return An accumulator for analyzing this algorithm and data set.
	 */
	@Override
	public TestUserMetricAccumulator makeAccumulator(AlgorithmInstance algorithm, TTDataSet data) {
		return new TagEntropyAccumulator();
	}

	/**
	 * Return the labels for the (global) columns returned by this metric.
	 * @return The labels for the global columns.
	 */
	@Override
	public List<String> getColumnLabels() {
		return columns;
	}

	/**
	 * Return the lables for the per-user columns returned by this metric.
	 */
	@Override
	public List<String> getUserColumnLabels() {
		// per-user and global have the same fields, they just differ in aggregation.
		return columns;
	}


	private class TagEntropyAccumulator implements TestUserMetricAccumulator {
		private double totalEntropy = 0;
		private int userCount = 0;

		/**
		 * Evaluate a single test user's recommendations or predictions.
		 * @param testUser The user's recommendation result.
		 * @return The values for the per-user columns.
		 */
		@Nonnull
		@Override
		public Object[] evaluate(TestUser testUser) {
			List<ScoredId> recommendations =
					testUser.getRecommendations(listSize,
							ItemSelectors.allItems(),
							ItemSelectors.trainingItems());
			if (recommendations == null) {
				return new Object[1];
			}
			LenskitRecommender lkrec = (LenskitRecommender) testUser.getRecommender();
			ItemTagDAO tagDAO = lkrec.get(ItemTagDAO.class);
			//TagVocabulary vocab = lkrec.get(TagVocabulary.class);

			double entropy = 0;

			/*
			 * Creating the uniqueCaseInsensitiveTagsInRecommendationListForThisUser
			 */
			// unique Case Insensitive Tags In Recommendation List For This User
			Set<String> uniqTags = new HashSet<String>();
			// {movie_id : set(movie_tags)}
			Map<Long, Set<String>> moviesTags = new HashMap<Long, Set<String>>(recommendations.size());
			for (ScoredId movie : recommendations) {
				Set<String> movieTags = new HashSet<String>();
				List<String> tags = tagDAO.getItemTags(movie.getId());
				for(String tag : tags){
					String normalized = tag.toLowerCase();
					movieTags.add(normalized);
				}
				moviesTags.put(movie.getId(), movieTags);
				uniqTags.addAll(movieTags);
			}

			/*
			 * # Unique Case Insensitive Tags For This Movie
			 */
			double numberOfUniqueCaseInsensitiveTagsForThisMovie = uniqTags.size();
			double numberOfMoviesInTheRecommendationList = recommendations.size();

			/*
			 * calculating entropy
			 */
			for(String tag : uniqTags){
				double Pt_given_List = 0.0;
				for(Entry<Long, Set<String>> entry : moviesTags.entrySet()){
					Set<String> mTags = entry.getValue();
					if(mTags.contains(tag)){
						Pt_given_List += 1.0/numberOfUniqueCaseInsensitiveTagsForThisMovie;
					}
				}
				Pt_given_List /= numberOfMoviesInTheRecommendationList;
				entropy -= Pt_given_List*Math.log(Pt_given_List); //entropy in nats
			}
			entropy /= Math.log(2.0); //entropy in bits 
			totalEntropy += entropy; //overall entropy for all users on this partition 
			userCount += 1; //number of users for averaging the entropy for this partition
			return new Object[]{entropy};
		}

		/**
		 * Get the final aggregate results.  This is called after all users have been evaluated, and
		 * returns the values for the columns in the global output.
		 *
		 * @return The final, aggregated columns.
		 */
		@Nonnull
		@Override
		public Object[] finalResults() {
			// return a single field, the average entropy
			return new Object[]{totalEntropy / userCount};
		}
	}
}
