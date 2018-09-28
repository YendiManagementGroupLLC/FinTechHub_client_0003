package ext.com.galacticfog.algo;

import java.util.ArrayList;
import java.util.List;

public class BulbDrop {

	// Builds the list of steps for given max # of floors and max # of bulbs
	public static List<Integer> bulbDropSteps(int maxFloors) {

		// Change here to increase/decrease max # of bulbs in test
		int maxBulbs = 2;

		// Initialize low and high as first and max # floors
		int low = 1, high = maxFloors;

		// Do binary search and for every mid, find binomial coefficients sum
		while (low < high) {
			
			// Split for binary search, looking for middle
			int mid = (low + high) / 2;
			
			// Do binomial coefficent fxn sum: binCoeff(mid,maxBulbs,maxFloors)
			int sum = 0, term = 1;
			for (int counter = 1; counter <= maxBulbs && sum < maxFloors; ++counter) {
				term *= mid - counter + 1;
				term /= counter;
				sum += term;
			}

			// Check sum to see which portion of the split needs more testing
			if (sum < maxFloors)
				low = mid + 1;
			else
				high = mid;
		}

		// Low value is the minimum of number of trials
		int minTrials = low;

		// Create the return list that shows all the floors that visited to minimum
		List<Integer> steps = new ArrayList<Integer>();
		for (int counter = 1; counter <= minTrials; counter++)
			steps.add(counter);

		// Return the list to caller
		return steps;
	}

	// Entry point for testing
	public static void main(String args[]) {
		int maxFloors = 100;
		System.out.println("For " + maxFloors + " floors, minimum steps: " + bulbDropSteps(maxFloors));
	}
}