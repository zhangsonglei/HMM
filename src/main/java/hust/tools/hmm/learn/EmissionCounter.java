package hust.tools.hmm.learn;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import hust.tools.hmm.utils.State;

public class EmissionCounter {
	/**
	 * 发射的起始状态数量
	 */
	private int total;
	
	/**
	 * 发射到某个观测状态的数量
	 */
	private HashMap<State, Integer> emissionCount;
	
	public EmissionCounter() {
		total = 0;
		emissionCount = new HashMap<>();
	}
	
	/**
	 * 返回发射的起始状态总数量
	 * @return	起始状态总数量
	 */
	public int getTotal() {
		return total;
	}
	
	/**
	 * 增加一个发射的目标状态
	 * @param state	待增加的状态
	 */
	public void add(State state) {
		if(emissionCount.containsKey(state))
			emissionCount.put(state, emissionCount.get(state) + 1);
		else
			emissionCount.put(state, 1);
		
		total++;
	}
	
	/**
	 * 返回发射到目标状态state的数量
	 * @param state	目标观测状态
	 * @return				数量大小
	 */
	public int getStateCount(State state) {
		if(emissionCount.containsKey(state))
			return emissionCount.get(state);

		return 0;
	}
	
	/**
	 * 返回该发射是否包含目标观测状态state
	 * @param state	目标观测状态
	 * @return				true-包含/false-不包含
	 */
	public boolean contain(State state) {
		return emissionCount.containsKey(state);
	}
	
	/**
	 * 返回该发射的所有目标观测状态迭代器
	 * @return	该发射的所有目标观测状态迭代器
	 */
	public Iterator<Entry<State, Integer>> entryIterator() {
		return emissionCount.entrySet().iterator();
	}
	
	/**
	 * 返回该发射的所有目标观测状态迭代器
	 * @return	该发射的所有目标观测状态迭代器
	 */
	public Iterator<State> observationsIterator() {
		return emissionCount.keySet().iterator();
	}
	
	/**
	 * 返回该发射的所有目标观测状态集合
	 * @return	该发射的所有目标观测状态集合
	 */
	public Set<State> getObservations() {
		return emissionCount.keySet();
	}
	
	/**
	 * 返回该发射的目标观测状态种类数量
	 * @return	该发射的目标观测状态种类数量
	 */
	public int size() {
		return emissionCount.size();
	}
}