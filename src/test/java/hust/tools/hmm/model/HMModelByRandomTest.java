package hust.tools.hmm.model;

import static org.junit.Assert.*;

import java.util.HashSet;
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
 *<li>Description: 测试随机生成HMM模型
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年3月6日
 *</ul>
 */
public class HMModelByRandomTest {

	private HMModel model;
	private HashSet<State> statesSet;
	private HashSet<Observation> observationsSet;
	
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
		
		HMModelByRandom initModelTrainer = new HMModelByRandom(observationsSet, statesSet, 3L);
		
		model = (HMModelBasedMap) initModelTrainer.train();
	}

	@Test
	public void testTrain() {
		double sum_pi, sum_A, sum_B;
		sum_pi = 0.0;
		
		for(State start : statesSet) {
			double prob = Math.pow(10, model.getLogPi(start));
			sum_pi += prob;
			assertTrue(prob > 0.0 && prob < 1.0);//初始转移概率均>0且<1.0
		
			sum_A = 0.0;
			for(State target : statesSet) {
				prob = Math.pow(10, model.transitionLogProb(new StateSequence(start), target));
				assertTrue(prob > 0.0 && prob < 1.0);//转移概率均>0且<1.0
				sum_A +=prob;
			}
			assertEquals(1.0, sum_A, 0.000001);//转移概率之和为1
			
			sum_B = 0.0;
			prob = Math.pow(10, model.emissionLogProb(start, CommonUtils.UNKNOWN));
			sum_B += prob;
			assertTrue(prob > 0.0 && prob < 1.0);//未登录词的发射>0且<1.0
			for(Observation observation : observationsSet) {
				prob = Math.pow(10, model.emissionLogProb(start, observation));
				sum_B += prob;
				assertTrue(prob > 0.0 && prob < 1.0);//发射概率均>0且<1.0
			}
			assertEquals(1.0, sum_B, 0.000001);//发射概率之和为1
		}
		assertEquals(1.0, sum_pi, 0.000001);//初始转移概率之和为1
	}
}
