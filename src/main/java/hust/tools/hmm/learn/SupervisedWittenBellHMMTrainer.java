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
 *<li>Description: 基于Witten-Bell平滑的监督学习模型训练类器
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月10日
 *</ul>
 */
public class SupervisedWittenBellHMMTrainer extends AbstractSupervisedHMMTrainer {
	
	/**
	 * 对初始转移向量和发射概率进行加delta平滑
	 */
	private final double delta = 0.01;
	
	public SupervisedWittenBellHMMTrainer(TransitionAndEmissionCounter counter) {
		super(counter);
	}
	
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
		
		HMModel model = new HMModelBasedBO(order, counter.getDictionary(), pi, transitionMatrix, emissionMatrix);
		
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
			double prob = (count + delta) / (M + N * delta);
			
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
		Iterator<StateSequence> iterator = counter.transitionIterator();
		while(iterator.hasNext()) {//遍历所有转移状态,计算转移的最大似然概率
			StateSequence sequence = iterator.next();
			double prob = calcTransitionMLProbability(sequence, counter);
			
			transitionMatrix.put(sequence, new ARPAEntry(Math.log10(prob), 0));
		}
		
		Set<StateSequence> sequences = null;
		for(int i = 2; i <= order + 1; i++) {//根据串的长度依次为多元串计算Witten-Bell概率, 长度为1的采用最大似然估计
			sequences = transitionMatrix.keySet();
			for(StateSequence sequence : sequences) {
				if(sequence.length() == i) {
					
					Set<State> suffixs = counter.getSuffixs(sequence.remove(sequence.length() - 1));
					int totalCount = 0;
					for(State state : suffixs) {
						StateSequence newSequence = sequence.set(state, sequence.length() - 1);
						totalCount += counter.getSequenceCount(newSequence);
					}
					double lamda = 1.0 - 1.0 * suffixs.size() / (suffixs.size() + totalCount);
					
					double sequenceMLProb = Math.pow(10, transitionMatrix.get(sequence).getLog_prob());
					double _sequenceWBProb = Math.pow(10, transitionMatrix.get(sequence.remove(0)).getLog_prob());
					double sequenceWBProb = lamda * sequenceMLProb + (1.0 - lamda) * _sequenceWBProb;
					
					transitionMatrix.put(sequence, new ARPAEntry(Math.log10(sequenceWBProb), 0));
				}
			}
		}

		sequences = transitionMatrix.keySet();
		for(StateSequence sequence : sequences) {//遍历计算回退权重
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
	 * 采用加0.01平滑方式计算发射概率矩阵:p=(C+0.01)/(M+N*0.01)（已确保概率之和为1，不需要归一化）
	 * @param counter	转移发射计数器
	 */	
	protected void calcEmissionMatrix(TransitionAndEmissionCounter counter) {
		Iterator<State> iterator = counter.emissionIterator();
		long N = counter.getDictionary().observationCount();//观测状态的类型数
		while(iterator.hasNext()) {//遍历所有发射状态
			State state = iterator.next();
			Iterator<Observation> observationsIterator = counter.iterator(state);
			long M = counter.getStateCount(state);//以state为发射起点的总数量
			EmissionProbEntry emissionProbEntry = new EmissionProbEntry();
			while(observationsIterator.hasNext()) {//计算当前状态的所有发射概率
				Observation observation = observationsIterator.next();
				long C = counter.getEmissionCount(state, observation);//当前发射的数量
				double prob = (C + delta) / (M + N * delta);
				emissionProbEntry.put(observation, Math.log10(prob));
			}

			emissionProbEntry.put(UNKNOWN, Math.log10(delta / (M + N * delta)));
			emissionMatrix.put(state, emissionProbEntry);
		}//end while
	}
}
