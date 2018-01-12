package hust.tools.hmm.utils;

/**
 *<ul>
 *<li>Description: 状态接口
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2017年12月27日
 *</ul>
 */
public interface State {
	
	/**
	 * 返回隐藏状态的哈希码，该哈希码只计算状态不包含索引
	 * @return	哈希码
	 */
	@Override
	public int hashCode();

	/**
	 * 比较两个隐藏状态是否相等，只比较状态不比较索引
	 * @param object	待比较的隐藏状态
	 * @return			true-相等/false-不想等
	 */
	@Override
	public boolean equals(Object object);
	
	/**
	 * 返回隐藏状态的字符串类型，不包括索引
	 * @return	字符串
	 */
	@Override
	public String toString();
}
