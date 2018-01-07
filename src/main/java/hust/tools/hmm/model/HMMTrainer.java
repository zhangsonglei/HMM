package hust.tools.hmm.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import hust.tools.hmm.stream.AbstractHMMSample;
import hust.tools.hmm.stream.SupervisedHMMSample;
import hust.tools.hmm.stream.SupervisedHMMSampleStream;
import hust.tools.hmm.stream.UnsupervisedHMMSampleStream;
import hust.tools.hmm.utils.Dictionary;
import hust.tools.hmm.utils.StateSequence;

/**
 *<ul>
 *<li>Description: 模型训练类 
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2017年12月26日
 *</ul>
 * @param <T>
 */
public class HMMTrainer {

	/**
	 * 默认HMM模型阶数
	 */
	public final int DEFAULT_ORDER = 1;
	
	/**
	 * 默认隐藏状态或观测状态计数阈值
	 */
	public final int DEFAULT_CUTOFF = 0;
	
	private Dictionary dict;
	
	private double[] pi;
	
	private HashMap<Emission, ProbBowEntry>  transitionMatrix;
	
	private HashMap<Transition, ProbBowEntry>  emissionMatrix;
	
	public HMMTrainer() {
		dict = new Dictionary();
		pi = new double[dict.stateCount()];
		transitionMatrix = new HashMap<>();
		emissionMatrix = new HashMap<>();
	}
	
	/**
	 * 加载模型文件
	 * @param modelFile	模型文件
	 * @return			HMM模型
	 */
	public HMMModel loadModel(File modelFile) {
		
		
		return new HMMModel(dict, pi, transitionMatrix, emissionMatrix);
	}
	
	/**
	 * 训练HMM模型
	 * @param sampleStream		无监督样本(只有观测序列)流
	 * @param order				HMM模型的阶数
	 * @param cutoff			阈值
	 * @param modelFile			模型写出路径
	 * @return					HMM模型
	 */
	public HMMModel train(UnsupervisedHMMSampleStream<?> sampleStream, int order, int cutoff, File modelFile) {
		
		return new HMMModel(dict, pi, transitionMatrix, emissionMatrix);
	}
	
	public HMMModel train(UnsupervisedHMMSampleStream<?> sampleStream, int order, int cutoff) {
		return train(sampleStream, order, cutoff, null);
	}
	
	/**
	 * 训练HMM模型
	 * @param sampleStream		监督样本(包含观测和状态序列)流
	 * @param order				HMM模型的阶数
	 * @param cutoff			阈值
	 * @param modelFile			模型写出路径
	 * @return					HMM模型
	 */
	public HMMModel train(SupervisedHMMSampleStream<?> sampleStream, int order, int cutoff, File modelFile) {
		
		
		return new HMMModel(dict, pi, transitionMatrix, emissionMatrix);
	}
	
	public HMMModel train(SupervisedHMMSampleStream<?> sampleStream, int order, int cutoff) {		
		return train(sampleStream, order, cutoff, null);
	}
	
	/**
	 * 返回转移概率矩阵
	 * @param sampleStream	监督样本流
	 * @param order			HMM模型的阶数
	 * @param cutoff		阈值
	 * @param file			转移概率矩阵写出路径
	 * @return				转移概率矩阵
	 */	
	public HashMap<Transition, ProbBowEntry> getTransitionMatrix(SupervisedHMMSampleStream<?> sampleStream, int order, int cutoff, File file) {
		List<StateSequence> stateSequences = new ArrayList<>();
		
		AbstractHMMSample sample = null;
		try {
			while ((sample = sampleStream.read()) != null) {
				stateSequences.add(((SupervisedHMMSample) sample).getStateSequence());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return getTransitionMatrix(stateSequences, order, cutoff, file);
	}
	
	public HashMap<Transition, ProbBowEntry> getTransitionMatrix(SupervisedHMMSampleStream<?> sampleStream, int order, int cutoff) {
		return getTransitionMatrix(sampleStream, order, cutoff, null);
	}
	
	public HashMap<Transition, ProbBowEntry> getTransitionMatrix(List<StateSequence> stateSequences, int order, int cutoff, File file) {
		
		return null;
	}
	
	/**
	 * 返回发射概率矩阵
	 * @param sampleStream	监督样本流
	 * @param order			HMM模型的阶数
	 * @param cutoff		阈值
	 * @param file			发射概率矩阵写出路径
	 * @return				发射概率矩阵
	 */
	public HashMap<Emission, ProbBowEntry> getEmissionMatrix(SupervisedHMMSampleStream<?> sampleStream, int order, int cutoff, File file) {
		
		return null;
	}
	
	public HashMap<Emission, ProbBowEntry> getEmissionMatrix(SupervisedHMMSampleStream<?> sampleStream, int order, int cutoff) {
		return getEmissionMatrix(sampleStream, order, cutoff, null);
	}
}
