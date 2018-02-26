package hust.tools.hmm.learn;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
public class SupervisedAdditionHMMTrainer extends AbstractSupervisedHMMTrainer {

	/**
	 * 默认加0.01平滑
	 */
	private final double DEFAULT_DELTA = 0.01;
	
	/**
	 * 加法平滑中加数大小
	 */
	private double delta;
	
	public SupervisedAdditionHMMTrainer(TransitionAndEmissionCounter counter) {
		super(counter);
		this.delta = DEFAULT_DELTA;
	}
	
	public SupervisedAdditionHMMTrainer(TransitionAndEmissionCounter counter, double delta) {
		super(counter);
		this.delta = delta <= 0 ? DEFAULT_DELTA : delta;
	}
	
	public SupervisedAdditionHMMTrainer(SupervisedHMMSampleStream<?> sampleStream, int order) throws IOException {
		super(sampleStream, order);
		this.delta = DEFAULT_DELTA;
	}
	
	public SupervisedAdditionHMMTrainer(SupervisedHMMSampleStream<?> sampleStream, int order, double delta) throws IOException {
		super(sampleStream, order);
		this.delta = delta <= 0 ? DEFAULT_DELTA : delta;
	}
	
	public SupervisedAdditionHMMTrainer(List<SupervisedHMMSample> samples, int order) throws IOException {
		super(samples, order);
		this.delta = DEFAULT_DELTA;
	}
	
	public SupervisedAdditionHMMTrainer(List<SupervisedHMMSample> samples, int order, double delta) throws IOException {
		super(samples, order);
		this.delta = delta <= 0 ? DEFAULT_DELTA : delta;
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
			double prob = (count + delta) / (M + N * delta);
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
				double prob = (delta + count) / (n_Count + N * delta);
				entry.put(target, Math.log10(prob));
			}
			
			transitionMatrix.put(start, entry);
		}
		
		if(order > 1) {
			StateSequence[] sequences = transitionMatrix.keySet().toArray(new StateSequence[transitionMatrix.size()]);
			for(StateSequence sequence : sequences) {
				StateSequence start = sequence.addFirst(CommonUtils.SOS);
				int n_Count = counter.getTransitionStartCount(start);
				TransitionProbEntry entry = new TransitionProbEntry();
				for(State target : statesSet) {
					int count = counter.getTransitionCount(start, target);
					double prob = (delta + count) / (n_Count + N * delta);
					entry.put(target, Math.log10(prob));
				}
				
				transitionMatrix.put(start, entry);
			}
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
							double prob = (delta + count) / (n_Count + N * delta);
							entry.put(target, Math.log10(prob));
						}
						
						transitionMatrix.put(start, entry);
					}
				}
			}
		}
		
		//删除低阶转移概率，仅仅保留order阶概率
		for(Iterator<Map.Entry<StateSequence, TransitionProbEntry>> it = transitionMatrix.entrySet().iterator(); it.hasNext();) {
		    StateSequence start = it.next().getKey();
		    
		    if(start.length() != order && !start.get(0).equals(CommonUtils.SOS))
		    	it.remove();
		}
	}
	
	/**
	 * 采用加1平滑方式计算发射概率矩阵:p=(C+1)/(M+N)（已确保概率之和为1，不需要归一化）
	 * @param counter	转移发射计数器
	 */
	protected void calcEmissionMatrix(TransitionAndEmissionCounter counter) {
		Iterator<State> iterator = counter.emissionIterator();
		int N = counter.getDictionary().observationCount();//观测状态的类型数
		
		while(iterator.hasNext()) {//遍历所有发射
			State state = iterator.next();
			Iterator<Observation> observationsIterator = counter.iterator(state);
			int M = counter.getEmissionStateCount(state);//以state为发射起点的总数量
			
			EmissionProbEntry emissionProbEntry = new EmissionProbEntry();
			while(observationsIterator.hasNext()) {//计算当前状态的所有发射概率
				Observation observation = observationsIterator.next();
				int C = counter.getEmissionCount(state, observation);//当前发射的数量
				double prob = (C + delta) / (M + N * delta);
				emissionProbEntry.put(observation, Math.log10(prob));
			}

			emissionProbEntry.put(CommonUtils.UNKNOWN, Math.log10(delta / (M + N * delta)));
			emissionMatrix.put(state, emissionProbEntry);
		}//end while
	}
}
