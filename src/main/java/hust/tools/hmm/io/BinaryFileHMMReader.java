package hust.tools.hmm.io;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 *<ul>
 *<li>Description: 读取二进制模型文件 
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月14日
 *</ul>
 */
public class BinaryFileHMMReader extends AbstractHMMReader {
	
	public BinaryFileHMMReader(String pathname) throws IOException {
		this(new File(pathname));
	}

	public BinaryFileHMMReader(File file) throws IOException {
		this(new BinaryDataReader(file));
	}

	public BinaryFileHMMReader(InputStream is) {
		super(new BinaryDataReader(is));
	}
	
	public BinaryFileHMMReader(DataInputStream dis) {
		super(new BinaryDataReader(dis));
	}
	
	public BinaryFileHMMReader(DataReader reader) {
		super(reader);
	}
}
