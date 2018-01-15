package hust.tools.hmm.learn;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import hust.tools.hmm.stream.SupervisedHMMSample;
import hust.tools.hmm.stream.SupervisedHMMSampleStream;
import hust.tools.hmm.utils.CommonUtils;
import hust.tools.hmm.utils.Dictionary;
import hust.tools.hmm.utils.Observation;
import hust.tools.hmm.utils.ObservationSequence;
import hust.tools.hmm.utils.State;
import hust.tools.hmm.utils.StateSequence;

/**
 *<ul>
 *<li>Description: 统计转移和发射的数量 
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月8日
 *</ul>
 */
public class TransitionAndEmissionCounter {
		
	/**
	 * 统计状态和观测的集合，并赋予索引
	 */
	private Dictionary dict;
	
	/**
	 * 序列、发射的计数
	 */
	private HashMap<StateSequence, TransitionCountEntry> transitionCountMap;
	
	/**
	 * 状态、转移的计数
	 */
	private HashMap<State, EmissionCountEntry> emissionCountMap;
	
	/**
	 * 出现在样本开始位置的隐藏状态计数，用于计算初始转移概率
	 */
	private HashMap<State, Integer> startStateCount;

	/**
	 * 样本起始隐藏状态之和
	 */
	private int totalStartStateCount;
	
	/**
	 * 默认HMM阶数
	 */
	private final static short DEFAULT_ORDER = 1; 
	
	private int order;
	
	public TransitionAndEmissionCounter() {
		init();
	}
	
	public TransitionAndEmissionCounter(SupervisedHMMSampleStream<?> sampleStream) throws IOException {
		this(sampleStream, DEFAULT_ORDER);
	}
	
	public TransitionAndEmissionCounter(SupervisedHMMSampleStream<?> sampleStream, int order) throws IOException {
		if(order < 1)
			throw new IllegalArgumentException("模型阶数和阈值应为正整数： order = " + order);
		this.order = order;
			
		init();
		
		SupervisedHMMSample sample = null;
		while((sample = (SupervisedHMMSample) sampleStream.read()) != null) {
			update(sample);
		}
	}
	
	public TransitionAndEmissionCounter(List<SupervisedHMMSample> samples) {
		this(samples, DEFAULT_ORDER);
	}
	
	public TransitionAndEmissionCounter(List<SupervisedHMMSample> samples, int order) {
		if(order < 1)
			throw new IllegalArgumentException("模型阶数和阈值应为正整数： order = " + order);
		this.order = order;
		
		init();
		
		for(SupervisedHMMSample sample : samples)
			update(sample);
	}
	
	public void update(SupervisedHMMSample sample) {
		StateSequence stateSequence = sample.getStateSequence();
		dict.add(stateSequence);
		
		add(stateSequence);
		
		ObservationSequence observationSequence = sample.getObservationSequence();
		dict.add(observationSequence);
		
		//统计隐藏状态到观测状态的发射计数
		for(int i = 0; i < observationSequence.length(); i++) {//当为终点发射时，state大小为1，当为边发射时为2
			State state = stateSequence.get(i);
			Observation observation = observationSequence.get(i);
			add(state, observation);
		}
		
		for(int i = 1; i <= order + 1; i++) {//遍历n元的阶数，将隐藏序列切分成不同阶的n元序列
			List<State[]> list = CommonUtils.generate(stateSequence, i);
			for(int j = 0; j < list.size(); j++) {//遍历n元序列
				State[] states = list.get(j);
				
				if(states.length > 1) {//统计隐藏状态之间的转移计数
					State[] temp = new State[states.length - 1];
					for(int index = 0; index < temp.length; index++)
						temp[index] = states[index];
					
					StateSequence start = new StateSequence(temp);
					State target = states[states.length - 1];
					add(start, target);
				}
			}
		}
	}
	
	private void init() {
		dict = new Dictionary();
		transitionCountMap = new HashMap<>();
		emissionCountMap = new HashMap<>();
		startStateCount = new HashMap<>();
	}
	
	/**
	 * 提取第一个隐藏状态并计数
	 * @param sequence 样本隐藏状态序列
	 */
	private void add(StateSequence sequence) {
		State state = sequence.get(0);
		if(startStateCount.containsKey(state))
			startStateCount.put(state, startStateCount.get(state) + 1);
		else 
			startStateCount.put(state, 1);
		
		totalStartStateCount++;
	}
	
	/**
	 * 增加一个起点为starts，终点为target的转移
	 * @param starts	转移的起点
	 * @param target	转移的终点
	 */
	private void add(StateSequence starts, State target) {
		if(transitionCountMap.containsKey(starts)) {
			TransitionCountEntry entry = transitionCountMap.get(starts);
			entry.add(target);
			transitionCountMap.put(starts, entry);
		}else {
			TransitionCountEntry entry = new TransitionCountEntry();
			entry.add(target);
			transitionCountMap.put(starts, entry);
		}
	}
	
	/**
	 * 增加一个状态为state，观测为observation的发射
	 * @param state			发射的状态
	 * @param observation	发射的观测
	 */
	private void add(State state, Observation observation) {
		if(emissionCountMap.containsKey(state)) {
			EmissionCountEntry entry = emissionCountMap.get(state);
			entry.add(observation);
			emissionCountMap.put(state, entry);
		}else {
			EmissionCountEntry entry = new EmissionCountEntry();
			entry.add(observation);
			emissionCountMap.put(state, entry);
		}
	}
	
	/**
	 * 返回模型的阶数
	 * @return	阶数
	 */
	public int getOrder() {
		return order;
	}
		
	/**
	 * 返回转移起点的的总数量
	 * @param start	转移的起点
	 * @return		转移起点的的总数量
	 */
	public long getSequenceCount(StateSequence start) {
		if(transitionCountMap.containsKey(start))
			return transitionCountMap.get(start).getTotal();
		
		return 0;
	}
	
	/**
	 * 返回起点为start，终点为target的转移数量
	 * @param start		转移的起点
	 * @param target	转移的终点
	 * @return			转移的数量
	 */
	public long getTransitionCount(StateSequence start, State target) {
		if(transitionCountMap.containsKey(start))
			return transitionCountMap.get(start).getTransitionCount(target);
		
		return 0;
	}
	
	/**
	 * 返回以给定state发射的总数量
	 * @param state	给定state
	 * @return		发射的总数量
	 */
	public long getStateCount(State state) {
		return emissionCountMap.get(state).getTotal();
	}

	/**
	 * 返回状态为state，观测为observation的发射的数量
	 * @param state			发射的状态
	 * @param observation	发射的观测
	 * @return				发射的数量
	 */
	public long getEmissionCount(State state, Observation observation) {
		if(emissionCountMap.containsKey(state))
			return emissionCountMap.get(state).getEmissionCount(observation);
		
		return 0;
	}
	
	/**
	 * 返回给定隐藏状态出现在样本起点的次数
	 * @param state	给定隐藏状态
	 * @return		隐藏状态出现在样本起点的次数
	 */
	public int getStartStateCount(State state) {
		if(startStateCount.containsKey(state))
			return startStateCount.get(state);
		
		return 0;
	}
	
	/**
	 * 返回起始状态的总数量
	 * @return	起始状态的总数量
	 */
	public int getTotalStartStatesCount() {
		return totalStartStateCount;
	}
	
	/**
	 * 返回隐藏状态的集合
	 * @return	隐藏状态的集合
	 */
	public Set<State> getStates() {
		return dict.getStates();
	}
	
	/**
	 * 返回观测状态的集合
	 * @return	观测状态的集合
	 */
	public Set<Observation> getObservations() {
		return dict.getObservations();
	}
	
	/**
	 * 返回观测状态和隐藏状态的索引信息
	 * @return	观测状态和隐藏状态的索引信息
	 */
	public Dictionary getDictionary() {
		return dict;
	}
	
	/**
	 * 返回转移起点的迭代器
	 * @return	迭代器
	 */
	public Iterator<StateSequence> transitionIterator() {
		return transitionCountMap.keySet().iterator();
	}
	
	/**
	 * 返回给定转移起点的转移目标状态的迭代器
	 * @param start	给定的转移起点
	 * @return		目标状态的迭代器
	 */
	public Iterator<State> iterator(StateSequence start) {
		return transitionCountMap.get(start).statesIterator();
	}
	
	/**
	 * 发射起始状态的迭代器
	 * @return	迭代器
	 */
	public Iterator<State> emissionIterator() {
		return emissionCountMap.keySet().iterator();
	}
	
	/**
	 * 返回给定状态的发射目标观测状态的迭代器
	 * @param state	给定的状态
	 * @return		目标观测状态的迭代器
	 */
	public Iterator<Observation> iterator(State state) {
		return emissionCountMap.get(state).observationsIterator();
	}
	
	/**
	 * 返回给定发射状态的所有发射信息（发射的目标观测状态及其计数）
	 * @param state	发射状态
	 * @return		发射状态的所有发射信息
	 */
	public EmissionCountEntry get(State state) {
		if(contain(state))
			return emissionCountMap.get(state);
		
		return null;
	}
	
	/**
	 * 返回给定转移起点的所有转移信息（转移的目标状态及其计数）
	 * @param start	转移起点
	 * @return		转移起点的所有转移信息
	 */
	public TransitionCountEntry get(StateSequence start) {
		if(contain(start))
			return transitionCountMap.get(start);
		
		return null;
	}
	
	/**
	 * 返回是否包含以state为发射的状态
	 * @param state	待判断的发射
	 * @return		true-包含/false-不包含
	 */
	public boolean contain(State state) {
		return emissionCountMap.containsKey(state);
	}
	
	/**
	 * 返回是否包含状态为state，观测为observation的发射
	 * @param state			发射的状态
	 * @param observation	发射的观测
	 * @return				true-包含/false-不包含
	 */
	public boolean contain(State state, Observation observation) {
		if(contain(state))
			return emissionCountMap.get(state).contain(observation);
		
		return false;
	}
	
	/**
	 * 返回是否包含给定转移起点
	 * @param start	待判断的状态序列
	 * @return		true-包含/false-不包含
	 */
	public boolean contain(StateSequence start) {
		return transitionCountMap.containsKey(start);
	}
	
	/**
	 * 返回是否包含起点为start，终点为target的转移
	 * @param starts	转移的起点
	 * @param target	转移的终点
	 * @return			true-包含/false-不包含
	 */
	public boolean contain(StateSequence starts, State target) {
		if(contain(starts)) 
			return transitionCountMap.get(starts).contain(target);
		
		return false;
	}
}