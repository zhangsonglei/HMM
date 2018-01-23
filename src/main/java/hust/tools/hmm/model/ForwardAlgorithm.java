package hust.tools.hmm.model;

import hust.tools.hmm.utils.ObservationSequence;

/**
 *<ul>
 *<li>Description: 前向算法，给定HMM模型参数，计算观测序列概率
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月3日
 *</ul>
 */
public class ForwardAlgorithm {

	/**
	 * alpha[t][i] = P(O(1), O(2),..., O(t), i(t) = S(i) | HMM)取对数
	 */
	private double[][] alpha = null;
	
	private HMModel model;
	
	/**
	 * 观测序列概率
	 */
	private double prob;
	
	public ForwardAlgorithm(HMModel model, ObservationSequence observations) {
		if(observations.length() == 0)
			throw new IllegalArgumentException("观测序列不能为空");
		
		this.model = model;
		computeAlpha(observations);
	}

	private void computeAlpha(ObservationSequence observations) {
		//初始化
		int stateNum = model.statesCount();
		initAlpha(observations, stateNum);
		
		//递推
		if(observations.length() > 1) {
			for(int t = 1; t < observations.length(); t++)				
				for(int j = 0; j < stateNum; j++)
					alphaStep(observations, t, j);
		}
		
		//终止
		computeObservationProb(observations.length(), stateNum);
	}
	
	/**
	 * 对前向概率进行初始化：alpha[0][i] = pi(i) * b[i][0];
	 * @param observations
	 */
	private void initAlpha(ObservationSequence observations, int count) {
		alpha = new double[observations.length()][count];
		
		int firstObservation = model.getObservationIndex(observations.get(0));
		for(int i = 0; i < count; i++)
			alpha[0][i] = model.getLogPi(i) + model.emissionLogProb(i, firstObservation);
	}
	
	/**
	 * 计算观测序列第t时刻第j个状态的前向概率
	 * @param observations	观测序列
	 * @param t				当前时刻
	 * @param j				状态索引
	 */
	private void alphaStep(ObservationSequence observations, int t, int j) {
		double sum = 0;
		
		for(int i = 0; i < model.statesCount(); i++)
			sum += Math.pow(10, alpha[t - 1][i] + model.transitionLogProb(i, j));
		
		int observation = model.getObservationIndex(observations.get(t));//观测状态的索引
		alpha[t][j] = Math.log10(sum) + model.emissionLogProb(j, observation);
	}
	
	/**
	 * 计算观测序列的概率
	 * @param T				观测序列的长度
	 * @param stateCount	隐藏状态的个数
	 */
	private void computeObservationProb(int T, int stateCount) {
		for(int i = 0; i < stateCount; i++)
			prob += Math.pow(10, alpha[T - 1][i]);
	}
	
	/**
	 * 返回观测序列的概率
	 * @return	观测序列的概率
	 */
	public double getProb() {
		return prob;
	}

	/**
	 * 返回t时刻i状态的前向概率
	 * @param t	观测序列的时刻
	 * @param i	隐藏状态的索引
	 * @return	前向概率
	 */
	public double getAlpha(int t, int i) {
		if(alpha == null)
			throw new UnsupportedOperationException("前向概率未计算。");
		
		return alpha[t][i];
	}
	
	public double[][] getAlpha() {
		return alpha;
	}
}
