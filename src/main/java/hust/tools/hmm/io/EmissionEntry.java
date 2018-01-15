package hust.tools.hmm.io;

import java.io.Serializable;

import hust.tools.hmm.utils.Observation;
import hust.tools.hmm.utils.State;

/**
 *<ul>
 *<li>Description: 发射条目 
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月14日
 *</ul>
 */
public class EmissionEntry implements Serializable {
	
	/**
	 * 版本序列号
	 */
	private static final long serialVersionUID = -2836043148116107814L;

	/**
	 * 发射的隐藏状态
	 */
	private State state;
	
	/**
	 * 发射的观测状态
	 */
	private Observation observation;
	
	/**
	 * 发射的概率和回退权重
	 */
	private double logProb;
	
	public EmissionEntry(State state, Observation observation, double logProb) {
		this.state = state;
		this.observation = observation;
		this.logProb = logProb;
	}

	public State getState() {
		return state;
	}
	
	public Observation getObservation() {
		return observation;
	}

	public double getLogProb() {
		return logProb;
	}

	@Override
	public String toString() {		
		return state + "\t" + observation + "\t" + logProb;
	}
}
