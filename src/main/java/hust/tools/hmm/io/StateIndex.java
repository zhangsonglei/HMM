package hust.tools.hmm.io;

import java.io.Serializable;

import hust.tools.hmm.utils.State;

public class StateIndex implements Serializable {

	/**
	 * 版本序列号
	 */
	private static final long serialVersionUID = 7188915439204287101L;

	private State state;
	
	private int index;
	
	public StateIndex(State state, int index) {
		this.state = state;
		this.index = index;
	}

	public State getState() {
		return state;
	}

	public int getIndex() {
		return index;
	}
	
	@Override
	public String toString() {
		return state + "\t" + index;
	}
}
