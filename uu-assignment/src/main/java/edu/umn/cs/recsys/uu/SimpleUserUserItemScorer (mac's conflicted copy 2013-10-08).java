package edu.umn.cs.recsys.uu;

import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.grouplens.lenskit.basic.AbstractItemScorer;
import org.grouplens.lenskit.data.dao.ItemEventDAO;
import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.history.History;
import org.grouplens.lenskit.data.history.RatingVectorUserHistorySummarizer;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.vectors.ImmutableSparseVector;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.grouplens.lenskit.vectors.similarity.CosineVectorSimilarity;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * User-user item scorer.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SimpleUserUserItemScorer extends AbstractItemScorer {
    private final UserEventDAO userDao;
    private final ItemEventDAO itemDao;

    @Inject
    public SimpleUserUserItemScorer(UserEventDAO udao, ItemEventDAO idao) {
        userDao = udao;
        itemDao = idao;
    }

    @Override
    public void score(long user, @Nonnull MutableSparseVector scores) {
        SparseVector userVector = getUserRatingVector(user);
       
        // This is the loop structure to iterate over items to score
        for (VectorEntry e: scores.fast(VectorEntry.State.EITHER)) {
        	
        	// get the 30 most similar users who have rated the item
        	LongSet neighbors = similarUsers(user, e.getKey());
        }
    }


	private LongSet similarUsers(long activeUser, long item) {
		
		// the mean-centered rating vector for the active user
		MutableSparseVector amrv = getUserMeanCenteredRatingVector(activeUser);
		
		SortedMap<Double, Long> neighborhood = new TreeMap<Double, Long>(); 
		

		
		// all users who rated the given item
		System.err.println("# getting users who rated item: " + item);
		LongSet possibleNeighbors = itemDao.getUsersForItem(item);
		for (Long user : possibleNeighbors) {
			if(user == activeUser) continue;
			
			MutableSparseVector umrv = getUserMeanCenteredRatingVector(user);
			double sim = new CosineVectorSimilarity().similarity(amrv, umrv);
			
			neighborhood.put(sim, user);			
		}
		
		// return the top 30 similar users who rated the given item
		LongSet returned = new LongArraySet(30);
		for(Map.Entry<Double, Long> entry : neighborhood.entrySet()){
			
		}
		
		
		
		
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
     * Get a user's rating vector.
     * @param user The user ID.
     * @return The modified rating vector.
     */
	private MutableSparseVector getUserMeanCenteredRatingVector(long user){
		SparseVector urv = getUserRatingVector(user);
		MutableSparseVector mrv = urv.mutableCopy();
		MutableSparseVector meanVector = MutableSparseVector.create(mrv.keySet());
		meanVector.fill(mrv.mean());
		mrv.subtract(meanVector);
		return mrv;
	}
	
	/**
     * Get a user's rating vector.
     * @param user The user ID.
     * @return The rating vector.
     */
    private SparseVector getUserRatingVector(long user) {
        UserHistory<Rating> history = userDao.getEventsForUser(user, Rating.class);
        if (history == null) {
            history = History.forUser(user);
        }
        return RatingVectorUserHistorySummarizer.makeRatingVector(history);
    }
}
