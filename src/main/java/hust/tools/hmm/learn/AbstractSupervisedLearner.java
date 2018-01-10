package hust.tools.hmm.learn;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import hust.tools.hmm.model.ARPAEntry;
import hust.tools.hmm.model.EmissionProbEntry;
import hust.tools.hmm.model.HMMModel;
import hust.tools.hmm.stream.SupervisedHMMSample;
import hust.tools.hmm.stream.SupervisedHMMSampleStream;
import hust.tools.hmm.utils.Dictionary;
import hust.tools.hmm.utils.Observation;
import hust.tools.hmm.utils.State;
import hust.tools.hmm.utils.StateSequence;

/**
 *<ul>
 *<li>Description: 模型训练类 
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2017年12月26日
 *</ul>
 */
public abstract class AbstractSupervisedLearner extends AbstractLearner {
	
	protected TransitionAndEmissionCounter counter;

	public AbstractSupervisedLearner(TransitionAndEmissionCounter counter) {
		super();
		this.counter = counter;
	}
	
	public AbstractSupervisedLearner(Dictionary dict, TransitionAndEmissionCounter counter) {
		super(dict);
		this.counter = counter;
	}
	
	public AbstractSupervisedLearner(SupervisedHMMSampleStream<?> sampleStream, int order, int cutoff) throws IOException {
		this.counter = new TransitionAndEmissionCounter(sampleStream, order, cutoff);
	}
	
	public AbstractSupervisedLearner(List<SupervisedHMMSample> samples,  int order, int cutoff) {
		this.counter = new TransitionAndEmissionCounter(samples, order, cutoff);
	}
	
	/**
	 * 根据转移发射计数器训练HMM模型
	 * @param counter	转移发射计数器
	 * @param modelFile	模型写出路径
	 * @return			HMM模型
	 */
	public HMMModel train(File modelFile) {
		calcPi(counter);
		calcTransitionMatrix(counter);
		calcEmissionMatrix(counter);
		
		HMMModel model = new HMMModel(dict, pi, transitionMatrix, emissionMatrix);
		
		if(modelFile != null)
			writeModel(model, modelFile);
		
		return model;
	}
	
	public HMMModel train() {		
		return train(null);
	}
	
	/**
	 * 计算初始概率矩阵
	 * @param counter	转移发射计数器
	 */	
	private void calcPi(TransitionAndEmissionCounter counter) {
		Iterator<State> iterator = dict.statesIterator();
		long count = 0;
		double prob = 0;
		double normalization = 0;	//归一化因子
		State state = null;
		while(iterator.hasNext()) {
			state = iterator.next();
			count = counter.getSequenceCount(new StateSequence(state));
			
			if(count != 0) {
				prob = count / counter.getTotalStatesCount();
				normalization += prob;
				pi.put(state, prob);
			}
		}
		
		//归一化
		iterator = pi.keySet().iterator();
		double logProb = 0;
		while (iterator.hasNext()) {
			state = iterator.next();
			logProb = Math.log10(pi.get(state) / normalization);
			pi.put(state, logProb);
		}
	}
	
	/**
	 * 计算转移概率矩阵
	 * @param counter	转移发射计数器
	 */	
	protected abstract void calcTransitionMatrix(TransitionAndEmissionCounter counter);
		
	/**
	 * 计算发射概率矩阵
	 * @param counter	转移发射计数器
	 */	
	private void calcEmissionMatrix(TransitionAndEmissionCounter counter) {
		Iterator<State> iterator = counter.emissionIterator();
				
		while(iterator.hasNext()) {//遍历所有发射状态
			State state = iterator.next();
			Iterator<Observation> observationsIterator = counter.iterator(state);
			
			EmissionProbEntry emissionProbEntry = new EmissionProbEntry();
			ARPAEntry entry = null;
			Observation observation = null;
			double prob = 0;
			while(observationsIterator.hasNext()) {//计算当前状态的所有发射概率
				observation = observationsIterator.next();
				prob = calcMLProbability(state, observation, counter);
				entry = new ARPAEntry(Math.log10(prob), 0);
				emissionProbEntry.put(observation, entry);
			}
			emissionMatrix.put(state, emissionProbEntry);
		}//end while
	}
	
	/**
	 * 返回给定n元的回退权重
	 * @param nGram 待求回退权重的n元
	 * @return		给定n元的回退权重
	 */
    protected double calcBOW(StateSequence stateSequence) {
    	//例子：求w1 w2的回退权重
    	double sum_N = 0.0;		//所有出现的以w1 w2为前缀的trigram (w1 w2 *) 的概率之和
    	double sum_N_1 = 0.0;	//所有出现的以w1 w2为前缀的trigram (w1 w2 *) 的低阶：bigram (w2 *)的概率之和
		
    	Set<State> suffixs = counter.get(stateSequence).getStates();
    	if(suffixs != null) {
			for(State state : suffixs) {
				if(transitionMatrix.containsKey(stateSequence))
					sum_N += Math.pow(10, transitionMatrix.get(stateSequence).getTransitionLogProb(state));
				
				if(stateSequence.length() == 1)
					sum_N_1 += Math.pow(10, pi.get(stateSequence.get(0)));
				else {
					StateSequence states = stateSequence.remove(0);
					if(transitionMatrix.containsKey(states))
						sum_N_1 += Math.pow(10, transitionMatrix.get(states).getTransitionLogProb(state));
				}
			}
			
			return (1 - sum_N) / (1 - sum_N_1);
		}else
			return 1.0;
    }
  
    /**
     * 计算给定转移的最大似然概率
     * @param start		转移的起点
     * @param target	转移的终点
     * @param counter	计数器
     * @return			最大似然概率
     */
	protected double calcMLProbability(StateSequence start, State target, TransitionAndEmissionCounter counter) {
		long totalCount = counter.getSequenceCount(start);
		
		if(0 == totalCount || 0 == start.length() || start == null)
			return 0;
		else
			return 1.0 * counter.getTransitionCount(start, target) / totalCount;
	}
	
	/**
	 * 计算给定发射的最大似然概率
	 * @param state			发射的状态
	 * @param observation	发射的目标观测状态
	 * @param counter		计数器
	 * @return				最大似然概率
	 */
	protected double calcMLProbability(State state, Observation observation, TransitionAndEmissionCounter counter) {
		long totalCount = counter.getStateCount(state);
		
		if(0 == totalCount || state == null)
			return 0;
		else
			return 1.0 * counter.getEmissionCount(state, observation) / totalCount;
	}
}
