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
 *<li>Description: 基于AStar解码的HMM模型单元测试
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年2月27日
 *</ul>
 */
public class HMMWithAStarTest {

	private int order;
	private HMM model;
	private ObservationSequence sequence1;
	private ObservationSequence sequence2;
	
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
		model = new HMMWithAStar(learner.train());
		
		Observation[] testObservaitons = new StringObservation[]{
				new StringObservation("2"),
				new StringObservation("4"),
				new StringObservation("0")};//未登录词
		sequence1 = new ObservationSequence(testObservaitons);
		
		testObservaitons = new StringObservation[]{
				new StringObservation("5"),
				new StringObservation("3"),
				new StringObservation("1")};
		sequence2 = new ObservationSequence(testObservaitons);
	}

	@Test
	public void testBestStateSeqence() {
		State[] states = new StringState[]{new StringState("b"), new StringState("a"), new StringState("b")};
		assertEquals(new StateSequence(states), model.bestStateSeqence(sequence1));
		
		states = new StringState[]{new StringState("a"), new StringState("b"), new StringState("a")};
		assertEquals(new StateSequence(states), model.bestStateSeqence(sequence2));
	}

	@Test
	public void testGetProb() {
		double[] probs1 = new double[]{
				36.0 / (5*11*11*17*17*17), 	//aaa
				162.0 / (5*11*11*17*17*19), //aab
				729.0 / (5*11*14*17*19*17), //aba
				81.0 / (14*11*17*19*19),  	//abb
				1458.0 / (5*7*11*19*17*19),	//bab
				162.0 / (7*14*19*19*17),  	//bba
				324.0 / (5*7*11*19*17*17), 	//baa
				90.0 / (7*14*19*19*19)};	//bbb
		
		double[] probs2 = new double[]{
				120.0 / (11*11*17*17*17), //aaa
				216.0 / (11*11*17*17*19), //aab
				3645.0 / (11*7*17*19*17), //aba
				810.0 / (7*11*17*19*19),  //abb
				324.0 / (5*7*11*19*17*19),//bab
				135.0 / (7*7*19*19*17),	  //bba
				36.0 / (7*11*19*17*17),   //baa
				30.0 / (7*7*19*19*19)};	  //bbb
		
		double totalProb1 = 0;
		double totalProb2 = 0;
		for(int i = 0; i < 8; i++) {
			totalProb1 += probs1[i];
			totalProb2 += probs2[i];
		}
		
		assertEquals(totalProb1, model.getLogProb(sequence1), 0.00000000000000001);
		assertEquals(totalProb2, model.getLogProb(sequence2), 0.00000000000000001);
		
		State[] states = new StringState[]{new StringState("a"), new StringState("a"), new StringState("a")};
		assertEquals(Math.log10(probs1[0]), model.getLogProb(sequence1, new StateSequence(states)), 0.000000000000001);
		assertEquals(Math.log10(probs2[0]), model.getLogProb(sequence2, new StateSequence(states)), 0.000000000000001);
		
		states = new StringState[]{new StringState("a"), new StringState("a"), new StringState("b")};
		assertEquals(Math.log10(probs1[1]), model.getLogProb(sequence1, new StateSequence(states)), 0.000000000000001);
		assertEquals(Math.log10(probs2[1]), model.getLogProb(sequence2, new StateSequence(states)), 0.000000000000001);
		
		states = new StringState[]{new StringState("a"), new StringState("b"), new StringState("a")};
		assertEquals(Math.log10(probs1[2]), model.getLogProb(sequence1, new StateSequence(states)), 0.000000000000001);
		assertEquals(Math.log10(probs2[2]), model.getLogProb(sequence2, new StateSequence(states)), 0.000000000000001);
		
		states = new StringState[]{new StringState("a"), new StringState("b"), new StringState("b")};
		assertEquals(Math.log10(probs1[3]), model.getLogProb(sequence1, new StateSequence(states)), 0.000000000000001);
		assertEquals(Math.log10(probs2[3]), model.getLogProb(sequence2, new StateSequence(states)), 0.000000000000001);
		
		states = new StringState[]{new StringState("b"), new StringState("a"), new StringState("b")};
		assertEquals(Math.log10(probs1[4]), model.getLogProb(sequence1, new StateSequence(states)), 0.000000000000001);
		assertEquals(Math.log10(probs2[4]), model.getLogProb(sequence2, new StateSequence(states)), 0.000000000000001);
		
		states = new StringState[]{new StringState("b"), new StringState("b"), new StringState("a")};
		assertEquals(Math.log10(probs1[5]), model.getLogProb(sequence1, new StateSequence(states)), 0.000000000000001);
		assertEquals(Math.log10(probs2[5]), model.getLogProb(sequence2, new StateSequence(states)), 0.000000000000001);
		
		states = new StringState[]{new StringState("b"), new StringState("a"), new StringState("a")};
		assertEquals(Math.log10(probs1[6]), model.getLogProb(sequence1, new StateSequence(states)), 0.000000000000001);
		assertEquals(Math.log10(probs2[6]), model.getLogProb(sequence2, new StateSequence(states)), 0.000000000000001);
		
		states = new StringState[]{new StringState("b"), new StringState("b"), new StringState("b")};
		assertEquals(Math.log10(probs1[7]), model.getLogProb(sequence1, new StateSequence(states)), 0.000000000000001);
		assertEquals(Math.log10(probs2[7]), model.getLogProb(sequence2, new StateSequence(states)), 0.000000000000001);
	}
}