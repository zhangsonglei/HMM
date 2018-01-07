package hust.tools.hmm.model;

import hust.tools.hmm.utils.StateSequence;
import hust.tools.hmm.utils.Observation;
import hust.tools.hmm.utils.ObservationSequence;
import hust.tools.hmm.utils.State;

/**
 *<ul>
 *<li>Description: 隐式马尔科夫模型接口， 给出了模型的功能 
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
	 * 返回给定观测状态序列的k个最优隐藏序列(优先顺序从大到小)
	 * @param observations	观测状态序列
	 * @param k				返回状态序列的个数
	 * @return				k个最优隐藏序列
	 */
	public StateSequence[] bestKStateSeqence(ObservationSequence observations, int k);
	
	/**
	 * 返回给定观测状态序列的最大似然估计
	 * @param observation	观测状态序列
	 * @return	最大似然估计
	 */
	public double getProb(ObservationSequence observations);
	
	/**
	 * 返回给定隐藏状态的初始转移概率
	 * @param i	隐藏状态
	 * @return	初始转移概率
	 */
	public double getPi(State i);
	
	/**
	 * 返回给定观测状态序列和隐藏状态序列在模型中的概率
	 * @param observation	观测状态序列
	 * @param hidden		隐藏状态序列
	 * @return
	 */
	public double getProb(ObservationSequence observations, StateSequence states);

	/**
	 * 返回隐藏状态i到j的转移概率
	 * @param i	起始隐藏状态
	 * @param j	目的隐藏状态
	 * @return	转移概率
	 */
	public double transitionProb(State i, State j);
	
	public double transitionProb(State[] i, State j);
	
	/**
	 * 返回隐藏状态i到观测状态t的发射概率
	 * @param i	隐藏状态
	 * @param t	观测状态
	 * @return	发射概率
	 */
	public double emissionProb(State i, Observation t);
	
	public double emissionProb(State[] i, Observation t);
	
	/**
	 * 返回所有观测状态
	 * @return	观测状态
	 */
	public Observation[] getObservationStates();
	
	/**
	 * 返回所有隐藏状态
	 * @return	隐藏状态
	 */
	public State[] getStates();
}
