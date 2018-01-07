package hust.tools.hmm.model;

import java.util.Arrays;
import java.util.List;

import hust.tools.hmm.utils.State;

/**
 *<ul>
 *<li>Description: 隐藏状态到下一个隐藏状态的转移实体 
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月7日
 *</ul>
 */
public class Transition {
	
	private State[] starts;
	
	private State target;
	
	public Transition(State start, State target) {
		this(new State[]{start}, target);
	}
	
	public Transition(List<State> starts, State target) {
		this(starts.toArray(new State[starts.size()]), target);
	}
	
	public Transition(State[] starts, State target) {
		this.starts = starts;
		this.target = target;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(starts);
		result = prime * result + ((target == null) ? 0 : target.hashCode());
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
		Transition other = (Transition) obj;
		if (!Arrays.equals(starts, other.starts))
			return false;
		if (target == null) {
			if (other.target != null)
				return false;
		} else if (!target.equals(other.target))
			return false;
		return true;
	}

	public String toString() {
		String string = "[";
		for(State state : starts) {
			string += state + "  ";
		}
		
		return string.trim() + "]" + "->" + target;
	}
}
