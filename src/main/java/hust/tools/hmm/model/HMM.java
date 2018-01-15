package hust.tools.hmm.model;

import hust.tools.hmm.utils.StateSequence;
import hust.tools.hmm.utils.ObservationSequence;

/**
 *<ul>
 *<li>Description: 隐式马尔科夫模型接口
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2017年12月26日
 *</ul>
 */
public interface HMM {
	
	/**
	 * 返回给定观测状态序列的最优隐藏序列
	 * @param observation	观测状态序列
	 * @return				最优隐藏序列
	 */
	public StateSequence bestStateSeqence(ObservationSequence observations, int order);
	
	/**
	 * 返回给定观测状态序列的k个最优隐藏序列(优先顺序从大到小)
	 * @param observations	观测状态序列
	 * @param k				返回状态序列的个数
	 * @return				k个最优隐藏序列
	 */
	public StateSequence[] bestKStateSeqence(ObservationSequence observations, int order, int k);
	
	/**
	 * 返回给定观测状态序列的最大似然估计
	 * @param observation	观测状态序列
	 * @return	最大似然估计
	 */
	public double getProb(ObservationSequence observations, int order);
	
	
}
