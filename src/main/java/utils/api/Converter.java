package utils.api;

public interface Converter<T, K> {
    public T convert(K o);
}
