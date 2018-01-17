package hust.tools.hmm.model;

import hust.tools.hmm.utils.StateSequence;
import hust.tools.hmm.utils.StringObservation;

import java.util.HashMap;
import java.util.Iterator;
import hust.tools.hmm.utils.Dictionary;
import hust.tools.hmm.utils.Observation;
import hust.tools.hmm.utils.State;

/**
 *<ul>
 *<li>Description: 基于回退的隐式马尔科夫模型
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2017年12月26日
 *</ul>
 */
public class HMModelBasedBO implements HMModel {
	
	private final Observation UNKNOWN = new StringObservation("UNKNOWN");
	
	private int order;
	
	private Dictionary dict;
	
	private HashMap<State, Double> pi;
	
	private HashMap<StateSequence, ARPAEntry>  transitionMatrix;
	
	private HashMap<State, EmissionProbEntry>  emissionMatrix;
	
	public HMModelBasedBO(int order, Dictionary dict, HashMap<State, Double> pi, HashMap<StateSequence, ARPAEntry>  transitionMatrix, HashMap<State, EmissionProbEntry>  emissionMatrix) {
		this.order = order;
		this.dict = dict;
		this.pi = pi;
		this.transitionMatrix = transitionMatrix;
		this.emissionMatrix = emissionMatrix;
	}
	
	public int getOrder() {
		return order;
	}
	
	public Dictionary getDict() {
		return dict;
	} 
	
	public HashMap<State, Double> getPi() {
		return pi;
	}

	public HashMap<StateSequence, ARPAEntry> getTransitionMatrix() {
		return transitionMatrix;
	}

	public HashMap<State, EmissionProbEntry> getEmissionMatrix() {
		return emissionMatrix;
	}
	
	@Override
	public double transitionProb(StateSequence start, State target) {
		StateSequence sequence = start.add(target);
		
		return transitionProb(sequence);
	}
	
	private double transitionProb(StateSequence sequence) {		
		if(contain(sequence))
			return transitionMatrix.get(sequence).getLog_prob();
		
		return oovTransitionProb(sequence);
	}
	
	/**
	 * 回退计算不存在的转移概率
	 * @param oov	不存在的转移
	 * @return		不存在的转移概率
	 */
	private double oovTransitionProb(StateSequence oov) {
		StateSequence n_States = oov.remove(oov.length() - 1);
		StateSequence _States = oov.remove(0);
		if(contain(n_States))
			return transitionMatrix.get(n_States).getLog_bo() + transitionProb(_States);
		else
			return transitionProb(_States);
	}
	
	public double transitionProb(int[] start, int target) {
		for(int index : start)
			if(!dict.containState(index))
				throw new IllegalArgumentException("不存在的状态索引：" + index);
		
		if(!dict.containState(target))
			throw new IllegalArgumentException("不存在的状态索引：" + target);

		State[] states = new State[start.length];
		for(int i = 0; i < start.length; i++)
			states[i] = dict.getState(start[i]);
		
		State state = dict.getState(target);
				
		return transitionProb(new StateSequence(states), state);
	}

	@Override
	public double transitionProb(int i, int j) {
		return transitionProb(new int[]{i}, j);
	}
	
	@Override
	public double emissionProb(State state, Observation observation) {
		if(!dict.containState(state))
			throw new IllegalArgumentException("不存在的隐藏状态:" + state);
		
		if(contain(state, observation))
			return emissionMatrix.get(state).getEmissionLogProb(observation);
		
		return emissionMatrix.get(state).getEmissionLogProb(UNKNOWN);
	}
	
	public double emissionProb(int state, int observation) {
		if(!dict.containState(state))
			throw new IllegalArgumentException("不存在的状态索引：" + state);
		
		State si = dict.getState(state);
		Observation ot = null;
		if(observation != -1)
			ot = dict.getObservation(observation);
		else
			ot = UNKNOWN;
		
		return emissionProb(si, ot);
	}

	@Override
	public Observation[] getObservations() {
		Iterator<Observation> iterator = dict.observationsIterator();
		Observation[] observations = new Observation[dict.observationCount()];
		int i = 0;
		while(iterator.hasNext()) {
			observations[i++] = iterator.next();
		}
		
		return observations;
	}

	@Override
	public State[] getStates() {
		Iterator<State> iterator = dict.statesIterator();
		State[] states = new State[dict.stateCount()];
		int i = 0;
		while(iterator.hasNext()) {
			states[i++] = iterator.next();
		}
		
		return states;
	}

	@Override
	public double getPi(State state) {
		if(pi.containsKey(state))
			return pi.get(state);
		
		return 0;
	}
	
	public double getPi(int state) {
		if(!dict.containState(state))
			throw new IllegalArgumentException("不存在的状态索引：" + state);
		
		return getPi(dict.getState(state));
	}
	
	public Observation getObservation(int observation) {
		if(!dict.containState(observation))
			throw new IllegalArgumentException("不存在的观测状态索引：" + observation);
		
		return dict.getObservation(observation);
	}
	
	public int getObservationIndex(Observation observation) {
		if(!dict.containObservation(observation))
			return dict.getIndex(UNKNOWN);
		
		return dict.getIndex(observation);
	}
	
	public State getState(int state) {
		if(!dict.containState(state))
			throw new IllegalArgumentException("不存在的状态索引：" + state);
		
		return dict.getState(state);
	}
	
	public int getStateIndex(State state) {
		if(!dict.containState(state))
			throw new IllegalArgumentException("不存在的状态：" + state);
		
		return dict.getIndex(state);
	}
	
	public int statesCount() {
		return dict.stateCount();
	}
	
	public int observationsCount() {
		return dict.observationCount();
	}
	
	/**
	 * 判断是否包含给定转移
	 * @param start		转移的的起点
	 * @param target	转移的的目标状态
	 * @return			true-包含/false-不包含
	 */
	private boolean contain(StateSequence sequence) {
		if(transitionMatrix.containsKey(sequence))
			return true;
		
		return false;
	}
	
	/**
	 * 判断是否包含给定发射
	 * @param state			发射的隐藏状态
	 * @param observation	发射的观测状态
	 * @return				true-包含/false-不包含
	 */
	private boolean contain(State state, Observation observation) {
		if(emissionMatrix.containsKey(state))
			if(emissionMatrix.get(state).contain(observation))
				return true;
		
		return false;
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
		HMModelBasedBO other = (HMModelBasedBO) obj;
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
