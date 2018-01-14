package hust.tools.hmm.io;

import hust.tools.hmm.model.ARPAEntry;
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
public class EmissionEntry {
	
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
	private ARPAEntry entry;
	
	public EmissionEntry(State state, Observation observation, ARPAEntry entry) {
		this.state = state;
		this.observation = observation;
		this.entry = entry;
	}

	public State getState() {
		return state;
	}
	
	public Observation getObservation() {
		return observation;
	}

	public ARPAEntry getEntry() {
		return entry;
	}

	@Override
	public String toString() {		
		return state + "\t" + observation + "\t" + entry;
	}
}
