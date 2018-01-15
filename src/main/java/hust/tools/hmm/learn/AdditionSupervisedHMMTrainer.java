package hust.tools.hmm.learn;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import hust.tools.hmm.model.ARPAEntry;
import hust.tools.hmm.model.EmissionProbEntry;
import hust.tools.hmm.model.TransitionProbEntry;
import hust.tools.hmm.stream.SupervisedHMMSample;
import hust.tools.hmm.stream.SupervisedHMMSampleStream;
import hust.tools.hmm.utils.Observation;
import hust.tools.hmm.utils.State;
import hust.tools.hmm.utils.StateSequence;

/**
 *<ul>
 *<li>Description: 基于加法平滑的监督学习训练类 
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月10日
 *</ul>
 */
public class AdditionSupervisedHMMTrainer extends AbstractSupervisedHMMTrainer {

	/**
	 * 默认加1(Laplace)平滑
	 */
	private final double DEFAULT_DELTA = 1.0;
	
	/**
	 * 加法平滑中加数大小
	 */
	private double delta;
	
	public AdditionSupervisedHMMTrainer(TransitionAndEmissionCounter counter) {
		super(counter);
		this.delta = DEFAULT_DELTA;
	}
	
	public AdditionSupervisedHMMTrainer(TransitionAndEmissionCounter counter, double delta) {
		super(counter);
		this.delta = delta <= 0 ? DEFAULT_DELTA : delta;
	}
	
	public AdditionSupervisedHMMTrainer(SupervisedHMMSampleStream<?> sampleStream, int order) throws IOException {
		super(sampleStream, order);
		this.delta = DEFAULT_DELTA;
	}
	
	public AdditionSupervisedHMMTrainer(SupervisedHMMSampleStream<?> sampleStream, int order, double delta) throws IOException {
		super(sampleStream, order);
		this.delta = delta <= 0 ? DEFAULT_DELTA : delta;
	}
	
	public AdditionSupervisedHMMTrainer(List<SupervisedHMMSample> samples, int order) throws IOException {
		super(samples, order);
		this.delta = DEFAULT_DELTA;
	}
	
	public AdditionSupervisedHMMTrainer(List<SupervisedHMMSample> samples, int order, double delta) throws IOException {
		super(samples, order);
		this.delta = delta <= 0 ? DEFAULT_DELTA : delta;
	}
	
	/**
	 * 计算初始概率矩阵
	 * @param counter	转移发射计数器
	 */	
	@Override
	protected void calcPi(TransitionAndEmissionCounter counter) {
		int N = dict.stateCount();
		int M = counter.getTotalStartStatesCount();
		
		Iterator<State> iterator = counter.getDictionary().statesIterator();
		State state = null;
		while(iterator.hasNext()) {
			state = iterator.next();
			int count = counter.getStartStateCount(state);
			double prob = (count + 1.0) / (M + N);
			pi.put(state, new ARPAEntry(Math.log10(prob), 0));
		}
		
		//计算回退权重
		iterator = pi.keySet().iterator();
		while (iterator.hasNext()) {
			state = iterator.next();
			double logBow = Math.log10(calcBOW(new StateSequence(state)));
			pi.put(state, pi.get(state).setLog_bo(logBow));
		}
	}
	
	@Override
	protected void calcTransitionMatrix(TransitionAndEmissionCounter counter) {
		Iterator<StateSequence> iterator = counter.transitionIterator();
		
		while(iterator.hasNext()) {//遍历所有转移状态,计算转移概率
			StateSequence start = iterator.next();
			Iterator<State> statesIterator = counter.iterator(start);
			
			TransitionProbEntry transitionProbEntry = new TransitionProbEntry();
			ARPAEntry entry = null;
			State target = null;
			double prob = 0;
			double totalCount = counter.getSequenceCount(start);
			while(statesIterator.hasNext()) {//计算当前状态的所有转移概率
				target = statesIterator.next();
				prob = (counter.getTransitionCount(start, target) + delta)/ (totalCount + delta * counter.getDictionary().stateCount());
				entry = new ARPAEntry(Math.log10(prob), 0);
				transitionProbEntry.put(target, entry);
			}
			
			transitionMatrix.put(start, transitionProbEntry);
		}
		
		iterator = counter.transitionIterator();
		
		while(iterator.hasNext()) {//遍历所有转移状态， 计算回退权重
			StateSequence start = iterator.next();
			if(start.length() < order) {//最高阶无回退权重
				Iterator<State> statesIterator = counter.iterator(start);
				TransitionProbEntry transitionProbEntry = transitionMatrix.get(start);
				
				while(statesIterator.hasNext()) {//计算当前状态的所有转移的回退权重
					State target = statesIterator.next();
					ARPAEntry entry = transitionProbEntry.get(target);
					if(entry != null) {
						double logBow = Math.log10(calcBOW(start.add(target)));
						transitionProbEntry.put(target, entry.setLog_bo(logBow));
					}
				}
				
				transitionMatrix.put(start, transitionProbEntry);
			}//end if
		}//end while
	}
	
	/**
	 * 采用加1平滑方式计算发射概率矩阵:p=(C+1)/(M+N)
	 * @param counter	转移发射计数器
	 */	
	protected void calcEmissionMatrix(TransitionAndEmissionCounter counter) {
		Iterator<State> iterator = counter.emissionIterator();
		long N = counter.getDictionary().observationCount();//观测状态的类型数
		while(iterator.hasNext()) {//遍历所有发射状态
			State state = iterator.next();
			Iterator<Observation> observationsIterator = counter.iterator(state);
			long M = counter.getStateCount(state);//以state为发射起点的总数量
			EmissionProbEntry emissionProbEntry = new EmissionProbEntry();
			while(observationsIterator.hasNext()) {//计算当前状态的所有发射概率
				Observation observation = observationsIterator.next();
				long C = counter.getEmissionCount(state, observation);//当前发射的数量
				double prob = 1.0 * (C + 1) / (M + N);
				emissionProbEntry.put(observation, Math.log10(prob));
			}

			emissionProbEntry.put(UNKNOWN, Math.log10(1.0 / (M + N)));
			emissionMatrix.put(state, emissionProbEntry);
		}//end while
	}
}
