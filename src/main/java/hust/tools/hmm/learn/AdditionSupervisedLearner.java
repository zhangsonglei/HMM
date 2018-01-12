package hust.tools.hmm.learn;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import hust.tools.hmm.model.ARPAEntry;
import hust.tools.hmm.model.HMMModel;
import hust.tools.hmm.model.TransitionProbEntry;
import hust.tools.hmm.stream.SupervisedHMMSample;
import hust.tools.hmm.stream.SupervisedHMMSampleStream;
import hust.tools.hmm.utils.State;
import hust.tools.hmm.utils.StateSequence;

/**
 *<ul>
 *<li>Description: 基于加法平滑的监督学习方法 
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月10日
 *</ul>
 */
public class AdditionSupervisedLearner extends AbstractSupervisedLearner {

	private final double DEFAULT_DELTA = 1.0;
	
	private double delta;
	
	public AdditionSupervisedLearner(TransitionAndEmissionCounter counter) {
		super(counter);
		this.delta = DEFAULT_DELTA;
	}
	
	public AdditionSupervisedLearner(TransitionAndEmissionCounter counter, double delta) {
		super(counter);
		this.delta = delta <= 0 ? DEFAULT_DELTA : delta;
	}
	
	public AdditionSupervisedLearner(SupervisedHMMSampleStream<?> sampleStream, int order) throws IOException {
		super(sampleStream, order);
		this.delta = DEFAULT_DELTA;
	}
	
	public AdditionSupervisedLearner(SupervisedHMMSampleStream<?> sampleStream, int order, double delta) throws IOException {
		super(sampleStream, order);
		this.delta = delta <= 0 ? DEFAULT_DELTA : delta;
	}
	
	public AdditionSupervisedLearner(List<SupervisedHMMSample> samples, int order) throws IOException {
		super(samples, order);
		this.delta = DEFAULT_DELTA;
	}
	
	public AdditionSupervisedLearner(List<SupervisedHMMSample> samples, int order, double delta) throws IOException {
		super(samples, order);
		this.delta = delta <= 0 ? DEFAULT_DELTA : delta;
	}
	
	public HMMModel train(File modelFile) {
		calcPi(counter);
		calcTransitionMatrix(counter);
		calcEmissionMatrix(counter);
		
		HMMModel model = new HMMModel(counter.getDictionary(), pi, transitionMatrix, emissionMatrix);
		
		if(modelFile != null)
			writeModel(model, modelFile);
		
		return model;
	}
	
	@Override
	public HMMModel train() {
		return train(null);
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
				
				TransitionProbEntry transitionProbEntry = new TransitionProbEntry();
				while(statesIterator.hasNext()) {//计算当前状态的所有转移的回退权重
					State target = statesIterator.next();
					ARPAEntry entry = transitionProbEntry.get(target);
					if(entry != null) {
						double logBow = Math.log10(calcBOW(start.add(target)));
						transitionProbEntry.put(target, entry.setLog_bo(logBow));
					}
				}
				
				transitionMatrix.put(start, transitionProbEntry);
			}
		}
	}
}
