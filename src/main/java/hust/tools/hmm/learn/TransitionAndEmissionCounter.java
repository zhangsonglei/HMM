package hust.tools.hmm.learn;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
	private HashMap<StateSequence, Long> transitionCountMap;
	
	/**
	 * 记录状态序列后缀
	 */
	private HashMap<StateSequence, Set<State>> stateSuffix;
	
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
	 * 所有隐藏状态的总数量
	 */
	private long totalStatesCount;
	
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
	
	public void update(SupervisedHMMSample sample) {
		StateSequence stateSequence = sample.getStateSequence();
		ObservationSequence observationSequence = sample.getObservationSequence();
		
		//统计观测和隐藏状态的类型并建立索引
		dict.add(stateSequence);
		dict.add(observationSequence);
		
		totalStatesCount += stateSequence.length();
		
		//统计起始隐藏状态
		add(stateSequence.get(0));		
		
		//统计转移计数
		for(int i = 1; i <= order + 1; i++) {//遍历n元的阶数，将隐藏序列切分成不同阶的n元序列
			List<State[]> list = CommonUtils.generate(stateSequence, i);
			for(State[] states : list) //遍历n元序列
				add(new StateSequence(states));
		}
		
		//统计隐藏状态到观测状态的发射计数
		for(int i = 0; i < observationSequence.length(); i++) {//当为终点发射时，state大小为1，当为边发射时为2
			State state = stateSequence.get(i);
			Observation observation = observationSequence.get(i);
			add(state, observation);
		}		
	}
	
	private void init() {
		dict = new Dictionary();
		transitionCountMap = new HashMap<>();
		stateSuffix = new HashMap<>();
		emissionCountMap = new HashMap<>();
		startStateCount = new HashMap<>();
		totalStartStateCount = 0;
		totalStatesCount = 0;
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
		
		totalStartStateCount++;
	}
	
	/**
	 * 增加一条发射，并统计历史发射的所有目标状态
	 * @param sequence	发射
	 */
	private void add(StateSequence sequence) {
		if(transitionCountMap.containsKey(sequence)) 
			transitionCountMap.put(sequence, transitionCountMap.get(sequence) + 1);
		else
			transitionCountMap.put(sequence, 1L);
		
		//统计后缀
		if(sequence.length() > 1) {
			StateSequence states = sequence.remove(sequence.length() - 1);
			State suffix = sequence.get(sequence.length() - 1);
			Set<State> set = null;
			if(stateSuffix.containsKey(states)) 
				set = stateSuffix.get(states);
			else
				set = new HashSet<>();
			
			set.add(suffix);
			stateSuffix.put(states, set);
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
	
	public long getTotalStatesCount() {
		return totalStatesCount;
	}
		
	/**
	 * 返回序列的总数量
	 * @param sequence	序列
	 * @return			序列的总数量
	 */
	public long getSequenceCount(StateSequence sequence) {
		if(transitionCountMap.containsKey(sequence))
			return transitionCountMap.get(sequence);
		
		return 0;
	}
	
	/**
	 * 返回给定发射的数量
	 * @param start		发射的起点
	 * @param target	发射的终点
	 * @return			发射的数量
	 */
	public long getTransitionCount(StateSequence start, State target) {
		if(contain(start, target))
			return transitionCountMap.get(start.add(target));
		
		return 0;
	}
	
	public Set<State> getSuffixs(StateSequence sequence) {
		if(stateSuffix.containsKey(sequence))
			return stateSuffix.get(sequence);
		
		return null;
	}
	
	public Set<Entry<StateSequence, Set<State>>> suffixsEntrySet() {
		return stateSuffix.entrySet();
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
	 * 返回是否包含给定状态序列
	 * @param sequence	待判断的状态序列
	 * @return			true-包含/false-不包含
	 */
	public boolean contain(StateSequence sequence) {
		return transitionCountMap.containsKey(sequence);
	}
	
	public boolean contain(StateSequence start, State target) {
		return transitionCountMap.containsKey(start.add(target));
	}
}