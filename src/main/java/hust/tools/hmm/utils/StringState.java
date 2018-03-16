package hust.tools.hmm.utils;

import hust.tools.hmm.utils.State;

/**
 *<ul>
 *<li>Description: 字符串型隐藏状态 
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2017年12月26日
 *</ul>
 */
public class StringState implements State{

	/**
	 * 版本序列号
	 */
	private static final long serialVersionUID = -7272749798298835365L;
	
	private String state;
		
	public StringState(String state) {
		this.state = state;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((state == null) ? 0 : state.hashCode());
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
		StringState other = (StringState) obj;
		if (state == null) {
			if (other.state != null)
				return false;
		} else if (!state.equals(other.state))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return state;
	}
}