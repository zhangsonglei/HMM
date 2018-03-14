package hust.tools.hmm.learn;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import hust.tools.hmm.utils.State;
import hust.tools.hmm.utils.StateSequence;

/**
 *<ul>
 *<li>Description: 计算GoodTuring折扣系数
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月10日
 *</ul>
 */
public class GoodTuringCounts {
	
	/**
	 * 模型阶数
	 */
	private int order;
	
	/**
	 * 大与K的计数不进行折扣
	 */
	private int K;

	/**
	 * n元数量的折扣系数
	 * 第一维度代表n元的长度
	 */
	private double[][] disCoeffs;
	
	/**
	 * total[i] i阶所有类型数
	 */
	private double[] total;
	
	/**
	 * N1[i] i阶出现1次的转移类型数
	 */
	private double[] N1;
	
	/**
	 * 构造方法
	 * @param nGramCountMap	计数器
	 * @param order			模型阶数
	 * @param K				计数折扣阈值，大于此值不进行折扣
	 */
	public GoodTuringCounts(HashMap<StateSequence, TransitionCountEntry> nGramCountMap, int order, int K) {
		this.order = order;
		this.K = K;
		disCoeffs = new double[order][];
		total = new double[order];
		N1 = new double[order];
		statisticsNGramCountOfTimesSeen(nGramCountMap);
	}
	
	/**
	 * 返回给定元组的出现次数的折扣系数
	 * 对于出现次数大于7的n元不进行打折,对于unigram,大于1的不进行打折
	 * 
	 * @param order	给定元组的长度
	 * @param r		给定元组的出现次数
	 * @return		给定元组的出现次数的折扣系数
	 */
	public double getDiscountCoeff(int order, int r) {
		if(order > disCoeffs.length || order < 1)
			throw new IllegalArgumentException("错误的模型阶数");
		
		if(r < 0)
			throw new IllegalArgumentException("计数不能为负数");
		
		if(r == 0)
			return 1.0;
		
		return disCoeffs[order - 1][r - 1];
	}
	
	/**
	 * 返回给定阶数的出现一次的转移类型数
	 * @param order	转移阶数
	 * @return		出现一次的转移类型数
	 */
	public double getN1ByOrder(int order) {
		return N1[order - 1];
	}
	
	/**
	 * 返回给定阶数总转移数
	 * @param order	转移阶数
	 * @return		总转移数
	 */
	public double getTotalByOrder(int order) {
		return total[order - 1];
	}
	
	/**
	 * 根据n的大小统计出现r次的n元的数量
	 * @param n				n元的最大小
	 * @param nGramCountMap	n元及其计数的索引
	 * @return				根据n的大小统计出现r次的n元的数量
	 */
	private void statisticsNGramCountOfTimesSeen(HashMap<StateSequence, TransitionCountEntry> nGramCountMap) {
		int[] max_r = new int[order];
		HashMap<Integer, HashMap<Integer, Double>> countOfCounts = new HashMap<>();
		
		for(Entry<StateSequence, TransitionCountEntry> entry : nGramCountMap.entrySet()) {
			int order = entry.getKey().length();
			
			Iterator<Entry<State, Integer>> iterator = entry.getValue().entryIterator();
			while(iterator.hasNext()) {
				int r = iterator.next().getValue();
				max_r[order - 1] = max_r[order - 1] < r ? r : max_r[order - 1];
				
				if(countOfCounts.containsKey(order)) {
					if(countOfCounts.get(order).containsKey(r))
						countOfCounts.get(order).put(r, countOfCounts.get(order).get(r) + 1);
					else
						countOfCounts.get(order).put(r, 1.0);
				}else {
					HashMap<Integer, Double> map = new HashMap<>();
					map.put(r, 1.0);
					countOfCounts.put(order, map);
				}
			}//end while
		}//end for
		
		processCountOfCount(countOfCounts, max_r);
		
		
		for(int i = 1; i <= order; i++) {
			N1[i - 1] = countOfCounts.get(i).get(1);
			
			//计算n元出现次数1——maxK的折扣系数
			double[] temp = new double[max_r[i - 1] + 1];
			Arrays.fill(temp, 1.0);
			
			double common = (K + 1) * countOfCounts.get(i).get(K + 1) / countOfCounts.get(i).get(1);
			for(int j = 1; j <= max_r[i - 1] + 1 && j < K; j++) {
				double coeff = ((j + 1) * countOfCounts.get(i).get(j + 1) / (j * countOfCounts.get(i).get(j)) - common) / (1 - common);
				
				if(Double.isInfinite(coeff) || coeff <= 0.0 || coeff > 1.0) {
					System.out.println("警告: 折扣系数 "+ i+"-"+j +" 越界: " + coeff+" 默认为1.0");
				    coeff = 1.0;
				}
				temp[j - 1] = coeff;
			}
			disCoeffs[i - 1] = temp;
		}
		
		countOfCounts.clear();
	}

	/**
	 * 填充n元出现次数r的数量的空缺
	 * @param nGramCountOfTimesSeen n元出现次数r的数量
	 * @param n 最大n元长度
	 */
	private void processCountOfCount(HashMap<Integer, HashMap<Integer, Double>> countOfCounts, int[] max_r){
		for(int i = 1; i <= order; i++) {
			HashMap<Integer, Double> map = countOfCounts.get(i);

			if(map.size() < 2) {
				System.out.println("训练语料过少,无法使用GoodTuring折扣");
				System.out.println(i+"gram:\n"+map);
				System.exit(0);
			}
			
			//线性回归,log(Nr) = a + b*log(r)
			double[] parameters = linearRegression(map);
			for(int r = 1; r <= max_r[i - 1] + 1; r++) {
				double Nr = Math.pow(10, parameters[0] + parameters[1] * Math.log10(r));
				total[i] += r * Nr;
				if(countOfCounts.containsKey(r)) {
					if(!countOfCounts.get(r).containsKey(i))
						countOfCounts.get(r).put(i, Nr);
				}else {
					HashMap<Integer, Double> tempMap = new HashMap<>();
					tempMap.put(i, Nr);
					countOfCounts.put(r, tempMap);
				}
			}
		}
	}

	/**
	 * 最小二乘法经验方程求解线性回归
	 * @param data 样本数据
	 * @return 参数
	 */
	private static double[] linearRegression(Map<Integer, Double> data) {
		double[] parameters;	//方程参数，0-截距，1-斜率
		double sum_xy = 0.0;	//自变量因变量之积的和
		double sum_xx = 0.0;	//自变量的平方和
		double _x = 0.0;		//自变量的均值
		double _y = 0.0;		//因变量的均值
				
		//统计变量信息
		for(Entry<Integer, Double> entry : data.entrySet()) {
			sum_xy += Math.log10(entry.getKey()) * Math.log10(entry.getValue());
			sum_xx += Math.log10(entry.getKey()) * Math.log10(entry.getKey());
			_x += Math.log10(entry.getKey());
			_y += Math.log10(entry.getValue());
		}
		_x /= data.size();
		_y /= data.size();
		
		/**
		 * 最小二乘法经验拟合方程解参数
		 */
		parameters = new double[2];
		parameters[1] = (sum_xy - data.size() * _x * _y)/(sum_xx - data.size() * _x * _x);
		parameters[0] = _y - parameters[1] * _x;
		
		return parameters;
	}
}