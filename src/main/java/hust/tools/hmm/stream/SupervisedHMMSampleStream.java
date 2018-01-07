package hust.tools.hmm.stream;

import java.io.IOException;

import opennlp.tools.util.FilterObjectStream;
import opennlp.tools.util.ObjectStream;

/**
 *<ul>
 *<li>Description: 抽象样本流 
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2017年12月29日
 *</ul>
 * @param <S>源流/输入流的类型
 */
public abstract class SupervisedHMMSampleStream<S> extends FilterObjectStream<S, AbstractHMMSample> {
	
	public SupervisedHMMSampleStream(ObjectStream<S> stream) {
		super(stream);
	}

	@Override
	public abstract AbstractHMMSample read() throws IOException;
}
