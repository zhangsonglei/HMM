package hust.tools.hmm.demo.pos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import hust.tools.hmm.stream.SupervisedHMMSample;

public class POSTagger {
	
	private static File file;	
	private static final int order = 3;
	private static final boolean isSupervised = true;
	private static final double ratio = 0.03;
	
	public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException {
		file = new File(POSTagger.class.getClassLoader().getResource("corpus/conll.pos").getFile());
		List<SupervisedHMMSample> samples = TrainCorpusReader.readSupervisedHMMSamples(file, order);
		TrainAndEvaluate trainAndEvaluate = new TrainAndEvaluate(samples, order, "wb", ratio);
		trainAndEvaluate.crossValidation(order, 10, isSupervised);
//		UnSupervisedTrainBySupervisedModel trainBySupervisedModel = new UnSupervisedTrainBySupervisedModel(samples);
//		trainBySupervisedModel.crossValidation(order, 10);
	}
}
