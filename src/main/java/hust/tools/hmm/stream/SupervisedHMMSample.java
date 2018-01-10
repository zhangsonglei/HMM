package hust.tools.hmm.stream;

import hust.tools.hmm.utils.StateSequence;
import hust.tools.hmm.utils.Observation;
import hust.tools.hmm.utils.ObservationSequence;
import hust.tools.hmm.utils.State;

/**
 *<ul>
 *<li>Description: 包含隐藏状态序列的监督学习样本类 
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2017年12月29日
 *</ul>
 */
public class SupervisedHMMSample extends AbstractHMMSample {
	
	private StateSequence stateSequence;
	
	public SupervisedHMMSample() {
		super();
		stateSequence = new StateSequence();
	}

	public SupervisedHMMSample(StateSequence stateSequence, ObservationSequence observationSequence) {
		super(observationSequence);
		this.stateSequence = stateSequence;
	}
	
	public StateSequence getStateSequence() {
		return stateSequence;
	}

	public void add(State state, Observation observation) {
		stateSequence = stateSequence.add(state);
		add(observation);
	}
	
	public State getState(int i) {
		if(i >= 0 && i < stateSequence.length())
			return stateSequence.get(i);
		
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((stateSequence == null) ? 0 : stateSequence.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SupervisedHMMSample other = (SupervisedHMMSample) obj;
		if (stateSequence == null) {
			if (other.stateSequence != null)
				return false;
		} else if (!stateSequence.equals(other.stateSequence))
			return false;
		return true;
	}

	@Override
	public String toString() {
		String string = "[";
		
		for(int i = 0; i < observationSequence.length(); i++)
			string += "[" + observationSequence.get(i) + ", " + stateSequence.get(i) + "]" + "  ";
		
		return string.trim() + "]";
	}
}
