package hust.tools.hmm.utils;

import java.io.Serializable;

/**
 *<ul>
 *<li>Description: 观测接口
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月3日
 *</ul>
 */
public interface Observation extends Serializable {
	
	/**
	 * 返回观测状态的哈希码，该哈希码只计算状态不包含索引
	 * @return	哈希码
	 */
	@Override
	public int hashCode();

	/**
	 * 比较两个观测状态是否相等，只比较状态不比较索引
	 * @param object	待比较的观测状态
	 * @return			true-相等/false-不想等
	 */
	@Override
	public boolean equals(Object object);
	
	/**
	 * 返回观测状态的字符串类型，不包含索引
	 * @return	字符串
	 */
	@Override
	public String toString();
}
