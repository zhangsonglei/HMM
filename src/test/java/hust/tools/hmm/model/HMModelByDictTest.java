package hust.tools.hmm.model;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import hust.tools.hmm.utils.CommonUtils;
import hust.tools.hmm.utils.Observation;
import hust.tools.hmm.utils.State;
import hust.tools.hmm.utils.StateSequence;
import hust.tools.hmm.utils.StringObservation;
import hust.tools.hmm.utils.StringState;

/**
 *<ul>
 *<li>Description: 测试基于词典生成HMM模型
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年3月6日
 *</ul>
 */
public class HMModelByDictTest {
	
	private HMModel model;
	private HashSet<State> statesSet;
	private HashSet<Observation> observationsSet;
	private HashMap<State, List<Observation>> dict;
	
	@Before
	public void setUp() throws Exception {
		observationsSet = new HashSet<>();
		statesSet = new HashSet<>();
		State[] hs = new State[]{new StringState("1"), new StringState("2"), new StringState("3"), new StringState("4"), new StringState("5")};
		Observation[] os = new Observation[]{new StringObservation("a"), new StringObservation("b"), new StringObservation("c"), new StringObservation("d")};

		for(State h : hs)
			statesSet.add(h);
		for(Observation o : os)
			observationsSet.add(o);
		
		dict = new HashMap<>();
		List<Observation> list = new ArrayList<>();
		list.add(new StringObservation("a"));
		list.add(new StringObservation("b"));
		dict.put(new StringState("1"), list);
		
		list = new ArrayList<>();
		list.add(new StringObservation("b"));
		list.add(new StringObservation("c"));
		list.add(new StringObservation("d"));
		dict.put(new StringState("3"), list);
		
		list = new ArrayList<>();
		list.add(new StringObservation("d"));
		dict.put(new StringState("4"), list);
		
		HMModelByDict initModelTrainer = new HMModelByDict(observationsSet, statesSet, dict);
		
		model = (HMModelBasedMap) initModelTrainer.train();
	}

	@Test
	public void testTrain() {
		for(State start : statesSet) {//初始转移概率为均值
			assertEquals(Math.log10(1.0 / 5), model.getLogPi(start), 0.000001);
		
			for(State target : statesSet)//转移概率为均值
				assertEquals(Math.log10(1.0 / 5), model.transitionLogProb(new StateSequence(start), target), 0.000001);
		
			for(Observation observation : observationsSet) {//词典中存在的发射，发射概率平分0.9，不存在的平分0.1
				if(start.equals(new StringState("1"))) {
					if(dict.get(start).contains(observation))
						assertEquals(Math.log10(0.9 / 2), model.emissionLogProb(start, observation), 0.000001);
					else
						assertEquals(Math.log10(0.1 / 3), model.emissionLogProb(start, observation), 0.000001);
					
					assertEquals(Math.log10(0.1 / 3), model.emissionLogProb(start, CommonUtils.UNKNOWN), 0.000001);
				}else if(start.equals(new StringState("3"))) {
					if(dict.get(start).contains(observation))
						assertEquals(Math.log10(0.9 / 3), model.emissionLogProb(start, observation), 0.000001);
					else
						assertEquals(Math.log10(0.1 / 2), model.emissionLogProb(start, observation), 0.000001);
					
					assertEquals(Math.log10(0.1 / 2), model.emissionLogProb(start, CommonUtils.UNKNOWN), 0.000001);
				}else if(start.equals(new StringState("4"))) {
					if(dict.get(start).contains(observation))
						assertEquals(Math.log10(0.9), model.emissionLogProb(start, observation), 0.000001);
					else
						assertEquals(Math.log10(0.1 / 4), model.emissionLogProb(start, observation), 0.000001);
					
					assertEquals(Math.log10(0.1 / 4), model.emissionLogProb(start, CommonUtils.UNKNOWN), 0.000001);
				}else {
					assertEquals(Math.log10(1.0 / 5), model.emissionLogProb(start, observation), 0.000001);
					assertEquals(Math.log10(1.0 / 5), model.emissionLogProb(start, CommonUtils.UNKNOWN), 0.000001);
				}
			}
		}		
	}
}
