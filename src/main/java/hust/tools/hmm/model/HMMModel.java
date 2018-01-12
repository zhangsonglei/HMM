package hust.tools.hmm.model;

import hust.tools.hmm.utils.StateSequence;

import java.util.HashMap;
import java.util.Iterator;
import hust.tools.hmm.utils.Dictionary;
import hust.tools.hmm.utils.Observation;
import hust.tools.hmm.utils.ObservationSequence;
import hust.tools.hmm.utils.State;

/**
 *<ul>
 *<li>Description: 隐式马尔科夫模型 
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2017年12月26日
 *</ul>
 */
public class HMMModel implements HMM {
	
	private Dictionary dict;
	
	private HashMap<State, ARPAEntry> pi;
	
	private HashMap<StateSequence, TransitionProbEntry>  transitionMatrix;
	
	private HashMap<State, EmissionProbEntry>  emissionMatrix;
	
	public HMMModel(Dictionary dict, HashMap<State, ARPAEntry> pi, HashMap<StateSequence, TransitionProbEntry>  transitionMatrix, HashMap<State, EmissionProbEntry>  emissionMatrix) {
		this.dict = dict;
		this.pi = pi;
		this.transitionMatrix = transitionMatrix;
		this.emissionMatrix = emissionMatrix;
	}
	
	@Override
	public StateSequence bestStateSeqence(ObservationSequence observations, int order) {
		
		return null;
	}
	
	@Override
	public StateSequence[] bestKStateSeqence(ObservationSequence observations, int order, int k) {
		return null;
	}

	@Override
	public double getProb(ObservationSequence observations, int order) {
		return 0;
	}
	
	@Override
	public double transitionProb(StateSequence si, State sj) {
		return transitionMatrix.get(si).getTransitionLogBow(sj);
	}
	
	public double transitionProb(int[] si, int sj) {
		State[] start = new State[si.length];
		for(int i = 0; i < start.length; i++)
			start[i] = dict.getState(i);
		
		State target = dict.getState(sj);
		
		return transitionProb(new StateSequence(start), target);
	}

	@Override
	public double emissionProb(State state, Observation observation) {
		
		return emissionMatrix.get(state).getEmissionLogProb(observation);
	}
	
	public double emissionProb(int i, int t) {
		State si = dict.getState(i);
		Observation ot = dict.getObservation(t);
		
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
	public double getProb(ObservationSequence observations, StateSequence states, int order) {
		return 0;
	}

	@Override
	public double getPi(State state) {		
		return pi.get(state).getLog_prob();
	}
	
	public double getPi(int i) {
		return getPi(dict.getState(i));
	}
	
	public Observation getObservation(int i) {
		return dict.getObservation(i);
	}
	
	public int getObservationIndex(Observation observation) {
		return dict.getIndex(observation);
	}
	
	public State getState(int i) {
		return dict.getState(i);
	}
	
	public int getStateIndex(State state) {
		return dict.getIndex(state);
	}
	
	public int getStateCount() {
		return dict.stateCount();
	}
	
	public int getObservationCount() {
		return dict.observationCount();
	}
}
