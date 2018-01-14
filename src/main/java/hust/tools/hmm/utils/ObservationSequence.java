package hust.tools.hmm.utils;

import java.util.Arrays;
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
	
	private Observation[] observations;
	
	public ObservationSequence() {
		observations = null;
	}
	
	public ObservationSequence(Observation[] observations) {
		this.observations = observations;
	}
	
	public ObservationSequence(List<Observation> observations) {
		if(observations.size() == 0)
			throw new IllegalArgumentException("观测列表不能为空");
		
		this.observations = observations.toArray(new Observation[observations.size()]);
	}
	
	public ObservationSequence(Observation observation) {
		this.observations = new Observation[]{observation};
	}

	@Override
	public ObservationSequence add(Observation observation) {
		if(observations == null)
			return new ObservationSequence(observation);
		
		Observation[] arr = new Observation[length() + 1];
		int i = 0;
		for(i = 0; i < length(); i++)
			arr[i] = observations[i];
		
		arr[i] = observation;
		
		return new ObservationSequence(arr);
	}
	
	@Override
	public ObservationSequence add(Observation[] observations) {
		if(observations == null)
			return new ObservationSequence(observations);
		
		Observation[] arr = new Observation[length() + observations.length];
		int i = 0;
		for(i = 0; i < length(); i++)
			arr[i] = observations[i];
		
		for(int j = 0; j < observations.length; j++)
			arr[i++] = observations[j];
		
		return new ObservationSequence(arr);
	}

	@Override
	public ObservationSequence remove(int index) {
		if(length() <= 1)
			return null;
		
		Observation[] arr = new Observation[length() - 1];
		for(int i = 0; i < arr.length; i++)
			arr[i] = observations[i];
		
		return new ObservationSequence(arr);
	}

	@Override
	public ObservationSequence set(Observation observation, int index) {
		if(index < 0 || index >= length())
			throw new IllegalArgumentException("");
		
		Observation[] arr = new Observation[length()];
		if(index >= 0 && index < length()) {
			for(int i = 0; i < length(); i++) {
				if(i == index)
					arr[i] = observation;
				else
					arr[i] = observations[i];
			}
		}
		
		return new ObservationSequence(arr);
	}
	
	@Override
	public Observation get(int index) {
		if(index >= 0 && index < length())
			return observations[index];
		
		return null;
	}

	@Override
	public List<Observation> asList() {
		if(length() != 0)
			return Arrays.asList(observations);
		
		return null;
	}
	
	@Override
	public Observation[] toArray() {
		if(length() != 0)
			return observations;
		
		return null;
	}
	
	@Override
	public int length() {
		if(observations != null)
			return observations.length;
		
		return 0;		
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(observations);
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
		if (!Arrays.equals(observations, other.observations))
			return false;
		return true;
	}

	@Override
	public String toString() {
		String string = "";
		for(Observation observation : observations)
			string += observation + "  ";
		
		return string.trim();
	}
}
