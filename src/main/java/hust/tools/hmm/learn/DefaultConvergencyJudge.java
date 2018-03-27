package hust.tools.hmm.learn;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Set;

import hust.tools.hmm.model.ForwardAlgorithm;
import hust.tools.hmm.model.HMModel;
import hust.tools.hmm.utils.Observation;
import hust.tools.hmm.utils.ObservationSequence;
import hust.tools.hmm.utils.State;
import hust.tools.hmm.utils.StateSequence;

/**
 *<ul>
 *<li>Description: 默认模型收敛判断, 两个模型对相同观测序列计算的概率值的差小于某个阈值（0.01）,或者迭代次数大于阈值(100)
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年3月5日
 *</ul>
 */
public class DefaultConvergencyJudge implements ConvergencyJudge {

	/**
	 * 默认迭代次数
	 */
	private final int DEFAULT_ITERATION = 30;
	
	private double min;
	private double max;
	
	@Override
	public boolean isConvergency(HMModel preModel, HMModel currentModel, List<ObservationSequence> trainSequences, int iteration) {	
		double preLogProb, currentLogProb;
		preLogProb = currentLogProb = 0.0;
		ForwardAlgorithm algorithm = null;
		
		try {
			write(preModel, currentModel, iteration);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for(ObservationSequence sequence : trainSequences) {
			algorithm = new ForwardAlgorithm(preModel, sequence);
			preLogProb += algorithm.getProb();
			algorithm = new ForwardAlgorithm(currentModel, sequence);
			currentLogProb += algorithm.getProb();
		}
		
		System.out.println("iter = " + iteration + "\tpreLogProb = " + Math.log10(preLogProb) +"\tcurrentLogProb = " + Math.log10(currentLogProb));
		System.out.println("max = " + max + "\tmin = " + min);
		
		if(iteration >= DEFAULT_ITERATION)
			return true;
				
		return false;
	}
	
	private void write(HMModel pre, HMModel current, int iter) throws IOException{
		min = Double.MAX_VALUE;
		max = Double.MIN_VALUE;
		
		File file = new File("F:\\param\\"+iter+"iter.txt");
		OutputStreamWriter oWriter = new OutputStreamWriter(new FileOutputStream(file), "utf8");
		BufferedWriter writer = new BufferedWriter(oWriter);
		
		Set<State> states = pre.getDict().getStates();
		Set<Observation> observations = pre.getDict().getObservations();
		
		double preProb = 0.0;
		double curProb = 0.0;
		writer.write("状态\t前概率\t当前概率");
		writer.newLine();
		for(State state : states) {
			preProb = pre.getLogPi(state);
			curProb = current.getLogPi(state);
			max = Math.max(max, Math.abs(Math.pow(10, curProb) - Math.pow(10, preProb)));
			min = Math.min(min, Math.abs(Math.pow(10, curProb) - Math.pow(10, preProb)));
			writer.write(state + "\t" + preProb + "\t" + curProb);
			writer.newLine();
			
			
			for(State target : states) {
				StateSequence start = new StateSequence(state);
				preProb = pre.transitionLogProb(start, target);
				curProb = current.transitionLogProb(new StateSequence(state), target);
				max = Math.max(max, Math.abs(Math.pow(10, curProb) - Math.pow(10, preProb)));
				min = Math.min(min, Math.abs(Math.pow(10, curProb) - Math.pow(10, preProb)));
				writer.write(start+ "\t" + target + "\t" + preProb + "\t" + curProb);
				writer.newLine();
			}
			
			for(Observation observation : observations) {
				preProb = pre.emissionLogProb(state, observation);
				curProb = current.emissionLogProb(state, observation);
				max = Math.max(max, Math.abs(Math.pow(10, curProb) - Math.pow(10, preProb)));
				min = Math.min(min, Math.abs(Math.pow(10, curProb) - Math.pow(10, preProb)));
				writer.write(state + "\t" + observation + "\t" + preProb + "\t" + curProb);
				writer.newLine();
			}
		}
		writer.close();
	}
}