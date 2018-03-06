package hust.tools.hmm.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

import hust.tools.hmm.learn.HMMTrainer;
import hust.tools.hmm.utils.CommonUtils;
import hust.tools.hmm.utils.Dictionary;
import hust.tools.hmm.utils.Observation;
import hust.tools.hmm.utils.State;
import hust.tools.hmm.utils.StateSequence;

/**
 *<ul>
 *<li>Description: 生成随机HMM模型
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年3月6日
 *</ul>
 */
public class HMModelByRandom implements HMMTrainer {

	private Collection<Observation> observationsSet;
	private Collection<State> statesSet;
	private long seed;
		
	/**
	 * 构造方法，随机生成初始模型
	 * @param observationsSet	观测集
	 * @param statesSet			状态集
	 * @param seed				随机种子
	 */
	public HMModelByRandom(Collection<Observation> observationsSet, Collection<State> statesSet, long seed) {		
		this.statesSet = statesSet;
		this.observationsSet = observationsSet;
		this.seed = seed;
	}
	
	@Override
	public HMModel train() {
		Dictionary dict = new Dictionary();
		HashMap<State, Double> pi = new HashMap<>();
		HashMap<StateSequence, TransitionProbEntry> transitionMatrix = new HashMap<>();
		HashMap<State, EmissionProbEntry> emissionMatrix = new HashMap<>();
		
		for(State state : statesSet)
			dict.add(state);
		
		for(Observation observation : observationsSet)
			dict.add(observation);
		dict.add(CommonUtils.UNKNOWN);
		
		int N = dict.stateCount();
		int M = dict.observationCount();

		Random rand = new Random(seed);
		double[] Pi = new double[N];
		double[][] A = new double[N][N];
		double[][] B = new double[N][M];
		
		double sumPi = 0.0;
		for(int i = 0; i < N; i++) {
			State start = dict.getState(i);
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
			TransitionProbEntry transitionProbEntry = new TransitionProbEntry();
			for(int j = 0; j < N; j++) {
				State target = dict.getState(j);
				transitionProbEntry.put(target, Math.log10(A[i][j] / sumA));
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
				Observation observation = dict.getObservation(j);
				EmissionProbEntry entry = null;
				if(emissionMatrix.containsKey(start)) 
					entry = emissionMatrix.get(start);
				else
					entry = new EmissionProbEntry();
				
				entry.put(observation, Math.log10(B[i][j] / sumB));
				emissionMatrix.put(start, entry);
			}
		}
		
		//初始转移概率归一化
		for(int i = 0; i < N; i++)
			pi.put(dict.getState(i), Math.log10(Pi[i] / sumPi));
		
		return new HMModelBasedMap(1, dict, pi, transitionMatrix, emissionMatrix);
	}
}
