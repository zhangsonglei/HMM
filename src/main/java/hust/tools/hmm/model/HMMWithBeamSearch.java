package hust.tools.hmm.model;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import hust.tools.hmm.utils.CommonUtils;
import hust.tools.hmm.utils.ObservationSequence;
import hust.tools.hmm.utils.State;
import hust.tools.hmm.utils.StateSequence;

/**
 *<ul>
 *<li>Description: 基于beamSearch和前向算法的的HMM，用于1阶HMM
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月15日
 *</ul>
 */
public class HMMWithBeamSearch implements HMM {
		
	/**
	 * HMM模型
	 */
	private HMModel model;
	
	private int order;
	
	private int beamSize;
	
	public HMMWithBeamSearch(HMModel model, int beamSize) {
		this.model = model;
		this.order = model.getOrder();
		this.beamSize = beamSize;
	}

	@Override
	public double getProb(ObservationSequence observations, StateSequence states) {
		if(observations.length() == 0 || states.length() == 0)
			throw new IllegalArgumentException("状态序列或观测序列不能为空");
		
		double logProb = model.getLogPi(states.get(0)) + model.emissionLogProb(states.get(0), observations.get(0));
		List<StateSequence> list = CommonUtils.spilt(states, order);
		for(int i = 1; i < list.size(); i++) {
			StateSequence transition = list.get(i);
			State target = states.get(i);
			logProb += model.transitionLogProb(transition.remove(transition.length() - 1), target) +
					model.emissionLogProb(target, observations.get(i));
		}
		
		return Math.pow(10, logProb);
	}

	@Override
	public double getProb(ObservationSequence observations) {
		ForwardAlgorithm algorithm = new ForwardAlgorithm(model, observations);
		
		return algorithm.getProb();
	}
	
	@Override
	public StateSequence bestStateSeqence(ObservationSequence observationSequence) {
		List<StateSequence> bestK = beamSearch(observationSequence, beamSize, order, 1);
		
		return bestK.get(0);
	}

	/**
	 * viterbi算法计算给定观测序列的最优隐藏序列
	 * @param observationSequence	给定的观测序列
	 */
	private List<StateSequence> beamSearch(ObservationSequence observationSequence, int beamSize, int order, int k) {
		Queue<StateSequenceWithScore> prev = new PriorityQueue<>(beamSize);
	    Queue<StateSequenceWithScore> next = new PriorityQueue<>(beamSize);
	    Queue<StateSequenceWithScore> tmp;
	    
	    StateSequence states = new StateSequence();
	    ObservationSequence observations = new ObservationSequence(observationSequence.get(0));
	    for(int i = 0; i < model.statesCount(); i++) {
	    	State candState = model.getState(i);
	    	states = states.addLast(candState);
	    	double score = getProb(observations, states);
	    	prev.add(new StateSequenceWithScore(states, score));
	    }
	    
	    for(int t = 1; t < observationSequence.length(); t++) {
	    	observations = observations.addLast(observationSequence.get(t));
	    	
	    	int sz = Math.min(beamSize, prev.size());
	    	for(int sc = 0; prev.size() > 0 && sc < sz; sc++) {
		    	StateSequenceWithScore top = prev.remove();
		    	
		    	for(int i = 0; i < model.statesCount(); i++) {
		    		State candState = model.getState(i);
		    		StateSequence newStateSequence = top.getStateSequence().addLast(candState);
		    		double score = getProb(observations, newStateSequence);
		    		
		    		next.add(new StateSequenceWithScore(newStateSequence, score));
		    	}
		    }
	    	
	    	 prev.clear();
	    	 tmp = prev;
	    	 prev = next;
	    	 next = tmp;
	    }
	    	    
	    //选取得分最高的bestSize个候选句子
	    ArrayList<StateSequence> result = new ArrayList<>();
	    int num = Math.min(k, prev.size());

	    for(int i = 0; i < num; i++) 
	    	result.add(prev.remove().getStateSequence());
	    
	    return result;
	}
}
