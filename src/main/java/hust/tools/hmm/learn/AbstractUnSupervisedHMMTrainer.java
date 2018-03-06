package hust.tools.hmm.learn;

import hust.tools.hmm.model.HMModel;

/**
 *<ul>
 *<li>Description: 基于半监督的抽象模型训练类 
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2017年12月26日
 *</ul>
 */
public abstract class AbstractUnSupervisedHMMTrainer implements HMMTrainer {
		
	protected HMModel model;
	
	public AbstractUnSupervisedHMMTrainer() {
		
	}
	
	public AbstractUnSupervisedHMMTrainer(HMModel model) {
		this.model = model;
		if(model.getOrder() != 1)
			throw new IllegalArgumentException("非监督训练目前只支持1阶HMM");
	}

	@Override
	public abstract HMModel train();
}
