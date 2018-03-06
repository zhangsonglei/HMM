package hust.tools.hmm.model;

import java.util.List;

import hust.tools.hmm.utils.ObservationSequence;

/**
 *<ul>
 *<li>Description: 默认模型收敛判断, 两个模型对相同观测序列计算的概率值的差小于某个阈值（0.01）,或者迭代次数大于阈值(100)
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年3月5日
 *</ul>
 */
public class DefaultConvergencyJudge implements ConvergencyJudge {

	/**
	 * 默认迭代次数
	 */
	private final int DEFAULT_ITERATION = 100;
	
	/**
	 * 默认阈值(收敛条件, 两种模型对同一个训练语料的概率差值小于默认阈值)
	 */
	private final double DEFAULT_DELTA = 0.01;
	
	@Override
	public boolean isConvergency(HMModel preModel, HMModel currentModel, List<ObservationSequence> trainSequences, int iteration) {
		if(iteration > DEFAULT_ITERATION)
			return true;
		
		double preProb, currentProb;
		preProb = currentProb = 0.0;
		ForwardAlgorithm algorithm = null;
		
		for(ObservationSequence sequence : trainSequences) {
			algorithm = new ForwardAlgorithm(preModel, sequence);
			preProb += algorithm.getProb();
			algorithm = new ForwardAlgorithm(currentModel, sequence);
			currentProb += algorithm.getProb();
		}
		
		System.out.println("iter = " + iteration + "\tpreProb = " + preProb +"\tcurrentProb = " + currentProb);
		
		if(Math.acos(currentProb - preProb) < DEFAULT_DELTA)
			return true;
		
		return false;
	}
}