package hust.tools.hmm.stream;

import java.io.IOException;

/**
 * Abstract base class for filtering {@link ObjectStream}s.
 * <p>
 * Filtering streams take an existing stream and convert
 * its output to something else.
 *
 * @param <S> the type of the source/input stream
 * @param <T> the type of this stream
 */
public abstract class FilterObjectStream<S, T> implements ObjectStream<T> {

	protected final ObjectStream<S> samples;

	protected FilterObjectStream(ObjectStream<S> samples) {
		if (samples == null)
			throw new IllegalArgumentException("samples must not be null!");

		this.samples = samples;
	}

	public void reset() throws IOException, UnsupportedOperationException {
		samples.reset();
	}

	public void close() throws IOException {
		samples.close();
	}
}
