/// Base class representing async operation state in a ViewModel.
/// Use this instead of managing _isLoading / _errorMessage separately.
sealed class ViewState<T> {
  const ViewState();
}

/// Waiting for user interaction — no op has started yet.
final class Idle<T> extends ViewState<T> {
  const Idle();
}

/// Operation is in progress.
final class Loading<T> extends ViewState<T> {
  const Loading();
}

/// Operation completed successfully.
final class Success<T> extends ViewState<T> {
  final T data;
  const Success(this.data);
}

/// Operation failed with an error message.
final class Failure<T> extends ViewState<T> {
  final String message;
  const Failure(this.message);
}
