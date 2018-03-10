package hust.tools.hmm.learn;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

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
	 * 序列的计数
	 */
	private HashMap<StateSequence, TransitionCountEntry> transitionCountMap;

	/**
	 * 隐藏状态发射到观测的计数
	 */
	private HashMap<State, EmissionCountEntry> emissionCountMap;
	
	/**
	 * 观测状态发射到隐藏状态的计数
	 */
	private HashMap<Observation, EmissionCounter> reverseEmissionCountMap;
	
	/**
	 * 出现在样本开始位置的隐藏状态计数，用于计算初始转移概率
	 */
	private HashMap<State, Integer> startStateCount;

	/**
	 * 样本起始隐藏状态之和
	 */
	private int totalStartStateCount;
	
	/**
	 * 所有隐藏状态的总数量
	 */
	private int totalStatesCount;
	
	/**
	 * 默认HMM阶数
	 */
	private final static int DEFAULT_ORDER = 1;
	
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
	
	/**
	 * 初始化数据
	 */
	private void init() {
		dict = new Dictionary();
		transitionCountMap = new HashMap<>();
		emissionCountMap = new HashMap<>();
		reverseEmissionCountMap = new HashMap<>();
		startStateCount = new HashMap<>();
		totalStartStateCount = 0;
		totalStatesCount = 0;
	}
	
	public void update(SupervisedHMMSample sample) {
		StateSequence stateSequence = sample.getStateSequence();
		ObservationSequence observationSequence = sample.getObservationSequence();
		
		//统计观测和隐藏状态的类型并建立索引
		dict.add(stateSequence);
		dict.add(observationSequence);
		
		totalStatesCount += stateSequence.length();
		totalStartStateCount++;
		
		//统计起始隐藏状态
		add(stateSequence.get(0));		
		
		//统计转移计数
		for(int i = 2; i <= order + 1; i++) {//遍历n元的阶数，将隐藏序列切分成不同阶的n元序列
			List<StateSequence> list = CommonUtils.generate(stateSequence, i);
			
			for(int j = 0; j < list.size(); j++) {
				StateSequence transition = list.get(j);
				if(j == 0 && i <= order){
					add(transition);
					transition = transition.addFirst(CommonUtils.SOS);
				}
				add(transition);
			}
		}
		
		//统计隐藏状态到观测状态的发射计数
		for(int i = 0; i < observationSequence.length(); i++) {//当为终点发射时，state大小为1，当为边发射时为2
			State state = stateSequence.get(i);
			Observation observation = observationSequence.get(i);
			add(state, observation);
		}
	}
	
	/**
	 * 增加一个隐藏状态序列的首部
	 * @param state 隐藏状态序列的首部
	 */
	private void add(State state) {
		if(startStateCount.containsKey(state))
			startStateCount.put(state, startStateCount.get(state) + 1);
		else 
			startStateCount.put(state, 1);
	}
	
	/**
	 * 增加一条转移，并统计历史转移的所有目标状态（统计n元串计数及其后缀）
	 * @param sequence	发射
	 */
	private void add(StateSequence transition) {
		StateSequence start = transition.remove(transition.length() - 1);
		State target = transition.get(transition.length() - 1);
		
		TransitionCountEntry entry = null;
		if(transitionCountMap.containsKey(start)) 
			entry =  transitionCountMap.get(start);
		else
			entry = new TransitionCountEntry();
		entry.add(target);
		transitionCountMap.put(start, entry);
	}
	
	/**
	 * 增加一个状态为state，观测为observation的发射
	 * @param state			发射的状态
	 * @param observation	发射的观测
	 */
	private void add(State state, Observation observation) {
		EmissionCountEntry entry = null;
		if(emissionCountMap.containsKey(state)) 
			entry = emissionCountMap.get(state);
		else 
			entry = new EmissionCountEntry();
		
		entry.add(observation);
		emissionCountMap.put(state, entry);
		
		//反向发射
		EmissionCounter revEntry = null;
		if(reverseEmissionCountMap.containsKey(observation)) 
			revEntry = reverseEmissionCountMap.get(observation);
		else 
			revEntry = new EmissionCounter();
		
		revEntry.add(state);
		reverseEmissionCountMap.put(observation, revEntry);
	}
	
	/**
	 * 返回模型的阶数
	 * @return	阶数
	 */
	public int getOrder() {
		return order;
	}

	public HashMap<StateSequence, TransitionCountEntry> getTransitionCount() {
		return transitionCountMap;
	}
	
	/**
	 * 返回训练语料中状态的总数量
	 * @return	状态总数量
	 */
	public int getTotalStatesCount() {
		return totalStatesCount;
	}
		
	/**
	 * 返回序列的总数量
	 * @param sequence	序列
	 * @return			序列的总数量
	 */
	public int getTransitionStartCount(StateSequence start) {
		if(transitionCountMap.containsKey(start))
			return transitionCountMap.get(start).getTotal();
		
		return 0;
	}
	
	public int getTransitionSuffixCount(StateSequence start) {
		if(transitionCountMap.containsKey(start))
			return transitionCountMap.get(start).size();
		
		return 0;
	}
	
	/**
	 * 返回给定发射的数量
	 * @param start		发射的起点
	 * @param target	发射的终点
	 * @return			发射的数量
	 */
	public int getTransitionCount(StateSequence start, State target) {
		if(transitionCountMap.containsKey(start))
			return transitionCountMap.get(start).getTransitionTargetCount(target);
		
		return 0;
	}

	/**
	 * 返回以给定state发射的总数量
	 * @param state	给定state
	 * @return		发射的总数量
	 */
	public int getEmissionStateCount(State state) {
		return emissionCountMap.get(state).getTotal();
	}
	
	/**
	 * 返回以给定observation发射的总数量
	 * @param observation	给定observation
	 * @return				发射的总数量
	 */
	public int getObservationCount(Observation observation) {
		return reverseEmissionCountMap.get(observation).getTotal();
	}

	/**
	 * 返回起点为state，目标为observation的发射的数量
	 * @param state			发射的起点
	 * @param observation	发射的目标
	 * @return				发射的数量
	 */
	public int getEmissionCount(State state, Observation observation) {
		if(emissionCountMap.containsKey(state))
			if(emissionCountMap.get(state).contain(observation))
				return emissionCountMap.get(state).getObservationCount(observation);
		
		return 0;
	}
	
	/**
	 * 返回起点为observation，目标为state的发射的数量
	 * @param observation	发射的起点
	 * @param state			发射的目标
	 * @return				发射的数量
	 */
	public int getRevEmissionCount(Observation observation, State state) {
		if(reverseEmissionCountMap.containsKey(observation))
			if(reverseEmissionCountMap.get(observation).contain(state))
				return reverseEmissionCountMap.get(observation).getStateCount(state);
		
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
	 * 返回给定转移起点的所有转移目标及其数量
	 * @param start	转移起点
	 * @return		所有转移目标及其数量
	 */
	public Iterator<Entry<State, Integer>> transitionTargetCountIterator(StateSequence start) {
		return transitionCountMap.get(start).entryIterator();
	}
	
	/**
	 * 发射起始状态的迭代器
	 * @return	迭代器
	 */
	public Iterator<State> emissionIterator() {
		return emissionCountMap.keySet().iterator();
	}
	
	/**
	 * 发射起始观测的迭代器
	 * @return	迭代器
	 */
	public Iterator<Observation> revEmissionIterator() {
		return reverseEmissionCountMap.keySet().iterator();
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
	 * 返回给定观测状态的发射目标状态的迭代器
	 * @param observation	给定的观测状态
	 * @return				目标状态的迭代器
	 */
	public Iterator<State> iterator(Observation observation) {
		return reverseEmissionCountMap.get(observation).observationsIterator();
	}
}