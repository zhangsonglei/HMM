package hust.tools.hmm.utils;

/**
 *<ul>
 *<li>Description: 隐藏状态序列验证的接口
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月23日
 *</ul>
 */
public interface StateSequenceValidator {
	
	/**
	 * 决定生成的下一时刻隐藏状态是否合法
	 * @param t						当前时刻
	 * @param observationSequence	观测序列
	 * @param stateSequence			截至到当前时刻生成的隐藏状态序列
	 * @param outcome				下一时刻生成的的隐藏状态
	 * @return						true-合法/false-不合法
	 */
	public boolean validStateSequence(int t, ObservationSequence observationSequence, StateSequence stateSequence,  State outcome);
}
