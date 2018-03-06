package hust.tools.hmm.learn;

import java.io.IOException;
import java.util.ArrayList;
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
	public UnSupervisedBaumWelchHMMTrainer(HMModel initHMModel, List<UnSupervisedHMMSample> trainSequences) throws IOException {
		this(initHMModel, trainSequences, new DefaultConvergencyJudge());
	}
	
	
	@Override
	public HMModel train() {
		HMModel preModel, currentModel;
		currentModel = model;
		int current_iteration = 0;
		do{
			preModel = currentModel;
			currentModel = iterate(preModel, trainSequences);
			current_iteration++;
		}while(convergencyJudge.isConvergency(preModel, currentModel, trainSequences, current_iteration));
		
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
		
		double[][] beta, alpha;
		double[][][] allGamma = new double[trainSequences.size()][][];
		double aijNum[][] = new double[N][N];
		double aijDen[] = new double[N];
		double aijDenT[] = new double[N];
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
		
			double[][][] xi = calcXi(model, sequence.length(), observationsIndex, alpha, beta);
			double[][] gamma = allGamma[no] = calcGamma(model, sequence.length(), alpha, beta);
			
			for(int i = 0; i < N; i++) {
				for(int t = 0; t < T; t++) {
					aijDenT[i] += gamma[t][i];
					if(t != T - 1)
						aijDen[i] += gamma[t][i];
					
					for(int j = 0; j < N; j++)
						aijNum[i][j] += xi[t][i][j];
				}
			
				for(int k = 0; k < M; k++) {
					sum1 = sum2 = 0.0;
					for(int t = 0; t < T; t++) {
						if(sequence.get(t).equals(dict.getState(k)))
							sum1 += gamma[t][i];
						sum2 += gamma[t][i];
					}
					
					tempEmissionMatrixNumerator[i][k] += sum1;
					tempEmissionMatrixDenominator[i][k] += sum2;
				}
			}			
		}

		//计算初始转移概率
		double sum = 0.0;
		for(int no = 0; no < sequences.size(); no++) {
			for(int i = 0; i < N; i++) {
				State state = dict.getState(i);
				if(pi.containsKey(state))
					pi.put(state, pi.get(state) + allGamma[no][0][i] / sequences.size());
				else
					pi.put(state, allGamma[no][0][i] / sequences.size());
				
				sum += allGamma[no][0][i] / sequences.size();
			}
		}
		//归一化并取对数
		for(Entry<State, Double> entry : pi.entrySet()) {
			entry.setValue(Math.log10(entry.getValue() / sum));
		}
		
		//计算转移概率
		for(int i = 0; i < N; i++) {
			StateSequence start = new StateSequence(dict.getState(i));
			TransitionProbEntry entry = new TransitionProbEntry();
			sum = 0.0;
			double prob = 0.0;
			if(aijDen[i] == 0.0) { // 状态i不可以作为转移起点
				for(int j = 0; j < N; j++) {
					State target = dict.getState(j);
					prob = Math.pow(10, transitionMatrix.get(start).getTransitionLogProb(target));
					sum += prob;
					entry.put(target, prob);
				}
				//归一化
				for(int j = 0; j < N; j++) {
					State target = dict.getState(j);
					entry.put(target, Math.log10(entry.getTransitionLogProb(target) / sum));
				}
			}else {
				for(int j = 0; j < N; j++) {
					State target = dict.getState(j);
					prob = Math.pow(10, aijNum[i][j] / aijDen[i]);
					sum += prob;
					entry.put(target, prob);
				}
				//归一化
				for(int j = 0; j < N; j++) {
					State target = dict.getState(j);
					entry.put(target, Math.log10(entry.getTransitionLogProb(target) / sum));
				}
			}
			
			transitionMatrix.put(start, entry);
		}
		
		//计算发射概率
		for(int i = 0; i < N; i++) {
			State state = dict.getState(i);
			EmissionProbEntry entry = new EmissionProbEntry();
			sum = 0.0;
			double prob = 0.0;
			Observation observation;
			for(int j = 0; j < M; j++) {
				observation = dict.getObservation(j);
				prob = tempEmissionMatrixNumerator[i][j] / tempEmissionMatrixDenominator[i][j];
				sum += prob;
				entry.put(observation, prob);
			}
			
			//归一化
			for(int j = 0; j < M; j++) {
				observation = dict.getObservation(j);
				entry.put(observation, Math.log10(entry.getEmissionLogProb(observation) / sum));
			}
			
			emissionMatrix.put(state, entry);
		}

		return new HMModelBasedMap(1, dict, pi, transitionMatrix, emissionMatrix);
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
		int N = model.statesCount();
		double[][] gamma = new double[T][N];
		
		for(int t = 1; t < T; t++) {
			double denominator = 0.0;
			for(int j = 1; j < N; j++) {
				gamma[t][j] = Math.pow(10, alpha[t][j]) * Math.pow(10, beta[t][j]);
				denominator += gamma[t][j];
			}

			for(int i = 1; i < N; i++) 
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
		int N = model.statesCount();
		double[][][] xi = new double[T][N][N];

		for (int t = 1; t < T - 1; t++) {
			double denominator = 0.0;
			for (int i = 1; i < N; i++) {
				for (int j = 1; j < N; j++) {
					xi[t][i][j] = Math.pow(10, alpha[t][i]) * Math.pow(10, beta[t+1][j]) * Math.pow(10, model.transitionLogProb(new int[]{i}, j)) * Math.pow(10, model.emissionLogProb(j, observations[t+1]));
					denominator += xi[t][i][j];
				}
			}
			
			for(int i = 1; i < N; i++) {
				for(int j = 1; j < N; j++) 
					xi[t][i][j]  /= denominator;
			}
		}
		
		return xi;
	}
}