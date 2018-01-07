package hust.tools.hmm.utils;

import java.util.HashMap;
import java.util.HashSet;
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
	private HashSet<Observation> observationToIndex;
	private HashMap<Integer, Observation> indexToObservation;
	
	private static int state_index;
	private HashSet<State> stateToIndex;
	private HashMap<Integer, State> indexToState;
	
	public Dictionary() {
		observationToIndex = new HashSet<>();
		indexToObservation = new HashMap<>();
		stateToIndex = new HashSet<>();
		indexToState = new HashMap<>();
		state_index = 0;
		observation_index = 0;
	}
	
	public State add(State state) {
		if(!stateToIndex.contains(state)) {
			state.setIndex(state_index);
			stateToIndex.add(state);
			indexToState.put(state_index, state);
			
			state_index++;
			return state;
		}else 
			return indexToState.get(state_index);
	}
	
	public StateSequence add(StateSequence states) {
		for(int i = 0; i < states.size(); i++) {
			State ns = add(states.get(i));
			states.update(ns, i);
		}
		
		return states;
	}
	
	public Observation add(Observation observation) {
		if(!observationToIndex.contains(observation)) {
			observation.setIndex(observation_index);
			observationToIndex.add(observation);
			indexToObservation.put(observation_index, observation);
			
			observation_index++;
			return observation;
		}else 
			return indexToObservation.get(observation_index);
	}
	
	public ObservationSequence add(ObservationSequence observations) {	
		for(int i = 0; i < observations.size(); i++) {
			Observation no = add(observations.get(i));
			observations.update(no, i);
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
	
	public Iterator<State> getStates() {
		return stateToIndex.iterator();
	}
	
	public Iterator<Observation> getObservations() {
		return observationToIndex.iterator();
	}
	
	public boolean contain(State state) {
		return stateToIndex.contains(state);
	}
	
	public boolean contain(Observation observation) {
		return observationToIndex.contains(observation);
	}
	
	public int stateCount() {
		return stateToIndex.size();
	}
	
	public int observationCount() {
		return observationToIndex.size();
	}
}
