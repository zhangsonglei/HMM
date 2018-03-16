package hust.tools.hmm.learn;

import java.util.List;

import hust.tools.hmm.model.HMModel;
import hust.tools.hmm.utils.ObservationSequence;

/**
 *<ul>
 *<li>Description: 模型收敛判断的接口
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年3月5日
 *</ul>
 */
public interface ConvergencyJudge {

	/**
	 * 返回模型是否收敛
	 * @param preModel				上一次迭代的模型
	 * @param currentModel			当前模型
	 * @param observationSequences	观测序列集
	 * @return						true-收敛/false-不收敛
	 */
	public boolean isConvergency(HMModel preModel, HMModel currentModel, List<ObservationSequence> trainSequences, int iteration);
}
