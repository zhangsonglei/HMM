package hust.tools.hmm.learn;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import hust.tools.hmm.utils.State;

/**
 *<ul>
 *<li>Description: 记录转移的目标状态数量
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月8日
 *</ul>
 */
public class TransitionCountEntry {
	
	/**
	 * 转移的起始状态数量
	 */
	private long total;
	
	/**
	 * 转移到某个状态的数量
	 */
	private HashMap<State, Long> transitionCount;
	
	public TransitionCountEntry() {
		total = 0;
		transitionCount = new HashMap<>();
	}
	
	public TransitionCountEntry(HashMap<State, Long> transitionCount) {
		total = 0;
		this.transitionCount = transitionCount;
	}
	
	
	/**
	 * 返回转移的起始状态数量
	 * @return	数量大小
	 */
	public long getTotal() {
		return total;
	}
	
	/**
	 * 增加一个转移目标状态
	 * @param state	待增加的目标状态
	 */
	public void add(State state) {
		if(contain(state))
			transitionCount.put(state, transitionCount.get(state) + 1);
		else
			transitionCount.put(state, 1L);
		
		total++;
	}
	
	/**
	 * 返回转移到目标状态state的数量
	 * @param state	转移目标状态
	 * @return		数量大小
	 */
	public long getTransitionCount(State state) {
		if(contain(state))
			return transitionCount.get(state);
		else
			return 0;
	}
	
	/**
	 * 返回该转移是否包含目标状态state
	 * @param state	转移目标状态
	 * @return		true-包含/false-不包含
	 */
	public boolean contain(State state) {
		return transitionCount.containsKey(state);
	}
	
	/**
	 * 返回该转移的目标状态计数的迭代器
	 * @return	该转移的目标状态计数的迭代器
	 */
	public Iterator<Entry<State, Long>> entryIterator() {
		return transitionCount.entrySet().iterator();
	}
	
	/**
	 * 返回该转移的所有目标状态迭代器
	 * @return	该转移的所有目标状态迭代器
	 */
	public Iterator<State> statesIterator() {
		return transitionCount.keySet().iterator();
	}
	
	/**
	 * 返回该转移的所有目标状态集合
	 * @return	该转移的所有目标状态集合
	 */
	public Set<State> getStates() {
		return transitionCount.keySet();
	}
	
	/**
	 * 删除目标状态
	 * @param state	待删除的目标状态
	 */
	public void remove(State state) {
		if(contain(state)) {
			total -= getTransitionCount(state);
			transitionCount.remove(state);
		}
	}
	
	/**
	 * 返回该转移的目标状态种类数量
	 * @return	该转移的目标状态种类数量
	 */
	public int size() {
		return transitionCount.size();
	}
}
