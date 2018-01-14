package hust.tools.hmm.learn;

import java.io.File;
import java.io.IOException;
import java.util.List;

import hust.tools.hmm.model.HMModel;
import hust.tools.hmm.stream.UnsupervisedHMMSample;
import hust.tools.hmm.stream.UnsupervisedHMMSampleStream;

/**
 *<ul>
 *<li>Description: 模型训练类 
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2017年12月26日
 *</ul>
 * @param <T>
 */
public abstract class AbstractUnsupervisedLearner extends AbstractLearner {
	
	public AbstractUnsupervisedLearner() {
		super();
	}
	
	/**
	 * 根据无监督样本流训练HMM模型
	 * @param sampleStream		无监督样本(只有观测序列)流
	 * @param order				HMM模型的阶数
	 * @param cutoff			阈值
	 * @param modelFile			模型写出路径
	 * @return					HMM模型
	 */
	public abstract HMModel train(UnsupervisedHMMSampleStream<?> sampleStream, int order, int cutoff, File modelFile) throws IOException;

	/**
	 * 根据无监督样本列表训练HMM模型
	 * @param samples	无监督样本(只有观测序列)列表
	 * @param order		HMM模型的阶数
	 * @param cutoff	阈值
	 * @param modelFile	模型写出路径
	 * @return			HMM模型
	 */
	public abstract HMModel train(List<UnsupervisedHMMSample> samples, int order, int cutoff, File modelFile);
}
