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
	 * 序列、发射的计数
	 */
	private HashMap<StateSequence, TransitionCountEntry> transitionCountMap;
	
	/**
	 * 状态、转移的计数
	 */
	private HashMap<State, EmissionCountEntry> emissionCountMap;
	
	/**
	 * 统计状态和观测的集合，并赋予索引
	 */
	private Dictionary dict;
	
	/**
	 * 隐藏状态的总数量,用于计算初始转移概率
	 */
	private long totalStatesCount;
	
	/**
	 * 默认的阈值
	 */
	private final static short DEFAULT_CUTOFF = 0;
	
	/**
	 * 默认HMM阶数
	 */
	private final static short DEFAULT_ORDER = 1; 
	
	public TransitionAndEmissionCounter() {
		init();
	}
	
	public TransitionAndEmissionCounter(SupervisedHMMSampleStream<?> sampleStream) throws IOException {
		this(sampleStream, DEFAULT_ORDER, DEFAULT_CUTOFF);
	}
	
	public TransitionAndEmissionCounter(SupervisedHMMSampleStream<?> sampleStream, int order) throws IOException {
		this(sampleStream, order, DEFAULT_CUTOFF);
	}
	
	public TransitionAndEmissionCounter(SupervisedHMMSampleStream<?> sampleStream, int order, int cutoff) throws IOException {
		if(order < 1 || cutoff < 0)
			throw new IllegalArgumentException("模型阶数和阈值应为正整数： order = " + order + ", cutoff = " + cutoff);
			
		init();
		
		SupervisedHMMSample sample = null;
		while((sample = (SupervisedHMMSample) sampleStream.read()) != null) {
			update(sample, order);
		}
		
		cutoff(cutoff);
	}
	
	public TransitionAndEmissionCounter(List<SupervisedHMMSample> samples) {
		this(samples, DEFAULT_ORDER, DEFAULT_CUTOFF);
	}
	
	public TransitionAndEmissionCounter(List<SupervisedHMMSample> samples, int order) {
		this(samples, order, DEFAULT_CUTOFF);
	}
	
	public TransitionAndEmissionCounter(List<SupervisedHMMSample> samples, int order, int cutoff) {
		if(order < 1 || cutoff < 0)
			throw new IllegalArgumentException("模型阶数和阈值应为正整数： order = " + order + ", cutoff = " + cutoff);
		
		init();
		
		for(SupervisedHMMSample sample : samples)
			update(sample, order);
		
		cutoff(cutoff);
	}
	
	public void update(SupervisedHMMSample sample, int order) {
		StateSequence stateSequence = sample.getStateSequence();
		stateSequence = dict.add(stateSequence);
		totalStatesCount += stateSequence.length();
		
		ObservationSequence observationSequence = sample.getObservationSequence();
		observationSequence = dict.add(observationSequence);
		
		for(int i = 1; i <= order + 1; i++) {//遍历n元的阶数，将隐藏序列切分成不同阶的n元序列
			List<State[]> list = CommonUtils.generate(stateSequence, i);
			for(int j = 0; j < list.size(); j++) {//遍历n元序列
				State[] states = list.get(j);
				
				if(states.length < order + 1)//统计n元序列的计数, 最高阶的状态序列不需要统计
					add(new StateSequence(states));
				
				if(states.length > 1) {//统计隐藏状态之间的转移计数
					State[] temp = new State[states.length - 1];
					for(int index = 0; index < temp.length; index++)
						temp[index] = states[index];
					
					StateSequence start = new StateSequence(temp);
					State target = states[i - 1];
					add(start, target);
				}
				
				//统计隐藏状态到观测状态的发射计数
				if(states.length == 1) {//当为终点发射时，state大小为1，当为边发射时为2
					State state = states[0];
					Observation observation = observationSequence.get(j + i - 1);
					add(state, observation);
				}
			}
		}
	}
	
	private void init() {
		dict = new Dictionary();
		transitionCountMap = new HashMap<>();
		emissionCountMap = new HashMap<>();
	}
	
	/**
	 * 增加一个状态序列
	 * @param sequence	待增加的状态序列
	 */
	private void add(StateSequence sequence) {
		if(transitionCountMap.containsKey(sequence))
			transitionCountMap.put(sequence, transitionCountMap.get(sequence).setCount(transitionCountMap.get(sequence).getCount() + 1));
		else {
			TransitionCountEntry entry = new TransitionCountEntry();
			transitionCountMap.put(sequence, entry.setCount(1L));
		}
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
			entry.setCount(1L);
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
			entry.setCount(entry.getCount() + 1);
			entry.add(observation);
			emissionCountMap.put(state, entry);
		}else {
			EmissionCountEntry entry = new EmissionCountEntry();
			entry.setCount(1L);
			entry.add(observation);
			emissionCountMap.put(state, entry);
		}
	}
	
	/**
	 * 返回隐藏状态序列的数量
	 * @param sequence	待返回数量的隐藏状态序列
	 * @return			隐藏状态序列的数量
	 */
	public long getSequenceCount(StateSequence sequence) {
		if(transitionCountMap.containsKey(sequence))
			return transitionCountMap.get(sequence).getCount();
		
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
		return emissionCountMap.get(state).getCount();
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
	 * 返回状态的总数量
	 * @return	状态的总数量
	 */
	public long getTotalStatesCount() {
		return totalStatesCount;
	}
	
	/**
	 * 返回转移起始状态序列的迭代器
	 * @return	迭代器
	 */
	public Iterator<StateSequence> transitionIterator() {
		return transitionCountMap.keySet().iterator();
	}
	
	/**
	 * 返回给定状态序列的转移目标状态的迭代器
	 * @param stateSequence	给定的状态序列
	 * @return				目标状态的迭代器
	 */
	public Iterator<State> iterator(StateSequence stateSequence) {
		return transitionCountMap.get(stateSequence).keyIterator();
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
		return emissionCountMap.get(state).keyIterator();
	}
	
	public EmissionCountEntry get(State state) {
		if(contain(state))
			return emissionCountMap.get(state);
		
		return null;
	}
	
	public TransitionCountEntry get(StateSequence stateSequence) {
		if(contain(stateSequence))
			return transitionCountMap.get(stateSequence);
		
		return null;
	}
	
	/**
	 * 返回是否包含状态序列
	 * @param sequence	待判断的状态序列
	 * @return			true-包含/false-不包含
	 */
	public boolean contain(StateSequence sequence) {
		return transitionCountMap.containsKey(sequence);
	}
	
	/**
	 * 返回是否包含以state为发射的状态的记录
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

	/**
	 * 删除数量小于cutoff的元素
	 * @param cutoff	阈值
	 */
	public void cutoff(int cutoff) {
		//对transitionCountMap删剪
		for(Iterator<Entry<StateSequence, TransitionCountEntry>> mapIt = transitionCountMap.entrySet().iterator(); mapIt.hasNext();) {
		    Entry<StateSequence, TransitionCountEntry> entrys = mapIt.next();
		    TransitionCountEntry transitionEntry = entrys.getValue();
		    
		    for(Iterator<Entry<State, Long>> entryIt = transitionEntry.entryIterator(); entryIt.hasNext();) {
		    	 Entry<State, Long> stateEntry = entryIt.next();
		    	 if(stateEntry.getValue() < cutoff) {	    		 
		    		 transitionEntry.setCount(transitionEntry.getCount() - stateEntry.getValue());
		    		 entryIt.remove();
		    	 }
		    }
		    
		    if(transitionEntry.size() == 0)
		    	mapIt.remove();
		}
		
		//对emissionCountMap删剪
		for(Iterator<Entry<State, EmissionCountEntry>> mapIt = emissionCountMap.entrySet().iterator(); mapIt.hasNext();) {
		    Entry<State, EmissionCountEntry> entrys = mapIt.next();
		    EmissionCountEntry emissionEntry = entrys.getValue();
		    
		    for(Iterator<Entry<Observation, Long>> entryIt = emissionEntry.entryIterator(); entryIt.hasNext();) {
		    	 Entry<Observation, Long> observationEntry = entryIt.next();
		    	 if(observationEntry.getValue() < cutoff) {
		    		 emissionEntry.setCount(emissionEntry.getCount() - observationEntry.getValue());
		    		 entryIt.remove();
		    	 }
		    }
		    
		    if(emissionEntry.size() == 0)
		    	mapIt.remove();
		}
	}
}