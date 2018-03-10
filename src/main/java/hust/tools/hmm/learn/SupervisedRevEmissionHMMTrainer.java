package hust.tools.hmm.learn;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import hust.tools.hmm.model.EmissionProbEntry;
import hust.tools.hmm.model.HMModel;
import hust.tools.hmm.model.HMModelBasedMap;
import hust.tools.hmm.model.TransitionProbEntry;
import hust.tools.hmm.stream.SupervisedHMMSample;
import hust.tools.hmm.stream.SupervisedHMMSampleStream;
import hust.tools.hmm.utils.CommonUtils;
import hust.tools.hmm.utils.Observation;
import hust.tools.hmm.utils.State;
import hust.tools.hmm.utils.StateSequence;

/**
 *<ul>
 *<li>Description: 基于加法平滑的监督学习模型训练类器
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月10日
 *</ul>
 */
public class SupervisedRevEmissionHMMTrainer extends AbstractSupervisedHMMTrainer {
	
	public SupervisedRevEmissionHMMTrainer(SupervisedHMMSampleStream<?> sampleStream, int order) throws IOException {
		super(sampleStream, order);
	}
	
	public SupervisedRevEmissionHMMTrainer(List<SupervisedHMMSample> samples, int order) throws IOException {
		super(samples, order);
	}
	
	@Override
	public HMModel train() {
		calcPi(counter);
		calcTransitionMatrix(counter);
		calcEmissionMatrix(counter);
		
		HMModel model = new HMModelBasedMap(order, counter.getDictionary(), pi, transitionMatrix, emissionMatrix);
		
		return model;
	}
	
	/**
	 * 计算初始概率矩阵（已确保概率之和为1，不需要归一化）
	 * @param counter	转移发射计数器
	 */	
	@Override
	protected void calcPi(TransitionAndEmissionCounter counter) {
		int N = dict.stateCount();
		int M = counter.getTotalStartStatesCount();
		
		Set<State> set = counter.getDictionary().getStates();
		for(State state : set) {
			int count = counter.getStartStateCount(state);
			double prob = (count + DEFAULT_DELTA) / (M + N * DEFAULT_DELTA);
			pi.put(state, Math.log10(prob));
		}
	}
	
	@Override
	protected void calcTransitionMatrix(TransitionAndEmissionCounter counter) {
		Set<State> statesSet = counter.getDictionary().getStates();
		int N = statesSet.size();
		for(State state : statesSet) {//遍历所有隐藏状态，增加所有可能的一阶转移
			StateSequence start = new StateSequence(state);
			int n_Count = counter.getTransitionStartCount(start);
			TransitionProbEntry entry = new TransitionProbEntry();
			for(State target : statesSet) {
				int count = counter.getTransitionCount(start, target);
				double prob = (DEFAULT_DELTA + count) / (n_Count + N * DEFAULT_DELTA);
				entry.put(target, Math.log10(prob));
			}
			
			transitionMatrix.put(start, entry);
		}
		
		for(int i = 1; i < order; i++) {//遍历增加所有2-order阶的转移概率
			StateSequence[] sequences = transitionMatrix.keySet().toArray(new StateSequence[transitionMatrix.size()]);
			for(StateSequence sequence : sequences) {
				if(sequence.length() == i) {
					for(State state : statesSet) {
						StateSequence start = sequence.addLast(state);
						int n_Count = counter.getTransitionStartCount(start);
						
						TransitionProbEntry entry = new TransitionProbEntry();
						for(State target : statesSet) {
							int count = counter.getTransitionCount(start, target);
							double prob = (DEFAULT_DELTA + count) / (n_Count + N * DEFAULT_DELTA);
							entry.put(target, Math.log10(prob));
						}
						
						transitionMatrix.put(start, entry);
					}
				}
			}
		}
	}
	
	/**
	 * 采用加1平滑方式计算发射概率矩阵:p=(C+1)/(M+N)（已确保概率之和为1，不需要归一化）
	 * @param counter	转移发射计数器
	 */	
	protected void calcEmissionMatrix(TransitionAndEmissionCounter counter) {
		Iterator<Observation> iterator = counter.revEmissionIterator();
		
		while(iterator.hasNext()) {//遍历所有发射
			Observation observation = iterator.next();
			Iterator<State> statesIterator = counter.iterator(observation);
			
			int M = counter.getObservationCount(observation);//以state为发射起点的总数量
			while(statesIterator.hasNext()) {//计算当前状态的所有发射概率
				State state = statesIterator.next();
				int C = counter.getRevEmissionCount(observation, state);//当前发射的数量
				double prob = 1.0 * C / M;
				putEmissionProb(state, observation, prob);
			}
		}//end while
		
		Set<State> set = counter.getDictionary().getStates();
		for(State state : set) {
			double prob = 10.0 / counter.getTotalStatesCount();
			putEmissionProb(state, CommonUtils.UNKNOWN, prob);
		}
	}
	
	/**
	 * 
	 * @param state
	 * @param observation
	 * @param prob
	 */
	private void putEmissionProb(State state, Observation observation, double prob) {
		EmissionProbEntry entry = null;
		if(emissionMatrix.containsKey(state)) 
			entry = emissionMatrix.get(state);
		else 
			entry = new EmissionProbEntry();
		
		entry.put(observation, Math.log10(prob));
		emissionMatrix.put(state, entry);
	}
}
