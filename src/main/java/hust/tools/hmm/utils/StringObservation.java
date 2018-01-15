package hust.tools.hmm.utils;

import hust.tools.hmm.utils.Observation;

/**
 *<ul>
 *<li>Description: 字符串型观测状态 
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2017年12月26日
 *</ul>
 */
public class StringObservation implements Observation {

	/**
	 * 版本序列号
	 */
	private static final long serialVersionUID = -4250685459905341143L;
	
	private String observation;
	
	public StringObservation(String observation) {
		this.observation = observation;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((observation == null) ? 0 : observation.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StringObservation other = (StringObservation) obj;
		if (observation == null) {
			if (other.observation != null)
				return false;
		} else if (!observation.equals(other.observation))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return observation;
	}
}
