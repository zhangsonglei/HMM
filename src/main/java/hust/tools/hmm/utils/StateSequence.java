package hust.tools.hmm.utils;

import java.util.Arrays;
import java.util.List;

/**
 *<ul>
 *<li>Description: 状态序列
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2017年12月26日
 *</ul>
 */
public class StateSequence implements Sequence<State> {

	/**
	 * 版本序列号
	 */
	private static final long serialVersionUID = -1786541057795927676L;
	
	private State[] states;
	
	public StateSequence() {
		states = null;
	}
	
	public StateSequence(State[] states) {
		this.states = states;
	}
	
	public StateSequence(List<State> states) {
		if(states.size() == 0)
			throw new IllegalArgumentException("状态列表不能为空");
		
		this.states = states.toArray(new State[states.size()]);
	}
	
	public StateSequence(State state) {
		this.states = new State[]{state};
	}

	@Override
	public StateSequence add(State state) {
		if(states == null)
			return new StateSequence(state);
		
		State[] arr = new State[length() + 1];
		int i = 0;
		for(i = 0; i < length(); i++)
			arr[i] = states[i];
		
		arr[i] = state;
		
		return new StateSequence(arr);
	}
	
	@Override
	public StateSequence add(State[] states) {
		if(states == null)
			return new StateSequence(states);
		
		State[] arr = new State[length() + states.length];
		int i = 0;
		for(i = 0; i < length(); i++)
			arr[i] = states[i];
		
		for(int j = 0; j < states.length; j++)
			arr[i++] = states[j];
		
		return new StateSequence(arr);
	}

	@Override
	public StateSequence remove(int index) {
		if(length() <= 1)
			return null;
		
		State[] arr = new State[length() - 1];
		for(int i = 0; i < arr.length; i++)
			arr[i] = states[i];
		
		return new StateSequence(arr);
	}

	@Override
	public StateSequence set(State state, int index) {
		if(index < 0 || index >= length())
			throw new IllegalArgumentException("索引:" + index +"大于状态序列长度:" + length());
		
		State[] arr = new State[length()];
		if(index >= 0 && index < length()) {
			for(int i = 0; i < length(); i++) {
				if(i == index)
					arr[i] = state;
				else
					arr[i] = states[i];
			}
		}
		
		return new StateSequence(arr);
	}
	
	@Override
	public State get(int index) {
		if(index >= 0 && index < length())
			return states[index];
		
		return null;
	}

	@Override
	public List<State> asList() {
		if(length() != 0)
			return Arrays.asList(states);
		
		return null;
	}
	
	@Override
	public State[] toArray() {
		if(length() != 0)
			return states;
		
		return null;
	}
	
	@Override
	public int length() {
		if(states != null)
			return states.length;
		
		return 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		StateSequence other = (StateSequence) obj;
		if (!Arrays.equals(states, other.states))
			return false;
		return true;
	}

	@Override
	public String toString() {
		String string = "";
		for(State state : states)
			string += state + "  ";
		
		return string.trim();
	}
}
