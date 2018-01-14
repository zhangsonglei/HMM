package hust.tools.hmm.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

/**
 *<ul>
 *<li>Description: 读取序列化模型文件
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月14日
 *</ul>
 */
public class ObjectFileHMMReader extends AbstractHMMReader {

	public ObjectFileHMMReader(String filename) throws FileNotFoundException, IOException {
		this(new File(filename));
	}
	
	public ObjectFileHMMReader(File file) throws FileNotFoundException, IOException {
		this(new ObjectInputStream(new FileInputStream(file)));
	}
	
	public ObjectFileHMMReader(InputStream is) throws IOException {
		super(new ObjectDataReader(is));
	}
	
	public ObjectFileHMMReader(ObjectInputStream ois) {
		super(new ObjectDataReader(ois));
	}

	public ObjectFileHMMReader(DataReader reader) {
		super(reader);
	}
}
