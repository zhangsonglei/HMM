package hust.tools.hmm.model;

import hust.tools.hmm.utils.Observation;
import hust.tools.hmm.utils.ObservationSequence;
import hust.tools.hmm.utils.State;
import hust.tools.hmm.utils.StateSequence;

/**
 *<ul>
 *<li>Description: HMM模型接口，基于
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月15日
 *</ul>
 */
public interface HMModel {
	
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
	public double getProb(ObservationSequence observations, StateSequence states, int order);
	
	/**
	 * 返回隐藏状态i到j的转移概率
	 * @param i	起始隐藏状态
	 * @param j	目的隐藏状态
	 * @return	转移概率
	 */
	public double transitionProb(StateSequence start, State target);
	
	/**
	 * 返回隐藏状态i到观测状态t的发射概率
	 * @param i	隐藏状态
	 * @param t	观测状态
	 * @return	发射概率
	 */
	public double emissionProb(State i, Observation t);
		
	/**
	 * 返回所有观测状态
	 * @return	观测状态
	 */
	public Observation[] getObservations();
	
	/**
	 * 返回所有隐藏状态
	 * @return	隐藏状态
	 */
	public State[] getStates();
	
}
