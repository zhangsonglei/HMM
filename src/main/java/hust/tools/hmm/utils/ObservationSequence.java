package hust.tools.hmm.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *<ul>
 *<li>Description: 观测序列
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2017年12月26日
 *</ul>
 */
public class ObservationSequence implements Sequence<Observation> {
	
	private List<Observation> observations;
	
	public ObservationSequence() {
		observations = new ArrayList<>();
	}
	
	public ObservationSequence(Observation[] observations) {
		this.observations = new ArrayList<>();

		for(Observation observation : observations)
			this.observations.add(observation);
	}
	
	public ObservationSequence(List<Observation> observations) {
		this.observations = observations;
	}
	
	public ObservationSequence(Observation observation) {
		observations = new ArrayList<>();
		observations.add(observation);
	}

	@Override
	public void add(Observation observation) {
		observations.add(observation);
	}
	
	@Override
	public void add(Observation[] observations) {
		for(Observation observation : observations)
			this.observations.add(observation);
	}

	@Override
	public void remove(int index) {
		if(index >= 0 && index < observations.size() - 1)
			observations.remove(index);
	}
	
	@Override
	public void update(Observation token, int index) {
		if(index >= 0 && index < observations.size() - 1)
			observations.set(index, token);
	}

	@Override
	public Observation get(int index) {
		if(index >= 0 && index < observations.size())
			return observations.get(index);
		
		return null;
	}

	@Override
	public List<Observation> get() {
		return observations;
	}

	@Override
	public Iterator<Observation> iterator() {
		return observations.iterator();
	}
	
	@Override
	public int size() {
		return observations.size();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((observations == null) ? 0 : observations.hashCode());
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
		ObservationSequence other = (ObservationSequence) obj;
		if (observations == null) {
			if (other.observations != null)
				return false;
		} else if (!observations.equals(other.observations))
			return false;
		return true;
	}

	@Override
	public String toString() {
		String string = "";
		for(Observation observation : observations)
			string += observation;
		
		return string;
	}
}
