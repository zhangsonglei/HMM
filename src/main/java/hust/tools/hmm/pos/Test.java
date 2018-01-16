package hust.tools.hmm.pos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import hust.tools.hmm.io.AbstractHMMReader;
import hust.tools.hmm.io.HMMWriter;
import hust.tools.hmm.io.TextFileHMMReader;
import hust.tools.hmm.io.TextFileHMMWriter;
import hust.tools.hmm.learn.AbstractSupervisedHMMTrainer;
import hust.tools.hmm.learn.AdditionSupervisedHMMTrainer;
import hust.tools.hmm.learn.TransitionAndEmissionCounter;
import hust.tools.hmm.model.HMModel;
import hust.tools.hmm.model.HMModelBasedBOW;
import hust.tools.hmm.utils.Dictionary;
import hust.tools.hmm.utils.Observation;
import opennlp.tools.util.MarkableFileInputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;

public class Test {
	
	public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException {
		File file = new File("E:\\JOB\\hmm\\data\\wordTagCorpus.txt");
		String modelPath = "E:\\hmm.txt";
		
		long start = System.currentTimeMillis();
		HMModelBasedBOW model = train(file, 3);
		long end = System.currentTimeMillis();
		System.out.println("训练模型时间："+(end - start) / 1000.0 + "s");
		
		writeModel(model, modelPath);
		long write = System.currentTimeMillis();
		System.out.println("模型写出时间："+(write - start) / 1000.0 + "s");
		
//		State[] states = new StringState[]{new StringState("v"), new StringState("ns"), new StringState("nr")};
//		StateSequence sequence = new StateSequence(states);
//		State target = new StringState("nr");
//		System.out.println(model.transitionProb(sequence, target));
		HMModelBasedBOW readmodel = (HMModelBasedBOW) loadModel(new File(modelPath));
		System.out.println(model.equals(readmodel));
	}
	
	private static HMModelBasedBOW train(File file, int order) throws IOException {
		ObjectStream<String> input = new PlainTextByLineStream(new MarkableFileInputStreamFactory(file), "utf8");
		StringToHMMSampleStream sampleStream = new StringToHMMSampleStream(input);
		TransitionAndEmissionCounter counter = new TransitionAndEmissionCounter(sampleStream, order);
		AbstractSupervisedHMMTrainer learner = new AdditionSupervisedHMMTrainer(counter);
		return (HMModelBasedBOW) learner.train();
	}
	
	private static void writeModel(HMModelBasedBOW model, String pathname) throws IOException {
		HMMWriter writer = new TextFileHMMWriter(model, pathname);
		
		writer.persist();
	}
	
	private static HMModel loadModel(File modelFile) throws IOException, ClassNotFoundException {
		AbstractHMMReader reader = new TextFileHMMReader(modelFile);
		
		return reader.readModel();
	}
	
	/**
	 * 预测词性标注的效果
	 * @param model					
	 * @param observationSequence
	 * @return
	 */
	@SuppressWarnings("unused")
	private static void eval(Dictionary dict, List<String[]> words, List<String[]> refPOS, List<String[]> prePOS) {
		Set<Observation> observations = dict.getObservations();
		HashSet<String> vocab = new HashSet<>();
		
		for(Observation word : observations)
			vocab.add(word.toString());
		
		POSBasedWordMeasure measure = new POSBasedWordMeasure(vocab);
		for(int i = 0; i < words.size(); i++) 
			measure.updateScores(words.get(i), refPOS.get(i), prePOS.get(i));
		
		System.out.println(measure);
	}
}
