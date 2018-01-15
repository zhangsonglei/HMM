package hust.tools.hmm.learn;

import hust.tools.hmm.model.HMModel;

/**
 *<ul>
 *<li>Description: 顶层HMM模型训练学习接口，被AbstractSupervisedLearner与AbstractUnsupervisedLearner实现分别实现基于监督和非监督的训练方法 
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月10日
 *</ul>
 */
public interface HMMTrainer {
	
	/**
	 * 	训练并返回HMM模型
	 * @return	HMM模型
	 */
	public HMModel train();
}