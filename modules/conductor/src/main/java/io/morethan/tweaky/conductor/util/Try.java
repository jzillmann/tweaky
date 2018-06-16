package io.morethan.tweaky.conductor.util;

/**
 * Try-Success-Failure construct.
 *
 * @param <S>
 *            success object
 * @param <F>
 *            failure object
 * 
 */
public interface Try<S, F> {

    abstract boolean isSuccess();

    abstract F failure();

    abstract S get();

    public static <S, F> Try<S, F> failure(F failure) {
        return new Failure<>(failure);
    }

    public static <S, F> Try<S, F> success(S success) {
        return new Success<>(success);
    }

    public static class Success<S, F> implements Try<S, F> {

        private final S _success;

        public Success(S success) {
            _success = success;
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public F failure() {
            throw new IllegalStateException("Don't call failure() on success!");
        }

        @Override
        public S get() {
            return _success;
        }

    }

    public static class Failure<S, F> implements Try<S, F> {

        private final F _failure;

        public Failure(F failure) {
            _failure = failure;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public F failure() {
            return _failure;
        }

        @Override
        public S get() {
            throw new IllegalStateException("Don't call get() on failure!");
        }

    }
}
