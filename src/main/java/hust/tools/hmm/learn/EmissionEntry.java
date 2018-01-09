package hust.tools.hmm.learn;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import hust.tools.hmm.utils.Observation;

/**
 *<ul>
 *<li>Description: 记录发射的目标观测状态数量
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月8日
 *</ul>
 */
public class EmissionEntry {

	/**
	 * 发射的起始状态数量
	 */
	private long count;
	
	/**
	 * 发射到某个观测状态的数量
	 */
	private HashMap<Observation, Long> emissionCount;
	
	public EmissionEntry() {
		emissionCount = new HashMap<>();
	}
	
	public EmissionEntry(long count, HashMap<Observation, Long> emissionCount) {
		this.count = count;
		this.emissionCount = emissionCount;
	}
	
	/**
	 * 设置发射的起始状态数量
	 * @param count	数量大小
	 * @return		更新数量后的实体
	 */
	public EmissionEntry setCount(long count) {
		this.count = count;
		
		return this;
	}
	
	/**
	 * 返回发射的起始状态数量
	 * @return	数量大小
	 */
	public long getCount() {
		return count;
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
	 * 返回是否包含目标观测状态observation
	 * @param observation	目标观测状态
	 * @return				true-包含/false-不包含
	 */
	public boolean contain(Observation observation) {
		return emissionCount.containsKey(observation);
	}
	
	/**
	 * 返回发射观测计数的迭代器
	 * @return	迭代器
	 */
	public Iterator<Entry<Observation, Long>> iterator() {
		return emissionCount.entrySet().iterator();
	}
	
	/**
	 * 删除目标观测状态
	 * @param state	待删除的目标观测状态
	 */
	public void remove(Observation observation) {
		if(contain(observation))
			emissionCount.remove(observation);
	}
	
	/**
	 * 返回发射的种类数量
	 * @return	发射的种类数量
	 */
	public int size() {
		return emissionCount.size();
	}
}
