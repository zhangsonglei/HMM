package hust.tools.hmm.learn;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import hust.tools.hmm.utils.Observation;

/**
 *<ul>
 *<li>Description: 记录发射的目标观测状态数量
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月8日
 *</ul>
 */
public class EmissionCountEntry {

	/**
	 * 发射的起始状态数量
	 */
	private long total;
	
	/**
	 * 发射到某个观测状态的数量
	 */
	private HashMap<Observation, Long> emissionCount;
	
	public EmissionCountEntry() {
		total = 0;
		emissionCount = new HashMap<>();
	}
	
	public EmissionCountEntry(HashMap<Observation, Long> emissionCount) {
		total = 0;
		this.emissionCount = emissionCount;
	}
	
	/**
	 * 返回发射的起始状态总数量
	 * @return	起始状态总数量
	 */
	public long getTotal() {
		return total;
	}
	
	/**
	 * 增加一个发射的目标观测状态
	 * @param observation	待增加的目标观测状态
	 */
	public void add(Observation observation) {
		if(emissionCount.containsKey(observation))
			emissionCount.put(observation, emissionCount.get(observation) + 1);
		else
			emissionCount.put(observation, 1L);
		
		total++;
	}
	
	/**
	 * 返回发射到目标观测状态observation的数量
	 * @param observation	目标观测状态
	 * @return				数量大小
	 */
	public long getEmissionCount(Observation observation) {
		if(emissionCount.containsKey(observation))
			return emissionCount.get(observation);
		else
			return 0;
	}
	
	/**
	 * 返回该发射是否包含目标观测状态observation
	 * @param observation	目标观测状态
	 * @return				true-包含/false-不包含
	 */
	public boolean contain(Observation observation) {
		return emissionCount.containsKey(observation);
	}
	
	/**
	 * 返回该发射的所有目标观测状态迭代器
	 * @return	该发射的所有目标观测状态迭代器
	 */
	public Iterator<Entry<Observation, Long>> entryIterator() {
		return emissionCount.entrySet().iterator();
	}
	
	/**
	 * 返回该发射的所有目标观测状态迭代器
	 * @return	该发射的所有目标观测状态迭代器
	 */
	public Iterator<Observation> observationsIterator() {
		return emissionCount.keySet().iterator();
	}
	
	/**
	 * 返回该发射的所有目标观测状态集合
	 * @return	该发射的所有目标观测状态集合
	 */
	public Set<Observation> getObservations() {
		return emissionCount.keySet();
	}
	
	/**
	 * 删除目标观测状态
	 * @param state	待删除的目标观测状态
	 */
	public void remove(Observation observation) {
		if(contain(observation)) {
			total -= getEmissionCount(observation);
			emissionCount.remove(observation);
		}
	}
	
	/**
	 * 返回该发射的目标观测状态种类数量
	 * @return	该发射的目标观测状态种类数量
	 */
	public int size() {
		return emissionCount.size();
	}
}
