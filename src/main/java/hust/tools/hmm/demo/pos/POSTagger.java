package hust.tools.hmm.demo.pos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import hust.tools.hmm.stream.SupervisedHMMSample;

public class POSTagger {
	
	private static File file;
	private static List<SupervisedHMMSample> samples;
	
	
	public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException {
		file = new File("E:\\JOB\\hmm\\data\\conll.pos");
//		file = new File("E:\\JOB\\hmm\\data\\pos.train");
		samples = TrainCorpusReader.read(file);
		int order = 1;
		
		TrainAndEvaluate trainAndEvaluate = new TrainAndEvaluate(samples, order, "add");
		trainAndEvaluate.crossValidation(order, 10);

//		List<SupervisedHMMSample> trainSamples = new ArrayList<>();
//		List<SupervisedHMMSample> testSamples = new ArrayList<>();
//		int i = 0;
//		for(SupervisedHMMSample sample : samples) {
//			if(i++ % 10 == 0)
//				testSamples.add(sample);
//			else
//				trainSamples.add(sample);
//		}
//		
//		HMModel model = trainAndEvaluate.train(trainSamples);
//		HMM hmm = new HMMWithViterbi(model);
//		EvaluatePrintHTML evaluator = new EvaluatePrintHTML(hmm, testSamples, order);
//		evaluator.eval(new File("E:\\JOB\\hmm\\data\\result.html"));
	}
}
