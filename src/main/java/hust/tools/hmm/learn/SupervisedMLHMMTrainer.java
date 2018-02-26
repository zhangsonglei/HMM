package hust.tools.hmm.learn;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import hust.tools.hmm.model.EmissionProbEntry;
import hust.tools.hmm.model.HMModel;
import hust.tools.hmm.model.HMModelBasedMap;
import hust.tools.hmm.model.TransitionProbEntry;
import hust.tools.hmm.stream.SupervisedHMMSample;
import hust.tools.hmm.stream.SupervisedHMMSampleStream;
import hust.tools.hmm.utils.CommonUtils;
import hust.tools.hmm.utils.Observation;
import hust.tools.hmm.utils.State;
import hust.tools.hmm.utils.StateSequence;

/**
 *<ul>
 *<li>Description: 只包含发射概率的的监督学习模型训练类器
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
		
		HMModel model = new HMModelBasedMap(order, counter.getDictionary(), pi, transitionMatrix, emissionMatrix);
		
		return model;
	}
	
	/**
	 * 计算初始概率矩阵（已确保概率之和为1，不需要归一化）
	 * @param counter	转移发射计数器
	 */	
	@Override
	protected void calcPi(TransitionAndEmissionCounter counter) {
		int M = counter.getTotalStartStatesCount();
		
		Set<State> set = counter.getDictionary().getStates();
		for(State state : set) {
			int count = counter.getStartStateCount(state);
			if(count == 0)
				pi.put(state, Math.log10(Double.MIN_VALUE));
			else
				pi.put(state, Math.log10(1.0 * count / M));
		}
	}
	
	@Override
	protected void calcTransitionMatrix(TransitionAndEmissionCounter counter) {
		Set<State> statesSet = counter.getDictionary().getStates();
		for(State state : statesSet) {//遍历所有隐藏状态，增加所有可能的一阶转移
			StateSequence start = new StateSequence(state);
			int n_Count = counter.getTransitionStartCount(start);
			TransitionProbEntry entry = new TransitionProbEntry();
			for(State target : statesSet) {
				int count = counter.getTransitionCount(start, target);
				if(count != 0 && n_Count != 0)
					entry.put(target, Math.log10(1.0 * count / n_Count));
				else
					entry.put(target, Math.log10(Double.MIN_VALUE));
			}
			
			transitionMatrix.put(start, entry);
		}
		
		for(int i = 1; i < order; i++) {//遍历增加所有2-order阶的转移概率
			StateSequence[] sequences = transitionMatrix.keySet().toArray(new StateSequence[transitionMatrix.size()]);
			for(StateSequence sequence : sequences) {
				if(sequence.length() == i) {
					for(State state : statesSet) {
						StateSequence start = sequence.addLast(state);
						int n_Count = counter.getTransitionStartCount(start);
						
						TransitionProbEntry entry = new TransitionProbEntry();
						for(State target : statesSet) {
							int count = counter.getTransitionCount(start, target);
							if(count != 0 || n_Count != 0)
								entry.put(target, Math.log10(1.0 * count / n_Count));
							else
								entry.put(target, Math.log10(Double.MIN_VALUE));
						}
						
						transitionMatrix.put(start, entry);
					}
				}
			}
		}
	}
	
	/**
	 * 计算发射概率矩阵:p=C/M
	 * @param counter	转移发射计数器
	 */	
	protected void calcEmissionMatrix(TransitionAndEmissionCounter counter) {
		Iterator<State> iterator = counter.emissionIterator();
		
		while(iterator.hasNext()) {//遍历所有发射
			State state = iterator.next();
			Iterator<Observation> observationsIterator = counter.iterator(state);
			int M = counter.getEmissionStateCount(state);//以state为发射起点的总数量
			
			EmissionProbEntry emissionProbEntry = new EmissionProbEntry();
			while(observationsIterator.hasNext()) {//计算当前状态的所有发射概率
				Observation observation = observationsIterator.next();
				int C = counter.getEmissionCount(state, observation);//当前发射的数量
				double prob = 1.0 * C / M;
				emissionProbEntry.put(observation, Math.log10(prob));
			}

			emissionProbEntry.put(CommonUtils.UNKNOWN, Math.log10(Double.MIN_VALUE));
			emissionMatrix.put(state, emissionProbEntry);
		}//end while
	}
}
