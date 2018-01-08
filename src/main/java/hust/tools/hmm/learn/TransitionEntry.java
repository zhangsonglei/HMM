package hust.tools.hmm.learn;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import hust.tools.hmm.utils.State;

/**
 *<ul>
 *<li>Description: 记录转移的目标状态数量
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月8日
 *</ul>
 */
public class TransitionEntry {
	
	/**
	 * 转移的起始状态数量
	 */
	private long count;
	
	/**
	 * 转移到某个状态的数量
	 */
	private HashMap<State, Long> transitionCount;
	
	public TransitionEntry() {
		transitionCount = new HashMap<>();
	}
	
	public TransitionEntry(long count, HashMap<State, Long> transitionCount) {
		this.count = count;
		this.transitionCount = transitionCount;
	}
	
	/**
	 * 设置转移的起始状态数量
	 * @param count	数量大小
	 * @return		更新数量后的实体
	 */
	public TransitionEntry setCount(long count) {
		this.count = count;
		
		return this;
	}
	
	/**
	 * 返回转移的起始状态数量
	 * @return	数量大小
	 */
	public long getCount() {
		return count;
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
	 * 返回是否包含目标状态state
	 * @param state	转移目标状态
	 * @return		true-包含/false-不包含
	 */
	public boolean contain(State state) {
		return transitionCount.containsKey(state);
	}
	
	/**
	 * 返回转移状态计数的迭代器
	 * @return	迭代器
	 */
	public Iterator<Entry<State, Long>> iterator() {
		return transitionCount.entrySet().iterator();
	}
	
	/**
	 * 删除目标状态
	 * @param state	待删除的目标状态
	 */
	public void remove(State state) {
		if(contain(state))
			transitionCount.remove(state);
	}
}
