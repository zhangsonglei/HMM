package hust.tools.hmm.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import hust.tools.hmm.model.HMModelBasedBO;

/**
 *<ul>
 *<li>Description: 将模型写入对象文件
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2018年1月14日
 *</ul>
 */
public class ObjectFileHMMWriter extends AbstractHMMWriter {
	
	ObjectOutputStream dos;
	
	public ObjectFileHMMWriter(HMModelBasedBO model, String pathname) throws FileNotFoundException, IOException {
		this(model, new File(pathname));
	}
	
	public ObjectFileHMMWriter(HMModelBasedBO model, File file) throws FileNotFoundException, IOException {
		this(model, new FileOutputStream(file));
	}
	
	public ObjectFileHMMWriter(HMModelBasedBO model, OutputStream os) throws IOException {
		this(model, new ObjectOutputStream(os));
	}
	
	public ObjectFileHMMWriter(HMModelBasedBO model, ObjectOutputStream dos) {
		super(model);
		this.dos = dos;
	}

	@Override
	public void writeCount(long count) throws IOException {
		dos.writeLong(count);
	}

	@Override
	public void writeObservationIndex(ObservationIndex entry) throws IOException {
		dos.writeObject(entry);
	}
	
	@Override
	public void writeStateIndex(StateIndex entry) throws IOException {
		dos.writeObject(entry);
	}

	@Override
	public void writePi(PiEntry entry) throws IOException {
		dos.writeObject(entry);
	}

	@Override
	public void writeTransitionMatrix(TransitionEntry entry) throws IOException {
		dos.writeObject(entry);
	}

	@Override
	public void writeEmissionMatrix(EmissionEntry entry) throws IOException {
		dos.writeObject(entry);
	}
	
	@Override
	public void close() throws IOException {
		dos.flush();
		dos.close();
	}
}
