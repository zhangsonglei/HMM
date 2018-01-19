package hust.tools.hmm.learn;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import hust.tools.hmm.model.ARPAEntry;
import hust.tools.hmm.model.EmissionProbEntry;
import hust.tools.hmm.model.HMModel;
import hust.tools.hmm.model.HMModelBasedBO;
import hust.tools.hmm.stream.SupervisedHMMSample;
import hust.tools.hmm.stream.SupervisedHMMSampleStream;
import hust.tools.hmm.utils.Observation;
import hust.tools.hmm.utils.State;
import hust.tools.hmm.utils.StateSequence;

/**
 *<ul>
 *<li>Description: 基于最大似然估计的的监督学习模型训练类器
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月18日
 *</ul>
 */
public class SupervisedMLHMMTrainer extends AbstractSupervisedHMMTrainer {

	public SupervisedMLHMMTrainer(TransitionAndEmissionCounter counter) {
		super(counter);
	}
	
	public SupervisedMLHMMTrainer(SupervisedHMMSampleStream<?> sampleStream, int order) throws IOException {
		super(sampleStream, order);
	}

	public SupervisedMLHMMTrainer(List<SupervisedHMMSample> samples, int order) throws IOException {
		super(samples, order);
	}

	@Override
	public HMModel train() {
		calcPi(counter);
		calcTransitionMatrix(counter);
		calcEmissionMatrix(counter);
		
		HMModel model = new HMModelBasedBO(order, counter.getDictionary(), pi, transitionMatrix, emissionMatrix);
		
		return model;
	}
	
	/**
	 * 计算初始概率矩阵
	 * @param counter	转移发射计数器
	 */	
	@Override
	protected void calcPi(TransitionAndEmissionCounter counter) {
		int M = counter.getTotalStartStatesCount();
		
		Set<State> set = counter.getDictionary().getStates();
		for(State state : set) {
			int count = counter.getStartStateCount(state);
			double prob = 1.0 * count / M;
			if(prob == 0.0)
				pi.put(state, 0.0);
			else
				pi.put(state, Math.log10(prob));
		}
	}
	
	@Override
	protected void calcTransitionMatrix(TransitionAndEmissionCounter counter) {
		Iterator<StateSequence> iterator = counter.transitionIterator();
		while(iterator.hasNext()) {//遍历所有转移状态,计算转移概率
			StateSequence sequence = iterator.next();
			double prob = calcTransitionMLProbability(sequence, counter);
			if(prob <= 0)
				transitionMatrix.put(sequence, new ARPAEntry(0, 0));
			else
				transitionMatrix.put(sequence, new ARPAEntry(Math.log10(prob), 0));
		}
	}
	
	/**
	 * 计算发射概率矩阵:p=C/M
	 * @param counter	转移发射计数器
	 */	
	protected void calcEmissionMatrix(TransitionAndEmissionCounter counter) {
		Iterator<State> iterator = counter.emissionIterator();

		while(iterator.hasNext()) {//遍历所有发射状态
			State state = iterator.next();
			Iterator<Observation> observationsIterator = counter.iterator(state);
			int M = counter.getStateCount(state);//以state为发射起点的总数量
			EmissionProbEntry emissionProbEntry = new EmissionProbEntry();
			while(observationsIterator.hasNext()) {//计算当前状态的所有发射概率
				Observation observation = observationsIterator.next();
				int C = counter.getEmissionCount(state, observation);//当前发射的数量
				double prob = 1.0 * C / M;
				emissionProbEntry.put(observation, Math.log10(prob));
			}
			emissionMatrix.put(state, emissionProbEntry);
		}//end while
	}
}
