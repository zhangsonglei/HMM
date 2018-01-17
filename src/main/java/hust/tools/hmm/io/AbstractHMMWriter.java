package hust.tools.hmm.io;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import hust.tools.hmm.model.ARPAEntry;
import hust.tools.hmm.model.EmissionProbEntry;
import hust.tools.hmm.model.HMModelBasedBO;
import hust.tools.hmm.utils.Dictionary;
import hust.tools.hmm.utils.Observation;
import hust.tools.hmm.utils.State;
import hust.tools.hmm.utils.StateSequence;

/**
 *<ul>
 *<li>Description: 写HMM模型抽象类
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月14日
 *</ul>
 */
public abstract class AbstractHMMWriter implements HMMWriter {

	private int order;
	
	private Dictionary dictionary;
	
	private HashMap<State, Double> pi;
	
	private HashMap<StateSequence, ARPAEntry>  transitionMatrix;
	
	private HashMap<State, EmissionProbEntry>  emissionMatrix;
	
	private long[] counts;
	
	public AbstractHMMWriter(HMModelBasedBO model) {
		order = model.getOrder();
		dictionary = model.getDict();
		pi = model.getPi();
		transitionMatrix = model.getTransitionMatrix();
		emissionMatrix = model.getEmissionMatrix();
		counts = new long[6];
		
		statCount();
	}

	private void statCount() {
		counts[0] = order;							//模型阶数
		counts[1] = dictionary.stateCount();		//隐藏状态数量
		counts[2] = dictionary.observationCount();	//观测状态数量
		counts[3] = pi.size();						//隐藏状态数量
		counts[4] = transitionMatrix.size();		//转移条目数量
		
		long total = 0;
		for(Entry<State, EmissionProbEntry> entry : emissionMatrix.entrySet())
			total += entry.getValue().size();
		
		counts[5] = total;			//发射条目数量
	}

	@Override
	public void persist() throws IOException {
		//写出各个条目的数量
		for(long count : counts)
			writeCount(count);
		
		//写出隐藏状态索引
		Iterator<State> statesIterator = dictionary.statesIterator();
		while(statesIterator.hasNext()) {
			State  state = statesIterator.next();
			writeStateIndex(new StateIndex(state, dictionary.getIndex(state)));
		}
		
		//写出观测状态索引
		Iterator<Observation> observationsIterator = dictionary.observationsIterator();
		while(observationsIterator.hasNext()) {
			Observation  observation = observationsIterator.next();
			writeObservationIndex(new ObservationIndex(observation, dictionary.getIndex(observation)));
		}
		
		//写出初始转移向量
		for(Entry<State, Double> entry : pi.entrySet())
			writePi(new PiEntry(entry.getKey(), entry.getValue()));
	
		//写出状态转移概率矩阵
		for(Entry<StateSequence, ARPAEntry> entry : transitionMatrix.entrySet()) 
			writeTransitionMatrix(new TransitionEntry(entry.getKey(), entry.getValue()));
					
		//写出发射概率矩阵
		for(Entry<State, EmissionProbEntry> entry : emissionMatrix.entrySet()) {
			Iterator<Entry<Observation, Double>> iterator = entry.getValue().entryIterator();
					
			while(iterator.hasNext()) {
				Entry<Observation, Double> probEntry = iterator.next();
				writeEmissionMatrix(new EmissionEntry(entry.getKey(), probEntry.getKey(), probEntry.getValue()));
			}
		}
		
		close();
	}
}
