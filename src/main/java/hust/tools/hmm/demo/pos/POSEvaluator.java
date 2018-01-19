package hust.tools.hmm.demo.pos;

import java.util.HashSet;
import java.util.List;
import hust.tools.hmm.model.HMM;
import hust.tools.hmm.stream.SupervisedHMMSample;
import hust.tools.hmm.utils.ObservationSequence;
import hust.tools.hmm.utils.StateSequence;


public class POSEvaluator {

	private HMM model;
	private List<SupervisedHMMSample> samples;
	private HashSet<String> dict;
	private int order;
		
	public POSEvaluator(HMM model, HashSet<String> dict, List<SupervisedHMMSample> samples, int order) {
		this.model = model;
		this.dict = dict;
		this.samples = samples;
		this.order = order;
	}
	
	public void eval() {
		EvaluateMeasure measure = new EvaluateMeasure(dict);
		
		for(SupervisedHMMSample sample : samples) {
			StateSequence refStateSeuence = sample.getStateSequence();
			ObservationSequence wordSequence = sample.getObservationSequence();
			
			StateSequence preStateSeuence = model.bestStateSeqence(wordSequence, order);
			String[] words = new String[wordSequence.length()];
			String[] refPOS = new String[refStateSeuence.length()];
			String[] prePOS = new String[refStateSeuence.length()];
			for(int i = 0; i < words.length; i++) {
				words[i] = wordSequence.get(i).toString();
				refPOS[i] = refStateSeuence.get(i).toString();
				prePOS[i] = preStateSeuence.get(i).toString();
			}
			measure.updateScores(words, refPOS, prePOS);
		}
						
		System.out.println(measure);
	}
}
