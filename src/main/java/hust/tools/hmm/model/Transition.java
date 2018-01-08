package hust.tools.hmm.model;

import java.util.Arrays;
import java.util.List;

import hust.tools.hmm.utils.State;
import hust.tools.hmm.utils.StateSequence;

/**
 *<ul>
 *<li>Description: 隐藏状态到下一个隐藏状态的转移实体 
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月7日
 *</ul>
 */
public class Transition {
	
	/**
	 * 转移序列，前size-1个为转移条件，最后
	 */
	private State[] start;
	
	private State target;
	
	public Transition(State[] states) {
		if(states.length < 2) 
			throw new IllegalArgumentException("状态数不能小于2");
		
		start = new State[states.length - 1];
		int i = 0;
		for(i = 0; i < start.length; i++)
			start[i] = states[i];
		
		target = states[i];
	}
	
	public Transition(State start, State target) {
		this(new State[]{start}, target);
	}
	
	public Transition(List<State> start, State target) {
		this(start.toArray(new State[start.size()]), target);
	}
	
	public Transition(State[] start, State target) {
		this.start = start;
		this.target = target;
	}
	
	public Transition(StateSequence start, State target) {
		this(start.toArray(), target);
	}
	
	public StateSequence getStartSequence() {
		return new StateSequence(start);
	}
	
	public State[] getStart() {
		return start;
	}
	
	public State getTarget() {
		return target;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(start);
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
		if (!Arrays.equals(start, other.start))
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
		for(State state : start) {
			string += state + "  ";
		}
		
		return string.trim() + "]" + "->" + target;
	}
}
