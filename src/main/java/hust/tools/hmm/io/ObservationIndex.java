package hust.tools.hmm.io;

import java.io.Serializable;

import hust.tools.hmm.utils.Observation;

/**
 *<ul>
 *<li>Description: 字典条目 
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月14日
 *</ul>
 */
public class ObservationIndex implements Serializable {

	/**
	 * 版本序列号
	 */
	private static final long serialVersionUID = -5243301349045327300L;

	private Observation observation;
	
	private int index;
	
	public ObservationIndex(Observation observation, int index) {
		this.observation = observation;
		this.index = index;
	}

	public Observation getObservation() {
		return observation;
	}

	public int getIndex() {
		return index;
	}
	
	@Override
	public String toString() {
		return observation + "\t" + index;
	}
}
