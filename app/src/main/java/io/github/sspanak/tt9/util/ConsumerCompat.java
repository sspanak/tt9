package io.github.sspanak.tt9.util;

@FunctionalInterface
public interface ConsumerCompat<T> {
	void accept(T t);
}
