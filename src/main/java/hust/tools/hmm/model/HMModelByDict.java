package hust.tools.hmm.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import hust.tools.hmm.learn.HMMTrainer;
import hust.tools.hmm.utils.CommonUtils;
import hust.tools.hmm.utils.Dictionary;
import hust.tools.hmm.utils.Observation;
import hust.tools.hmm.utils.State;
import hust.tools.hmm.utils.StateSequence;

/**
 *<ul>
 *<li>Description: 根据字典生成均值模型(转移概率取均值，存在的发射平分90%的概率，不存在的发射平分10%的概率)
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年3月6日
 *</ul>
 */
public class HMModelByDict implements HMMTrainer {

	private Collection<Observation> observationsSet;
	private Collection<State> statesSet;
	private HashMap<State, List<Observation>> dict;
	
	/**
	 * 构造方法, 根据字典生成初始模型
	 * @param observationsSet	观测集
	 * @param statesSet			状态集
	 * @param dict				字典(观测与其状态的映射)
	 */
	public HMModelByDict(Collection<Observation> observationsSet, Collection<State> statesSet, HashMap<State, List<Observation>> dict) {		
		this.statesSet = statesSet;
		this.observationsSet = observationsSet;
		this.dict = dict;
	}
	
	@Override
	public HMModel train() {
		Dictionary indexDict = new Dictionary();
		HashMap<State, Double> pi = new HashMap<>();
		HashMap<StateSequence, TransitionProbEntry> transitionMatrix = new HashMap<>();
		HashMap<State, EmissionProbEntry> emissionMatrix = new HashMap<>();

		for(State state : statesSet)
			indexDict.add(state);
		
		for(Observation observation : observationsSet)
			indexDict.add(observation);
		indexDict.add(CommonUtils.UNKNOWN);
		
		int M = indexDict.observationCount();
		int N = indexDict.stateCount();
				
		for(int i = 0; i < N; i++) {
			State start = indexDict.getState(i);
			//为初始概率矩阵赋均值
			pi.put(start, Math.log10(1.0 / N));
			
			//为转移概率矩阵赋均值
			TransitionProbEntry transitionProbEntry = new TransitionProbEntry();
			for(int j = 0; j < N; j++) 
				transitionProbEntry.put(indexDict.getState(j), Math.log10(1.0 / N));
			transitionMatrix.put(new StateSequence(start), transitionProbEntry);
			
			//为发射概率矩阵赋均值，字典中存在的发射平分90%，不存在的发射平分10%
			for(int j = 0; j < M; j++) {
				Observation observation = indexDict.getObservation(j);
				int existCount = 0;
				if(dict.containsKey(start))
					existCount = dict.get(start).size();	//字典中存在的发射数量

				int unExistCount = N - existCount;				//字典中不存在的发射数量
				
				double logProb = 0.0;
				if(existCount != 0) {
					if(dict.get(start).contains(observation)) 
						logProb = 0.9 /existCount;	//字典中存在的发射平分90%
					else
						logProb = 0.1 /unExistCount;//字典中不存在的发射平分10%
				}else 
					logProb = 1.0 / N;
				
				EmissionProbEntry entry = null;
				if(emissionMatrix.containsKey(start)) 
					entry = emissionMatrix.get(start);
				else
					entry = new EmissionProbEntry();
				
				entry.put(observation, Math.log10(logProb));
				emissionMatrix.put(start, entry);
			}
		}
		
		return new HMModelBasedMap(1, indexDict, pi, transitionMatrix, emissionMatrix);
	}
}
