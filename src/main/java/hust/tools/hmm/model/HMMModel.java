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
	
	private double[] pi;
	
	private HashMap<Emission, ProbBowEntry>  transitionMatrix;
	
	private HashMap<Transition, ProbBowEntry>  emissionMatrix;
	
	public HMMModel(Dictionary dict, double[] pi, HashMap<Emission, ProbBowEntry>  transitionMatrix, HashMap<Transition, ProbBowEntry>  emissionMatrix) {
		this.dict = dict;
		this.pi = pi;
		this.transitionMatrix = transitionMatrix;
		this.emissionMatrix = emissionMatrix;
	}
	
	@Override
	public StateSequence bestStateSeqence(ObservationSequence observations) {
		
		return null;
	}
	
	@Override
	public StateSequence[] bestKStateSeqence(ObservationSequence observations, int k) {
		return null;
	}

	@Override
	public double getProb(ObservationSequence observations) {
		return 0;
	}

	@Override
	public double transitionProb(State si, State sj) {
		Transition transition = new Transition(si, sj);
		
		return transitionMatrix.get(transition).getProb();
	}
	
	@Override
	public double transitionProb(State[] si, State sj) {
		Transition transition = new Transition(si, sj);
		
		return transitionMatrix.get(transition).getProb();
	}
	
	public double transitionProb(int i, int j) {	
		State si = dict.getState(i);
		State sj = dict.getState(j);
		
		return transitionProb(si, sj);
	}

	@Override
	public double emissionProb(State state, Observation observation) {
		Emission emission = new Emission(state, observation);
		
		return emissionMatrix.get(emission).getProb();
	}
	
	@Override
	public double emissionProb(State[] state, Observation observation) {
		Emission emission = new Emission(state, observation);
		
		return emissionMatrix.get(emission).getProb();
	}
	
	public double emissionProb(int i, int t) {		
		State si = dict.getState(i);
		Observation ot = dict.getObservation(t);
		
		return emissionProb(si, ot);
	}

	@Override
	public Observation[] getObservationStates() {
		Observation[] observations = new Observation[dict.observationCount()];
		
		int i = 0;
		Iterator<Observation> iterator = dict.getObservations();
		while(iterator.hasNext()) {
			observations[i++] = iterator.next();
		}
		
		return observations;
	}

	@Override
	public State[] getStates() {
		State[] states = new State[dict.stateCount()];
		
		int i = 0;
		Iterator<State> iterator = dict.getStates();
		while(iterator.hasNext()) {
			states[i++] = iterator.next();
		}
		
		return states;
	}

	@Override
	public double getProb(ObservationSequence observations, StateSequence states) {
		return 0;
	}

	@Override
	public double getPi(State state) {		
		return pi[state.getIndex()];
	}
	
	public double getPi(int i) {		
		return pi[i];
	}
	
	public Observation getObservation(int i) {
		return dict.getObservation(i);
	}
	
	public int getObservation(Observation observation) {
		return observation.getIndex();
	}
	
	public State getState(int i) {
		return dict.getState(i);
	}
	
	public int getState(State state) {
		return state.getIndex();
	}
	
	public int getStateCount() {
		return dict.stateCount();
	}
	
	public int getObservationCount() {
		return dict.observationCount();
	}
}
