package hust.tools.hmm.utils;

import java.util.HashMap;
import java.util.Iterator;

/**
 *<ul>
 *<li>Description: 观测/状态的索引类
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月4日
 *</ul>
 */
public class Dictionary {
	
	private static int observation_index;
	private HashMap<Observation, Integer> observationToIndex;
	private HashMap<Integer, Observation> indexToObservation;
	
	private static int state_index;
	private HashMap<State, Integer> stateToIndex;
	private HashMap<Integer, State> indexToState;
	
	public Dictionary() {
		observationToIndex = new HashMap<>();
		indexToObservation = new HashMap<>();
		stateToIndex = new HashMap<>();
		indexToState = new HashMap<>();
		state_index = 0;
		observation_index = 0;
	}
	
	public State add(State state) {
		if(!contain(state)) {
			state.setIndex(state_index);
			stateToIndex.put(state, state_index);
			indexToState.put(state_index, state);
			
			state_index++;
			return state;
		}else
			return state.setIndex(stateToIndex.get(state));
	}
	
	public StateSequence add(StateSequence sequence) {
		for(int i = 0; i < sequence.length(); i++) {
			State ns = add(sequence.get(i));
			sequence = sequence.set(ns, i);
		}
		
		return sequence;
	}
	
	public Observation add(Observation observation) {
		if(!contain(observation)) {
			observation.setIndex(observation_index);
			observationToIndex.put(observation, observation_index);
			indexToObservation.put(observation_index, observation);
			observation_index++;
			
			return observation;
		}else 
			return observation.setIndex(observationToIndex.get(observation));
	}
	
	public ObservationSequence add(ObservationSequence observations) {	
		for(int i = 0; i < observations.length(); i++) {
			Observation no = add(observations.get(i));
			observations = observations.set(no, i);
		}
		
		return observations;
	}
	
	public State getState(int index) {
		if(indexToState.containsKey(index))
			return indexToState.get(index);
		
		return null;
	}
	
	public Observation getObservation(int index) {
		if(indexToObservation.containsKey(index))
			return indexToObservation.get(index);
		
		return null;
	}
	
	public Iterator<State> statesIterator() {
		return stateToIndex.keySet().iterator();
	}
	
	public Iterator<Observation> observationsIterator() {
		return observationToIndex.keySet().iterator();
	}
	
	public boolean contain(State state) {
		return stateToIndex.containsKey(state);
	}
	
	public boolean contain(Observation observation) {
		return observationToIndex.containsKey(observation);
	}
	
	public int stateCount() {
		return stateToIndex.size();
	}
	
	public int observationCount() {
		return observationToIndex.size();
	}
}
