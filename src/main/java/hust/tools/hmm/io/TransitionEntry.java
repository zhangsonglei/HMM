package hust.tools.hmm.io;

import hust.tools.hmm.model.ARPAEntry;
import hust.tools.hmm.utils.State;
import hust.tools.hmm.utils.StateSequence;

/**
 *<ul>
 *<li>Description: 转移条目
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月14日
 *</ul>
 */
public class TransitionEntry {
	
	/**
	 * 转移的条件
	 */
	private StateSequence start;
	
	/**
	 * 转移的目标状态
	 */
	private State target;
	
	/**
	 * 转移的概率及回退权重
	 */
	private ARPAEntry entry;
	
	public TransitionEntry(StateSequence start, State target, ARPAEntry entry) {
		this.start = start;
		this.target = target;
		this.entry = entry;
	}
	
	public StateSequence getStart() {
		return start;
	}
	
	public State getTarget() {
		return target;
	}

	public ARPAEntry getEntry() {
		return entry;
	}

	@Override
	public String toString() {
		return start + "\t" + target + "\t" + entry;
	}
}