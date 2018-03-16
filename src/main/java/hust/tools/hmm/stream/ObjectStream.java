package hust.tools.hmm.stream;

import java.io.IOException;

public interface ObjectStream<T> extends AutoCloseable {

	/**
	 * Returns the next object. Calling this method repeatedly until it returns
	 * null will return each object from the underlying source exactly once.
	 *
	 * @return the next object or null to signal that the stream is exhausted
	 *
	 * @throws IOException if there is an error during reading
	 */
	T read() throws IOException;

	/**
	 * Repositions the stream at the beginning and the previously seen object sequence
	 * will be repeated exactly. This method can be used to re-read
	 * the stream if multiple passes over the objects are required.
	 *
	 * The implementation of this method is optional.
	 *
	 * @throws IOException if there is an error during reseting the stream
	 */
	void reset() throws IOException, UnsupportedOperationException;
	  
	/**
	 * Closes the <code>ObjectStream</code> and releases all allocated
	 * resources. After close was called its not allowed to call
	 * read or reset.
	 *
	 * @throws IOException if there is an error during closing the stream
	 */
	void close() throws IOException;
}

