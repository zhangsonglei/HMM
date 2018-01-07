package hust.tools.hmm.stream;

import hust.tools.hmm.utils.ObservationSequence;

/**
 *<ul>
 *<li>Description: 只包含观测状态序列的无监督学习样本类 
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月1日
 *</ul>
 */
public class UnsupervisedHMMSample extends AbstractHMMSample {

	public UnsupervisedHMMSample() {
		super();
	}
	
	public UnsupervisedHMMSample(ObservationSequence observationSequence) {
		super(observationSequence);
	}
	
	@Override
	public String toString() {
		String string = "[";
		
		for(int i = 0; i < observationSequence.size(); i++)
			string += observationSequence.get(i) + "  ";
		
		return string.trim() + "]";
	}
}
