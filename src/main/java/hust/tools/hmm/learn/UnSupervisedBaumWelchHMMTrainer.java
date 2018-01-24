package hust.tools.hmm.learn;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import hust.tools.hmm.model.BackwardAlgorithm;
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
 *<li>Description: 基于Baum-Welch的非监督HMM训练器
 *<li>训练器需有初始模型，初始模型可以导入现有的模型，也可以随机生成
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月23日
 *</ul>
 */
public class UnSupervisedBaumWelchHMMTrainer extends AbstractUnSupervisedHMMTrainer {

	private final int DEFAULT_ITERATION = 100;
	
	private int iteration;
	
	private final double DELTA = 0.001;
	
	private ObservationSequence sequence;
	
	/**
	 * 构造方法
	 * @param initHMModel	初始模型
	 * @param order			模型的阶数
	 * @param sequence		进行评价模型好坏的观测序列
	 */
	public UnSupervisedBaumWelchHMMTrainer(HMModel initHMModel, int order, ObservationSequence sequence) {
		super(initHMModel, order);
		this.sequence = sequence;
		this.iteration = DEFAULT_ITERATION;
	}
	
	/**
	 * 构造方法，随机生成初始模型
	 * @param sampleStream	训练样本（观测样本）流
	 * @param states		隐藏状态集
	 * @param order			模型的阶数
	 * @param seed			随机种子
	 * @throws IOException	
	 */
	public UnSupervisedBaumWelchHMMTrainer(UnSupervisedHMMSampleStream<?> sampleStream, Collection<State> states, int order, long seed) throws IOException {
		HashSet<Observation> observationSet = new HashSet<>();
		sequence = new ObservationSequence();
		UnSupervisedHMMSample sample = null;
		while((sample = (UnSupervisedHMMSample) sampleStream.read()) != null) {
			ObservationSequence observationSequence = sample.getObservationSequence();
			sequence = 	sequence.add(observationSequence);
			for(int i = 0; i < observationSequence.length(); i++)
				observationSet.add(observationSequence.get(i));
		}
		sampleStream.close();
		
		HMModel initHMModel = initHMModel(states, observationSet, order, seed);
		this.model = initHMModel;
		this.order = order;
		this.iteration = DEFAULT_ITERATION;
	}
	
	/**
	 * 构造方法，随机生成初始模型
	 * @param observationSet	观测状态集
	 * @param states			隐藏状态集
	 * @param sequence			进行评价模型好坏的观测序列
	 * @param order				模型的阶数
	 * @param seed				随机种子
	 */
	public UnSupervisedBaumWelchHMMTrainer(Collection<Observation> observationSet, Collection<State> states, ObservationSequence sequence, int order, long seed) {
		HMModel initHMModel = initHMModel(states, observationSet, order, seed);
		this.model = initHMModel;
		this.sequence = sequence;
		this.order = order;
		this.iteration = DEFAULT_ITERATION;
	}

	@Override
	public HMModel train() {
		double delta, logProbPre, logProbForward;
		double[][] beta, alpha;
		
		delta = logProbPre = logProbForward = 0;
		
		BackwardAlgorithm backward = new BackwardAlgorithm(model, sequence);
		beta = backward.getBeta();
		ForwardAlgorithm forward = new ForwardAlgorithm(model, sequence);
		alpha = forward.getAlpha();
		logProbForward = Math.log10(forward.getProb());
		logProbPre = logProbForward;
		
		Dictionary dict = model.getDict();
		HashMap<State, Double> pi = model.getPi();
		HashMap<StateSequence, TransitionProbEntry> matrixA = model.getTransitionMatrix();
		HashMap<State, EmissionProbEntry> matrixB = model.getEmissionMatrix();
		int N = model.statesCount();
		int M = model.observationsCount();
		int T = sequence.length();
		
		int[] observationsIndex = new int[sequence.length()];
		for(int i = 0; i < observationsIndex.length; i++)
			observationsIndex[i] = dict.getIndex(sequence.get(i));
		
		double[][][] xi = calcXi(model, sequence.length(), observationsIndex, alpha, beta);
		double[][] gamma = calcGamma(model, sequence.length(), alpha, beta);
		int currentIterator = 0;
		do {
			//重新估计模型的参数转移概率矩阵和发射概率矩阵
			for(int i = 0; i < N; i++) {
				pi.put(dict.getState(i), 0.001 + 0.999 * gamma[1][i]);

				
				double denominatorA = 0.0;
				for(int t = 0; t < T - 1; t++)
					denominatorA += gamma[t][i];

				TransitionProbEntry transitionProbEntry = new TransitionProbEntry();
				StateSequence start = new StateSequence(new State[]{dict.getState(i)});
				for(int j = 0; j < N; j++) {
					double numeratorA = 0.0;
					for (int t = 0; t < T - 1; t++) 
						numeratorA += xi[t][i][j];
					
					State target = dict.getState(j);
					transitionProbEntry.put(target, Math.log10(0.001 + 0.999 * numeratorA / denominatorA));
				}
				matrixA.put(start, transitionProbEntry);
				
				double denominatorB = denominatorA + gamma[T][i]; 
				State state = dict.getState(i);
				EmissionProbEntry emissionProbEntry = matrixB.get(state);
				for(int k = 0; k < M; k++) {
					double numeratorB = 0.0;
					for (int t = 0; t < T; t++) {
						if (observationsIndex[t] == k) 
							numeratorB += gamma[t][i];
					}
					
					Observation observation = dict.getObservation(k);
					emissionProbEntry.put(observation, Math.log10(0.001 + 0.999 * numeratorB / denominatorB));
				}
				matrixB.put(state, emissionProbEntry);
			}
			
			model = new HMModelBasedMap(order, dict, pi, matrixA, matrixB);
			
			backward = new BackwardAlgorithm(model, sequence);
			beta = backward.getBeta();
			forward = new ForwardAlgorithm(model, sequence);
			alpha = forward.getAlpha();
			
			logProbForward = Math.log10(forward.getProb());
			
			xi = calcXi(model, sequence.length(), observationsIndex, alpha, beta);
			gamma = calcGamma(model, sequence.length(), alpha, beta);

			delta = logProbForward - logProbPre; 
			logProbPre = logProbForward;
			currentIterator++;
		}while (delta < DELTA ||currentIterator > iteration);

		return model;
	}
	
	/**
	 * 给定模型和观测，计算在t时刻处于状态i的概率gamma[t][i]
	 * gamma[t][i] = alpha[t][i] * beta[t][i] / denominator
	 * @param model	HMM模型
	 * @param T		观测序列长度
	 * @param alpha	前向概率
	 * @param beta	后向概率
	 * @return		gamma
	 */
	private double[][] calcGamma(HMModel model, int T, double[][] alpha, double[][] beta) {
		double[][] gamma = new double[T][model.statesCount()];
		
		for(int t = 1; t <= T; t++) {
			double denominator = 0.0;
			for(int j = 1; j <= model.statesCount(); j++) {
				gamma[t][j] = Math.pow(10, alpha[t][j]) * Math.pow(10, beta[t][j]);
				denominator += gamma[t][j];
			}

			for(int i = 1; i <= model.statesCount(); i++) 
				gamma[t][i] = gamma[t][i] / denominator;
		}
		
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
	private double[][][] calcXi(HMModel model, int T, int[] observations, double[][] alpha, double[][] beta) {
		double[][][] xi = new double[T][model.statesCount()][model.statesCount()];

		for (int t = 1; t <= T - 1; t++) {
			double denominator = 0.0;	
			for (int i = 1; i <= model.statesCount(); i++) {
				for (int j = 1; j <= model.statesCount(); j++) {
					xi[t][i][j] = Math.pow(10, alpha[t][i]) * Math.pow(10, beta[t+1][j]) * Math.pow(10, model.transitionLogProb(new int[]{i}, j)) * Math.pow(10, model.emissionLogProb(j, observations[t+1]));
					denominator += xi[t][i][j];
				}
			}
			
			for(int i = 1; i <= model.statesCount(); i++) {
				for(int j = 1; j <= model.statesCount(); j++) 
					xi[t][i][j]  /= denominator;
			}
		}
		
		return xi;
	}
	
	/**
	 * 生成随机模型
	 * @param states		隐藏状态集
	 * @param observations	观测状态集
	 * @param order			模型的阶数
	 * @param seed			随即种子
	 * @return				随机模型
	 */
	private HMModel initHMModel(Collection<State> states, Collection<Observation> observations, int order, long seed) {
		Dictionary dict = new Dictionary();
		HashMap<State, Double> pi = new HashMap<>();
		HashMap<StateSequence, TransitionProbEntry> transitionMatrix = new HashMap<>();
		HashMap<State, EmissionProbEntry> emissionMatrix = new HashMap<>();
		
		for(State state : states)
			dict.add(state);
		for(Observation observation : observations)
			dict.add(observation);
		
		int M = observations.size();
		int N = states.size();

		
		Random rand = new Random(seed);
		double[] Pi = new double[N];
		double[][] A = new double[N][N];
		double[][] B = new double[N][M];
		
		double sumPi = 0.0;
		for(int i = 0; i < N; i++) {
			//为初始概率矩阵随机赋值
			Pi[i] = rand.nextDouble();
			sumPi += Pi[i];
			
			//为转移概率矩阵随机赋值
			double sumA = 0.0;
			for(int j = 0; j < N; j++) {
				A[i][j] = rand.nextDouble();
				sumA += A[i][j];
			}
			//转移概率归一化
			State[] start = new State[]{dict.getState(i)};
			TransitionProbEntry transitionProbEntry = new TransitionProbEntry();
			for(int j = 0; j < N; j++) {
				State target = dict.getState(j);
				transitionProbEntry.put(target, A[i][j] /= sumA);
			}
			transitionMatrix.put(new StateSequence(start), transitionProbEntry);
			
			//为发射概率矩阵随机赋值
			double sumB = 0.0;
			for(int j = 0; j < M; j++) {
				B[i][j] = rand.nextDouble();
				sumB += B[i][j];
			}
			//发射概率归一化
			for(int j = 0; j < M; j++) {
				State state = dict.getState(i);
				Observation observation = dict.getObservation(j);
				EmissionProbEntry entry = null;
				if(emissionMatrix.containsKey(state)) 
					entry = emissionMatrix.get(state);
				else
					entry = new EmissionProbEntry();
				
				entry.put(observation, B[i][j] /= sumB);
				emissionMatrix.put(state, entry);
			}
		}
		
		//初始转移概率归一化
		for(int i = 0; i < M; i++)
			pi.put(dict.getState(i), Math.log10(Pi[i] /= sumPi));
		
		return new HMModelBasedMap(order, dict, pi, transitionMatrix, emissionMatrix);
	}
}