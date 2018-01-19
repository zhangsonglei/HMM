package hust.tools.hmm.learn;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import hust.tools.hmm.model.ARPAEntry;
import hust.tools.hmm.model.EmissionProbEntry;
import hust.tools.hmm.model.HMModel;
import hust.tools.hmm.model.HMModelBasedBO;
import hust.tools.hmm.stream.SupervisedHMMSample;
import hust.tools.hmm.stream.SupervisedHMMSampleStream;
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

	/**
	 * 对初始转移向量和发射概率进行加delta平滑
	 */
	private final double delta = 0.01;
	
	public SupervisedRevEmissionHMMTrainer(TransitionAndEmissionCounter counter) {
		super(counter);
	}
	
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
		
		HMModel model = new HMModelBasedBO(order, counter.getDictionary(), pi, transitionMatrix, emissionMatrix);
		
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
			double prob = (count + delta) / (M + N * delta);
			pi.put(state, Math.log10(prob));
		}
	}
	
	@Override
	protected void calcTransitionMatrix(TransitionAndEmissionCounter counter) {
		Iterator<StateSequence> iterator = counter.transitionIterator();
		while(iterator.hasNext()) {//遍历所有转移状态,计算转移概率
			StateSequence sequence = iterator.next();
			
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
			
			double prob = (nCount + delta)/ (n_Count + delta * counter.getDictionary().stateCount());
			transitionMatrix.put(sequence, new ARPAEntry(Math.log10(prob), 0));
		}

		Set<StateSequence> keySet = transitionMatrix.keySet();
		for(StateSequence sequence : keySet) {//遍历计算回退权重
			if(sequence.length() != order + 1) {//最高阶无回退权重
				ARPAEntry entry = transitionMatrix.get(sequence);
				double bow = calcBOW(sequence);
				if(!(Double.isNaN(bow) || Double.isInfinite(bow))) {
					entry = entry.setLog_bo(Math.log10(bow));
					transitionMatrix.put(sequence, entry);
				}
			}//end if
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
			putEmissionProb(state, UNKNOWN, prob);
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
