package hust.tools.hmm.demo.pos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import hust.tools.hmm.stream.SupervisedHMMSample;

public class POSTagger {
	
	private static File file;	
	private static final int order = 1;
	private static final boolean isSupervised = true;	//true-监督学习，false-非监督学习
	private static final double ratio = 0.03;			//删除插值平滑中的留存数据比例
	
	public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException {
		file = new File(POSTagger.class.getClassLoader().getResource("corpus/conll.train").getFile());
//		file = new File(POSTagger.class.getClassLoader().getResource("corpus/pos.train").getFile());
		List<SupervisedHMMSample> samples = TrainCorpusReader.readSupervisedHMMSamples(file, order);
		TrainAndEvaluate trainAndEvaluate = new TrainAndEvaluate(samples, order, "int", ratio);
		trainAndEvaluate.crossValidation(order, 10, isSupervised);
//		UnSupervisedTrainBySupervisedModel trainBySupervisedModel = new UnSupervisedTrainBySupervisedModel(samples, "wb");
//		trainBySupervisedModel.crossValidation(order, 10);
	}
}
