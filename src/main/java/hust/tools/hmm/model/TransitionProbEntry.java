package hust.tools.hmm.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import hust.tools.hmm.utils.State;

/**
 *<ul>
 *<li>Description: 记录转移的目标状态概率对数
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月10日
 *</ul>
 */
public class TransitionProbEntry {
	
	/**
	 * 转移到某个状态的概率的对数
	 */
	private HashMap<State, Double> transitionProb;
	
	public TransitionProbEntry() {
		transitionProb = new HashMap<>();
	}
	
	public TransitionProbEntry(HashMap<State, Double> transitionProb) {
		this.transitionProb = transitionProb;
	}
	
	/**
	 * 增加或修改给定目标转移状态的概率及回退权重
	 * @param state	转移到的目标状态
	 */
	public void put(State state, double logProb) {
		transitionProb.put(state, logProb);
	}
	
	/**
	 * 返回转移到目标状态state的概率的对数
	 * @param state	转移到的目标状态
	 * @return		概率的对数
	 */
	public double getTransitionLogProb(State state) {
		if(transitionProb.containsKey(state))
			return transitionProb.get(state);

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
	public Iterator<Entry<State, Double>> entryIterator() {
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
	 * 返回转移的种类数量
	 * @return	转移的种类数量
	 */
	public int size() {
		return transitionProb.size();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((transitionProb == null) ? 0 : transitionProb.hashCode());
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
		TransitionProbEntry other = (TransitionProbEntry) obj;
		if (transitionProb == null) {
			if (other.transitionProb != null)
				return false;
		} else if (!transitionProb.equals(other.transitionProb))
			return false;
		return true;
	}
}
