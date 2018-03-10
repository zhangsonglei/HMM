package hust.tools.hmm.learn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import hust.tools.hmm.model.BackwardAlgorithm;
import hust.tools.hmm.model.ConvergencyJudge;
import hust.tools.hmm.model.DefaultConvergencyJudge;
import hust.tools.hmm.model.EmissionProbEntry;
import hust.tools.hmm.model.ForwardAlgorithm;
import hust.tools.hmm.model.HMModel;
import hust.tools.hmm.model.HMModelBasedMap;
import hust.tools.hmm.model.TransitionProbEntry;
import hust.tools.hmm.stream.UnSupervisedHMMSample;
import hust.tools.hmm.stream.UnSupervisedHMMSampleStream;
import hust.tools.hmm.utils.Dictionary;
import hust.tools.hmm.utils.Observation;
import hust.tools.hmm.utils.ObservationSequence;
import hust.tools.hmm.utils.State;
import hust.tools.hmm.utils.StateSequence;

/**
 *<ul>
 *<li>Description: 基于Baum-Welch的非监督HMM训练器(目前只支持1阶HMM训练)
 *<li>训练器需有初始模型，初始模型可以导入现有的模型，也可以随机生成
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月23日
 *</ul>
 */
public class UnSupervisedBaumWelchHMMTrainer extends AbstractUnSupervisedHMMTrainer {

	private ConvergencyJudge convergencyJudge;
	private List<ObservationSequence> trainSequences;
	
	/**
	 * 构造方法
	 * @param initHMModel		初始模型
	 * @param sampleStream		训练样本（观测样本）流
	 * @param convergencyJudge	收敛判断
	 * @throws IOException
	 */
	public UnSupervisedBaumWelchHMMTrainer(HMModel initHMModel, UnSupervisedHMMSampleStream<?> sampleStream, ConvergencyJudge convergencyJudge) throws IOException {
		super(initHMModel);
		this.convergencyJudge = convergencyJudge;
		trainSequences = new ArrayList<>();
		
		UnSupervisedHMMSample sample = null;
		while((sample = (UnSupervisedHMMSample) sampleStream.read()) != null) {
			trainSequences.add(sample.getObservationSequence());
		}
		sampleStream.close();
	}
	public UnSupervisedBaumWelchHMMTrainer(HMModel initHMModel, UnSupervisedHMMSampleStream<?> sampleStream) throws IOException {
		this(initHMModel, sampleStream, new DefaultConvergencyJudge());
	}
	public UnSupervisedBaumWelchHMMTrainer(HMModel initHMModel, List<UnSupervisedHMMSample> trainSamples, ConvergencyJudge convergencyJudge) throws IOException {
		super(initHMModel);
		this.convergencyJudge = convergencyJudge;
		
		trainSequences = new ArrayList<>();
		for(UnSupervisedHMMSample sample : trainSamples)
			trainSequences.add(sample.getObservationSequence());
	}
	public UnSupervisedBaumWelchHMMTrainer(HMModel initHMModel, List<ObservationSequence> trainSequences) throws IOException {
		super(initHMModel);
		this.trainSequences = trainSequences;
		convergencyJudge = new DefaultConvergencyJudge();
	}
	
	@Override
	public HMModel train() {
		HMModel preModel, currentModel;
		currentModel = model;
		int iteration = 1;
		
		do{
			preModel = currentModel;
			currentModel = iterate(preModel, trainSequences);
		}while(!convergencyJudge.isConvergency(preModel, currentModel, trainSequences, iteration++));
		
		return currentModel;
	}
	
	/**
	 * 一次迭代，在当前HMM模型的基础上生成一个新的HMM模型
	 * @param model		当前模型
	 * @param sequences	训练语料(观测序列集)
	 * @return			新的HMM模型
	 */
	private HMModel iterate(HMModel model, final List<ObservationSequence> sequences) {
		Dictionary dict = model.getDict();
		HashMap<State, Double> pi = new HashMap<>();
		HashMap<StateSequence, TransitionProbEntry> transitionMatrix = model.getTransitionMatrix();
		HashMap<State, EmissionProbEntry> emissionMatrix = new HashMap<>();
		
		int N = model.statesCount();
		int M = model.observationsCount();
		
		double[][] alpha, beta;//前向概率和后向概率
		double tempPiNumerator[] = new double[N];
		double tempPiDenominator = 0.0;
		double tempTransitionMatrixNumerator[][] = new double[N][N];
		double tempTransitionMatrixDenominator[] = new double[N];
		double[][] tempEmissionMatrixNumerator = new double[N][M];
		double[][] tempEmissionMatrixDenominator = new double[N][M];		
		double sum1, sum2;
		
		BackwardAlgorithm backward = null;
		ForwardAlgorithm forward = null;
		for(int no = 0; no < trainSequences.size(); no++) {
			ObservationSequence sequence = trainSequences.get(no);
			
			backward = new BackwardAlgorithm(model, sequence);
			beta = backward.getBeta();
			forward = new ForwardAlgorithm(model, sequence);
			alpha = forward.getAlpha();
						
			int T = sequence.length();
			int[] observationsIndex = new int[T];
			for(int i = 0; i < observationsIndex.length; i++)
				observationsIndex[i] = dict.getIndex(sequence.get(i));
			
			double[][][] xi = calcXi(model, observationsIndex, alpha, beta);
			double[][] gamma = calcGamma(model, xi);
			
			for(int i = 0; i < N; i++) {
				for(int t = 0; t < T - 1; t++) {
					if(t== 0) {
						tempPiNumerator[i] += gamma[t][i];
						tempPiDenominator += gamma[t][i];
					}
					
					tempTransitionMatrixDenominator[i] += gamma[t][i];
					
					for(int j = 0; j < N; j++)
						tempTransitionMatrixNumerator[i][j] += xi[t][i][j];
				}
			
				for(int k = 0; k < M; k++) {
					sum1 = sum2 = 0.0;
					for(int t = 0; t < T; t++) {
						if(sequence.get(t).equals(dict.getObservation(k)))
							sum1 += gamma[t][i];
						
						sum2 += gamma[t][i];
					}
					
					tempEmissionMatrixNumerator[i][k] += sum1;
					tempEmissionMatrixDenominator[i][k] += sum2;
				}
			}
		}
		
		/**
		 * 重新估算模型参数
		 * prob = 0.001 + 0.999 * prob 防止概率为0
		 */		
		double sumPi = 0.0;
		double prob = 0.0;
		for(int i = 0; i < N; i++) {
			State state = dict.getState(i);
			//计算初始转移概率
			prob = 0.001 + 0.999 * tempPiNumerator[i] / tempPiDenominator;
			sumPi += prob;
			pi.put(state, prob);
			
			//计算转移概率
			TransitionProbEntry transitionProbEntry = new TransitionProbEntry();
			double sumA = 0.0;
			StateSequence start = new StateSequence(state);
			if(tempTransitionMatrixDenominator[i] == 0.0) { // 状态i不可以作为转移起点
				sumA = 0.0;
				for(int j = 0; j < N; j++) {
					State target = dict.getState(j);
					prob = Math.pow(10, transitionMatrix.get(start).getTransitionLogProb(target));
					sumA += prob;
					transitionProbEntry.put(target, prob);
				}
				//归一化
				for(int j = 0; j < N; j++) {
					State target = dict.getState(j);
					transitionProbEntry.put(target, Math.log10(transitionProbEntry.getTransitionLogProb(target) / sumA));
				}
			}else {
				sumA = 0.0;
				for(int j = 0; j < N; j++) {
					State target = dict.getState(j);
					prob = 0.001 + 0.999 * tempTransitionMatrixNumerator[i][j] / tempTransitionMatrixDenominator[i];
					sumA += prob;
					transitionProbEntry.put(target, prob);
				}
				//归一化
				for(int j = 0; j < N; j++) {
					State target = dict.getState(j);
					transitionProbEntry.put(target, Math.log10(transitionProbEntry.getTransitionLogProb(target) / sumA));
				}
			}
			transitionMatrix.put(start, transitionProbEntry);
		
			//计算发射概率
			EmissionProbEntry emissionProbEntry = new EmissionProbEntry();
			double sumB = 0.0;
			for(int j = 0; j < M; j++) {
				Observation observation = dict.getObservation(j);
				prob = 0.001 + 0.999 * tempEmissionMatrixNumerator[i][j] / tempEmissionMatrixDenominator[i][j];
				sumB += prob;
				emissionProbEntry.put(observation, prob);
			}
			//归一化
			for(int j = 0; j < M; j++) {
				Observation observation = dict.getObservation(j);
				emissionProbEntry.put(observation, Math.log10(emissionProbEntry.getEmissionLogProb(observation) / sumB));
			}
			
			emissionMatrix.put(state, emissionProbEntry);
		}
		
		//归一化Pi并取对数
		for(Entry<State, Double> entry : pi.entrySet()) {
			entry.setValue(Math.log10(entry.getValue() / sumPi));
		}

		return new HMModelBasedMap(1, dict, pi, transitionMatrix, emissionMatrix);
	}
	
	/**
	 * 给定模型和观测，计算在t时刻处于状态i的概率gamma[t][i]
	 * gamma[t][i] = SUMj{xi[t][i][j]}
	 * @param model	HMM模型
	 * @param xi	
	 * @return		gamma
	 */
	private double[][] calcGamma(HMModel model, double[][][] xi) {
//		if(T <= 1)
//		throw new IllegalArgumentException("观测序列太短");
//	
//	int N = model.statesCount();
//	double[][] gamma = new double[T][N];
//	
//	for(int t = 0; t < T; t++) {
//		double denominator = 0.0;
//		for(int j = 0; j < N; j++) {
//			gamma[t][j] = Math.pow(10, alpha[t][j]) * Math.pow(10, beta[t][j]);
//			denominator += gamma[t][j];
//		}
//
//		for(int i = 0; i < N; i++)
//			gamma[t][i] = gamma[t][i] / denominator;
//	}		
		
		double[][] gamma = new double[xi.length + 1][xi[0].length];
		
		for (int t = 0; t < xi.length + 1; t++)
			Arrays.fill(gamma[t], 0.);
		
		for (int t = 0; t < xi.length; t++)
			for (int i = 0; i < xi[0].length; i++)
				for (int j = 0; j < xi[0].length; j++)
					gamma[t][i] += xi[t][i][j];
		
		for (int j = 0; j < xi[0].length; j++)
			for (int i = 0; i < xi[0].length; i++)
				gamma[xi.length][j] += xi[xi.length - 1][i][j];		
				
		return gamma;
	}
	
	/**
	 * 给定模型和观测，计算在t时刻处于状态i，且在t+1时刻处于状态j的概率xi[t][i][j]
	 * xi[t][i][j] = alpha[t][i]*A[i][j]*B[j][t+1]*beta[j][t+1]/denominator
	 * @param model			HMM模型
	 * @param T				观测序列长度
	 * @param observations	观测序列
	 * @param alpha			前向概率
	 * @param beta			后向概率
	 * @return				xi
	 */
	private double[][][] calcXi(HMModel model, int[] observations, double[][] alpha, double[][] beta) {
		int T = observations.length;
		if(T <= 1)
			throw new IllegalArgumentException("观测序列太短:" + model.getDict().getObservationSequence(observations));
		
		int N = model.statesCount();
		double[][][] xi = new double[T - 1][N][N];

		for (int t = 0; t < T - 1; t++) {
			double denominator = 0.0;
			for (int i = 0; i < N; i++) {
				for (int j = 0; j < N; j++) {
					xi[t][i][j] = Math.pow(10, alpha[t][i]) * Math.pow(10, beta[t+1][j]) * Math.pow(10, model.transitionLogProb(new int[]{i}, j)) * Math.pow(10, model.emissionLogProb(j, observations[t+1]));
					denominator += xi[t][i][j];
				}
			}
			
			for(int i = 0; i < N; i++) {
				for(int j = 0; j < N; j++)
					xi[t][i][j]  /= denominator;
			}
		}
		
		return xi;
	}
}