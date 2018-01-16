package hust.tools.hmm.io;

import java.io.Serializable;

import hust.tools.hmm.utils.State;

/**
 *<ul>
 *<li>Description: 初始转移概率条目
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月14日
 *</ul>
 */
public class PiEntry implements Serializable {
	
	/**
	 * 版本序列号
	 */
	private static final long serialVersionUID = 2024944700684632862L;

	/**
	 * 转移到的目标状态
	 */
	private State state;
	
	/**
	 * 转移的概率及回退权重
	 */
	private double logProb;
	
	public PiEntry(State state, double logProb) {
		this.state = state;
		this.logProb = logProb;
	}

	public State getState() {
		return state;
	}
	
	public double getLogProb() {
		return logProb;
	}

	public String toString() {
		return state + "\t" + logProb;
	}
}
