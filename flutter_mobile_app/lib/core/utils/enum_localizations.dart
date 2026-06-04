import 'package:flutter/widgets.dart';
import 'package:smart_parking_mobile/core/l10n/app_localizations.dart';

import 'package:smart_parking_mobile/features/customer_packages/models/booking_models.dart';
import 'package:smart_parking_mobile/features/customer_history/models/history_models.dart' as history;
import 'package:smart_parking_mobile/features/customer_complaint/models/complaint_models.dart';
import 'package:smart_parking_mobile/features/customer_parking/models/parking_session_models.dart';
import 'package:smart_parking_mobile/features/customer_home/models/home_models.dart';
import 'package:smart_parking_mobile/features/customer_notification/models/notification_models.dart';

extension BookingStatusL10n on BookingStatus {
  String label(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    switch (this) {
      case BookingStatus.active: return l10n.enumBookingActive;
      case BookingStatus.expired: return l10n.enumBookingExpired;
      case BookingStatus.pendingPayment: return l10n.enumBookingPendingPayment;
      case BookingStatus.canceled: return l10n.enumBookingCanceled;
      case BookingStatus.pendingActivation: return l10n.enumBookingPendingActivation;
      case BookingStatus.needsAttention: return l10n.enumBookingNeedsAttention;
      case BookingStatus.partialPayment: return l10n.enumBookingPartialPayment;
      case BookingStatus.draft: return l10n.enumBookingDraft;
      case BookingStatus.complete: return l10n.enumBookingComplete;
    }
  }
}

extension PaymentStatusL10n on PaymentStatus {
  String label(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    switch (this) {
      case PaymentStatus.pending: return l10n.enumPaymentPending;
      case PaymentStatus.success: return l10n.enumPaymentSuccess;
      case PaymentStatus.failed: return l10n.enumPaymentFailed;
      case PaymentStatus.refunded: return l10n.enumPaymentRefunded;
    }
  }
}

extension HistoryPaymentStatusL10n on history.PaymentStatus {
  String label(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    switch (this) {
      case history.PaymentStatus.pending: return l10n.enumPaymentPending;
      case history.PaymentStatus.success: return l10n.enumPaymentSuccess;
      case history.PaymentStatus.failed: return l10n.enumPaymentFailed;
    }
  }
}

extension PaymentMethodL10n on PaymentMethod {
  String label(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    switch (this) {
      case PaymentMethod.cash: return l10n.enumPaymentMethodCash;
      case PaymentMethod.payos: return l10n.enumPaymentMethodPayos;
      case PaymentMethod.vnpay: return l10n.enumPaymentMethodVnpay;
    }
  }
}

extension HistoryPaymentMethodL10n on history.PaymentMethod {
  String label(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    switch (this) {
      case history.PaymentMethod.cash: return l10n.enumPaymentMethodCash;
      case history.PaymentMethod.qr: return l10n.enumPaymentMethodQr;
      case history.PaymentMethod.other: return l10n.enumPaymentMethodOther;
    }
  }
}

extension SessionStatusL10n on SessionStatus {
  String label(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    switch (this) {
      case SessionStatus.ongoing: return l10n.enumSessionOngoing;
      case SessionStatus.completed: return l10n.enumSessionCompleted;
    }
  }
}

extension VehicleStatusL10n on VehicleStatus {
  String label(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    switch (this) {
      case VehicleStatus.active: return l10n.enumVehicleActive;
      case VehicleStatus.expiringSoon: return l10n.enumVehicleExpiringSoon;
      case VehicleStatus.expired: return l10n.enumVehicleExpired;
    }
  }
}

extension ComplaintStatusL10n on ComplaintStatus {
  String label(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    switch (this) {
      case ComplaintStatus.pending: return l10n.enumComplaintPending;
      case ComplaintStatus.processing: return l10n.enumComplaintProcessing;
      case ComplaintStatus.resolved: return l10n.enumComplaintResolved;
      case ComplaintStatus.rejected: return l10n.enumComplaintRejected;
    }
  }
}

extension NotificationTypeL10n on NotificationType {
  String label(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    switch (this) {
      case NotificationType.debt: return l10n.enumNotifDebt;
      case NotificationType.security: return l10n.enumNotifSecurity;
      case NotificationType.system: return l10n.enumNotifSystem;
      case NotificationType.broadcast: return l10n.enumNotifBroadcast;
    }
  }
}
