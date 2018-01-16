package hust.tools.hmm.io;

import java.io.Serializable;

import hust.tools.hmm.model.ARPAEntry;
import hust.tools.hmm.utils.StateSequence;

/**
 *<ul>
 *<li>Description: 转移条目
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月14日
 *</ul>
 */
public class TransitionEntry implements Serializable {
	
	/**
	 * 版本序列号
	 */
	private static final long serialVersionUID = -7083055836147200214L;

	/**
	 * 转移的条件
	 */
	private StateSequence sequence;
	
	/**
	 * 转移的概率及回退权重
	 */
	private ARPAEntry entry;
	
	public TransitionEntry(StateSequence sequence, ARPAEntry entry) {
		this.sequence = sequence;
		this.entry = entry;
	}
	
	public StateSequence getSequence() {
		return sequence;
	}

	public ARPAEntry getEntry() {
		return entry;
	}

	@Override
	public String toString() {
		return sequence + "\t" + entry;
	}
}