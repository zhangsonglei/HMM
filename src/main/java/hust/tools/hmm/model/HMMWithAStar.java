package hust.tools.hmm.model;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import hust.tools.hmm.utils.CommonUtils;
import hust.tools.hmm.utils.DefaultStateSequenceValidator;
import hust.tools.hmm.utils.ObservationSequence;
import hust.tools.hmm.utils.State;
import hust.tools.hmm.utils.StateSequence;
import hust.tools.hmm.utils.StateSequenceValidator;

/**
 *<ul>
 *<li>Description: 基于A*算法和前向算法的的HMM
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月15日
 *</ul>
 */
public class HMMWithAStar implements HMM {
		
	/**
	 * HMM模型
	 */
	private HMModel model;
	
	private int order;
	
	/**
	 * 序列合法性验证
	 */
	private StateSequenceValidator validator;
		
	public HMMWithAStar(HMModel model) {
		this(model, new DefaultStateSequenceValidator());
	}
	
	public HMMWithAStar(HMModel model, StateSequenceValidator validator) {
		this.model = model;
		this.validator = validator;
		order = model.getOrder();
	}

	@Override
	public double getLogProb(ObservationSequence observations, StateSequence states) {
		if(observations.length() == 0 || states.length() == 0)
			throw new IllegalArgumentException("状态序列或观测序列不能为空。");
		else if(observations.length() != states.length())
			throw new IllegalArgumentException("状态序列或观测序列长度不同。");
		
		//初始转移概率
		double logProb = model.getLogPi(states.get(0)) + model.emissionLogProb(states.get(0), observations.get(0));

		/**
		 * 计算高阶起始部分概率
		 * <sos>a->b,<sos>ab->c,<sos>abc->d,...
		 */
		StateSequence startSeq = new StateSequence(CommonUtils.SOS);
		for(int i = 1; i < order && i < observations.length(); i++) {
			State target = states.get(i);
			startSeq = startSeq.addLast(states.get(i - 1));
			logProb += model.transitionLogProb(startSeq, states.get(i)) + model.emissionLogProb(target, observations.get(i));
		}
		
		if(states.length() > order) {
			List<StateSequence> list = CommonUtils.generate(states, order + 1);
			for(int i = 0; i < list.size(); i++) {
				StateSequence transition = list.get(i);
				State target = transition.get(order);
				logProb += model.transitionLogProb(transition.remove(order), target) +
						model.emissionLogProb(target, observations.get(i + order));
			}
		}
		
		return logProb;
	}

	@Override
	public double getLogProb(ObservationSequence observations) {
//		ForwardAlgorithm algorithm = new ForwardAlgorithm(model, observations);
		BackwardAlgorithm algorithm = new BackwardAlgorithm(model, observations);
		
		return algorithm.getProb();
	}
	
	@Override
	public StateSequence bestStateSeqence(ObservationSequence observationSequence) {
		List<StateSequence> bestK = bestKStateSeqences(observationSequence, 5);
		
		return bestK.get(0);
	}
	
	/**
	 * A*算法计算给定观测序列的最优的k个隐藏序列
	 * @param observationSequence	给定的观测序列
	 */
	public List<StateSequence> bestKStateSeqences(ObservationSequence observationSequence, int k) {
		Queue<StateSequenceWithScore> prev = new PriorityQueue<>(k);
	    Queue<StateSequenceWithScore> next = new PriorityQueue<>(k);
	    Queue<StateSequenceWithScore> tmp;
	    
	    ObservationSequence observations = new ObservationSequence(observationSequence.get(0));
	    for(int i = 0; i < model.statesCount(); i++) {
	    	StateSequence states = new StateSequence(model.getState(i));
	    	double score = getLogProb(observations, states);
	    	prev.add(new StateSequenceWithScore(states, score));
	    }
	    
	    for(int t = 1; t < observationSequence.length(); t++) {
	    	observations = observations.addLast(observationSequence.get(t));
	    	
	    	int sz = Math.min(k, prev.size());
	    	for(int sc = 0; prev.size() > 0 && sc < sz; sc++) {
	    		StateSequenceWithScore top = prev.remove();
	    		StateSequence sequence = top.getStateSequence();
		    	
	    		for(int i = 0; i < model.statesCount(); i++) {
	    			State candState = model.getState(i);
	    			if(validator.validStateSequence(t, observationSequence, sequence, candState)) {
	    				StateSequence newStateSequence = sequence.addLast(candState);
	    				double score = getLogProb(observations, newStateSequence);
			    		
	    				next.add(new StateSequenceWithScore(newStateSequence, score));
	    			}
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
