package hust.tools.hmm.demo.pos;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

/**
 *<ul>
 *<li>Description:词性标注基准程序，给每个词分配其历史词性在训练语料中出现次数最多的一个，未登录词分配语料中出现次数最多的词性
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月21日
 *</ul>
 */
public class BaseLine {
	
	private static HashMap<String, HashMap<String, Integer>> word_tag_counter;	//每个词对应词性的数量
	private static HashMap<String, String> word_maxTag;							//每个词出现次数最多的词性
	private static HashSet<String> dict;										//训练语料中的词
	private static String maxTag;												//训练语料中出现次数最多的词性
	private static EvaluateMeasure measure;										//标注效果评估
	
	public static void main(String[] args) throws IOException {
		List<String> sentences = new ArrayList<>();
		InputStreamReader iReader = new InputStreamReader(new FileInputStream(new File("E:\\\\JOB\\\\hmm\\\\data\\\\pos.train")));
		BufferedReader reader = new BufferedReader(iReader);
		String sentence = null;
		while((sentence = reader.readLine()) != null) {
			sentence = sentence.trim();
			if(!sentence.equals("")) {
				sentences.add(sentence);
			}
		}
		reader.close();
		
		int folds = 10;
		
		if(folds > 1)
			crossValidation(sentences, folds);
	}

	private static void crossValidation(List<String> sentences, int folds) throws IOException {		
		int index = 0;
		for(int i = 1; i <= folds; i++) {
			System.out.println("\nCross:" + i);
			List<String> trainSentences = new ArrayList<>();
			List<String> testSentences = new ArrayList<>();
			for(String sentence : sentences) {
				if(index++ % folds == 0)
					testSentences.add(sentence);
				else
					trainSentences.add(sentence);
			}
			System.out.println("totalSize = " + sentences.size() + "\ttrainSize = " + trainSentences.size() + "\ttestSize = " + testSentences.size());

			train(trainSentences);
			eval(testSentences);
		}
	}

	private static void train(List<String> sentences) {
		dict = new HashSet<>();
		word_tag_counter = new HashMap<>();
		word_maxTag = new HashMap<>();
		maxTag = null;
		
		for(String sentence : sentences) {//统计每个词对应词性的频数
			String[] wordTags = sentence.split("\\s+");
			for(String wordTag : wordTags) {
				String[] wordAndTag = wordTag.split("/");
				String word = wordAndTag[0];
				dict.add(word);
				String tag = wordAndTag[1];
				
				HashMap<String, Integer> tagCount = null;
				if(word_tag_counter.containsKey(word)) {
					tagCount= word_tag_counter.get(word);
					if(tagCount.containsKey(tag)) 
						tagCount.put(tag, tagCount.get(tag) + 1);
					else
						tagCount.put(tag, 1);
				}else {
					tagCount = new HashMap<>();
					tagCount.put(tag, 1);
				}
				word_tag_counter.put(word, tagCount);
			}
		}
		
		//选取每个词出现次数最多的词性
		int maxTagCount = 0;
		for(Entry<String, HashMap<String, Integer>> wordEntry : word_tag_counter.entrySet()) {
			String word = wordEntry.getKey();
			HashMap<String, Integer> tagMap = wordEntry.getValue();
			int max = 0;
			for(Entry<String, Integer> tagEntry : tagMap.entrySet()) {
				if(max < tagEntry.getValue()) {
					max = tagEntry.getValue();
					word_maxTag.put(word, tagEntry.getKey());
				}
			}
			
			if(maxTagCount < max)
				maxTag = word_maxTag.get(word);
		}
		word_tag_counter.clear();//清空词性数量信息，节省空间
	}
	
	private static void eval(List<String> testSentences) {
		measure = new EvaluateMeasure(dict);
		for(String sentence : testSentences) {
			String[] wordTags = sentence.split("\\s+");
			String[] words = new String[wordTags.length];
			String[] refTags = new String[wordTags.length];
			String[] preTags = new String[wordTags.length];
			for(int i = 0; i < wordTags.length; i++) {
				String[] wordAndTag = wordTags[i].split("/");
				words[i] = wordAndTag[0];
				refTags[i] = wordAndTag[1];
				if(word_maxTag.containsKey(wordAndTag[0]))
					preTags[i] = word_maxTag.get(wordAndTag[0]);
				else
					preTags[i] = maxTag;
			}
			measure.updateScores(words, refTags, preTags);
		}
		
		System.out.println(measure);
	}
}