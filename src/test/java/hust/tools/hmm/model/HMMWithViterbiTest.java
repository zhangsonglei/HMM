package hust.tools.hmm.model;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import hust.tools.hmm.learn.SupervisedAdditionHMMTrainer;
import hust.tools.hmm.stream.SupervisedHMMSample;
import hust.tools.hmm.utils.Observation;
import hust.tools.hmm.utils.ObservationSequence;
import hust.tools.hmm.utils.State;
import hust.tools.hmm.utils.StateSequence;
import hust.tools.hmm.utils.StringObservation;
import hust.tools.hmm.utils.StringState;

/**
 *<ul>
 *<li>Description: 基于Viterbi解码的HMM模型单元测试
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月17日
 *</ul>
 */
public class HMMWithViterbiTest {

	private int order;
	private HMM model;
	private ObservationSequence sequence;
	
	@Before
	public void setUp() throws Exception {
		order = 1;
		List<SupervisedHMMSample> samples = new ArrayList<>();
		StateSequence stateSequence = null;
		ObservationSequence observationSequence = null;
		
		String[] h1 = new String[]{"a", "b", "b", "a", "b", "b", "b", "a"};
		String[] o1 = new String[]{"1", "2", "3", "4", "3", "2", "4", "5"};
		State[] states = new StringState[h1.length];
		Observation[] observations = new StringObservation[o1.length];
		for(int i = 0; i < h1.length; i++) {
			states[i] = new StringState(h1[i]);
			observations[i] = new StringObservation(o1[i]);
		}
		stateSequence = new StateSequence(states);
		observationSequence = new ObservationSequence(observations);
		samples.add(new SupervisedHMMSample(stateSequence, observationSequence));
		
		String[] h2 = new String[]{"b", "a", "b", "a", "b", "b", "a", "b", "a"};
		String[] o2 = new String[]{"3", "3", "4", "5", "2", "2", "1", "3", "5"};
		states = new StringState[h2.length];
		observations = new StringObservation[o2.length];
		for(int i = 0; i < h2.length; i++) {
			states[i] = new StringState(h2[i]);
			observations[i] = new StringObservation(o2[i]);
		}
		stateSequence = new StateSequence(states);
		observationSequence = new ObservationSequence(observations);
		samples.add(new SupervisedHMMSample(stateSequence, observationSequence));
		
		String[] h3 = new String[]{"a", "b", "a", "a", "b", "a", "b"};
		String[] o3 = new String[]{"5", "2", "1", "4", "3", "1", "1"};
		
		states = new StringState[h3.length];
		observations = new StringObservation[o3.length];
		for(int i = 0; i < h3.length; i++) {
			states[i] = new StringState(h3[i]);
			observations[i] = new StringObservation(o3[i]);
		}
		stateSequence = new StateSequence(states);
		observationSequence = new ObservationSequence(observations);
		samples.add(new SupervisedHMMSample(stateSequence, observationSequence));
		
		SupervisedAdditionHMMTrainer learner = new SupervisedAdditionHMMTrainer(samples, order, 1.0);
		model = new HMMWithViterbi(learner.train());
		
		Observation[] testObservaitons = new StringObservation[]{
				new StringObservation("2"),
				new StringObservation("1"),
				new StringObservation("5")};
		sequence = new ObservationSequence(testObservaitons);
	}

	@Test
	public void testBestStateSeqence() {
		State[] states = new StringState[]{new StringState("b"), new StringState("a"), new StringState("a")};
		assertEquals(new StateSequence(states), model.bestStateSeqence(sequence, 1));
	}

	@Test
	public void testGetProb() {
		double[] probs = new double[]{
				15.0 / (4*11*16*16*11), 
				3.0 / (11*16*16*11), 
				27.0 / (14*11*16*16), 
				1.0 / (14*11*16*6), 
				3.0 / (14*11*16), 
				5.0 / (14*14*6*4), 
				15.0 / (7*11*16*8), 
				5.0 / (7*7*18*18*3),};
		
		double totalProb = 0;
		for(double prob : probs)
			totalProb += prob;
		
		assertEquals(totalProb, model.getProb(sequence, 1), 0.00000000000000001);
		
		State[] states = new StringState[]{new StringState("a"), new StringState("a"), new StringState("a")};
		assertEquals(probs[0], model.getProb(sequence, new StateSequence(states), 1), 0.00000000000000001);
		
		states = new StringState[]{new StringState("a"), new StringState("a"), new StringState("b")};
		assertEquals(probs[1], model.getProb(sequence, new StateSequence(states), 1), 0.00000000000000001);
		
		states = new StringState[]{new StringState("a"), new StringState("b"), new StringState("a")};
		assertEquals(probs[2], model.getProb(sequence, new StateSequence(states), 1), 0.00000000000000001);
		
		states = new StringState[]{new StringState("a"), new StringState("b"), new StringState("b")};
		assertEquals(probs[3], model.getProb(sequence, new StateSequence(states), 1), 0.00000000000000001);
		
		states = new StringState[]{new StringState("b"), new StringState("a"), new StringState("b")};
		assertEquals(probs[4], model.getProb(sequence, new StateSequence(states), 1), 0.00000000000000001);
		
		states = new StringState[]{new StringState("b"), new StringState("b"), new StringState("a")};
		assertEquals(probs[5], model.getProb(sequence, new StateSequence(states), 1), 0.00000000000000001);
		
		states = new StringState[]{new StringState("b"), new StringState("a"), new StringState("a")};
		assertEquals(probs[6], model.getProb(sequence, new StateSequence(states), 1), 0.00000000000000001);
		
		states = new StringState[]{new StringState("b"), new StringState("b"), new StringState("b")};
		assertEquals(probs[7], model.getProb(sequence, new StateSequence(states), 1), 0.00000000000000001);
	}
}