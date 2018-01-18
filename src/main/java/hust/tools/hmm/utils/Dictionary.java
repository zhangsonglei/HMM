package hust.tools.hmm.utils;

import java.util.HashMap;
import java.util.Set;

/**
 *<ul>
 *<li>Description: 观测/状态的索引类
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月4日
 *</ul>
 */
public class Dictionary {
	
	private static int observation_index;						//动态递增，为观测状态赋索引值
	private HashMap<Observation, Integer> observationToIndex;	//观测状态及其索引的映射
	private HashMap<Integer, Observation> indexToObservation;	//索引指向的观测状态
	
	private static int state_index;								//动态递增，为隐藏状态赋索引值
	private HashMap<State, Integer> stateToIndex;				//隐藏状态及其索引的映射
	private HashMap<Integer, State> indexToState;				//索引指向的隐藏状态
	
	public Dictionary() {
		observationToIndex = new HashMap<>();
		indexToObservation = new HashMap<>();
		stateToIndex = new HashMap<>();
		indexToState = new HashMap<>();
		state_index = 0;
		observation_index = 0;
	}
	
	/**
	 * 增加一个隐藏状态，返回加上索引的隐藏状态
	 * @param state	增加的隐藏状态
	 * @return		加上索引的隐藏状态
	 */
	private void add(State state) {
		if(!containState(state)) {
			stateToIndex.put(state, state_index);
			indexToState.put(state_index, state);
			
			state_index++;
		}
	}
	
	/**
	 * 增加一个隐藏状态索引
	 * @param state	隐藏状态
	 * @param index	对应索引
	 */
	public void put(State state, int index) {
		stateToIndex.put(state, index);
		indexToState.put(index, state);
	}

	/**
	 * 增加一个观测状态索引
	 * @param observation	观测状态
	 * @param index			对应索引
	 */
	public void put(Observation observation, int index) {
		observationToIndex.put(observation, index);
		indexToObservation.put(index, observation);
	}
	
	/**
	 * 增加一系列隐藏状态，返回加上索引的隐藏状态序列
	 * @param state	增加的一系列隐藏状态
	 * @return		加上索引的隐藏状态序列
	 */
	public void add(StateSequence sequence) {
		for(int i = 0; i < sequence.length(); i++) 
			add(sequence.get(i));
	}
	
	/**
	 * 增加一个观测状态，返回加上索引的观测状态
	 * @param state	增加的观测状态
	 * @return		加上索引的观测状态序列
	 */
	private void add(Observation observation) {
		if(!containObservation(observation)) {
			observationToIndex.put(observation, observation_index);
			indexToObservation.put(observation_index, observation);
			observation_index++;
		}
	}
	
	/**
	 * 增加一系列观测状态，返回加上索引的观测状态序列
	 * @param state	增加的一系列观测状态
	 * @return		加上索引的观测状态序列
	 */
	public void add(ObservationSequence observations) {	
		for(int i = 0; i < observations.length(); i++)
			add(observations.get(i));
	}
	
	/**
	 * 返回给定索引对应隐藏状态
	 * @param index	给定索引
	 * @return		给定索引对应隐藏状态
	 */
	public State getState(int index) {
		if(indexToState.containsKey(index))
			return indexToState.get(index);
		
		return null;
	}
	
	/**
	 * 返回给定隐藏状态的索引
	 * @param state	待求索引的隐藏状态
	 * @return		索引
	 */
	public int getIndex(State state) {
		if(stateToIndex.containsKey(state))
			return stateToIndex.get(state);
		
		return -1;
	}
	
	/**
	 * 返回给定索引对应观测状态
	 * @param index	给定索引
	 * @return		给定索引对应观测状态
	 */
	public Observation getObservation(int index) {
		if(indexToObservation.containsKey(index))
			return indexToObservation.get(index);
		
		return null;
	}
	
	/**
	 * 返回给定观测状态的索引
	 * @param state	待求索引的观测状态
	 * @return		索引
	 */
	public int getIndex(Observation observation) {
		if(observationToIndex.containsKey(observation))
			return observationToIndex.get(observation);
		
		return -1;
	}
	
	/**
	 * 返回隐藏状态的集合
	 * @return	隐藏状态的集合
	 */
	public Set<State> getStates() {
		return stateToIndex.keySet();
	}
	
	/**
	 * 返回隐藏状态的类型数
	 * @return	隐藏状态的类型数
	 */
	public int stateCount() {
		return stateToIndex.size();
	}
	
	/**
	 * 判断是否包含给定隐藏状态
	 * @param state	待判断的隐藏状态
	 * @return		true-包含/false-不包含
	 */
	public boolean containState(State state) {
		return stateToIndex.containsKey(state);
	}
	
	/**
	 * 判断是否包含给定索引对应的隐藏状态
	 * @param index
	 * @return
	 */
	public boolean containState(int index) {
		return indexToState.containsKey(index);
	}
	
	/**
	 * 返回观测状态的集合
	 * @return	观测状态的集合
	 */
	public Set<Observation> getObservations() {
		return observationToIndex.keySet();
	}

	/**
	 * 返回观测状态的类型数
	 * @return	观测状态的类型数
	 */
	public int observationCount() {
		return observationToIndex.size();
	}
	
	/**
	 * 判断是否包含给定观测状态
	 * @param state	待判断的观测状态
	 * @return		true-包含/false-不包含
	 */
	public boolean containObservation(Observation observation) {
		return observationToIndex.containsKey(observation);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((indexToObservation == null) ? 0 : indexToObservation.hashCode());
		result = prime * result + ((indexToState == null) ? 0 : indexToState.hashCode());
		result = prime * result + ((observationToIndex == null) ? 0 : observationToIndex.hashCode());
		result = prime * result + ((stateToIndex == null) ? 0 : stateToIndex.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Dictionary other = (Dictionary) obj;
		if (indexToObservation == null) {
			if (other.indexToObservation != null)
				return false;
		} else if (!indexToObservation.equals(other.indexToObservation))
			return false;
		if (indexToState == null) {
			if (other.indexToState != null)
				return false;
		} else if (!indexToState.equals(other.indexToState))
			return false;
		if (observationToIndex == null) {
			if (other.observationToIndex != null)
				return false;
		} else if (!observationToIndex.equals(other.observationToIndex))
			return false;
		if (stateToIndex == null) {
			if (other.stateToIndex != null)
				return false;
		} else if (!stateToIndex.equals(other.stateToIndex))
			return false;
		return true;
	}
}
