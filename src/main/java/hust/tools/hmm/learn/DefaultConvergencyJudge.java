package hust.tools.hmm.learn;

import java.util.List;

import hust.tools.hmm.model.ForwardAlgorithm;
import hust.tools.hmm.model.HMModel;
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
	private final int DEFAULT_ITERATION = 13;
	
	@Override
	public boolean isConvergency(HMModel preModel, HMModel currentModel, List<ObservationSequence> trainSequences, int iteration) {	
		double preLogProb, currentLogProb;
		preLogProb = currentLogProb = 0.0;
		ForwardAlgorithm algorithm = null;
		
		for(ObservationSequence sequence : trainSequences) {
			algorithm = new ForwardAlgorithm(preModel, sequence);
			preLogProb += algorithm.getProb();
			algorithm = new ForwardAlgorithm(currentModel, sequence);
			currentLogProb += algorithm.getProb();
		}
		
		System.out.println("iter = " + iteration + "\tpreLogProb = " + Math.log10(preLogProb) +"\tcurrentLogProb = " + Math.log10(currentLogProb));
		if(iteration >= DEFAULT_ITERATION)
			return true;
				
		return false;
	}
}