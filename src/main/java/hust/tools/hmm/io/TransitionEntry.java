package hust.tools.hmm.io;

import java.io.Serializable;

import hust.tools.hmm.utils.State;
import hust.tools.hmm.utils.StateSequence;

/**
 *<ul>
 *<li>Description: 转移条目
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月14日
 *</ul>
 */
public class TransitionEntry implements Serializable {
	
	/**
	 * 版本序列号
	 */
	private static final long serialVersionUID = -7083055836147200214L;

	/**
	 * 转移的条件
	 */
	private StateSequence start;
	
	/**
	 * 转移的的目标状态
	 */
	private State target;
	
	/**
	 * 转移的概率及回退权重
	 */
	private double logProb;
	
	public TransitionEntry(StateSequence start, State target, double logProb) {
		this.start = start;
		this.target = target;
		this.logProb = logProb;
	}
	
	public StateSequence getStart() {
		return start;
	}

	public State getTarget() {
		return target;
	}

	public double getLogProb() {
		return logProb;
	}

	@Override
	public String toString() {
		return start + "\t" + target + "\t" + logProb;
	}
}