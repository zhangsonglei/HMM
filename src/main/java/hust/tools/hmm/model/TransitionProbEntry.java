package hust.tools.hmm.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import hust.tools.hmm.utils.State;

/**
 *<ul>
 *<li>Description: 记录转移的目标状态概率及回退权重
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月10日
 *</ul>
 */
public class TransitionProbEntry {
	
	/**
	 * 转移到某个状态的数量
	 */
	private HashMap<State, ARPAEntry> transitionProb;
	
	public TransitionProbEntry() {
		transitionProb = new HashMap<>();
	}
	
	public TransitionProbEntry(HashMap<State, ARPAEntry> transitionCount) {
		this.transitionProb = transitionCount;
	}
	
	/**
	 * 增加或修改给定目标转移状态的概率及回退权重
	 * @param state	转移到的目标状态
	 */
	public void put(State state, ARPAEntry entry) {
		transitionProb.put(state, entry);
	}
	
	/**
	 * 返回给定目标转移状态的概率及回退权重
	 * @param state	转移到的目标状态
	 * @return		概率及回退权重
	 */
	public ARPAEntry get(State state) {
		if(contain(state)) 
			return transitionProb.get(state);
		
		return null;
	}
	
	/**
	 * 返回转移到目标状态state的概率的对数
	 * @param state	转移到的目标状态
	 * @return		概率的对数
	 */
	public double getTransitionLogProb(State state) {
		if(contain(state))
			return transitionProb.get(state).getLog_prob();
		else
			return 0;
	}
	
	/**
	 * 返回给定转移到目标状态的回退权重的对数
	 * @param state	转移到的目标状态
	 * @return		回退权重的对数
	 */
	public double getTransitionLogBow(State state) {
		if(contain(state))
			return transitionProb.get(state).getLog_bo();
		else
			return 0;
	}
	
	/**
	 * 返回是否包含转移目标状态state
	 * @param state	转移目标状态
	 * @return		true-包含/false-不包含
	 */
	public boolean contain(State state) {
		return transitionProb.containsKey(state);
	}
	
	/**
	 * 返回转移状态概率的迭代器
	 * @return	迭代器
	 */
	public Iterator<Entry<State, ARPAEntry>> entryIterator() {
		return transitionProb.entrySet().iterator();
	}
	
	/**
	 * 返回转移目标状态的迭代器
	 * @return	迭代器
	 */
	public Iterator<State> keyIterator() {
		return transitionProb.keySet().iterator();
	}
	
	/**
	 * 删除目标状态
	 * @param state	待删除的目标状态
	 */
	public void remove(State state) {
		if(contain(state))
			transitionProb.remove(state);
	}
	
	/**
	 * 返回转移的种类数量
	 * @return	转移的种类数量
	 */
	public int size() {
		return transitionProb.size();
	}
}
