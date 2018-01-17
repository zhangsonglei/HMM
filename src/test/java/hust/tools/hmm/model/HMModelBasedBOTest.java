package hust.tools.hmm.model;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import hust.tools.hmm.learn.AdditionSupervisedHMMTrainer;
import hust.tools.hmm.stream.SupervisedHMMSample;
import hust.tools.hmm.utils.Observation;
import hust.tools.hmm.utils.ObservationSequence;
import hust.tools.hmm.utils.State;
import hust.tools.hmm.utils.StateSequence;
import hust.tools.hmm.utils.StringObservation;
import hust.tools.hmm.utils.StringState;

/**
 *<ul>
 *<li>Description: 基于Laplace平滑的HMM模型单元测试
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月12日
 *</ul>
 */
public class HMModelBasedBOTest {
	
	private int order;
	private HMModelBasedBO model;
	
	@Before
	public void setUp() throws Exception {
		order = 3;
		List<SupervisedHMMSample> samples = new ArrayList<>();
		StateSequence stateSequence = null;
		ObservationSequence observationSequence = null;
		
		String[] h1 = new String[]{"1", "2", "3", "4", "3", "2", "4", "5"};
		String[] o1 = new String[]{"a", "b", "c", "d", "d", "c", "b", "b"};
		State[] states = new StringState[h1.length];
		Observation[] observations = new StringObservation[o1.length];
		for(int i = 0; i < h1.length; i++) {
			states[i] = new StringState(h1[i]);
			observations[i] = new StringObservation(o1[i]);
		}
		stateSequence = new StateSequence(states);
		observationSequence = new ObservationSequence(observations);
		samples.add(new SupervisedHMMSample(stateSequence, observationSequence));
		
		String[] h2 = new String[]{"3", "3", "4", "5", "2", "2", "1", "3", "5"};
		String[] o2 = new String[]{"c", "a", "b", "c", "c", "a", "d", "a", "a"};
		states = new StringState[h2.length];
		observations = new StringObservation[o2.length];
		for(int i = 0; i < h2.length; i++) {
			states[i] = new StringState(h2[i]);
			observations[i] = new StringObservation(o2[i]);
		}
		stateSequence = new StateSequence(states);
		observationSequence = new ObservationSequence(observations);
		samples.add(new SupervisedHMMSample(stateSequence, observationSequence));
		
		String[] h3 = new String[]{"5", "2", "1", "4", "3", "1", "1"};
		String[] o3 = new String[]{"a", "b", "a", "c", "b", "a", "d"};
		states = new StringState[h3.length];
		observations = new StringObservation[o3.length];
		for(int i = 0; i < h3.length; i++) {
			states[i] = new StringState(h3[i]);
			observations[i] = new StringObservation(o3[i]);
		}
		stateSequence = new StateSequence(states);
		observationSequence = new ObservationSequence(observations);
		samples.add(new SupervisedHMMSample(stateSequence, observationSequence));
		
		AdditionSupervisedHMMTrainer learner = new AdditionSupervisedHMMTrainer(samples, order, 1.0);
		model = (HMModelBasedBO) learner.train();
	}

	//测试返回给定转移的概率
	@Test
	public void testTransitionProbStateSequenceState() {
		State[] states = new StringState[]{new StringState("3")};
		StateSequence start = new StateSequence(states);
		State target = new StringState("4");
		assertEquals(Math.log10(3.0/11), model.transitionProb(start, target), 0.01);
		
		states = new StringState[]{new StringState("5"), new StringState("2"), new StringState("1")};
		start = new StateSequence(states);
		assertTrue(Math.log10(2.0/6) == model.transitionProb(start, target));
	}

	//测试返回给定转移索引对应转移的概率
	@Test
	public void testTransitionProbIntArrayInt() {
		int[] start = new int[]{2};
		int target = 3;
		assertTrue(Math.log10(3.0/11) == model.transitionProb(start, target));
		
		start = new int[]{4, 1, 0};
		assertTrue(Math.log10(2.0/6) == model.transitionProb(start, target));
	}

	//测试返回给定发射的概率
	@Test
	public void testEmissionProbStateObservation() {
		State state = new StringState("2");
		Observation observation = new StringObservation("a");
		assertTrue(Math.log10(2.0/9) == model.emissionProb(state, observation));
		
		observation = new StringObservation("b");
		assertTrue(Math.log10(3.0/9) ==  model.emissionProb(state, observation));
		
		observation = new StringObservation("c");
		assertTrue(Math.log10(3.0/9) ==  model.emissionProb(state, observation));
		
		observation = new StringObservation("d");
		assertTrue(Math.log10(1.0/9) ==  model.emissionProb(state, observation));
		
		observation = new StringObservation("z");
		assertTrue(Math.log10(1.0/9) ==  model.emissionProb(state, observation));
	}

	//测试返回给定发射索引对应发射的概率
	@Test
	public void testEmissionProbIntInt() {
		int state = 1;
		int observation = 0;
		assertTrue(Math.log10(2.0/9) ==  model.emissionProb(state, observation));
		
		observation = 1;
		assertTrue(Math.log10(3.0/9) ==  model.emissionProb(state, observation));
		
		observation = 2;
		assertTrue(Math.log10(3.0/9) ==  model.emissionProb(state, observation));
		
		observation = 3;
		assertTrue(Math.log10(1.0/9) ==  model.emissionProb(state, observation));
	}

	//测试返回所有观测状态
	@Test
	public void testGetObservations() {
		Observation[] actual = model.getObservations();
		Observation[] observations = new StringObservation[]{
				new StringObservation("a"),
				new StringObservation("b"),
				new StringObservation("c"),
				new StringObservation("d")};
		
		List<Observation> list = Arrays.asList(observations);
		assertEquals(list.size(), actual.length);
		for(int i = 0; i < actual.length; i++)
			assertTrue(list.contains(actual[i]));
	}

	//测试返回所有隐藏状态
	@Test
	public void testGetStates() {
		State[] actual = model.getStates();
		State[] states = new StringState[]{
				new StringState("1"),
				new StringState("2"),
				new StringState("3"),
				new StringState("4"),
				new StringState("5")};
		
		List<State> list = Arrays.asList(states);
		assertEquals(list.size(), actual.length);
		for(int i = 0; i < actual.length; i++)
			assertTrue(list.contains(actual[i]));
	}

	//返回给定观测序列和隐藏状态序列的概率
	@Test
	public void testGetProbObservationSequenceStateSequenceInt() {
//		StateSequence stateSequence = new StateSequence();
//		ObservationSequence observationSequence = new ObservationSequence();
//		
//		assertEquals(0, model.getProb(observationSequence, stateSequence, order), 0.01);
	}

	//返回给定隐藏状态的初始概率
	@Test
	public void testGetPiState() {
		assertTrue(Math.log10(2.0/8) == model.getPi(new StringState("1")));
		assertTrue(Math.log10(1.0/8) == model.getPi(new StringState("2")));
		assertTrue(Math.log10(2.0/8) == model.getPi(new StringState("3")));
		assertTrue(Math.log10(1.0/8) == model.getPi(new StringState("4")));
		assertTrue(Math.log10(2.0/8) == model.getPi(new StringState("5")));
	}

	//返回给定隐藏状态索引对应索引的初始概率
	@Test
	public void testGetPiInt() {
		assertTrue(Math.log10(2.0/8) == model.getPi(0));
		assertTrue(Math.log10(1.0/8) == model.getPi(1));
		assertTrue(Math.log10(2.0/8) == model.getPi(2));
		assertTrue(Math.log10(1.0/8) == model.getPi(3));
		assertTrue(Math.log10(2.0/8) == model.getPi(4));
	}

	//测试返回给定索引对应的观测状态
	@Test
	public void testGetObservationInt() {
		assertEquals(new StringObservation("a"), model.getObservation(0));
		assertEquals(new StringObservation("b"), model.getObservation(1));
		assertEquals(new StringObservation("c"), model.getObservation(2));
		assertEquals(new StringObservation("d"), model.getObservation(3));
	}

	//测试返回给定观测状态的索引
	@Test
	public void testGetObservationIndexObservation() {
		assertEquals(0, model.getObservationIndex(new StringObservation("a")));
		assertEquals(1, model.getObservationIndex(new StringObservation("b")));
		assertEquals(2, model.getObservationIndex(new StringObservation("c")));
		assertEquals(3, model.getObservationIndex(new StringObservation("d")));
	}

	//测试返回给定索引对应的隐藏状态
	@Test
	public void testGetStateInt() {
		assertEquals(new StringState("1"), model.getState(0));
		assertEquals(new StringState("2"), model.getState(1));
		assertEquals(new StringState("3"), model.getState(2));
		assertEquals(new StringState("4"), model.getState(3));
		assertEquals(new StringState("5"), model.getState(4));
	}

	//测试返回给定隐藏状态的索引
	@Test
	public void testGetStateIndexState() {
		assertEquals(0, model.getStateIndex(new StringState("1")));
		assertEquals(1, model.getStateIndex(new StringState("2")));
		assertEquals(2, model.getStateIndex(new StringState("3")));
		assertEquals(3, model.getStateIndex(new StringState("4")));
		assertEquals(4, model.getStateIndex(new StringState("5")));
	}

	//测试返回模型中隐藏状态的类型数量
	@Test
	public void testGetStateCount() {
		assertEquals(5, model.statesCount());
	}

	//测试返回模型中观测状态的类型数量
	@Test
	public void testGetObservationCount() {
		assertEquals(4, model.observationsCount());
	}
}
