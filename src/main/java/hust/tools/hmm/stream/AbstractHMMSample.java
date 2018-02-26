package hust.tools.hmm.stream;

import hust.tools.hmm.utils.Observation;
import hust.tools.hmm.utils.ObservationSequence;

/**
 *<ul>
 *<li>Description: 抽象HMM样本类，只包含观测状态序列，可被子类继承，增加隐藏状态序列 
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月1日
 *</ul>
 */
public abstract class AbstractHMMSample {
	
	protected ObservationSequence observationSequence;
	
	public AbstractHMMSample() {
		observationSequence = new ObservationSequence();
	}
	
	public AbstractHMMSample(ObservationSequence observationSequence) {
		this.observationSequence = observationSequence;
	}
	
	public ObservationSequence getObservationSequence() {
		return observationSequence;
	}
	
	public void add(Observation observation) {
		observationSequence = observationSequence.addLast(observation);
	}
	
	public Observation getObservationState(int i) {
		if(i >= 0 && i < observationSequence.length())
			return observationSequence.get(i);
		
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((observationSequence == null) ? 0 : observationSequence.hashCode());
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
		AbstractHMMSample other = (AbstractHMMSample) obj;
		if (observationSequence == null) {
			if (other.observationSequence != null)
				return false;
		} else if (!observationSequence.equals(other.observationSequence))
			return false;
		return true;
	}

	@Override
	public abstract String toString();
}
