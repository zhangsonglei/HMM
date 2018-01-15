package hust.tools.hmm.learn;

import hust.tools.hmm.model.HMModel;
import hust.tools.hmm.stream.SemiSupervisedHMMSampleStream;

/**
 *<ul>
 *<li>Description: 基于半监督的抽象模型训练类 
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2017年12月26日
 *</ul>
 */
public abstract class AbstractSemiSupervisedHMMTrainer implements HMMTrainer {
	
	protected int order;
	
	protected final int DEFAULT_ORDER = 1;
	
	protected SemiSupervisedHMMSampleStream<?> sampleStream;
	
	public AbstractSemiSupervisedHMMTrainer(SemiSupervisedHMMSampleStream<?> sampleStream) {
		this.order = DEFAULT_ORDER;
		this.sampleStream = sampleStream;
	}
	
	public AbstractSemiSupervisedHMMTrainer(SemiSupervisedHMMSampleStream<?> sampleStream, int order) {
		this.sampleStream = sampleStream;
		this.order = order > 0 ? order : DEFAULT_ORDER;
	}

	@Override
	public abstract HMModel train();
}
