package hust.tools.hmm.demo.pos;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import hust.tools.hmm.stream.SupervisedHMMSample;
import hust.tools.hmm.utils.StringObservation;
import hust.tools.hmm.utils.StringState;

public class TrainCorpusReader {

	public static List<SupervisedHMMSample> read(File file) throws IOException {
		List<SupervisedHMMSample> samples = new ArrayList<>();
		InputStreamReader ireader = new InputStreamReader(new FileInputStream(file), "utf8");
		BufferedReader reader = new BufferedReader(ireader);
		
		String line = null;
		while((line = reader.readLine()) != null) {
			line = line.trim();
			if(!line.equals("")) {
				String[] wordTags = line.split("\\s+");
				SupervisedHMMSample sample = new SupervisedHMMSample();
				
				for(int i = 0; i < wordTags.length; i++)
					sample.add(new StringState(wordTags[i].split("/")[1]), new StringObservation(wordTags[i].split("/")[0]));
				
				samples.add(sample);
			}
		}
		reader.close();
		
		return samples;
	}
}
