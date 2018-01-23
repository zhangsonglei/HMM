package hust.tools.hmm.model;

import java.io.Serializable;
import java.util.HashMap;

import hust.tools.hmm.utils.Dictionary;
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
public interface HMModel extends Serializable, Cloneable {
	
	/**
	 * 返回给定隐藏状态的初始转移概率的对数
	 * @param i	隐藏状态
	 * @return	初始转移概率
	 */
	public double getLogPi(State i);
	
	/**
	 * 返回给定隐藏状态的初始转移概率的对数
	 * @param i	隐藏状态索引
	 * @return	初始转移概率
	 */
	public double getLogPi(int i);
	
	/**
	 * 返回给定转移的转移概率对数
	 * @param i	起始隐藏状态索引
	 * @param j	目的隐藏状态索引
	 * @return	转移概率对数
	 */
	public double transitionLogProb(int i, int j);
	
	/**
	 * 返回给定转移的转移概率对数
	 * @param start		起始隐藏状态
	 * @param target	目的隐藏状态
	 * @return			转移概率对数
	 */
	public double transitionLogProb(StateSequence start, State target);
	
	/**
	 * 返回给定发射的发射概率对数
	 * @param state			隐藏状态
	 * @param observation	观测状态
	 * @return				发射概率对数
	 */
	public double emissionLogProb(State state, Observation observation);
	
	/**
	 * 返回给定发射的发射概率对数
	 * @param i	隐藏状态索引
	 * @param t	观测状态索引
	 * @return	发射概率对数
	 */
	public double emissionLogProb(int i, int t);
	
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

	/**
	 * 返回索引对应的隐藏状态
	 * @param index	隐藏状态索引
	 * @return		隐藏状态
	 */
	public State getState(int index);

	/**
	 * 返回隐藏状态类型数量
	 * @return	隐藏状态类型数量
	 */
	public int statesCount();
	
	public int observationsCount();

	/**
	 * 返回观测状态的索引
	 * @param observation	观测状态
	 * @return				观测状态的索引
	 */
	public int getObservationIndex(Observation observation);	
	
	/**
	 * 返回模型的阶数
	 * @return	模型的阶数
	 */
	public int getOrder();
	
	/**
	 * 返回状态和观测的索引
	 * @return	状态和观测的索引
	 */
	public Dictionary getDict();
	
	/**
	 * 返回初始转移概率
	 * @return	初始转移概率
	 */
	public HashMap<State, Double> getPi();

	/**
	 * 返回转移概率矩阵
	 * @return	转移概率矩阵
	 */
	public HashMap<StateSequence, ARPAEntry> getTransitionMatrix();

	/**
	 * 返回发射概率矩阵
	 * @return	发射概率矩阵
	 */
	public HashMap<State, EmissionProbEntry> getEmissionMatrix();
	
	public HMModel clone() throws CloneNotSupportedException;
}
