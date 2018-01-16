package hust.tools.hmm.io;

import java.io.IOException;
import java.util.HashMap;

import hust.tools.hmm.model.ARPAEntry;
import hust.tools.hmm.model.EmissionProbEntry;
import hust.tools.hmm.model.HMModelBasedBOW;
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
	
	private HashMap<State, Double> pi;
	
	private HashMap<StateSequence, ARPAEntry> transitionMatrix;
	
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
			StateIndex entry = readStateIndex();
			dict.put(entry.getState(), entry.getIndex());
		}
		
		//读取观测状态及其索引
		for(int i = 0; i < observationsCount; i++) {
			ObservationIndex entry = readObservationIndex();
			dict.put(entry.getObservation(), entry.getIndex());
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
			pi.put(entry.getState(), entry.getLogProb());
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
			transitionMatrix.put(entry.getSequence(), entry.getEntry());
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
	
	private long readCount() throws IOException {
		return reader.readCount();
	}
	
	private ObservationIndex readObservationIndex() throws IOException, ClassNotFoundException {
		return reader.readObservationIndex();
	}
	
	private StateIndex readStateIndex() throws IOException, ClassNotFoundException {
		return reader.readStateIndex();
	}
	
	private PiEntry readPi() throws IOException, ClassNotFoundException {
		return reader.readPi();
	}

	private TransitionEntry readTransitionMatrix() throws IOException, ClassNotFoundException {
		return reader.readTransitionMatrix();
	}
	
	private EmissionEntry readEmissionMatrix() throws IOException, ClassNotFoundException {
		return reader.readEmissionMatrix();
	}

	private void close() throws IOException {
		reader.close();
	}
 }