package hust.tools.hmm.model;

import hust.tools.hmm.utils.Observation;
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
	 * 返回给定转移的转移概率
	 * @param start		起始隐藏状态
	 * @param target	目的隐藏状态
	 * @return			转移概率
	 */
	public double transitionProb(StateSequence start, State target);
	
	/**
	 * 返回给定发射的发射概率
	 * @param state			隐藏状态
	 * @param observation	观测状态
	 * @return				发射概率
	 */
	public double emissionProb(State state, Observation observation);
		
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

	public State getState(int index);

	public int statesCount();

	public double getPi(int i);

	public int getObservationIndex(Observation observation);

	public double emissionProb(int i, int j);

	public double transitionProb(int i, int j);
	
}