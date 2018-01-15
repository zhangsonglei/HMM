package hust.tools.hmm.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import hust.tools.hmm.utils.Observation;

/**
 *<ul>
 *<li>Description: 记录发射的目标观测状态概率及回退权重
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月10日
 *</ul>
 */
public class EmissionProbEntry {

	/**
	 * 发射到某个观测状态的概率及回退权重的对数
	 */
	private HashMap<Observation, Double> emissionProb;
	
	public EmissionProbEntry() {
		emissionProb = new HashMap<>();
	}
	
	public EmissionProbEntry(HashMap<Observation, Double> emissionProb) {
		this.emissionProb = emissionProb;
	}
	
	/**
	 * 增加或修改给定发射目标观测状态的概率及回退权重
	 * @param observation	发射到的目标观测状态
	 */
	public void put(Observation observation, double logProb) {
		emissionProb.put(observation, logProb);
	}
	
	/**
	 * 返回发射到目标状态observation的概率的对数
	 * @param observation	转移到的目标状态
	 * @return				概率的对数
	 */
	public double getEmissionLogProb(Observation observation) {
		if(contain(observation))
			return emissionProb.get(observation);
		else
			return 0;
	}
	
	/**
	 * 返回是否包含发射目标观测状态observation
	 * @param observation	转移目标状态
	 * @return				true-包含/false-不包含
	 */
	public boolean contain(Observation observation) {
		return emissionProb.containsKey(observation);
	}
	
	/**
	 * 返回发射的观测状态概率及权重的迭代器
	 * @return	迭代器
	 */
	public Iterator<Entry<Observation, Double>> entryIterator() {
		return emissionProb.entrySet().iterator();
	}
	
	/**
	 * 返回发射的目标观测状态的迭代器
	 * @return	迭代器
	 */
	public Iterator<Observation> keyIterator() {
		return emissionProb.keySet().iterator();
	}
	
	/**
	 * 删除目标观测状态
	 * @param state	待删除的目标观测状态
	 */
	public void remove(Observation observation) {
		if(contain(observation))
			emissionProb.remove(observation);
	}
	
	/**
	 * 返回发射的目标观测状态的类型数量
	 * @return	目标观测状态的类型数量
	 */
	public int size() {
		return emissionProb.size();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((emissionProb == null) ? 0 : emissionProb.hashCode());
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
		EmissionProbEntry other = (EmissionProbEntry) obj;
		if (emissionProb == null) {
			if (other.emissionProb != null)
				return false;
		} else if (!emissionProb.equals(other.emissionProb))
			return false;
		return true;
	}
}
