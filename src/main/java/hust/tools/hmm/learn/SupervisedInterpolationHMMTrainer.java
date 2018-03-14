package hust.tools.hmm.learn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
 *<li>Description:  
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年3月9日
 *</ul>
 */
public class SupervisedInterpolationHMMTrainer extends AbstractSupervisedHMMTrainer {
	
	/**
	 * 用于计算插值权重的语料
	 */
	private TransitionAndEmissionCounter held;
	
	/**
	 * 插值权重
	 */
	private double[] lamdas;

	public SupervisedInterpolationHMMTrainer(SupervisedHMMSampleStream<?> trainSampleStream, double ratio, int order) throws IOException {
		List<SupervisedHMMSample> trainSamples = new ArrayList<>();
		List<SupervisedHMMSample> heldSamples = new ArrayList<>();
		
		SupervisedHMMSample sample = null;
		if(ratio >= 1.0 || ratio <= 0.0)
			throw new IllegalArgumentException("留存数据比例不合法:(0, 1.0)");
		
		int no = (int) (1 / ratio);
		int index = 1;
		while((sample = (SupervisedHMMSample) trainSampleStream.read()) != null) {
			if(index++ % no == 0)
				heldSamples.add(sample);
			else
				trainSamples.add(sample);
		}
		
		init(trainSamples, heldSamples, order);
	}
	
	public SupervisedInterpolationHMMTrainer(SupervisedHMMSampleStream<?> trainSampleStream, SupervisedHMMSampleStream<?> heldSampleStream, int order) throws IOException {
		super(trainSampleStream, order);
		held = new TransitionAndEmissionCounter(heldSampleStream);
	}
	
	public SupervisedInterpolationHMMTrainer(List<SupervisedHMMSample> trainSamples, double ratio, int order) throws IOException {
		List<SupervisedHMMSample> heldSamples = new ArrayList<>();
		Iterator<SupervisedHMMSample> iterator = trainSamples.iterator();
		if(ratio >= 1.0 || ratio <= 0.0)
			throw new IllegalArgumentException("留存数据比例不合法:(0, 1.0)");
		
		int no = (int) (1 / ratio);
		int index = 1;
		
		while(iterator.hasNext()){
			SupervisedHMMSample sample = iterator.next();
			if(index++ % no == 0) {
				heldSamples.add(sample);
				iterator.remove();
			}
		}
		
		init(trainSamples, heldSamples, order);
	}
	
	public SupervisedInterpolationHMMTrainer(List<SupervisedHMMSample> trainSamples, List<SupervisedHMMSample> heldSamples, int order) throws IOException {
		super(trainSamples, order);
		held = new TransitionAndEmissionCounter(heldSamples);
	}
	
	/**
	 * 初始化数据和模型参数
	 * @param trainSamples	训练样本
	 * @param heldSamples	留存样本
	 * @param order			模型阶数
	 */
	private void init(List<SupervisedHMMSample> trainSamples, List<SupervisedHMMSample> heldSamples, int order) {
		this.order = order;
		this.counter = new TransitionAndEmissionCounter(trainSamples, order);
		this.held = new TransitionAndEmissionCounter(heldSamples, order);
		this.dict = counter.getDictionary();
		this.pi = new HashMap<>();
		this.transitionMatrix = new HashMap<>();
		this.emissionMatrix = new HashMap<>();
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
		calculateLamda();
		
		Set<State> statesSet = counter.getDictionary().getStates();				
		for(State state : statesSet) {//遍历所有隐藏状态，增加所有可能的一阶转移
			StateSequence start = new StateSequence(state);
			int n_Count = counter.getTransitionStartCount(start);
			TransitionProbEntry entry = new TransitionProbEntry();
			for(State target : statesSet) {
				int count = counter.getTransitionCount(start, target);
				double prob = 0.0;
				if(n_Count != 0)
					prob = 1.0 * count / n_Count;
				
				entry.put(target, prob);
			}
			
			transitionMatrix.put(start, entry);
		}
		
		StateSequence[] sequences = null;
		for(int i = 1; i < order; i++) {//遍历增加所有2——order阶的转移概率
			sequences = transitionMatrix.keySet().toArray(new StateSequence[transitionMatrix.size()]);
			for(StateSequence sequence : sequences) {
				if(sequence.length() == i) {
					for(State state : statesSet) {
						StateSequence start = sequence.addLast(state);
						int n_Count = counter.getTransitionStartCount(start);
						
						TransitionProbEntry entry = new TransitionProbEntry();
						for(State target : statesSet) {
							int count = counter.getTransitionCount(start, target);
							double prob = 0.0;
							if(n_Count != 0)
								prob = 1.0 * count / n_Count;
							
							entry.put(target, prob);
						}
						
						transitionMatrix.put(start, entry);
					}
				}
			}
		}
		
		//计算插值平滑p*(d|abc) = lamda4*P(d|abc) + lamda3*P(c|ab)  + lamda2*P(b|a) +lamda1*p(a) 
		sequences = transitionMatrix.keySet().toArray(new StateSequence[transitionMatrix.size()]);
		for(StateSequence sequence : sequences) {
			double prob = 0.0;
			TransitionProbEntry entry = new TransitionProbEntry();
			if(sequence.length() == order) {
				for(State target : statesSet) {
					StateSequence _sSequence = sequence;
					prob = transitionMatrix.get(_sSequence).getTransitionLogProb(target) * lamdas[_sSequence.length() - 1];
					for(int i = 1; i < order; i++) {
						_sSequence = _sSequence.remove(0);
						prob += transitionMatrix.get(_sSequence).getTransitionLogProb(target) * lamdas[_sSequence.length() - 1];
					}
					
					entry.put(target, Math.log10(prob));
				}
				transitionMatrix.put(sequence, entry);
			}
		}
		
		//删除低阶转移概率，仅仅保留order阶概率
		for(Iterator<Map.Entry<StateSequence, TransitionProbEntry>> it = transitionMatrix.entrySet().iterator(); it.hasNext();) {
		    StateSequence start = it.next().getKey();
		    
		    if(start.length() != order)
		    	it.remove();
		}
	}
	
	/**
	 * 采用加1平滑方式计算发射概率矩阵:p=(C+1)/(M+N)（已确保概率之和为1，不需要归一化）
	 * @param counter	转移发射计数器
	 */
	@Override
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
				double prob = (C + DEFAULT_DELTA) / (M + N * DEFAULT_DELTA + DEFAULT_DELTA);
				emissionProbEntry.put(observation, Math.log10(prob));
			}

			emissionProbEntry.put(CommonUtils.UNKNOWN, Math.log10(DEFAULT_DELTA / (M + N * DEFAULT_DELTA + DEFAULT_DELTA)));
			emissionMatrix.put(state, emissionProbEntry);
		}//end while
	}
	
	/**
	 * 根据留存数据计算插值权重
	 */
	private void calculateLamda() {
		lamdas = new double[order];
		Iterator<StateSequence> iterator = held.transitionIterator();
		
		iterator = held.transitionIterator();
		while(iterator.hasNext()) {	//对每一个计数大于零的order阶转移遍历
			StateSequence sequence = iterator.next();
			if(sequence.length() == order) {
				Iterator<Entry<State, Integer>> countIterator = held.transitionTargetCountIterator(sequence);
				while(countIterator.hasNext()) {
					Entry<State, Integer> entry = countIterator.next();
					double max = 0.0;	   	//最大数量
					int max_index = 0;		//最大数量对应的n元长度
					double accumulation = entry.getValue();	//lamda累加的值
						
					for(int i = 0; i < order; i++) {
						StateSequence n_Sequence = sequence.addLast(entry.getKey());
						for(int j = 0; j < i; j++)
							n_Sequence = n_Sequence.remove(0);
						
						double count = held.getTransitionCount(n_Sequence, n_Sequence.get(n_Sequence.length() - 1));
						int n_count = 0;
						double prob = 0.0;
						if(2  == n_Sequence.length())
							n_count	= held.getEmissionStateCount(n_Sequence.get(0));
						else
							n_count = held.getTransitionCount(n_Sequence.remove(n_Sequence.length() - 1), n_Sequence.get(n_Sequence.length() - 2));

						if(1 == n_count)
							prob = 0.0;
						else
							prob = (count - 1) / (n_count - 1);
						
						if(max < prob) {
							max = prob;
							max_index = order - i - 1;
						}
					}
					lamdas[max_index] += accumulation;
				}
			}
		}
				
		//正规化 
		double sum = 0.0;
		for(int i = 0; i < lamdas.length; i++)
			sum += lamdas[i] + 0.001;//+1防止权重为0
		for(int i = 0; i < lamdas.length; i++)
			lamdas[i] = (1 + lamdas[i]) / sum;
		
		for(int i = 0; i < lamdas.length; i++)
			System.out.println(lamdas[i] + "\t");
	}
}
