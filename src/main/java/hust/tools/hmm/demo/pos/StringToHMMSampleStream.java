package hust.tools.hmm.demo.pos;

import java.io.IOException;

import hust.tools.hmm.stream.AbstractHMMSample;
import hust.tools.hmm.stream.SupervisedHMMSample;
import hust.tools.hmm.stream.SupervisedHMMSampleStream;
import hust.tools.hmm.utils.ObservationSequence;
import hust.tools.hmm.utils.StateSequence;
import hust.tools.hmm.utils.StringObservation;
import hust.tools.hmm.utils.StringState;
import opennlp.tools.util.ObjectStream;

/**
 *<ul>
 *<li>Description: 字符串型(word/pos ...)输入流转为 样本
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2017年12月29日
 *</ul>
 */
public class StringToHMMSampleStream extends SupervisedHMMSampleStream<String> {

	public StringToHMMSampleStream(ObjectStream<String> stream) {
		super(stream);
	}

	@Override
	public AbstractHMMSample read() throws IOException {
		String sentence = samples.read();
		
		if(sentence != null) {
			if(!sentence.trim().equals("")) {
				String[] wordTags = sentence.trim().split("\\s+");
				SupervisedHMMSample sample = new SupervisedHMMSample();
				
				for(int i = 0; i < wordTags.length; i++)
					sample.add(new StringState(wordTags[i].split("/")[1]), new StringObservation(wordTags[i].split("/")[0]));
				
				return sample;
			}else
				return new SupervisedHMMSample(new StateSequence(), new ObservationSequence());
			
		}else
			return null;
	}

}
