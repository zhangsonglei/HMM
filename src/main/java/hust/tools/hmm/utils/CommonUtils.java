package hust.tools.hmm.utils;

import java.util.ArrayList;
import java.util.List;

public class CommonUtils {
	
	/**
	 * 观测状态未登录词
	 */
	public final static Observation UNKNOWN = new StringObservation("UNKNOWN");
	
	/**
	   * <li>将给定的数组和n，生成所有可能的n元组
	   * @param input 	待分割为N元组的序列
	   * @param n       N元组的大小
	   * @return		所有的N元组	
	   */
	public static List<StateSequence> generate(StateSequence input, int n)  {
		List<StateSequence> output = new ArrayList<>();
		
		for(int i = 0; i < input.length() - n + 1; i++) {
			State[] ngram = new State[n];
			for(int j = i, index = 0; j < i + n; j++, index++)
				ngram[index] = input.get(j);

			output.add(new StateSequence(ngram));
		}//end for
		
		return output;
	}
	
//	public static List<StateSequence> spilt(StateSequence sequence, int order) {
//		List<StateSequence> output = new ArrayList<>();
//		
//		if(sequence.length() >= order) {//序列长度大于等于order
//			//长度小于order的n元
//			State[] states = new State[order - 1];
//			for(int i = 0; i < states.length; i++)
//				states[i] = sequence.get(i);
//			
//			StateSequence n_States = new StateSequence(states);
//			for(int i = 0; i < order - 1; i++) {
//				output.add(n_States);
//				n_States = n_States.remove(n_States.length() - 1);
//			}
//			
//			//长度为order的n元
//			List<State[]> nStates = generate(sequence, order);
//			for(State[] nState : nStates)
//				output.add(new StateSequence(nState));
//		}else {//序列长度小于order
//			for(int i = 0; i < sequence.length(); i++) {
//				output.add(sequence);
//				sequence = sequence.remove(sequence.length() - 1);
//			}
//		}//end if-else in no-boundary
//		
//		return output;
//	}
}
