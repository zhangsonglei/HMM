package hust.tools.hmm.model;

import hust.tools.hmm.utils.ObservationSequence;
import hust.tools.hmm.utils.StateSequence;

/**
 *<ul>
 *<li>Description: 基于维特比解码和前向算法的的HMM，用于1阶HMM
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月15日
 *</ul>
 */
public class HMMWithViterbi implements HMM {
		
	/**
	 * HMM模型
	 */
	private HMModel model;
	
	/**
	 * Viterbi变量，delta[t][i]， 在时间t时，HMM沿某一条路径到达状态si，并输出观测序列O1O2...Ot的最大概率的对数
	 */
	private double[][] delta;
	
	/**
	 * 记忆回退路径，psi[t][i]记录该路径上状态si（在t-1时刻）的前一个状态
	 */
	private int[][] psi;
	
	/**
	 * 状态序列的索引
	 */
	private int[] stateSequenceIndex;
	
	/**
	 * 观测序列对应最佳路径的概率的对数
	 */
	private double logProb;
	
	public HMMWithViterbi(HMModel model) {
		this.model = model;
		if(model.getOrder() != 1)
			throw new IllegalArgumentException("");
	}

	@Override
	public double getProb(ObservationSequence observations, StateSequence states) {
		double logProb = model.getLogPi(states.get(0)) + model.emissionLogProb(states.get(0), observations.get(0));
		for(int i = 1; i < states.length(); i++) 
			logProb += model.transitionLogProb(new StateSequence(states.get(i - 1)), states.get(i)) +
					model.emissionLogProb(states.get(i), observations.get(i));
		
		return Math.pow(10, logProb);
	}

	@Override
	public double getProb(ObservationSequence observations) {
		ForwardAlgorithm algorithm = new ForwardAlgorithm(model, observations);
		
		return algorithm.getProb();
	}
	
	@Override
	public StateSequence bestStateSeqence(ObservationSequence observationSequence) {
		viterbiCalculator(observationSequence);
		
		StateSequence sequence = new StateSequence();
		for(int index : stateSequenceIndex)
			sequence = sequence.addLast(model.getState(index));
		
		return sequence;
	}

	/**
	 * viterbi算法计算给定观测序列的最优隐藏序列
	 * @param observationSequence	给定的观测序列
	 */
	private void viterbiCalculator(ObservationSequence observationSequence) {
		if(observationSequence.length() == 0)
			throw new IllegalArgumentException("观测序列不能为空");
		
		int stateTypesCount = model.statesCount();
		int observaionLength = observationSequence.length();
		
		delta = new double[observaionLength][stateTypesCount];
		psi = new int[observaionLength][stateTypesCount];
		stateSequenceIndex = new int[observaionLength];
		
		//将观测序列转为其索引
		int[] observationSequenceIndex = new int[observaionLength];
		for(int t = 0; t < observaionLength; t++) 
			observationSequenceIndex[t] = model.getObservationIndex(observationSequence.get(t));
		
		//Viterbi解码
		//初始化参数
		for(int i = 0; i < stateTypesCount; i++) {
			delta[0][i] = model.getLogPi(i) + model.emissionLogProb(i, observationSequenceIndex[0]);
			psi[0][i] = 0;
		}
		
		//归纳计算
		for(int t = 1; t < observationSequenceIndex.length; t++)
			for(int i = 0; i < stateTypesCount; i++)
				viterbiStep(observationSequenceIndex[t], t, i);
		
		//结束
		logProb = Math.log10(Double.MIN_VALUE);
		for(int i = 0; i < stateTypesCount; i++) {
			double currentLogProb = delta[observaionLength - 1][i];
			
			if (logProb < currentLogProb) {
				logProb = currentLogProb;
				stateSequenceIndex[observaionLength - 1] = i;
			}
		}
		
		//路径回溯
		for(int t = observaionLength - 2; t >= 0; t--)
			stateSequenceIndex[t] = psi[t + 1][stateSequenceIndex[t + 1]];
	}
	
	/**
	 * 维特比变量的归纳计算
	 * @param observationIndex	t时刻的观测状态
	 * @param t					时刻t
	 * @param j					隐藏状态
	 */
	private void viterbiStep(int observation, int t, int j) {
		double maxDelta = Math.log10(Double.MIN_VALUE);
		int max_psi = 0;//最短路径
		
		for(int i = 0; i < model.statesCount(); i++) {
			double currentDelta = delta[t - 1][i] + model.transitionLogProb(new int[]{i}, j);
			
			if(maxDelta < currentDelta) {
				maxDelta = currentDelta;
				max_psi = i;
			}
		}
		
		delta[t][j] = maxDelta + model.emissionLogProb(j, observation);
		psi[t][j] = max_psi;
	}

}
