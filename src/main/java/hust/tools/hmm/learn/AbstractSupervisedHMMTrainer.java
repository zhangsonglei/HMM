package hust.tools.hmm.learn;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import hust.tools.hmm.model.EmissionProbEntry;
import hust.tools.hmm.model.HMModel;
import hust.tools.hmm.model.TransitionProbEntry;
import hust.tools.hmm.stream.SupervisedHMMSample;
import hust.tools.hmm.stream.SupervisedHMMSampleStream;
import hust.tools.hmm.utils.Dictionary;
import hust.tools.hmm.utils.State;
import hust.tools.hmm.utils.StateSequence;

/**
 *<ul>
 *<li>Description: 基于监督学习的抽象模型训练类 
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2017年12月26日
 *</ul>
 */
public abstract class AbstractSupervisedHMMTrainer implements HMMTrainer {
		
	protected int order;
	
	private final int DEFAULT_ORDER = 1;
	
	protected Dictionary dict;
	
	protected TransitionAndEmissionCounter counter;
	
	protected HashMap<State, Double> pi;
	
	protected HashMap<StateSequence, TransitionProbEntry> transitionMatrix;
	
	protected HashMap<State, EmissionProbEntry> emissionMatrix;

	public AbstractSupervisedHMMTrainer(TransitionAndEmissionCounter counter) {
		this.counter = counter;
		order = counter.getOrder();
		dict = counter.getDictionary();
		pi = new HashMap<>();
		transitionMatrix = new HashMap<>();
		emissionMatrix = new HashMap<>();
	}
	
	public AbstractSupervisedHMMTrainer(SupervisedHMMSampleStream<?> sampleStream, int order) throws IOException {
		counter = new TransitionAndEmissionCounter(sampleStream, order);
		this.order = order > 0 ? order : DEFAULT_ORDER;
		dict = counter.getDictionary();
		pi = new HashMap<>();
		transitionMatrix = new HashMap<>();
		emissionMatrix = new HashMap<>();
	}
	
	public AbstractSupervisedHMMTrainer(List<SupervisedHMMSample> samples,  int order) {
		counter = new TransitionAndEmissionCounter(samples, order);
		this.order = order > 0 ? order : DEFAULT_ORDER;
		dict = counter.getDictionary();
		pi = new HashMap<>();
		transitionMatrix = new HashMap<>();
		emissionMatrix = new HashMap<>();
	}
	
	@Override
	public abstract HMModel train();
	
	/**
	 * 计算初始概率矩阵
	 * @param counter	转移发射计数器
	 */	
	protected abstract void calcPi(TransitionAndEmissionCounter counter);
	
	/**
	 * 计算转移概率矩阵
	 * @param counter	转移发射计数器
	 */	
	protected abstract void calcTransitionMatrix(TransitionAndEmissionCounter counter);
		
	/**
	 * 计算发射概率矩阵
	 * @param counter	转移发射计数器
	 */	
	protected abstract void calcEmissionMatrix(TransitionAndEmissionCounter counter);
  
    /**
     * 计算给定转移的最大似然概率
     * @param start		转移的起点
     * @param target	转移的终点
     * @param counter	计数器
     * @return			最大似然概率
     */
	protected double calcTransitionMLProbability(StateSequence start, State target, TransitionAndEmissionCounter counter) {
		if(start.length() == 0)
			throw new IllegalArgumentException("隐藏状态序列不能为空");
		
		int nCount = counter.getTransitionCount(start, target);
		int n_Count = counter.getTransitionStartCount(start);
		
		return (0 == n_Count || 0 == n_Count) ? 0 : 1.0 * nCount / n_Count;
	}
}
