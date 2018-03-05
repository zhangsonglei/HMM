package hust.tools.hmm.demo.pos;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import hust.tools.hmm.io.AbstractHMMReader;
import hust.tools.hmm.io.HMMWriter;
import hust.tools.hmm.io.TextFileHMMReader;
import hust.tools.hmm.io.TextFileHMMWriter;
import hust.tools.hmm.learn.HMMTrainer;
import hust.tools.hmm.learn.SupervisedAdditionHMMTrainer;
import hust.tools.hmm.learn.SupervisedGoodTuringHMMTrainer;
import hust.tools.hmm.learn.SupervisedMLHMMTrainer;
import hust.tools.hmm.learn.SupervisedEmissionOnlyHMMTrainer;
import hust.tools.hmm.learn.SupervisedRevEmissionHMMTrainer;
import hust.tools.hmm.learn.SupervisedWittenBellHMMTrainer;
import hust.tools.hmm.learn.TransitionAndEmissionCounter;
import hust.tools.hmm.model.HMM;
import hust.tools.hmm.model.HMMWithAStar;
import hust.tools.hmm.model.HMMWithViterbi;
import hust.tools.hmm.model.HMModel;
import hust.tools.hmm.stream.SupervisedHMMSample;
import hust.tools.hmm.utils.Observation;

public class TrainAndEvaluate {

	private int order;
	private List<SupervisedHMMSample> samples;
	private String smooth;
	
	public TrainAndEvaluate(List<SupervisedHMMSample> samples, int order, String smooth) {
		this.samples = samples;
		this.order = order;
		this.smooth = smooth;
	}
	
	/**
	 * 交叉验证
	 * @param order		模型阶数
	 * @param folds		交叉验证折数
	 * @throws IOException
	 */
	public void crossValidation(int order, int folds) throws IOException {
		if(folds < 1)
			throw new IllegalArgumentException("折数不能小于1：" + folds);
		System.out.println("cross validating...");

		for(int i = 0; i < folds; i++) {
			List<SupervisedHMMSample> trainSamples = new ArrayList<>();
			List<SupervisedHMMSample> testSamples = new ArrayList<>();
			int flag = 0;
			System.out.println("\nRunning : fold-" + (i + 1));
			for(SupervisedHMMSample sample : samples) {
				if(flag % folds == i)
					testSamples.add(sample);
				else
					trainSamples.add(sample);
				
				flag++;
			}
			System.out.println("totalSize = " + samples.size() + "\ttrainSize = " + trainSamples.size() + "\ttestSize = " + testSamples.size());
			long start = System.currentTimeMillis();
			HMModel model = train(trainSamples);
			long train = System.currentTimeMillis();
			evaluate(model, testSamples);
			long eval = System.currentTimeMillis();
			System.out.println("训练时间：" +(train - start)/1000.0 +"s\t评估时间："+(eval - train)/1000.0+"s");
		}
		
		System.out.println("cross validate over.");
	}
	
	/**
	 * 训练模型
	 * @param trainsamples	训练样本
	 * @return				HMM模型
	 * @throws IOException
	 */
	public HMModel train(List<SupervisedHMMSample> trainsamples) throws IOException {
		TransitionAndEmissionCounter counter = new TransitionAndEmissionCounter(trainsamples, order);
		
		HMMTrainer learner = null;
		switch (smooth.toUpperCase()) {
		case "ML":
			learner = new SupervisedMLHMMTrainer(counter);
			break;
		case "ADD":
			learner = new SupervisedAdditionHMMTrainer(counter);
			break;
		case "WB":
			learner = new SupervisedWittenBellHMMTrainer(counter);
			break;
		case "EO":
			learner = new SupervisedEmissionOnlyHMMTrainer(counter);
			break;
		case "RE":
			learner = new SupervisedRevEmissionHMMTrainer(counter);
			break;
		case "KATZ":
			learner = new SupervisedGoodTuringHMMTrainer(counter);
			break;
		default:
			throw new IllegalArgumentException("错误的平滑方法：" + smooth);
		}
		
		return learner.train();
	}
	
	/**
	 * 测试评估
	 * @param hmModel		模型
	 * @param testSamples	测试样本（带词性）
	 */
	public void evaluate(HMModel hmModel, List<SupervisedHMMSample> testSamples) {
		HMM hmm = null;
//		hmm = new HMMWithViterbi(hmModel);
		hmm = new HMMWithAStar(hmModel);
		
		Observation[] observations = hmModel.getObservations();
		HashSet<String> dict = new HashSet<>();
		for(Observation observation : observations)
			dict.add(observation.toString());
				
		POSEvaluator evaluator = new POSEvaluator(hmm, dict, testSamples);
		evaluator.eval();
	}
	
	/**
	 * 写模型
	 * @param model		模型
	 * @param file		写出路径
	 * @throws IOException
	 */
	public static void writeModel(HMModel model, File file) throws IOException {
		HMMWriter writer = new TextFileHMMWriter(model, file);
		
		writer.persist();
	}
	
	/**
	 * 读取模型
	 * @param modelFile	模型文件路径
	 * @return			HMM模型
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static HMModel loadModel(File modelFile) throws IOException, ClassNotFoundException {
		AbstractHMMReader reader = new TextFileHMMReader(modelFile);
		
		return reader.readModel();
	}
	
}
