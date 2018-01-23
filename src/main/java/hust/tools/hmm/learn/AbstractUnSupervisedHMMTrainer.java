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
	
	protected int order;
	
	protected final static int DEFAULT_ORDER = 1;
	
	protected HMModel model;
	
	public AbstractUnSupervisedHMMTrainer() {
		
	}
	
	public AbstractUnSupervisedHMMTrainer(HMModel model, int order) {
		this.model = model;
		this.order = order > 0 ? order : DEFAULT_ORDER;
	}

	@Override
	public abstract HMModel train();
}
