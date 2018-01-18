package hust.tools.hmm.model;

import hust.tools.hmm.utils.StateSequence;

public class StateSequenceWithScore implements Comparable<StateSequenceWithScore> {
	
	private StateSequence sequence;
	
	private double score;

	public StateSequenceWithScore(StateSequence sequence, double score) {
		this.sequence = sequence;
		this.score = score;
	}
	
	@Override
	public int compareTo(StateSequenceWithScore o) {
		if(o.score != score)
			return o.score > score ? 1 : -1;
		return 0;
	}

	public double getScore() {
		return score;
	}
	
	public StateSequence getStateSequence() {
		return sequence;
	}
	
	public String toString() {
		return sequence.toString();
	}
}
