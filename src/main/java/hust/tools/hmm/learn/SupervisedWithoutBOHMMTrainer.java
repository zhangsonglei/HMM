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
public class SupervisedWithoutBOHMMTrainer extends AbstractSupervisedHMMTrainer {
	
	/**
	 * 对初始转移向量和发射概率进行加delta平滑
	 */
	private final double delta = 0.01;
	
	public SupervisedWithoutBOHMMTrainer(TransitionAndEmissionCounter counter) {
		super(counter);
	}
	
	public SupervisedWithoutBOHMMTrainer(SupervisedHMMSampleStream<?> sampleStream, int order) throws IOException {
		super(sampleStream, order);
	}
	
	public SupervisedWithoutBOHMMTrainer(List<SupervisedHMMSample> samples, int order) throws IOException {
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
			double prob = (count + 0.01) / (M + N * 0.01);
			
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
		Set<State> set = counter.getDictionary().getStates();
		
		for(State start : set) {
			StateSequence sequence = new StateSequence(start);
			int total = 0;
			for(State target : set) 
				total += counter.getSequenceCount(sequence.add(target)) + 1;
			
			for(State target : set) {
				StateSequence transition = sequence.add(target);
				double prob = (0.01 + counter.getSequenceCount(transition)) / total;
				
				transitionMatrix.put(transition, new ARPAEntry(Math.log10(prob), 0));
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
