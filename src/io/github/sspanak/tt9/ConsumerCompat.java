package io.github.sspanak.tt9;

/**
 * ConsumerCompat
 * A fallback interface for Consumer in API < 24
 */
public interface ConsumerCompat<T>{
	void accept(T t);
	default ConsumerCompat<T> andThen(ConsumerCompat<? super T> after) {return null;}
}
