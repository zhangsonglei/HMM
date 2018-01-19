package hust.tools.hmm.learn;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import hust.tools.hmm.model.ARPAEntry;
import hust.tools.hmm.model.EmissionProbEntry;
import hust.tools.hmm.model.HMModel;
import hust.tools.hmm.stream.SupervisedHMMSample;
import hust.tools.hmm.stream.SupervisedHMMSampleStream;
import hust.tools.hmm.utils.Dictionary;
import hust.tools.hmm.utils.Observation;
import hust.tools.hmm.utils.State;
import hust.tools.hmm.utils.StateSequence;
import hust.tools.hmm.utils.StringObservation;

/**
 *<ul>
 *<li>Description: 基于监督学习的抽象模型训练类 
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2017年12月26日
 *</ul>
 */
public abstract class AbstractSupervisedHMMTrainer implements HMMTrainer {
	
	protected final Observation UNKNOWN = new StringObservation("UNKNOWN");
	
	protected int order;
	
	private final int DEFAULT_ORDER = 1;
	
	protected Dictionary dict;
	
	protected TransitionAndEmissionCounter counter;
	
	protected HashMap<State, Double> pi;
	
	protected HashMap<StateSequence, ARPAEntry> transitionMatrix;
	
	protected HashMap<State, EmissionProbEntry> emissionMatrix;

	public AbstractSupervisedHMMTrainer(TransitionAndEmissionCounter counter) {
		this.counter = counter;
		order = counter.getOrder();
		dict = counter.getDictionary();
		pi = new HashMap<>();
		transitionMatrix = new HashMap<>();
		emissionMatrix = new HashMap<>();
	}
	
	public AbstractSupervisedHMMTrainer(SupervisedHMMSampleStream<?> sampleStream, int order) throws IOException {
		counter = new TransitionAndEmissionCounter(sampleStream, order);
		this.order = order > 0 ? order : DEFAULT_ORDER;
		dict = counter.getDictionary();
		pi = new HashMap<>();
		transitionMatrix = new HashMap<>();
		emissionMatrix = new HashMap<>();
	}
	
	public AbstractSupervisedHMMTrainer(List<SupervisedHMMSample> samples,  int order) {
		counter = new TransitionAndEmissionCounter(samples, order);
		this.order = order > 0 ? order : DEFAULT_ORDER;
		dict = counter.getDictionary();
		pi = new HashMap<>();
		transitionMatrix = new HashMap<>();
		emissionMatrix = new HashMap<>();
	}
	
	@Override
	public abstract HMModel train();
	
	/**
	 * 计算初始概率矩阵
	 * @param counter	转移发射计数器
	 */	
	protected abstract void calcPi(TransitionAndEmissionCounter counter);
	
	/**
	 * 计算转移概率矩阵
	 * @param counter	转移发射计数器
	 */	
	protected abstract void calcTransitionMatrix(TransitionAndEmissionCounter counter);
		
	/**
	 * 计算发射概率矩阵
	 * @param counter	转移发射计数器
	 */	
	protected abstract void calcEmissionMatrix(TransitionAndEmissionCounter counter);
	
	/**
	 * 返回给定转移的回退权重：0-（len-2）为转移的起点，最后一个状态为转移的终点
	 * @param stateSequence 待求回退权重的转移
	 * @return				给定转移的回退权重
	 */
    protected double calcBOW(StateSequence stateSequence) {
    	//例子：求s1s2的回退权重
    	
    	Set<State> suffixs = counter.getSuffixs(stateSequence);
		if(suffixs != null) {
			double sum_N = 0.0;		//所有出现的以s1s2为起点 (s1s2->*)的发射概率之和
	    	double sum_N_1 = 0.0;	//所有出现的以s1s2为起点(s1s2->*) 的低阶发射：(s2->*)的概率之和
			for(State state : suffixs) {
				StateSequence states = stateSequence.add(state);
				if(transitionMatrix.containsKey(states))
					sum_N += Math.pow(10, transitionMatrix.get(states).getLog_prob());
				states = states.remove(0);
				if(transitionMatrix.containsKey(states))
					sum_N_1 += Math.pow(10, transitionMatrix.get(states).getLog_prob());
			}
			
			return (1.0 - sum_N) / (1.0 - sum_N_1);
		}
    	
    	return 1.0;
    }
  
    /**
     * 计算给定转移的最大似然概率
     * @param start		转移的起点
     * @param target	转移的终点
     * @param counter	计数器
     * @return			最大似然概率
     */
	protected double calcTransitionMLProbability(StateSequence sequence, TransitionAndEmissionCounter counter) {
		if(sequence.length() == 0)
			throw new IllegalArgumentException("隐藏状态序列不能为空");
		
		int nCount = counter.getSequenceCount(sequence);
		int n_Count = 0;
		if(sequence.length() == 1)
			n_Count = counter.getTotalStatesCount();
		else {//保证归一化
			StateSequence n_States = sequence.remove(sequence.length() - 1);
			Set<State> suffixs = counter.getSuffixs(n_States);
			for(State suffix : suffixs)
				n_Count += counter.getSequenceCount(n_States.add(suffix));
		}
		
		return (0 == n_Count || 0 == sequence.length()) ? 0 : 1.0 * nCount / n_Count;
	}
}
