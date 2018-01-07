package hust.tools.hmm.utils;

import java.util.ArrayList;
import java.util.Iterator;
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
	
	private List<State> states;
	
	public StateSequence() {
		states = new ArrayList<>();
	}
	
	public StateSequence(State[] states) {
		this.states = new ArrayList<>();

		for(State state : states)
			this.states.add(state);
	}
	
	public StateSequence(List<State> states) {
		this.states = states;
	}
	
	public StateSequence(State state) {
		this.states = new ArrayList<>();
		this.states.add(state);
	}

	@Override
	public void add(State state) {
		states.add(state);
	}
	
	@Override
	public void add(State[] states) {
		for(State state : states)
			this.states.add(state);
	}

	@Override
	public void remove(int index) {
		if(index >= 0 && index < states.size() - 1)
			states.remove(index);
	}

	@Override
	public void update(State token, int index) {
		if(index >= 0 && index < states.size() - 1)
			states.set(index, token);
	}
	
	@Override
	public State get(int index) {
		if(index >= 0 && index < states.size())
			return states.get(index);
		
		return null;
	}

	@Override
	public List<State> get() {
		return states;
	}

	@Override
	public Iterator<State> iterator() {
		return states.iterator();
	}
	
	@Override
	public int size() {
		return states.size();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((states == null) ? 0 : states.hashCode());
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
		if (states == null) {
			if (other.states != null)
				return false;
		} else if (!states.equals(other.states))
			return false;
		return true;
	}

	@Override
	public String toString() {
		String string = "";
		for(State state : states)
			string += state;
		
		return string;
	}
}
