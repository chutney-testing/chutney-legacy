package com.chutneytesting.tools;

public interface CloseableResource<T> extends AutoCloseable {

	void close();

	T getResource();

	static <T> CloseableResource<T> build(T resource, Runnable closer) {
		return new CloseableResource<T>() {

			@Override
			public void close() {
				closer.run();
			}

			@Override
			public T getResource() {
				return resource;
			}
		};
	}
}
