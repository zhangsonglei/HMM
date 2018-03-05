package hust.tools.hmm.utils;

/**
 *<ul>
 *<li>Description: 默认序列验证返回true
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月24日
 *</ul>
 */
public class DefaultStateSequenceValidator implements StateSequenceValidator {

	@Override
	public boolean validStateSequence(int t, ObservationSequence observationSequence, StateSequence stateSequence,
			State outcome) {
		return true;
	}
}
