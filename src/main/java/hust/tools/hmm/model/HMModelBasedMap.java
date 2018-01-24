package hust.tools.hmm.model;

import hust.tools.hmm.utils.StateSequence;
import hust.tools.hmm.utils.StringObservation;

import java.util.HashMap;
import java.util.Set;

import hust.tools.hmm.utils.Dictionary;
import hust.tools.hmm.utils.Observation;
import hust.tools.hmm.utils.State;

/**
 *<ul>
 *<li>Description: 基于Map数据结构的隐式马尔科夫模型
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2017年12月26日
 *</ul>
 */
public class HMModelBasedMap implements HMModel {
	
	/**
	 * 版本序列号
	 */
	private static final long serialVersionUID = 4845677181963311625L;

	private final Observation UNKNOWN = new StringObservation("UNKNOWN");
	
	private int order;
	
	private Dictionary dict;
	
	private HashMap<State, Double> pi;
	
	private HashMap<StateSequence, TransitionProbEntry>  transitionMatrix;
	
	private HashMap<State, EmissionProbEntry>  emissionMatrix;
	
	public HMModelBasedMap(int order, Dictionary dict, HashMap<State, Double> pi, HashMap<StateSequence, TransitionProbEntry>  transitionMatrix, HashMap<State, EmissionProbEntry>  emissionMatrix) {
		this.order = order;
		this.dict = dict;
		this.pi = pi;
		this.transitionMatrix = transitionMatrix;
		this.emissionMatrix = emissionMatrix;
	}
	
	@Override
	public int getOrder() {
		return order;
	}
	
	@Override
	public Dictionary getDict() {
		return dict;
	} 
	
	@Override
	public HashMap<State, Double> getPi() {
		return pi;
	}

	@Override
	public HashMap<StateSequence, TransitionProbEntry> getTransitionMatrix() {
		return transitionMatrix;
	}

	@Override
	public HashMap<State, EmissionProbEntry> getEmissionMatrix() {
		return emissionMatrix;
	}
	
	@Override
	public double transitionLogProb(StateSequence start, State target) {		
		if(transitionMatrix.containsKey(start))
			if(transitionMatrix.get(start).contain(target))
				return transitionMatrix.get(start).getTransitionLogProb(target);
		
		return Math.log10(Double.MIN_VALUE);
	}
	
	@Override
	public double transitionLogProb(int[] start, int target) {
		State[] si = new State[start.length];
		for(int i = 0; i < start.length; i++)
			si[i] = dict.getState(start[i]);
		
		State sj = dict.getState(target);
				
		return transitionLogProb(new StateSequence(si), sj);
	}
	
	@Override
	public double emissionLogProb(State state, Observation observation) {
		if(emissionMatrix.get(state).contain(observation))
			return emissionMatrix.get(state).getEmissionLogProb(observation);
					
		return emissionMatrix.get(state).getEmissionLogProb(UNKNOWN);
	}
	
	@Override
	public double emissionLogProb(int state, int observation) {
		State si = dict.getState(state);
		Observation ot = (observation != -1) ? dict.getObservation(observation) : UNKNOWN;
		
		return emissionLogProb(si, ot);
	}

	@Override
	public Observation[] getObservations() {
		Set<Observation> set = dict.getObservations();
		
		return set.toArray(new Observation[set.size()]);
	}

	@Override
	public State[] getStates() {
		Set<State> set = dict.getStates();
		
		return set.toArray(new State[set.size()]);
	}

	@Override
	public double getLogPi(State state) {
		if(pi.containsKey(state))
			return pi.get(state);
		
		return 0;
	}
	
	@Override
	public double getLogPi(int state) {		
		return getLogPi(dict.getState(state));
	}
	
	@Override
	public int getObservationIndex(Observation observation) {
		if(!dict.containObservation(observation))
			return dict.getIndex(UNKNOWN);
		
		return dict.getIndex(observation);
	}
	
	@Override
	public State getState(int state) {		
		return dict.getState(state);
	}
	
	@Override
	public int statesCount() {
		return dict.stateCount();
	}

	@Override
	public int observationsCount() {
		return dict.observationCount();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dict == null) ? 0 : dict.hashCode());
		result = prime * result + ((emissionMatrix == null) ? 0 : emissionMatrix.hashCode());
		result = prime * result + order;
		result = prime * result + ((pi == null) ? 0 : pi.hashCode());
		result = prime * result + ((transitionMatrix == null) ? 0 : transitionMatrix.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HMModelBasedMap other = (HMModelBasedMap) obj;
		if (dict == null) {
			if (other.dict != null)
				return false;
		} else if (!dict.equals(other.dict))
			return false;
		if (emissionMatrix == null) {
			if (other.emissionMatrix != null)
				return false;
		} else if (!emissionMatrix.equals(other.emissionMatrix))
			return false;
		if (order != other.order)
			return false;
		if (pi == null) {
			if (other.pi != null)
				return false;
		} else if (!pi.equals(other.pi))
			return false;
		if (transitionMatrix == null) {
			if (other.transitionMatrix != null)
				return false;
		} else if (!transitionMatrix.equals(other.transitionMatrix))
			return false;
		return true;
	}
}
