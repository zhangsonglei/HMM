package hust.tools.hmm.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 *<ul>
 *<li>Description: 读取普通文本模型文件 
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月14日
 *</ul>
 */
public class TextFileHMMReader extends AbstractHMMReader {

	public TextFileHMMReader(String pathname) throws IOException {
		this(new File(pathname));
	}
	
	public TextFileHMMReader(File file) throws IOException {
		super(new TextDataReader(file));
	}
	
	public TextFileHMMReader(InputStream is) {
		super(new TextDataReader(is));
	}
	
	public TextFileHMMReader(BufferedReader reader) {
		super(new TextDataReader(reader));
	}
	
	public TextFileHMMReader(DataReader reader) {
		super(reader);
	}
}
