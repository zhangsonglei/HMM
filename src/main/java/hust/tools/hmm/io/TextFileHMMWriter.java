package hust.tools.hmm.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import hust.tools.hmm.model.HMModelBasedBOW;

/**
 *<ul>
 *<li>Description: 将模型写入普通文本文件
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月14日
 *</ul>
 */
public class TextFileHMMWriter extends AbstractHMMWriter {

	private BufferedWriter bWriter;
	
	public TextFileHMMWriter (HMModelBasedBOW model, String filePath) throws IOException {
		super(model);
		bWriter = new BufferedWriter(new FileWriter(filePath));
	}
	
	public TextFileHMMWriter (HMModelBasedBOW model, File file) throws IOException {
		super(model);
		bWriter = new BufferedWriter(new FileWriter(file));
	}
	
	public TextFileHMMWriter (HMModelBasedBOW model, OutputStream os) throws IOException {
		super(model);
		bWriter = new BufferedWriter(new OutputStreamWriter(os));
	}
	
	public TextFileHMMWriter (HMModelBasedBOW model, BufferedWriter bWriter) throws IOException {
		super(model);
		this.bWriter = bWriter;
	}

	@Override
	public void writeCount(long count) throws IOException {
		bWriter.write(Long.toString(count));
		bWriter.newLine();
	}

	@Override
	public void writeIndex(DictionaryEntry entry) throws IOException {
		bWriter.write(entry.toString());
		bWriter.newLine();
	}

	@Override
	public void writePi(PiEntry entry) throws IOException {
		bWriter.write(entry.toString());
		bWriter.newLine();
	}

	@Override
	public void writeTransitionMatrix(TransitionEntry entry) throws IOException {
		bWriter.write(entry.toString());
		bWriter.newLine();
	}

	@Override
	public void writeEmissionMatrix(EmissionEntry entry) throws IOException {
		bWriter.write(entry.toString());
		bWriter.newLine();
	}
	
	@Override
	public void close () throws IOException {
		bWriter.flush();
		bWriter.close();
	}
}