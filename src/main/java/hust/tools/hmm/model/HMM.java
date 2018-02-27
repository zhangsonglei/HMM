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
	public StateSequence bestStateSeqence(ObservationSequence observations);
	
	/**
	 * 返回给定观测状态序列的概率的对数
	 * @param observation	观测状态序列
	 * @return				概率的对数
	 */
	public double getLogProb(ObservationSequence observations);
	
	/**
	 * 返回给定观测状态序列和隐藏状态序列在模型中的概率的对数
	 * @param observation	观测状态序列
	 * @param states		隐藏状态序列
	 * @return				概率的对数
	 */
	public double getLogProb(ObservationSequence observations, StateSequence states);
}
