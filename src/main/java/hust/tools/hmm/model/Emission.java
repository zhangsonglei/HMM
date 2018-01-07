package hust.tools.hmm.model;

import java.util.Arrays;
import java.util.List;

import hust.tools.hmm.utils.Observation;
import hust.tools.hmm.utils.State;

/**
 *<ul>
 *<li>Description: 隐藏状态到观测状态的发射实体 
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月7日
 *</ul>
 */
public class Emission {
	
	private State[] states;
	
	private Observation observation;
	
	public Emission(State state, Observation observation) {
		this(new State[]{state}, observation);
	}
	
	public Emission(List<State> states, Observation observation) {
		this(states.toArray(new State[states.size()]), observation);
	}
	
	public Emission(State[] states, Observation observation) {
		this.states = states;
		this.observation = observation;
	}

	public static long count(int[] numbers, int  k){
        int total = 0;
        int counter = 0;
        for (int i : numbers){
            if (i < k){
                total += 1 + solver(numbers, k, i, counter +1);
            }
            counter++;
        }
        return total;
    }

    public static long solver (int[] numbers, int k, int sofar, int index){
        if (numbers.length == index){
            return 0;
        } else{
            sofar = sofar * numbers[index];
            if (sofar < k){
                return 1 + solver(numbers, k, sofar, index+1);
            }else{
                return 0;
                // return solver(numbers, k, sofar, index+1, path + numbers[index] +" x ");
            }
        }
    }
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((observation == null) ? 0 : observation.hashCode());
		result = prime * result + Arrays.hashCode(states);
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
		Emission other = (Emission) obj;
		if (observation == null) {
			if (other.observation != null)
				return false;
		} else if (!observation.equals(other.observation))
			return false;
		if (!Arrays.equals(states, other.states))
			return false;
		return true;
	}

	public String toString() {
		String string = "[";
		for(State state : states) {
			string += state + "  ";
		}
		
		return string.trim() + "]" + "->" + observation;
	}
}
