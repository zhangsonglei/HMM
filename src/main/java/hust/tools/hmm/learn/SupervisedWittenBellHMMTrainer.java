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
 *<li>Description: 基于Witten-Bell平滑的监督学习模型训练类器
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月10日
 *</ul>
 */
public class SupervisedWittenBellHMMTrainer extends AbstractSupervisedHMMTrainer {

	public SupervisedWittenBellHMMTrainer(SupervisedHMMSampleStream<?> sampleStream, int order) throws IOException {
		super(sampleStream, order);
	}
	
	public SupervisedWittenBellHMMTrainer(List<SupervisedHMMSample> samples, int order) throws IOException {
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
	 * 计算初始概率矩阵（已确保概率之和为1，不需要归一化）为处理没有在句首出现过的隐藏状态，为每个状态加上0.01平滑
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
	
	/**
	 * 采用Witten-Bell回退平滑, X高阶Pwb(X)依赖于X的低阶x的Pwb(x)
	 * Pwb(Si|Si-1Si-2) = lamda * Pml(Si|Si-1Si-2) + (1 - lamda) * Pwb(Si|Si-1)
	 * Pwb(X), Pml(X)分别是X的Witten-Bell概率和最大似然概率
	 */
	@Override
	protected void calcTransitionMatrix(TransitionAndEmissionCounter counter) {
		Set<State> statesSet = counter.getDictionary().getStates();
		int N = statesSet.size();
		for(State state : statesSet) {//遍历所有隐藏状态，增加所有可能的一阶转移概率的最大似然估计
			StateSequence start = new StateSequence(state);
			int n_Count = counter.getTransitionStartCount(start);
			TransitionProbEntry entry = new TransitionProbEntry();
			for(State target : statesSet) {
				int count = counter.getTransitionCount(start, target);
				double prob = count / (n_Count + N);
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
						int suffixCount = counter.getTransitionSuffixCount(start);
						double lamda = 0;
						if(n_Count != 0 && suffixCount != 0)
							lamda = 1.0 - 1.0 *  suffixCount / (suffixCount + n_Count);
						else
							lamda = 1.0;
						
						TransitionProbEntry entry = new TransitionProbEntry();
						for(State target : statesSet) {
							double sequenceMLProb = 1.0 * counter.getTransitionCount(start, target) / n_Count;
							double _sequenceWBProb = Math.pow(10, transitionMatrix.get(start.remove(0)).getTransitionLogProb(target));
							double sequenceWBProb = lamda * sequenceMLProb + (1.0 - lamda) * _sequenceWBProb;
							entry.put(target, Math.log10(sequenceWBProb));
						}
												
						transitionMatrix.put(start, entry);
					}
				}
			}
		}
	}
	
	/**
	 * 采用加0.01平滑方式计算发射概率矩阵:p=(C+0.01)/(M+N*0.01)（已确保概率之和为1，不需要归一化）
	 * @param counter	转移发射计数器
	 */	
	protected void calcEmissionMatrix(TransitionAndEmissionCounter counter) {
		Iterator<State> iterator = counter.emissionIterator();
		long N = counter.getDictionary().observationCount();//观测状态的类型数
		while(iterator.hasNext()) {//遍历所有发射状态
			State state = iterator.next();
			Iterator<Observation> observationsIterator = counter.iterator(state);
			long M = counter.getEmissionStateCount(state);//以state为发射起点的总数量
			EmissionProbEntry emissionProbEntry = new EmissionProbEntry();
			while(observationsIterator.hasNext()) {//计算当前状态的所有发射概率
				Observation observation = observationsIterator.next();
				long C = counter.getEmissionCount(state, observation);//当前发射的数量
				double prob = (C + DEFAULT_DELTA) / (M + N * DEFAULT_DELTA + DEFAULT_DELTA);
				emissionProbEntry.put(observation, Math.log10(prob));
			}

			emissionProbEntry.put(CommonUtils.UNKNOWN, Math.log10(DEFAULT_DELTA / (M + N * DEFAULT_DELTA)));
			emissionMatrix.put(state, emissionProbEntry);
		}//end while
	}
}
