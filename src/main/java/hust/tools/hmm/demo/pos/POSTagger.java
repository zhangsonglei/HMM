package hust.tools.hmm.demo.pos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import hust.tools.hmm.learn.AbstractSupervisedHMMTrainer;
import hust.tools.hmm.learn.SupervisedAdditionHMMTrainer;
import hust.tools.hmm.learn.TransitionAndEmissionCounter;
import hust.tools.hmm.model.HMM;
import hust.tools.hmm.model.HMMWithViterbi;
import hust.tools.hmm.model.HMModelBasedBO;
import hust.tools.hmm.stream.SupervisedHMMSample;
import hust.tools.hmm.utils.Observation;

public class POSTagger {
	
	public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException {
		File file = new File("E:\\JOB\\hmm\\data\\conll.pos");
//		File file = new File("E:\\JOB\\hmm\\data\\pos.train");
		long start = System.currentTimeMillis();
		List<SupervisedHMMSample> samples = POSTrainCorpusReader.read(file);
		long read = System.currentTimeMillis();
		System.out.println("语料读取时间："+(read - start) / 1000.0 + "s");
		
		List<SupervisedHMMSample> trainSamples = new ArrayList<>();
		List<SupervisedHMMSample> testSamples = new ArrayList<>();
		
		int flag = 0;
		for(SupervisedHMMSample sample : samples) {
			if((flag++)%10 == 0)
				testSamples.add(sample);
			else
				trainSamples.add(sample);
		}
		System.out.println("total = " + samples.size() + "\ttrainSize = " + trainSamples.size() + "\ttestSize = " + testSamples.size());
		
		
		int order = 3;
		
		HMModelBasedBO hmModel = train(trainSamples, order);
		long train = System.currentTimeMillis();
		System.out.println("训练模型时间："+(train - read) / 1000.0 + "s");
		
//		String modelPath = "E:\\hmm.txt";
//		writeModel(hmModel, modelPath);
//		long write = System.currentTimeMillis();
//		System.out.println("模型写出时间："+(write - train) / 1000.0 + "s");
//
//		HMModelBasedBO readmodel = (HMModelBasedBO) loadModel(new File(modelPath));
//		long load = System.currentTimeMillis();
//		System.out.println("模型读取时间："+(load - write) / 1000.0 + "s");
//		System.out.println(hmModel.equals(readmodel));
		
		HMM hmm = new HMMWithViterbi(hmModel);
		Observation[] observations = hmModel.getObservations();
		HashSet<String> dict = new HashSet<>();
		for(Observation observation : observations)
			dict.add(observation.toString());
				
		POSEval evaluator = new POSEval(hmm, dict, testSamples, order);
		evaluator.eval();
		long eval = System.currentTimeMillis();
		System.out.println("解码时间："+(eval - train) / 1000.0 + "s");
	}
	
	private static HMModelBasedBO train(List<SupervisedHMMSample> samples, int order) throws IOException {
		TransitionAndEmissionCounter counter = new TransitionAndEmissionCounter(samples, order);
		AbstractSupervisedHMMTrainer learner = new SupervisedAdditionHMMTrainer(counter, 1.0);
//		AbstractSupervisedHMMTrainer learner = new SupervisedMLHMMTrainer(counter);
		
		return (HMModelBasedBO) learner.train();
	}
	
//	/**
//	 * 写模型
//	 * @param model		
//	 * @param pathname	
//	 * @throws IOException
//	 */
//	private static void writeModel(HMModelBasedBO model, String pathname) throws IOException {
//		HMMWriter writer = new TextFileHMMWriter(model, pathname);
//		
//		writer.persist();
//	}
//	
//	/**
//	 * 读取模型
//	 * @param modelFile
//	 * @return
//	 * @throws IOException
//	 * @throws ClassNotFoundException
//	 */
//	private static HMModel loadModel(File modelFile) throws IOException, ClassNotFoundException {
//		AbstractHMMReader reader = new TextFileHMMReader(modelFile);
//		
//		return reader.readModel();
//	}
}
