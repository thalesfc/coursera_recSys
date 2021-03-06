package edu.umn.cs.recsys.uu;

import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.grouplens.lenskit.basic.AbstractItemScorer;
import org.grouplens.lenskit.data.dao.ItemEventDAO;
import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.history.History;
import org.grouplens.lenskit.data.history.RatingVectorUserHistorySummarizer;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.grouplens.lenskit.vectors.similarity.CosineVectorSimilarity;

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
        	
        	Long item = e.getKey();
        	// get the 30 most similar users who have rated the item
        	List<Entry<Double, Long>> neighbors = similarUsers(user, item);
//        	System.err.println("# Neighboors of user " + user + " - item: " + item);
//        	System.err.println(neighbors);
        	
        	double mu = userVector.mean();
        	double downPart = 0.0;
        	double upperPart = 0.0;
        	
        	// iterate through all neighbors
        	for (Entry<Double, Long> entry : neighbors) {
        		double s_uv = entry.getKey();
        		long neighId = entry.getValue();
        		
        		SparseVector nrv = getUserRatingVector(neighId);
        		double mu_v = nrv.mean();
        		double r_vi = nrv.get(item);
        		
        		upperPart += s_uv * (r_vi - mu_v);
        		downPart += Math.abs(s_uv);
			}
        	
        	double p_ui = mu + upperPart / downPart; 
        	scores.set(item, p_ui);
        }
    }


	private List<Entry<Double, Long>> similarUsers(long activeUser, long item) {
		
		// the mean-centered rating vector for the active user
		MutableSparseVector amrv = getUserMeanCenteredRatingVector(activeUser);
		
		SortedMap<Double, Long> neighborhood = new TreeMap<Double, Long>(Collections.reverseOrder()); 
		

		
		// all users who rated the given item
//		System.err.println("# getting users who rated item: " + item);
		LongSet possibleNeighbors = itemDao.getUsersForItem(item);
		for (Long user : possibleNeighbors) {
			if(user == activeUser) continue;
			
			MutableSparseVector umrv = getUserMeanCenteredRatingVector(user);
			double sim = new CosineVectorSimilarity().similarity(amrv, umrv);
			
			neighborhood.put(sim, user);			
		}
		
		// return the top 30 similar users who rated the given item
		List<Entry<Double, Long>> returned = new ArrayList<Map.Entry<Double,Long>>(30);
		int count = 0;
		for(Map.Entry<Double, Long> entry : neighborhood.entrySet()){
			returned.add(entry);
			if(++count == 30) break;
		}
		return returned;
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
