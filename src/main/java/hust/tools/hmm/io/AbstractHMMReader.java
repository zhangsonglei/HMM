package hust.tools.hmm.io;

import java.io.IOException;
import java.util.HashMap;

import hust.tools.hmm.model.ARPAEntry;
import hust.tools.hmm.model.EmissionProbEntry;
import hust.tools.hmm.model.HMModelBasedBOW;
import hust.tools.hmm.model.TransitionProbEntry;
import hust.tools.hmm.utils.Dictionary;
import hust.tools.hmm.utils.Observation;
import hust.tools.hmm.utils.State;
import hust.tools.hmm.utils.StateSequence;

/**
 *<ul>
 *<li>Description: 读取HMM模型抽象类 
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月14日
 *</ul>
 */
public abstract class AbstractHMMReader {
	
	private int order;
	
	private Dictionary dict;
	
	private HashMap<State, ARPAEntry> pi;
	
	private HashMap<StateSequence, TransitionProbEntry> transitionMatrix;
	
	private HashMap<State, EmissionProbEntry> emissionMatrix;
	
	private long[] counts;
	
	private DataReader reader;
	
	public AbstractHMMReader(DataReader reader) {
		this.reader = reader;
	}
	
	/**
	 * 重构n元模型  
	 * @return 读取n元模型
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public HMModelBasedBOW readModel() throws IOException, ClassNotFoundException {
		counts = new long[6];
		
		//读取模型各参数的数量
		for(int i = 0; i < 6; i++)
			counts[i] = readCount();
		
		//模型阶数
		order = (int) counts[0];
		
		//构造字典
		constructDict(counts[1], counts[2]);
			
		//构造初始状态转移概率向量
		constructPi(counts[3]);
		
		//构造状态转移概率矩阵
		constructTransitionMatrix(counts[4]);
		
		//构造发射移概率矩阵
		constructEmissionMatrix(counts[5]);
		
		close();
		
		return new HMModelBasedBOW(order, dict, pi, transitionMatrix, emissionMatrix);
	}
	
	/**
	 * 构造隐藏状态与观测状态索引字典
	 * @param statesCount		隐藏状态数量
	 * @param observationsCount	观测状态数量
	 * @return					字典
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	private void constructDict(long statesCount, long observationsCount) throws IOException, ClassNotFoundException {
		dict = new Dictionary();
		
		//读取隐藏状态及其索引
		for(int i = 0; i < statesCount; i++) {
			DictionaryEntry entry = readDict();
			dict.put((State) entry.getObject(), entry.getIndex());
		}
		
		//读取观测状态及其索引
		for(int i = 0; i < observationsCount; i++) {
			DictionaryEntry entry = readDict();
			dict.put((Observation) entry.getObject(), entry.getIndex());
		}
	}
	
	/**
	 * 构造初始状态转移概率向量
	 * @param count	初始状态转移概率数量
	 * @throws IOException
	 * @throws ClassNotFoundException 
	 */
	private void constructPi(long count) throws IOException, ClassNotFoundException {
		pi = new HashMap<>();
		
		for(int i = 0; i < count; i++) {
			PiEntry entry = readPi();
			pi.put(entry.getState(), entry.getEntry());
		}
	}

	/**
	 * 构造状态转移概率矩阵
	 * @param count	转移数量
	 * @throws IOException
	 * @throws ClassNotFoundException 
	 */
	private void constructTransitionMatrix(long count) throws IOException, ClassNotFoundException {
		transitionMatrix = new HashMap<>();
		
		for(int i = 0; i < count; i++) {
			TransitionEntry entry = readTransitionMatrix();
			StateSequence start = entry.getStart();
			State targt = entry.getTarget();
			ARPAEntry arpaEntry = entry.getEntry();
			TransitionProbEntry probEntry = null;
			if(transitionMatrix.containsKey(start)) {
				probEntry = transitionMatrix.get(start);
				probEntry.put(targt, arpaEntry);
				transitionMatrix.put(start, probEntry);
			}else {
				probEntry = new TransitionProbEntry();
				probEntry.put(targt, arpaEntry);
				transitionMatrix.put(start,probEntry);
			}
		}
	}
	
	/**
	 * 构造发射概率矩阵
	 * @param count	发射数量
	 * @throws IOException
	 * @throws ClassNotFoundException 
	 */
	private void constructEmissionMatrix(long count) throws IOException, ClassNotFoundException {
		emissionMatrix = new HashMap<>();
		
		for(int i = 0; i < count; i++) {
			EmissionEntry entry = readEmissionMatrix();
			
			State state = entry.getState();
			Observation observation = entry.getObservation();
			double logProb = entry.getLogProb();

			System.out.println(state + "\t" + observation + "\t" + logProb);
			if(emissionMatrix.containsKey(state)) {
				EmissionProbEntry probEntry = emissionMatrix.get(state);
				probEntry.put(observation, logProb);
				emissionMatrix.put(state, probEntry);
			}else {
				EmissionProbEntry probEntry = new EmissionProbEntry();
				probEntry.put(observation, logProb);
				emissionMatrix.put(state, probEntry);
			}
		}
	}
	
	public long readCount() throws IOException {
		return reader.readCount();
	}
	
	public DictionaryEntry readDict() throws IOException, ClassNotFoundException {
		return reader.readDict();
	}
	
	public PiEntry readPi() throws IOException, ClassNotFoundException {
		return reader.readPi();
	}

	public TransitionEntry readTransitionMatrix() throws IOException, ClassNotFoundException {
		return reader.readTransitionMatrix();
	}
	
	public EmissionEntry readEmissionMatrix() throws IOException, ClassNotFoundException {
		return reader.readEmissionMatrix();
	}

	public void close() throws IOException {
		reader.close();
	}
 }