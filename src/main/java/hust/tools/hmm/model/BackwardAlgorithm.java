package hust.tools.hmm.model;

import hust.tools.hmm.utils.ObservationSequence;

/**
 *<ul>
 *<li>Description: 后向算法，给定HMM模型参数，计算观测序列概率  
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月3日
 *</ul>
 */
public class BackwardAlgorithm {
	
	/**
	 * beta[t][i] = P(O(t+1), O(t+2),..., O(T)|i(t) = S(i), HMM)
	 */
	private double[][] beta = null;
	
	private HMModel model;
	
	/**
	 * 观测序列概率
	 */
	protected double prob;

	public BackwardAlgorithm(HMModel model, ObservationSequence observations) {
		if(observations.length() == 0)
			throw new IllegalArgumentException("观测序列不能为空");
		
		this.model = model;
		computeBeta(observations);
	}

	private void computeBeta(ObservationSequence observations) {
		//初始化
		int stateNum = model.getStates().length;
		initBeta(observations, stateNum);
		
		//递推
		if(observations.length() > 1) {
			for(int t = observations.length() - 2; t >= 0; t--)
				for(int i = 0; i < stateNum; i++) 
					betaStep(observations, t, i);
		}
		
		//终止
		int index = model.getObservationIndex(observations.get(0));
		computeObservationProb(index);
	}
	
	/**
	 * 初始化后向概率
	 * @param stateCount	隐藏状态数量
	 * @param T			 	观测序列长度
	 */
	private void initBeta(ObservationSequence observations, int count) {
		beta = new double[observations.length()][count];
		
		for(int i = 0; i < count; i++)
			beta[observations.length() - 1][i] = 0;
	}
	
	/**
	 * 计算观测序列第t时刻第i个状态的后向概率
	 * @param model			HMM模型
	 * @param observations	观测序列
	 * @param t				当前时刻
	 * @param i				状态索引
	 */
	private void betaStep(ObservationSequence observations, int t, int i) {
		double sum = 0.0;
		int o = model.getObservationIndex(observations.get(t + 1));

		for(int j = 0; j < model.statesCount(); j++)
			sum += Math.pow(10, model.transitionProb(i, j) + model.emissionProb(j, o) + beta[t + 1][j]);

		beta[t][i] = Math.log10(sum);
	}
	
	/**
	 * 计算观测序列的概率
	 * @param model			HMM模型
	 * @param index			第一个观测状态索引
	 */
	private void computeObservationProb(int index) {
		for(int i = 0; i < model.statesCount(); i++)
			prob += Math.pow(10, model.getPi(i) + model.emissionProb(i, index) + beta[0][i]);
	}
	
	/**
	 * 返回观测序列的概率
	 * @return	观测序列的概率
	 */
	public double getProb() {
		return prob;
	}

//	/**
//	 * 返回t时刻i状态的后向概率
//	 * @param t	观测序列的时刻
//	 * @param i	隐藏状态的索引
//	 * @return	后向概率
//	 */
//	public double getBeta(int t, int i) {
//		if(beta == null)
//			throw new UnsupportedOperationException("后向概率未计算。");
//		
//		return beta[t][i];
//	}
}
