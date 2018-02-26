package hust.tools.hmm.demo.pos;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import hust.tools.hmm.model.HMM;
import hust.tools.hmm.stream.SupervisedHMMSample;
import hust.tools.hmm.utils.ObservationSequence;
import hust.tools.hmm.utils.StateSequence;

/**
 *<ul>
 *<li>Description: 进行词性标注，计算准确率，并统计出错词性的词的信息
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月19日
 *</ul>
 */
public class EvaluatePrintHTML {
    
	private HMM model;
	private List<SupervisedHMMSample> samples;
	private HashMap<String, Integer> tagCount;
	
	public EvaluatePrintHTML(HMM model, List<SupervisedHMMSample> samples) {
		this.model = model;
		this.samples = samples;
		tagCount = new HashMap<>();
	}
	
	public void eval(File file) {
        int agreeCount = 0;
        int disagreeCount = 0;
        HashMap<String, HashMap<String, ArrayList<String>>> tagChosenForTag = new HashMap<String, HashMap<String, ArrayList<String>>>();
        
        FileWriter outFile;
        try {
            for(SupervisedHMMSample sample : samples) {
            	StateSequence refStateSeuence = sample.getStateSequence();
    			ObservationSequence wordSequence = sample.getObservationSequence();
    			
    			StateSequence preStateSeuence = model.bestStateSeqence(wordSequence);
    			
    			String testPOS = null;
                String outputPOS = null;
                String testWord = null;
    			for(int i = 0; i < wordSequence.length(); i++) {
                    testPOS = refStateSeuence.get(i).toString();
                    outputPOS = preStateSeuence.get(i).toString();
                    testWord = wordSequence.get(i).toString();
                    
                    if(tagCount.containsKey(testPOS))
                    	tagCount.put(testPOS, tagCount.get(testPOS) + 1);
                    else
                    	tagCount.put(testPOS, 1);
                    
                    if(testPOS.equals(outputPOS))
                        agreeCount++;
                    else
                        disagreeCount++;
                    
                    //统计每个词性被错误标记为其他词性的数量及对应的词
                    if (!tagChosenForTag.containsKey(testPOS))
                        tagChosenForTag.put(testPOS, new HashMap<String, ArrayList<String>>());
                    
                    if (!tagChosenForTag.get(testPOS).containsKey(outputPOS)) 
                        tagChosenForTag.get(testPOS).put(outputPOS, new ArrayList<String>());
                    
                    tagChosenForTag.get(testPOS).get(outputPOS).add(testWord);
    			}
            }
            
            outFile = new FileWriter(file);
            
            //打印评分结果
            outFile.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">");
            outFile.write("<html lang=\"en\">");
            outFile.write("<head>");
            outFile.write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
            outFile.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\">");
            outFile.write("<title>Part of Speech Scoring</title>");
            outFile.write("</head>");
            outFile.write("<body>");

            outFile.write("<div>标注正确词数: " + agreeCount + "</div>");
            outFile.write("<div>标注错误词数: " + disagreeCount + "</div>");
            outFile.write("<div>标注正确率: " + (100.0*(double)agreeCount/(double)(agreeCount+disagreeCount)) + "%</div>");

            outFile.write("<table rules='all' cellpadding='5'>");
            outFile.write("<tr><th scope='col'></th>");
            for (String tag : tagCount.keySet()) {
                if (tag.equals("<s>")) {
                    outFile.write("<th scope='col'>&lt;s&gt;</th>");
                } else {
                    outFile.write("<th scope='col'>" + tag + "</th>");
                }
            }
            outFile.write("</tr>");
            
            for (String testTag : tagCount.keySet()) {
                if (testTag.equals("<s>"))
                    outFile.write("<tr>\n<th scope='row'>&lt;s&gt;</th>");
                else
                    outFile.write("<tr>\n<th scope='row'>" + testTag + "</th>");
 
                for (String outputTag : tagCount.keySet()) {
                	String htmlClass = " class='normal'";
                	if (tagChosenForTag.containsKey(testTag) && tagChosenForTag.get(testTag).containsKey(outputTag)) {
                		ArrayList<String> list = tagChosenForTag.get(testTag).get(outputTag);
                		int num = list.size();
                		if (testTag.equals(outputTag))
                			htmlClass = " class='self'";
                		else if (num > 1000)
                            htmlClass = " class='reallybad'";
                        else if (num > 500)
                            htmlClass = " class='bad'";
                        else if (num > 100)
                            htmlClass = " class='prettybad'";
                		
                		outFile.write("<td"+htmlClass+" onclick='if(this.childNodes[1].style.display===\"none\"){this.childNodes[1].style.display=\"block\"}else{this.childNodes[1].style.display=\"none\"}'>"+num+"<div style='display:none'>");
                		for (String item : list)
                			outFile.write(item + ", ");
                        
                		outFile.write("</div></td>");
                	} else
                        outFile.write("<td class='zero'>0</td>");
                }
                outFile.write("</tr>");
            }
            outFile.write("</table>");

            outFile.write("</body>");
            outFile.write("</html>");
            
            outFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}