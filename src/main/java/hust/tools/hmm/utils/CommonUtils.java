package hust.tools.hmm.utils;

import java.util.ArrayList;
import java.util.List;

public class CommonUtils {
	
	/**
	   * <li>将给定的数组和n，生成所有可能的n元组
	   * @param input 	待分割为N元组的序列
	   * @param n       N元组的大小
	   * @return		所有的N元组	
	   */
	public static List<State[]> generate(Sequence<State> input, int n)  {
		List<State[]> output = new ArrayList<>();
		
		for(int i = 0; i < input.size() - n + 1; i++) {
			State[] ngram = new State[n];
			for(int j = i, index = 0; j < i + n; j++, index++)
				ngram[index] = input.get(j);

			output.add(ngram);
		}//end for
		
		return output;
	}
}
