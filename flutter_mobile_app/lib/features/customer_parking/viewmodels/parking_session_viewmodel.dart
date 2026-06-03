import 'package:flutter/material.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/features/customer_parking/models/parking_session_models.dart';
import 'package:smart_parking_mobile/features/customer_parking/repositories/parking_session_repository.dart';

class ParkingSessionViewModel extends ChangeNotifier {
  ParkingSessionViewModel(this._repository);

  final ParkingSessionRepository _repository;

  ViewState<List<ParkingSession>> sessionsState = const Idle();
  ViewState<List<ParkingSession>> historySessionsState = const Idle();
  ViewState<ParkingSession> currentSessionState = const Idle();

  Future<void> fetchSessions([String? customerId]) async {
    sessionsState = const Loading();
    notifyListeners();

    try {
      final sessions = await _repository.getSessions();
      sessionsState = Success(_sortNewestFirst(sessions));
    } catch (e) {
      sessionsState = Failure(e.toString());
    }
    notifyListeners();
  }

  Future<void> fetchSessionById(String sessionId) async {
    currentSessionState = const Loading();
    notifyListeners();

    try {
      final session = await _repository.getSessionDetail(sessionId);
      currentSessionState = Success(session);
    } catch (e) {
      currentSessionState = Failure(e.toString());
    }
    notifyListeners();
  }

  Future<void> fetchSessionHistory({
    String? customerId,
    DateTime? startDate,
    DateTime? endDate,
    String? plateNumber,
  }) async {
    historySessionsState = const Loading();
    notifyListeners();

    try {
      final sessions = await _repository.getSessions(
        startDate: startDate,
        endDate: endDate == null
            ? null
            : DateTime(endDate.year, endDate.month, endDate.day, 23, 59, 59),
        plateNumber: plateNumber,
      );
      final completed = sessions
          .where((session) => session.status == SessionStatus.completed)
          .toList();
      historySessionsState = Success(_sortNewestFirst(completed));
    } catch (e) {
      historySessionsState = Failure(e.toString());
    }
    notifyListeners();
  }

  List<ParkingSession> _sortNewestFirst(List<ParkingSession> sessions) {
    return [...sessions]..sort((a, b) => b.entryTime.compareTo(a.entryTime));
  }
}
