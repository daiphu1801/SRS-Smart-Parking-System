// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for English (`en`).
class AppLocalizationsEn extends AppLocalizations {
  AppLocalizationsEn([String locale = 'en']) : super(locale);

  @override
  String get appTitle => 'Smart Parking';

  @override
  String get monthlyContracts => 'Monthly Contracts';

  @override
  String get dataLoadError => 'Data loading error';

  @override
  String get noContractsYet => 'No contracts yet';

  @override
  String get monthlyPackageSavesMoney =>
      'Monthly parking packages help you save significant parking costs.';

  @override
  String get vehicleList => 'Vehicle List';

  @override
  String get addVehicle => 'Add Vehicle';

  @override
  String get noVehiclesYet => 'No vehicles registered';

  @override
  String get clickAddVehicleToStart =>
      'Click \"Add Vehicle\" to start registering a monthly package.';

  @override
  String get monthlyPackageDetails => 'Monthly Package Details';

  @override
  String get contract => 'Contract';

  @override
  String get vehicleManagement => 'Vehicle Management';

  @override
  String get groupRepresentative => 'Group / Rep:';

  @override
  String get createdAt => 'Created at:';

  @override
  String get registeredVehiclesCount => 'Registered vehicles:';

  @override
  String get vehicles => 'vehicles';

  @override
  String get paymentMethod => 'Payment method:';

  @override
  String get addVehicleToBooking => 'Add Vehicle to Booking';

  @override
  String get bookingDetailList => 'Booking Detail List';

  @override
  String get childAccounts => 'Child Accounts';

  @override
  String get addChildAccount => 'Add Child Account';

  @override
  String get fullName => 'Full Name';

  @override
  String get address => 'Address';

  @override
  String get phoneNumber => 'Phone Number';

  @override
  String get cancel => 'Cancel';

  @override
  String get add => 'Add';

  @override
  String get nameAndPhoneRequired => 'Name and phone number are required';

  @override
  String get childAccountAdded => 'Child account added successfully';

  @override
  String get errorLoadingChildAccounts => 'Error loading child accounts';

  @override
  String get noChildAccountsYet => 'No child accounts yet';

  @override
  String get canAddChildAccountsForVehicles =>
      'You can add child accounts to manage their own vehicles.';

  @override
  String get pleaseEnterLicensePlate => 'Please enter license plate';

  @override
  String get addedToCartSuccessfully => 'Added to cart successfully!';

  @override
  String get errorAddingVehicle => 'An error occurred while adding vehicle';

  @override
  String get addNewVehicleRegistration => 'New Vehicle Registration';

  @override
  String get vehicleInformation => 'Vehicle Information';

  @override
  String get licensePlate => 'License Plate';

  @override
  String get licensePlateExample => 'E.g., 30A-123.45';

  @override
  String get vehicleType => 'Vehicle Type';

  @override
  String get selectPackage => 'Select Package';

  @override
  String get pleaseSelectVehicleTypeFirst => 'Please select vehicle type first';

  @override
  String get noSuitablePackageForThisVehicleType =>
      'No suitable package for this vehicle type';

  @override
  String get addToCart => 'Add to Cart';

  @override
  String get months => 'months';

  @override
  String get duration => 'Duration';

  @override
  String get validFrom => 'Valid from';

  @override
  String get validUntil => 'Until';

  @override
  String get total => 'Total';

  @override
  String get bookingDetailDetails => 'Booking Detail Details';

  @override
  String get id => 'ID:';

  @override
  String get bookingDetailId => 'Booking Detail ID:';

  @override
  String get bookingId => 'Booking ID:';

  @override
  String get customerId => 'Customer ID:';

  @override
  String get packagePriceId => 'Package Price ID:';

  @override
  String get licensePlateLabel => 'License plate:';

  @override
  String get vehicleTypeLabel => 'Vehicle type:';

  @override
  String get packageTypeLabel => 'Package:';

  @override
  String get durationLabel => 'Duration:';

  @override
  String get statusLabel => 'Status:';

  @override
  String get statusActive => 'Active';

  @override
  String get statusExpiringSoon => 'Expiring soon';

  @override
  String get statusExpired => 'Expired';

  @override
  String get priceLabel => 'Price:';

  @override
  String get startDateLabel => 'Start date:';

  @override
  String get endDateLabel => 'End date:';

  @override
  String get pay => 'Pay';

  @override
  String get pendingPayment => 'Pending payment';

  @override
  String get renewContract => 'Renew Contract';

  @override
  String get renewSuccess => 'Renewal successful!';

  @override
  String get renewFailedServerUpdate => 'Renewal failed at server update.';

  @override
  String get renewBookingDetail => 'Renew Booking Detail';

  @override
  String get packageType => 'Package Type';

  @override
  String get renew => 'Renew';

  @override
  String get selectRenewalMonths => 'Select renewal months:';

  @override
  String get totalAmount => 'Total amount:';

  @override
  String get payAndRenew => 'Pay & Renew';

  @override
  String get welcomeMorning => 'Good morning';

  @override
  String get welcomeAfternoon => 'Good afternoon';

  @override
  String get welcomeEvening => 'Good evening';

  @override
  String get accountVerified => 'Verified account';

  @override
  String get myVehicles => 'Your vehicles';

  @override
  String get currentParkingSession => 'Current parking session';

  @override
  String get quickActions => 'Quick actions';

  @override
  String get tryAgain => 'Try again';

  @override
  String get complaintStatusPending => 'Pending';

  @override
  String get complaintStatusProcessing => 'Processing';

  @override
  String get complaintStatusResolved => 'Resolved';

  @override
  String get complaintStatusRejected => 'Rejected';

  @override
  String complaintCodeLabel(String id) {
    return 'ID: $id';
  
  @override
  String get enumBookingActive => 'Active';
  @override
  String get enumBookingExpired => 'Expired';
  @override
  String get enumBookingPendingPayment => 'Pending Payment';
  @override
  String get enumBookingCanceled => 'Canceled';
  @override
  String get enumBookingPendingActivation => 'Pending Activation';
  @override
  String get enumBookingNeedsAttention => 'Needs Attention';
  @override
  String get enumBookingPartialPayment => 'Partial Payment';
  @override
  String get enumBookingDraft => 'Draft';
  @override
  String get enumBookingComplete => 'Complete';
  @override
  String get enumPaymentPending => 'Pending';
  @override
  String get enumPaymentSuccess => 'Success';
  @override
  String get enumPaymentFailed => 'Failed';
  @override
  String get enumPaymentRefunded => 'Refunded';
  @override
  String get enumPaymentCanceled => 'Canceled';
  @override
  String get enumPaymentMethodCash => 'Cash';
  @override
  String get enumPaymentMethodPayos => 'PayOS';
  @override
  String get enumPaymentMethodVnpay => 'VNPay';
  @override
  String get enumPaymentMethodBankTransfer => 'Bank Transfer';
  @override
  String get enumPaymentMethodCreditCard => 'Credit Card';
  @override
  String get enumPaymentMethodQr => 'VietQR';
  @override
  String get enumPaymentMethodOther => 'Other';
  @override
  String get enumSessionOngoing => 'Ongoing';
  @override
  String get enumSessionCompleted => 'Completed';
  @override
  String get enumVehicleActive => 'Active';
  @override
  String get enumVehicleExpiringSoon => 'Expiring Soon';
  @override
  String get enumVehicleExpired => 'Expired';
  @override
  String get enumComplaintPending => 'Pending';
  @override
  String get enumComplaintProcessing => 'Processing';
  @override
  String get enumComplaintResolved => 'Resolved';
  @override
  String get enumComplaintRejected => 'Rejected';
  @override
  String get enumNotifDebt => 'Debt Reminder';
  @override
  String get enumNotifSecurity => 'Security Alert';
  @override
  String get enumNotifSystem => 'System';
  @override
  String get enumNotifBroadcast => 'Broadcast';
  @override
  String get errPaymentNotRecorded => 'The system has not recorded the payment yet. Please wait a moment and try again.';
  @override
  String errInvalidStatus(String status) => 'Invoice has status: $status';
}

  @override
  String get expiringSoonAlert => 'Package expiring soon';

  @override
  String expiringSoonMessage(int count) {
    return 'You have $count vehicles expiring soon. Please renew to avoid interruption.';
  
  @override
  String get enumBookingActive => 'Active';
  @override
  String get enumBookingExpired => 'Expired';
  @override
  String get enumBookingPendingPayment => 'Pending Payment';
  @override
  String get enumBookingCanceled => 'Canceled';
  @override
  String get enumBookingPendingActivation => 'Pending Activation';
  @override
  String get enumBookingNeedsAttention => 'Needs Attention';
  @override
  String get enumBookingPartialPayment => 'Partial Payment';
  @override
  String get enumBookingDraft => 'Draft';
  @override
  String get enumBookingComplete => 'Complete';
  @override
  String get enumPaymentPending => 'Pending';
  @override
  String get enumPaymentSuccess => 'Success';
  @override
  String get enumPaymentFailed => 'Failed';
  @override
  String get enumPaymentRefunded => 'Refunded';
  @override
  String get enumPaymentCanceled => 'Canceled';
  @override
  String get enumPaymentMethodCash => 'Cash';
  @override
  String get enumPaymentMethodPayos => 'PayOS';
  @override
  String get enumPaymentMethodVnpay => 'VNPay';
  @override
  String get enumPaymentMethodBankTransfer => 'Bank Transfer';
  @override
  String get enumPaymentMethodCreditCard => 'Credit Card';
  @override
  String get enumPaymentMethodQr => 'VietQR';
  @override
  String get enumPaymentMethodOther => 'Other';
  @override
  String get enumSessionOngoing => 'Ongoing';
  @override
  String get enumSessionCompleted => 'Completed';
  @override
  String get enumVehicleActive => 'Active';
  @override
  String get enumVehicleExpiringSoon => 'Expiring Soon';
  @override
  String get enumVehicleExpired => 'Expired';
  @override
  String get enumComplaintPending => 'Pending';
  @override
  String get enumComplaintProcessing => 'Processing';
  @override
  String get enumComplaintResolved => 'Resolved';
  @override
  String get enumComplaintRejected => 'Rejected';
  @override
  String get enumNotifDebt => 'Debt Reminder';
  @override
  String get enumNotifSecurity => 'Security Alert';
  @override
  String get enumNotifSystem => 'System';
  @override
  String get enumNotifBroadcast => 'Broadcast';
  @override
  String get errPaymentNotRecorded => 'The system has not recorded the payment yet. Please wait a moment and try again.';
  @override
  String errInvalidStatus(String status) => 'Invoice has status: $status';
}

  @override
  String get noVehiclesHome => 'No vehicles registered';

  @override
  String get noVehiclesRegistered =>
      'You have not registered any vehicles yet.';

  @override
  String get defaultPackage => 'Default package';

  @override
  String get expired => 'Expired';

  @override
  String daysLeft(int days) {
    return '$days days left';
  
  @override
  String get enumBookingActive => 'Active';
  @override
  String get enumBookingExpired => 'Expired';
  @override
  String get enumBookingPendingPayment => 'Pending Payment';
  @override
  String get enumBookingCanceled => 'Canceled';
  @override
  String get enumBookingPendingActivation => 'Pending Activation';
  @override
  String get enumBookingNeedsAttention => 'Needs Attention';
  @override
  String get enumBookingPartialPayment => 'Partial Payment';
  @override
  String get enumBookingDraft => 'Draft';
  @override
  String get enumBookingComplete => 'Complete';
  @override
  String get enumPaymentPending => 'Pending';
  @override
  String get enumPaymentSuccess => 'Success';
  @override
  String get enumPaymentFailed => 'Failed';
  @override
  String get enumPaymentRefunded => 'Refunded';
  @override
  String get enumPaymentCanceled => 'Canceled';
  @override
  String get enumPaymentMethodCash => 'Cash';
  @override
  String get enumPaymentMethodPayos => 'PayOS';
  @override
  String get enumPaymentMethodVnpay => 'VNPay';
  @override
  String get enumPaymentMethodBankTransfer => 'Bank Transfer';
  @override
  String get enumPaymentMethodCreditCard => 'Credit Card';
  @override
  String get enumPaymentMethodQr => 'VietQR';
  @override
  String get enumPaymentMethodOther => 'Other';
  @override
  String get enumSessionOngoing => 'Ongoing';
  @override
  String get enumSessionCompleted => 'Completed';
  @override
  String get enumVehicleActive => 'Active';
  @override
  String get enumVehicleExpiringSoon => 'Expiring Soon';
  @override
  String get enumVehicleExpired => 'Expired';
  @override
  String get enumComplaintPending => 'Pending';
  @override
  String get enumComplaintProcessing => 'Processing';
  @override
  String get enumComplaintResolved => 'Resolved';
  @override
  String get enumComplaintRejected => 'Rejected';
  @override
  String get enumNotifDebt => 'Debt Reminder';
  @override
  String get enumNotifSecurity => 'Security Alert';
  @override
  String get enumNotifSystem => 'System';
  @override
  String get enumNotifBroadcast => 'Broadcast';
  @override
  String get errPaymentNotRecorded => 'The system has not recorded the payment yet. Please wait a moment and try again.';
  @override
  String errInvalidStatus(String status) => 'Invoice has status: $status';
}

  @override
  String get noParkingSession => 'No active session';

  @override
  String get notInAnyParkingLot => 'You are not currently in any parking lot.';

  @override
  String entryTimeLabel(String time) {
    return 'Entry: $time';
  
  @override
  String get enumBookingActive => 'Active';
  @override
  String get enumBookingExpired => 'Expired';
  @override
  String get enumBookingPendingPayment => 'Pending Payment';
  @override
  String get enumBookingCanceled => 'Canceled';
  @override
  String get enumBookingPendingActivation => 'Pending Activation';
  @override
  String get enumBookingNeedsAttention => 'Needs Attention';
  @override
  String get enumBookingPartialPayment => 'Partial Payment';
  @override
  String get enumBookingDraft => 'Draft';
  @override
  String get enumBookingComplete => 'Complete';
  @override
  String get enumPaymentPending => 'Pending';
  @override
  String get enumPaymentSuccess => 'Success';
  @override
  String get enumPaymentFailed => 'Failed';
  @override
  String get enumPaymentRefunded => 'Refunded';
  @override
  String get enumPaymentCanceled => 'Canceled';
  @override
  String get enumPaymentMethodCash => 'Cash';
  @override
  String get enumPaymentMethodPayos => 'PayOS';
  @override
  String get enumPaymentMethodVnpay => 'VNPay';
  @override
  String get enumPaymentMethodBankTransfer => 'Bank Transfer';
  @override
  String get enumPaymentMethodCreditCard => 'Credit Card';
  @override
  String get enumPaymentMethodQr => 'VietQR';
  @override
  String get enumPaymentMethodOther => 'Other';
  @override
  String get enumSessionOngoing => 'Ongoing';
  @override
  String get enumSessionCompleted => 'Completed';
  @override
  String get enumVehicleActive => 'Active';
  @override
  String get enumVehicleExpiringSoon => 'Expiring Soon';
  @override
  String get enumVehicleExpired => 'Expired';
  @override
  String get enumComplaintPending => 'Pending';
  @override
  String get enumComplaintProcessing => 'Processing';
  @override
  String get enumComplaintResolved => 'Resolved';
  @override
  String get enumComplaintRejected => 'Rejected';
  @override
  String get enumNotifDebt => 'Debt Reminder';
  @override
  String get enumNotifSecurity => 'Security Alert';
  @override
  String get enumNotifSystem => 'System';
  @override
  String get enumNotifBroadcast => 'Broadcast';
  @override
  String get errPaymentNotRecorded => 'The system has not recorded the payment yet. Please wait a moment and try again.';
  @override
  String errInvalidStatus(String status) => 'Invoice has status: $status';
}

  @override
  String parkingDurationLabel(String duration) {
    return 'Duration: $duration';
  
  @override
  String get enumBookingActive => 'Active';
  @override
  String get enumBookingExpired => 'Expired';
  @override
  String get enumBookingPendingPayment => 'Pending Payment';
  @override
  String get enumBookingCanceled => 'Canceled';
  @override
  String get enumBookingPendingActivation => 'Pending Activation';
  @override
  String get enumBookingNeedsAttention => 'Needs Attention';
  @override
  String get enumBookingPartialPayment => 'Partial Payment';
  @override
  String get enumBookingDraft => 'Draft';
  @override
  String get enumBookingComplete => 'Complete';
  @override
  String get enumPaymentPending => 'Pending';
  @override
  String get enumPaymentSuccess => 'Success';
  @override
  String get enumPaymentFailed => 'Failed';
  @override
  String get enumPaymentRefunded => 'Refunded';
  @override
  String get enumPaymentCanceled => 'Canceled';
  @override
  String get enumPaymentMethodCash => 'Cash';
  @override
  String get enumPaymentMethodPayos => 'PayOS';
  @override
  String get enumPaymentMethodVnpay => 'VNPay';
  @override
  String get enumPaymentMethodBankTransfer => 'Bank Transfer';
  @override
  String get enumPaymentMethodCreditCard => 'Credit Card';
  @override
  String get enumPaymentMethodQr => 'VietQR';
  @override
  String get enumPaymentMethodOther => 'Other';
  @override
  String get enumSessionOngoing => 'Ongoing';
  @override
  String get enumSessionCompleted => 'Completed';
  @override
  String get enumVehicleActive => 'Active';
  @override
  String get enumVehicleExpiringSoon => 'Expiring Soon';
  @override
  String get enumVehicleExpired => 'Expired';
  @override
  String get enumComplaintPending => 'Pending';
  @override
  String get enumComplaintProcessing => 'Processing';
  @override
  String get enumComplaintResolved => 'Resolved';
  @override
  String get enumComplaintRejected => 'Rejected';
  @override
  String get enumNotifDebt => 'Debt Reminder';
  @override
  String get enumNotifSecurity => 'Security Alert';
  @override
  String get enumNotifSystem => 'System';
  @override
  String get enumNotifBroadcast => 'Broadcast';
  @override
  String get errPaymentNotRecorded => 'The system has not recorded the payment yet. Please wait a moment and try again.';
  @override
  String errInvalidStatus(String status) => 'Invoice has status: $status';
}

  @override
  String get parkingSessionAction => 'Parking Session';

  @override
  String get billingAction => 'Billing';

  @override
  String get navHome => 'Home';

  @override
  String get navParking => 'Parking';

  @override
  String get navPackages => 'Packages';

  @override
  String get navHistory => 'History';

  @override
  String get navAccount => 'Account';

  @override
  String get accountTitle => 'Account';

  @override
  String get errorLoadingProfile => 'Error loading profile';

  @override
  String get settingsAndSupport => 'Settings & Support';

  @override
  String get profileDetails => 'Profile Details';

  @override
  String get profileDetailsSubtitle => 'View full info, ID, address';

  @override
  String get childAccountsManage => 'Manage child accounts & vehicles';

  @override
  String get changePassword => 'Change Password';

  @override
  String get changePasswordSubtitle => 'Update account security password';

  @override
  String get supportAndComplaints => 'Support & Complaints';

  @override
  String get supportAndComplaintsSubtitle => 'Submit your feedback';

  @override
  String get notifications => 'Notifications';

  @override
  String get notificationsSubtitle => 'Manage notifications';

  @override
  String get logout => 'Log Out';

  @override
  String get editProfileTooltip => 'Edit Profile';

  @override
  String get phoneLabel => 'Phone';

  @override
  String get groupLabel => 'Group';

  @override
  String get history => 'History';

  @override
  String get complaintsFeedback => 'Complaints / Feedback';

  @override
  String get filter => 'Filter';

  @override
  String get noComplaintsYet => 'No complaints yet';

  @override
  String get complaintsFeedbackEncourage =>
      'Your feedback helps us improve our service.';

  @override
  String get createComplaint => 'Create Complaint';

  @override
  String get submitNewComplaint => 'Submit New Complaint';

  @override
  String get complaintsListenMessage =>
      'We always listen to your feedback to improve our service.';

  @override
  String get complaintTitle => 'Title';

  @override
  String get complaintTitlePlaceholder =>
      'E.g., Plate recognition error at gate A';

  @override
  String get pleaseEnterTitle => 'Please enter title';

  @override
  String get complaintContent => 'Description';

  @override
  String get complaintContentPlaceholder => 'Describe your issue in detail...';

  @override
  String get pleaseEnterContent => 'Please enter content';

  @override
  String get attachedImages => 'Attached images (if any)';

  @override
  String get tapToUploadImage => 'Tap to upload image';

  @override
  String get imagePickerInDev => 'Image picker is currently under development';

  @override
  String get submitComplaint => 'Submit Complaint';

  @override
  String get complaintSubmitSuccess =>
      'Complaint submitted successfully! We will process it soon.';

  @override
  String get complaintSubmitError => 'An error occurred, please try again';

  @override
  String get readAll => 'Mark all read';

  @override
  String get cannotLoadNotifications => 'Cannot load notifications';

  @override
  String get noNotificationsYet => 'No notifications yet';

  @override
  String get systemNotificationsAppearHere =>
      'System notifications will appear here.';

  @override
  String get parkingSessions => 'Parking Sessions';

  @override
  String get filterAll => 'All';

  @override
  String get filterOngoing => 'Ongoing';

  @override
  String get filterCompleted => 'Completed';

  @override
  String get noParkingSessions => 'No parking sessions yet';

  @override
  String get parkingHistoryAppearHere =>
      'Your parking history will appear here.';

  @override
  String get noFilterResults => 'No results';

  @override
  String get noMatchingParkingSession =>
      'No parking sessions match the filter.';

  @override
  String get manualGateOpen => 'Manual gate opening';

  @override
  String get entryLabel => 'Entry:';

  @override
  String get exitLabel => 'Exit:';

  @override
  String get sessionDurationLabel => 'Duration:';

  @override
  String get vehicleInLot => 'â€” Vehicle is in lot';

  @override
  String gracePeriodPrompt(int mins, String secs) {
    return 'Please pick up vehicle in: ${mins}m ${secs}s';
  
  @override
  String get enumBookingActive => 'Active';
  @override
  String get enumBookingExpired => 'Expired';
  @override
  String get enumBookingPendingPayment => 'Pending Payment';
  @override
  String get enumBookingCanceled => 'Canceled';
  @override
  String get enumBookingPendingActivation => 'Pending Activation';
  @override
  String get enumBookingNeedsAttention => 'Needs Attention';
  @override
  String get enumBookingPartialPayment => 'Partial Payment';
  @override
  String get enumBookingDraft => 'Draft';
  @override
  String get enumBookingComplete => 'Complete';
  @override
  String get enumPaymentPending => 'Pending';
  @override
  String get enumPaymentSuccess => 'Success';
  @override
  String get enumPaymentFailed => 'Failed';
  @override
  String get enumPaymentRefunded => 'Refunded';
  @override
  String get enumPaymentCanceled => 'Canceled';
  @override
  String get enumPaymentMethodCash => 'Cash';
  @override
  String get enumPaymentMethodPayos => 'PayOS';
  @override
  String get enumPaymentMethodVnpay => 'VNPay';
  @override
  String get enumPaymentMethodBankTransfer => 'Bank Transfer';
  @override
  String get enumPaymentMethodCreditCard => 'Credit Card';
  @override
  String get enumPaymentMethodQr => 'VietQR';
  @override
  String get enumPaymentMethodOther => 'Other';
  @override
  String get enumSessionOngoing => 'Ongoing';
  @override
  String get enumSessionCompleted => 'Completed';
  @override
  String get enumVehicleActive => 'Active';
  @override
  String get enumVehicleExpiringSoon => 'Expiring Soon';
  @override
  String get enumVehicleExpired => 'Expired';
  @override
  String get enumComplaintPending => 'Pending';
  @override
  String get enumComplaintProcessing => 'Processing';
  @override
  String get enumComplaintResolved => 'Resolved';
  @override
  String get enumComplaintRejected => 'Rejected';
  @override
  String get enumNotifDebt => 'Debt Reminder';
  @override
  String get enumNotifSecurity => 'Security Alert';
  @override
  String get enumNotifSystem => 'System';
  @override
  String get enumNotifBroadcast => 'Broadcast';
  @override
  String get errPaymentNotRecorded => 'The system has not recorded the payment yet. Please wait a moment and try again.';
  @override
  String errInvalidStatus(String status) => 'Invoice has status: $status';
}

  @override
  String get parkingSessionDetail => 'Parking Session Detail';

  @override
  String get lprCameraImage => 'LPR Camera Plate Image';

  @override
  String get car => 'Car';

  @override
  String get motorbike => 'Motorbike';

  @override
  String get packageCode => 'Package ID';

  @override
  String get attention => 'Note';

  @override
  String get gatesInOut => 'Gates entry / exit';

  @override
  String get gateIn => 'Entry gate';

  @override
  String get gateOut => 'Exit gate';

  @override
  String get notExitedYet => 'â€” Not exited yet';

  @override
  String get parkingTime => 'Parking time';

  @override
  String get entryTime => 'Entry time';

  @override
  String get exitTime => 'Exit time';

  @override
  String get vehicleNotExited => 'â€” Vehicle has not exited';

  @override
  String get totalDuration => 'Total duration';

  @override
  String get pleaseRetrieveVehicle => 'Please retrieve your vehicle';

  @override
  String gracePeriodRemaining(int mins, String secs) {
    return 'You have ${mins}m ${secs}s remaining to exit';
  
  @override
  String get enumBookingActive => 'Active';
  @override
  String get enumBookingExpired => 'Expired';
  @override
  String get enumBookingPendingPayment => 'Pending Payment';
  @override
  String get enumBookingCanceled => 'Canceled';
  @override
  String get enumBookingPendingActivation => 'Pending Activation';
  @override
  String get enumBookingNeedsAttention => 'Needs Attention';
  @override
  String get enumBookingPartialPayment => 'Partial Payment';
  @override
  String get enumBookingDraft => 'Draft';
  @override
  String get enumBookingComplete => 'Complete';
  @override
  String get enumPaymentPending => 'Pending';
  @override
  String get enumPaymentSuccess => 'Success';
  @override
  String get enumPaymentFailed => 'Failed';
  @override
  String get enumPaymentRefunded => 'Refunded';
  @override
  String get enumPaymentCanceled => 'Canceled';
  @override
  String get enumPaymentMethodCash => 'Cash';
  @override
  String get enumPaymentMethodPayos => 'PayOS';
  @override
  String get enumPaymentMethodVnpay => 'VNPay';
  @override
  String get enumPaymentMethodBankTransfer => 'Bank Transfer';
  @override
  String get enumPaymentMethodCreditCard => 'Credit Card';
  @override
  String get enumPaymentMethodQr => 'VietQR';
  @override
  String get enumPaymentMethodOther => 'Other';
  @override
  String get enumSessionOngoing => 'Ongoing';
  @override
  String get enumSessionCompleted => 'Completed';
  @override
  String get enumVehicleActive => 'Active';
  @override
  String get enumVehicleExpiringSoon => 'Expiring Soon';
  @override
  String get enumVehicleExpired => 'Expired';
  @override
  String get enumComplaintPending => 'Pending';
  @override
  String get enumComplaintProcessing => 'Processing';
  @override
  String get enumComplaintResolved => 'Resolved';
  @override
  String get enumComplaintRejected => 'Rejected';
  @override
  String get enumNotifDebt => 'Debt Reminder';
  @override
  String get enumNotifSecurity => 'Security Alert';
  @override
  String get enumNotifSystem => 'System';
  @override
  String get enumNotifBroadcast => 'Broadcast';
  @override
  String get errPaymentNotRecorded => 'The system has not recorded the payment yet. Please wait a moment and try again.';
  @override
  String errInvalidStatus(String status) => 'Invoice has status: $status';
}

  @override
  String get timeIn => 'Entry';

  @override
  String get timeOut => 'Exit';

  @override
  String get noImageAvailable => 'No image';

  @override
  String get qrPayment => 'QR Payment';

  @override
  String get scanToPayTitle => 'Scan code to pay';

  @override
  String get scanToPaySubtitle =>
      'Use your banking app to scan the QR code below.';

  @override
  String get paymentInitError => 'Payment initialization error';

  @override
  String get transferInfo => 'Transfer Information';

  @override
  String get amount => 'Amount';

  @override
  String get transferContent => 'Description';

  @override
  String get checkingPaymentStatus => 'Checking payment status...';

  @override
  String get iHaveTransferred => 'I have transferred';

  @override
  String get paymentSuccess => 'Payment successful!';

  @override
  String get done => 'Done';

  @override
  String get afterTransferHint =>
      'After transferring, press the button below to confirm.';

  @override
  String get noQrAvailable => 'No QR available';

  @override
  String get login => 'Login';

  @override
  String get enterPhoneNumber => 'Enter phone number';

  @override
  String get phoneVerifyMessage =>
      'The system will verify your account and guide you to the next step.';

  @override
  String get phoneLabel2 => 'Phone Number';

  @override
  String get phoneExample => 'E.g., 0987654321';

  @override
  String get continueBtn => 'Continue';

  @override
  String get noAccountMessage =>
      'Don\'t have an account? Please contact parking manager.';

  @override
  String get phoneVerifyError => 'Phone verification error';

  @override
  String get stepPhone => 'Phone';

  @override
  String get stepOtp => 'OTP';

  @override
  String get stepPassword => 'Password';

  @override
  String get activateAccount => 'Activate Account';

  @override
  String get enterOtp => 'Enter OTP code';

  @override
  String otpSentTo(String phone) {
    return 'A verification code has been sent to $phone.';
  
  @override
  String get enumBookingActive => 'Active';
  @override
  String get enumBookingExpired => 'Expired';
  @override
  String get enumBookingPendingPayment => 'Pending Payment';
  @override
  String get enumBookingCanceled => 'Canceled';
  @override
  String get enumBookingPendingActivation => 'Pending Activation';
  @override
  String get enumBookingNeedsAttention => 'Needs Attention';
  @override
  String get enumBookingPartialPayment => 'Partial Payment';
  @override
  String get enumBookingDraft => 'Draft';
  @override
  String get enumBookingComplete => 'Complete';
  @override
  String get enumPaymentPending => 'Pending';
  @override
  String get enumPaymentSuccess => 'Success';
  @override
  String get enumPaymentFailed => 'Failed';
  @override
  String get enumPaymentRefunded => 'Refunded';
  @override
  String get enumPaymentCanceled => 'Canceled';
  @override
  String get enumPaymentMethodCash => 'Cash';
  @override
  String get enumPaymentMethodPayos => 'PayOS';
  @override
  String get enumPaymentMethodVnpay => 'VNPay';
  @override
  String get enumPaymentMethodBankTransfer => 'Bank Transfer';
  @override
  String get enumPaymentMethodCreditCard => 'Credit Card';
  @override
  String get enumPaymentMethodQr => 'VietQR';
  @override
  String get enumPaymentMethodOther => 'Other';
  @override
  String get enumSessionOngoing => 'Ongoing';
  @override
  String get enumSessionCompleted => 'Completed';
  @override
  String get enumVehicleActive => 'Active';
  @override
  String get enumVehicleExpiringSoon => 'Expiring Soon';
  @override
  String get enumVehicleExpired => 'Expired';
  @override
  String get enumComplaintPending => 'Pending';
  @override
  String get enumComplaintProcessing => 'Processing';
  @override
  String get enumComplaintResolved => 'Resolved';
  @override
  String get enumComplaintRejected => 'Rejected';
  @override
  String get enumNotifDebt => 'Debt Reminder';
  @override
  String get enumNotifSecurity => 'Security Alert';
  @override
  String get enumNotifSystem => 'System';
  @override
  String get enumNotifBroadcast => 'Broadcast';
  @override
  String get errPaymentNotRecorded => 'The system has not recorded the payment yet. Please wait a moment and try again.';
  @override
  String errInvalidStatus(String status) => 'Invoice has status: $status';
}

  @override
  String get otpCode => 'OTP Code';

  @override
  String get enter6Digits => 'Enter 6 digits';

  @override
  String get otpVerifyError => 'OTP verification error';

  @override
  String get otpResent => 'OTP resent successfully';

  @override
  String get resendOtp => 'Resend OTP';

  @override
  String resendOtpIn(int seconds) {
    return 'Resend OTP in ${seconds}s';
  
  @override
  String get enumBookingActive => 'Active';
  @override
  String get enumBookingExpired => 'Expired';
  @override
  String get enumBookingPendingPayment => 'Pending Payment';
  @override
  String get enumBookingCanceled => 'Canceled';
  @override
  String get enumBookingPendingActivation => 'Pending Activation';
  @override
  String get enumBookingNeedsAttention => 'Needs Attention';
  @override
  String get enumBookingPartialPayment => 'Partial Payment';
  @override
  String get enumBookingDraft => 'Draft';
  @override
  String get enumBookingComplete => 'Complete';
  @override
  String get enumPaymentPending => 'Pending';
  @override
  String get enumPaymentSuccess => 'Success';
  @override
  String get enumPaymentFailed => 'Failed';
  @override
  String get enumPaymentRefunded => 'Refunded';
  @override
  String get enumPaymentCanceled => 'Canceled';
  @override
  String get enumPaymentMethodCash => 'Cash';
  @override
  String get enumPaymentMethodPayos => 'PayOS';
  @override
  String get enumPaymentMethodVnpay => 'VNPay';
  @override
  String get enumPaymentMethodBankTransfer => 'Bank Transfer';
  @override
  String get enumPaymentMethodCreditCard => 'Credit Card';
  @override
  String get enumPaymentMethodQr => 'VietQR';
  @override
  String get enumPaymentMethodOther => 'Other';
  @override
  String get enumSessionOngoing => 'Ongoing';
  @override
  String get enumSessionCompleted => 'Completed';
  @override
  String get enumVehicleActive => 'Active';
  @override
  String get enumVehicleExpiringSoon => 'Expiring Soon';
  @override
  String get enumVehicleExpired => 'Expired';
  @override
  String get enumComplaintPending => 'Pending';
  @override
  String get enumComplaintProcessing => 'Processing';
  @override
  String get enumComplaintResolved => 'Resolved';
  @override
  String get enumComplaintRejected => 'Rejected';
  @override
  String get enumNotifDebt => 'Debt Reminder';
  @override
  String get enumNotifSecurity => 'Security Alert';
  @override
  String get enumNotifSystem => 'System';
  @override
  String get enumNotifBroadcast => 'Broadcast';
  @override
  String get errPaymentNotRecorded => 'The system has not recorded the payment yet. Please wait a moment and try again.';
  @override
  String errInvalidStatus(String status) => 'Invoice has status: $status';
}

  @override
  String get createPassword => 'Create Password';

  @override
  String get enterPassword => 'Enter Password';

  @override
  String createPasswordFor(String phone) {
    return 'Create password to activate account $phone.';
  
  @override
  String get enumBookingActive => 'Active';
  @override
  String get enumBookingExpired => 'Expired';
  @override
  String get enumBookingPendingPayment => 'Pending Payment';
  @override
  String get enumBookingCanceled => 'Canceled';
  @override
  String get enumBookingPendingActivation => 'Pending Activation';
  @override
  String get enumBookingNeedsAttention => 'Needs Attention';
  @override
  String get enumBookingPartialPayment => 'Partial Payment';
  @override
  String get enumBookingDraft => 'Draft';
  @override
  String get enumBookingComplete => 'Complete';
  @override
  String get enumPaymentPending => 'Pending';
  @override
  String get enumPaymentSuccess => 'Success';
  @override
  String get enumPaymentFailed => 'Failed';
  @override
  String get enumPaymentRefunded => 'Refunded';
  @override
  String get enumPaymentCanceled => 'Canceled';
  @override
  String get enumPaymentMethodCash => 'Cash';
  @override
  String get enumPaymentMethodPayos => 'PayOS';
  @override
  String get enumPaymentMethodVnpay => 'VNPay';
  @override
  String get enumPaymentMethodBankTransfer => 'Bank Transfer';
  @override
  String get enumPaymentMethodCreditCard => 'Credit Card';
  @override
  String get enumPaymentMethodQr => 'VietQR';
  @override
  String get enumPaymentMethodOther => 'Other';
  @override
  String get enumSessionOngoing => 'Ongoing';
  @override
  String get enumSessionCompleted => 'Completed';
  @override
  String get enumVehicleActive => 'Active';
  @override
  String get enumVehicleExpiringSoon => 'Expiring Soon';
  @override
  String get enumVehicleExpired => 'Expired';
  @override
  String get enumComplaintPending => 'Pending';
  @override
  String get enumComplaintProcessing => 'Processing';
  @override
  String get enumComplaintResolved => 'Resolved';
  @override
  String get enumComplaintRejected => 'Rejected';
  @override
  String get enumNotifDebt => 'Debt Reminder';
  @override
  String get enumNotifSecurity => 'Security Alert';
  @override
  String get enumNotifSystem => 'System';
  @override
  String get enumNotifBroadcast => 'Broadcast';
  @override
  String get errPaymentNotRecorded => 'The system has not recorded the payment yet. Please wait a moment and try again.';
  @override
  String errInvalidStatus(String status) => 'Invoice has status: $status';
}

  @override
  String loginWith(String phone) {
    return 'Login with number $phone.';
  
  @override
  String get enumBookingActive => 'Active';
  @override
  String get enumBookingExpired => 'Expired';
  @override
  String get enumBookingPendingPayment => 'Pending Payment';
  @override
  String get enumBookingCanceled => 'Canceled';
  @override
  String get enumBookingPendingActivation => 'Pending Activation';
  @override
  String get enumBookingNeedsAttention => 'Needs Attention';
  @override
  String get enumBookingPartialPayment => 'Partial Payment';
  @override
  String get enumBookingDraft => 'Draft';
  @override
  String get enumBookingComplete => 'Complete';
  @override
  String get enumPaymentPending => 'Pending';
  @override
  String get enumPaymentSuccess => 'Success';
  @override
  String get enumPaymentFailed => 'Failed';
  @override
  String get enumPaymentRefunded => 'Refunded';
  @override
  String get enumPaymentCanceled => 'Canceled';
  @override
  String get enumPaymentMethodCash => 'Cash';
  @override
  String get enumPaymentMethodPayos => 'PayOS';
  @override
  String get enumPaymentMethodVnpay => 'VNPay';
  @override
  String get enumPaymentMethodBankTransfer => 'Bank Transfer';
  @override
  String get enumPaymentMethodCreditCard => 'Credit Card';
  @override
  String get enumPaymentMethodQr => 'VietQR';
  @override
  String get enumPaymentMethodOther => 'Other';
  @override
  String get enumSessionOngoing => 'Ongoing';
  @override
  String get enumSessionCompleted => 'Completed';
  @override
  String get enumVehicleActive => 'Active';
  @override
  String get enumVehicleExpiringSoon => 'Expiring Soon';
  @override
  String get enumVehicleExpired => 'Expired';
  @override
  String get enumComplaintPending => 'Pending';
  @override
  String get enumComplaintProcessing => 'Processing';
  @override
  String get enumComplaintResolved => 'Resolved';
  @override
  String get enumComplaintRejected => 'Rejected';
  @override
  String get enumNotifDebt => 'Debt Reminder';
  @override
  String get enumNotifSecurity => 'Security Alert';
  @override
  String get enumNotifSystem => 'System';
  @override
  String get enumNotifBroadcast => 'Broadcast';
  @override
  String get errPaymentNotRecorded => 'The system has not recorded the payment yet. Please wait a moment and try again.';
  @override
  String errInvalidStatus(String status) => 'Invoice has status: $status';
}

  @override
  String get password => 'Password';

  @override
  String get createNewPassword => 'Create new password';

  @override
  String get loginFailed => 'Login failed';

  @override
  String get forgotPassword => 'Forgot password?';

  @override
  String get changePasswordTitle => 'Change Password';

  @override
  String get accountSecurity => 'Account Security';

  @override
  String get passwordMinLength =>
      'New password must be at least 6 characters long.';

  @override
  String get currentPassword => 'Current Password';

  @override
  String get enterCurrentPassword => 'Enter current password';

  @override
  String get pleaseEnterOldPassword => 'Please enter old password';

  @override
  String get newPassword => 'New Password';

  @override
  String get enterNewPassword => 'Enter new password';

  @override
  String get pleaseEnterNewPassword => 'Please enter new password';

  @override
  String get passwordTooShort => 'Password must be at least 6 characters';

  @override
  String get passwordMustDiffer =>
      'New password must differ from current password';

  @override
  String get confirmNewPassword => 'Confirm New Password';

  @override
  String get reenterNewPassword => 'Re-enter new password';

  @override
  String get pleaseConfirmPassword => 'Please confirm new password';

  @override
  String get passwordMismatch => 'Password confirmation does not match';

  @override
  String get processing => 'Processing...';

  @override
  String get confirmChangePassword => 'Confirm password change';

  @override
  String get changePasswordSuccess => 'Password changed successfully!';

  @override
  String get changePasswordFailed => 'Password change failed.';

  @override
  String get packageLabel => 'Package:';

  @override
  String get validityPeriod => 'Validity:';

  @override
  String daysRemainingRenewNow(int days) {
    return 'Only $days days left â€” Renew now!';
  
  @override
  String get enumBookingActive => 'Active';
  @override
  String get enumBookingExpired => 'Expired';
  @override
  String get enumBookingPendingPayment => 'Pending Payment';
  @override
  String get enumBookingCanceled => 'Canceled';
  @override
  String get enumBookingPendingActivation => 'Pending Activation';
  @override
  String get enumBookingNeedsAttention => 'Needs Attention';
  @override
  String get enumBookingPartialPayment => 'Partial Payment';
  @override
  String get enumBookingDraft => 'Draft';
  @override
  String get enumBookingComplete => 'Complete';
  @override
  String get enumPaymentPending => 'Pending';
  @override
  String get enumPaymentSuccess => 'Success';
  @override
  String get enumPaymentFailed => 'Failed';
  @override
  String get enumPaymentRefunded => 'Refunded';
  @override
  String get enumPaymentCanceled => 'Canceled';
  @override
  String get enumPaymentMethodCash => 'Cash';
  @override
  String get enumPaymentMethodPayos => 'PayOS';
  @override
  String get enumPaymentMethodVnpay => 'VNPay';
  @override
  String get enumPaymentMethodBankTransfer => 'Bank Transfer';
  @override
  String get enumPaymentMethodCreditCard => 'Credit Card';
  @override
  String get enumPaymentMethodQr => 'VietQR';
  @override
  String get enumPaymentMethodOther => 'Other';
  @override
  String get enumSessionOngoing => 'Ongoing';
  @override
  String get enumSessionCompleted => 'Completed';
  @override
  String get enumVehicleActive => 'Active';
  @override
  String get enumVehicleExpiringSoon => 'Expiring Soon';
  @override
  String get enumVehicleExpired => 'Expired';
  @override
  String get enumComplaintPending => 'Pending';
  @override
  String get enumComplaintProcessing => 'Processing';
  @override
  String get enumComplaintResolved => 'Resolved';
  @override
  String get enumComplaintRejected => 'Rejected';
  @override
  String get enumNotifDebt => 'Debt Reminder';
  @override
  String get enumNotifSecurity => 'Security Alert';
  @override
  String get enumNotifSystem => 'System';
  @override
  String get enumNotifBroadcast => 'Broadcast';
  @override
  String get errPaymentNotRecorded => 'The system has not recorded the payment yet. Please wait a moment and try again.';
  @override
  String errInvalidStatus(String status) => 'Invoice has status: $status';
}

  @override
  String contractWithId(String id) {
    return 'Contract $id';
  
  @override
  String get enumBookingActive => 'Active';
  @override
  String get enumBookingExpired => 'Expired';
  @override
  String get enumBookingPendingPayment => 'Pending Payment';
  @override
  String get enumBookingCanceled => 'Canceled';
  @override
  String get enumBookingPendingActivation => 'Pending Activation';
  @override
  String get enumBookingNeedsAttention => 'Needs Attention';
  @override
  String get enumBookingPartialPayment => 'Partial Payment';
  @override
  String get enumBookingDraft => 'Draft';
  @override
  String get enumBookingComplete => 'Complete';
  @override
  String get enumPaymentPending => 'Pending';
  @override
  String get enumPaymentSuccess => 'Success';
  @override
  String get enumPaymentFailed => 'Failed';
  @override
  String get enumPaymentRefunded => 'Refunded';
  @override
  String get enumPaymentCanceled => 'Canceled';
  @override
  String get enumPaymentMethodCash => 'Cash';
  @override
  String get enumPaymentMethodPayos => 'PayOS';
  @override
  String get enumPaymentMethodVnpay => 'VNPay';
  @override
  String get enumPaymentMethodBankTransfer => 'Bank Transfer';
  @override
  String get enumPaymentMethodCreditCard => 'Credit Card';
  @override
  String get enumPaymentMethodQr => 'VietQR';
  @override
  String get enumPaymentMethodOther => 'Other';
  @override
  String get enumSessionOngoing => 'Ongoing';
  @override
  String get enumSessionCompleted => 'Completed';
  @override
  String get enumVehicleActive => 'Active';
  @override
  String get enumVehicleExpiringSoon => 'Expiring Soon';
  @override
  String get enumVehicleExpired => 'Expired';
  @override
  String get enumComplaintPending => 'Pending';
  @override
  String get enumComplaintProcessing => 'Processing';
  @override
  String get enumComplaintResolved => 'Resolved';
  @override
  String get enumComplaintRejected => 'Rejected';
  @override
  String get enumNotifDebt => 'Debt Reminder';
  @override
  String get enumNotifSecurity => 'Security Alert';
  @override
  String get enumNotifSystem => 'System';
  @override
  String get enumNotifBroadcast => 'Broadcast';
  @override
  String get errPaymentNotRecorded => 'The system has not recorded the payment yet. Please wait a moment and try again.';
  @override
  String errInvalidStatus(String status) => 'Invoice has status: $status';
}

  @override
  String get overviewInfo => 'Overview';

  @override
  String get groupRepresentativeLabel => 'Group / representative';

  @override
  String get createdAtLabel => 'Created date';

  @override
  String get registeredVehiclesCountLabel => 'Registered vehicles';

  @override
  String vehiclesCountText(int count) {
    return '$count vehicles';
  
  @override
  String get enumBookingActive => 'Active';
  @override
  String get enumBookingExpired => 'Expired';
  @override
  String get enumBookingPendingPayment => 'Pending Payment';
  @override
  String get enumBookingCanceled => 'Canceled';
  @override
  String get enumBookingPendingActivation => 'Pending Activation';
  @override
  String get enumBookingNeedsAttention => 'Needs Attention';
  @override
  String get enumBookingPartialPayment => 'Partial Payment';
  @override
  String get enumBookingDraft => 'Draft';
  @override
  String get enumBookingComplete => 'Complete';
  @override
  String get enumPaymentPending => 'Pending';
  @override
  String get enumPaymentSuccess => 'Success';
  @override
  String get enumPaymentFailed => 'Failed';
  @override
  String get enumPaymentRefunded => 'Refunded';
  @override
  String get enumPaymentCanceled => 'Canceled';
  @override
  String get enumPaymentMethodCash => 'Cash';
  @override
  String get enumPaymentMethodPayos => 'PayOS';
  @override
  String get enumPaymentMethodVnpay => 'VNPay';
  @override
  String get enumPaymentMethodBankTransfer => 'Bank Transfer';
  @override
  String get enumPaymentMethodCreditCard => 'Credit Card';
  @override
  String get enumPaymentMethodQr => 'VietQR';
  @override
  String get enumPaymentMethodOther => 'Other';
  @override
  String get enumSessionOngoing => 'Ongoing';
  @override
  String get enumSessionCompleted => 'Completed';
  @override
  String get enumVehicleActive => 'Active';
  @override
  String get enumVehicleExpiringSoon => 'Expiring Soon';
  @override
  String get enumVehicleExpired => 'Expired';
  @override
  String get enumComplaintPending => 'Pending';
  @override
  String get enumComplaintProcessing => 'Processing';
  @override
  String get enumComplaintResolved => 'Resolved';
  @override
  String get enumComplaintRejected => 'Rejected';
  @override
  String get enumNotifDebt => 'Debt Reminder';
  @override
  String get enumNotifSecurity => 'Security Alert';
  @override
  String get enumNotifSystem => 'System';
  @override
  String get enumNotifBroadcast => 'Broadcast';
  @override
  String get errPaymentNotRecorded => 'The system has not recorded the payment yet. Please wait a moment and try again.';
  @override
  String errInvalidStatus(String status) => 'Invoice has status: $status';
}

  @override
  String pendingPaymentsBannerTitle(int count) {
    return 'You have $count packages pending payment';
  
  @override
  String get enumBookingActive => 'Active';
  @override
  String get enumBookingExpired => 'Expired';
  @override
  String get enumBookingPendingPayment => 'Pending Payment';
  @override
  String get enumBookingCanceled => 'Canceled';
  @override
  String get enumBookingPendingActivation => 'Pending Activation';
  @override
  String get enumBookingNeedsAttention => 'Needs Attention';
  @override
  String get enumBookingPartialPayment => 'Partial Payment';
  @override
  String get enumBookingDraft => 'Draft';
  @override
  String get enumBookingComplete => 'Complete';
  @override
  String get enumPaymentPending => 'Pending';
  @override
  String get enumPaymentSuccess => 'Success';
  @override
  String get enumPaymentFailed => 'Failed';
  @override
  String get enumPaymentRefunded => 'Refunded';
  @override
  String get enumPaymentCanceled => 'Canceled';
  @override
  String get enumPaymentMethodCash => 'Cash';
  @override
  String get enumPaymentMethodPayos => 'PayOS';
  @override
  String get enumPaymentMethodVnpay => 'VNPay';
  @override
  String get enumPaymentMethodBankTransfer => 'Bank Transfer';
  @override
  String get enumPaymentMethodCreditCard => 'Credit Card';
  @override
  String get enumPaymentMethodQr => 'VietQR';
  @override
  String get enumPaymentMethodOther => 'Other';
  @override
  String get enumSessionOngoing => 'Ongoing';
  @override
  String get enumSessionCompleted => 'Completed';
  @override
  String get enumVehicleActive => 'Active';
  @override
  String get enumVehicleExpiringSoon => 'Expiring Soon';
  @override
  String get enumVehicleExpired => 'Expired';
  @override
  String get enumComplaintPending => 'Pending';
  @override
  String get enumComplaintProcessing => 'Processing';
  @override
  String get enumComplaintResolved => 'Resolved';
  @override
  String get enumComplaintRejected => 'Rejected';
  @override
  String get enumNotifDebt => 'Debt Reminder';
  @override
  String get enumNotifSecurity => 'Security Alert';
  @override
  String get enumNotifSystem => 'System';
  @override
  String get enumNotifBroadcast => 'Broadcast';
  @override
  String get errPaymentNotRecorded => 'The system has not recorded the payment yet. Please wait a moment and try again.';
  @override
  String errInvalidStatus(String status) => 'Invoice has status: $status';
}

  @override
  String get pendingPaymentsBannerSubtitle =>
      'Please complete payment to activate package or Cancel transaction in History to restore draft.';

  @override
  String get goToHistoryTooltip => 'Go to History';

  @override
  String selectAllDraftsCount(int count) {
    return 'Select all drafts ($count)';
  
  @override
  String get enumBookingActive => 'Active';
  @override
  String get enumBookingExpired => 'Expired';
  @override
  String get enumBookingPendingPayment => 'Pending Payment';
  @override
  String get enumBookingCanceled => 'Canceled';
  @override
  String get enumBookingPendingActivation => 'Pending Activation';
  @override
  String get enumBookingNeedsAttention => 'Needs Attention';
  @override
  String get enumBookingPartialPayment => 'Partial Payment';
  @override
  String get enumBookingDraft => 'Draft';
  @override
  String get enumBookingComplete => 'Complete';
  @override
  String get enumPaymentPending => 'Pending';
  @override
  String get enumPaymentSuccess => 'Success';
  @override
  String get enumPaymentFailed => 'Failed';
  @override
  String get enumPaymentRefunded => 'Refunded';
  @override
  String get enumPaymentCanceled => 'Canceled';
  @override
  String get enumPaymentMethodCash => 'Cash';
  @override
  String get enumPaymentMethodPayos => 'PayOS';
  @override
  String get enumPaymentMethodVnpay => 'VNPay';
  @override
  String get enumPaymentMethodBankTransfer => 'Bank Transfer';
  @override
  String get enumPaymentMethodCreditCard => 'Credit Card';
  @override
  String get enumPaymentMethodQr => 'VietQR';
  @override
  String get enumPaymentMethodOther => 'Other';
  @override
  String get enumSessionOngoing => 'Ongoing';
  @override
  String get enumSessionCompleted => 'Completed';
  @override
  String get enumVehicleActive => 'Active';
  @override
  String get enumVehicleExpiringSoon => 'Expiring Soon';
  @override
  String get enumVehicleExpired => 'Expired';
  @override
  String get enumComplaintPending => 'Pending';
  @override
  String get enumComplaintProcessing => 'Processing';
  @override
  String get enumComplaintResolved => 'Resolved';
  @override
  String get enumComplaintRejected => 'Rejected';
  @override
  String get enumNotifDebt => 'Debt Reminder';
  @override
  String get enumNotifSecurity => 'Security Alert';
  @override
  String get enumNotifSystem => 'System';
  @override
  String get enumNotifBroadcast => 'Broadcast';
  @override
  String get errPaymentNotRecorded => 'The system has not recorded the payment yet. Please wait a moment and try again.';
  @override
  String errInvalidStatus(String status) => 'Invoice has status: $status';
}

  @override
  String draftsCountBadge(int count) {
    return '$count drafts';
  
  @override
  String get enumBookingActive => 'Active';
  @override
  String get enumBookingExpired => 'Expired';
  @override
  String get enumBookingPendingPayment => 'Pending Payment';
  @override
  String get enumBookingCanceled => 'Canceled';
  @override
  String get enumBookingPendingActivation => 'Pending Activation';
  @override
  String get enumBookingNeedsAttention => 'Needs Attention';
  @override
  String get enumBookingPartialPayment => 'Partial Payment';
  @override
  String get enumBookingDraft => 'Draft';
  @override
  String get enumBookingComplete => 'Complete';
  @override
  String get enumPaymentPending => 'Pending';
  @override
  String get enumPaymentSuccess => 'Success';
  @override
  String get enumPaymentFailed => 'Failed';
  @override
  String get enumPaymentRefunded => 'Refunded';
  @override
  String get enumPaymentCanceled => 'Canceled';
  @override
  String get enumPaymentMethodCash => 'Cash';
  @override
  String get enumPaymentMethodPayos => 'PayOS';
  @override
  String get enumPaymentMethodVnpay => 'VNPay';
  @override
  String get enumPaymentMethodBankTransfer => 'Bank Transfer';
  @override
  String get enumPaymentMethodCreditCard => 'Credit Card';
  @override
  String get enumPaymentMethodQr => 'VietQR';
  @override
  String get enumPaymentMethodOther => 'Other';
  @override
  String get enumSessionOngoing => 'Ongoing';
  @override
  String get enumSessionCompleted => 'Completed';
  @override
  String get enumVehicleActive => 'Active';
  @override
  String get enumVehicleExpiringSoon => 'Expiring Soon';
  @override
  String get enumVehicleExpired => 'Expired';
  @override
  String get enumComplaintPending => 'Pending';
  @override
  String get enumComplaintProcessing => 'Processing';
  @override
  String get enumComplaintResolved => 'Resolved';
  @override
  String get enumComplaintRejected => 'Rejected';
  @override
  String get enumNotifDebt => 'Debt Reminder';
  @override
  String get enumNotifSecurity => 'Security Alert';
  @override
  String get enumNotifSystem => 'System';
  @override
  String get enumNotifBroadcast => 'Broadcast';
  @override
  String get errPaymentNotRecorded => 'The system has not recorded the payment yet. Please wait a moment and try again.';
  @override
  String errInvalidStatus(String status) => 'Invoice has status: $status';
}

  @override
  String checkoutWithCount(int count) {
    return 'Checkout ($count vehicles)';
  
  @override
  String get enumBookingActive => 'Active';
  @override
  String get enumBookingExpired => 'Expired';
  @override
  String get enumBookingPendingPayment => 'Pending Payment';
  @override
  String get enumBookingCanceled => 'Canceled';
  @override
  String get enumBookingPendingActivation => 'Pending Activation';
  @override
  String get enumBookingNeedsAttention => 'Needs Attention';
  @override
  String get enumBookingPartialPayment => 'Partial Payment';
  @override
  String get enumBookingDraft => 'Draft';
  @override
  String get enumBookingComplete => 'Complete';
  @override
  String get enumPaymentPending => 'Pending';
  @override
  String get enumPaymentSuccess => 'Success';
  @override
  String get enumPaymentFailed => 'Failed';
  @override
  String get enumPaymentRefunded => 'Refunded';
  @override
  String get enumPaymentCanceled => 'Canceled';
  @override
  String get enumPaymentMethodCash => 'Cash';
  @override
  String get enumPaymentMethodPayos => 'PayOS';
  @override
  String get enumPaymentMethodVnpay => 'VNPay';
  @override
  String get enumPaymentMethodBankTransfer => 'Bank Transfer';
  @override
  String get enumPaymentMethodCreditCard => 'Credit Card';
  @override
  String get enumPaymentMethodQr => 'VietQR';
  @override
  String get enumPaymentMethodOther => 'Other';
  @override
  String get enumSessionOngoing => 'Ongoing';
  @override
  String get enumSessionCompleted => 'Completed';
  @override
  String get enumVehicleActive => 'Active';
  @override
  String get enumVehicleExpiringSoon => 'Expiring Soon';
  @override
  String get enumVehicleExpired => 'Expired';
  @override
  String get enumComplaintPending => 'Pending';
  @override
  String get enumComplaintProcessing => 'Processing';
  @override
  String get enumComplaintResolved => 'Resolved';
  @override
  String get enumComplaintRejected => 'Rejected';
  @override
  String get enumNotifDebt => 'Debt Reminder';
  @override
  String get enumNotifSecurity => 'Security Alert';
  @override
  String get enumNotifSystem => 'System';
  @override
  String get enumNotifBroadcast => 'Broadcast';
  @override
  String get errPaymentNotRecorded => 'The system has not recorded the payment yet. Please wait a moment and try again.';
  @override
  String errInvalidStatus(String status) => 'Invoice has status: $status';
}

  @override
  String get historyTitle => 'History';

  @override
  String get failedToLoadPaymentHistory => 'Failed to load payment history';

  @override
  String get retry => 'Retry';

  @override
  String get noBillsYet => 'No invoices yet';

  @override
  String get paymentHistorySubtitle => 'Payment invoices will appear here.';

  @override
  String get pendingPaymentHeader => 'Pending payment';

  @override
  String get processedHeader => 'Processed';

  @override
  String get continuePaymentButton => 'Continue payment';

  @override
  String get cancelInvoiceDialogTitle => 'Cancel invoice?';

  @override
  String cancelInvoiceDialogBody(String code) {
    return 'Invoice $code will be canceled. Related packages will be restored to their previous state.';
  
  @override
  String get enumBookingActive => 'Active';
  @override
  String get enumBookingExpired => 'Expired';
  @override
  String get enumBookingPendingPayment => 'Pending Payment';
  @override
  String get enumBookingCanceled => 'Canceled';
  @override
  String get enumBookingPendingActivation => 'Pending Activation';
  @override
  String get enumBookingNeedsAttention => 'Needs Attention';
  @override
  String get enumBookingPartialPayment => 'Partial Payment';
  @override
  String get enumBookingDraft => 'Draft';
  @override
  String get enumBookingComplete => 'Complete';
  @override
  String get enumPaymentPending => 'Pending';
  @override
  String get enumPaymentSuccess => 'Success';
  @override
  String get enumPaymentFailed => 'Failed';
  @override
  String get enumPaymentRefunded => 'Refunded';
  @override
  String get enumPaymentCanceled => 'Canceled';
  @override
  String get enumPaymentMethodCash => 'Cash';
  @override
  String get enumPaymentMethodPayos => 'PayOS';
  @override
  String get enumPaymentMethodVnpay => 'VNPay';
  @override
  String get enumPaymentMethodBankTransfer => 'Bank Transfer';
  @override
  String get enumPaymentMethodCreditCard => 'Credit Card';
  @override
  String get enumPaymentMethodQr => 'VietQR';
  @override
  String get enumPaymentMethodOther => 'Other';
  @override
  String get enumSessionOngoing => 'Ongoing';
  @override
  String get enumSessionCompleted => 'Completed';
  @override
  String get enumVehicleActive => 'Active';
  @override
  String get enumVehicleExpiringSoon => 'Expiring Soon';
  @override
  String get enumVehicleExpired => 'Expired';
  @override
  String get enumComplaintPending => 'Pending';
  @override
  String get enumComplaintProcessing => 'Processing';
  @override
  String get enumComplaintResolved => 'Resolved';
  @override
  String get enumComplaintRejected => 'Rejected';
  @override
  String get enumNotifDebt => 'Debt Reminder';
  @override
  String get enumNotifSecurity => 'Security Alert';
  @override
  String get enumNotifSystem => 'System';
  @override
  String get enumNotifBroadcast => 'Broadcast';
  @override
  String get errPaymentNotRecorded => 'The system has not recorded the payment yet. Please wait a moment and try again.';
  @override
  String errInvalidStatus(String status) => 'Invoice has status: $status';
}

  @override
  String get no => 'No';

  @override
  String get invoiceCancelSuccess => 'Invoice canceled successfully.';

  @override
  String get invoiceCancelFailed => 'Failed to cancel invoice.';

  @override
  String get parkingHistoryTitle => 'Parking History';

  @override
  String get filterButtonText => 'Filter';

  @override
  String get filterAppliedText => 'Filter applied';

  @override
  String get clearFilterButton => 'Clear filter';

  @override
  String get noParkingHistoryFound => 'No parking history found';

  @override
  String get changeFilterSubtitle => 'Try changing your filter settings.';

  @override
  String get searchFilterTitle => 'Search Filter';

  @override
  String get timeRangeLabel => 'Time range';

  @override
  String get fromDateLabel => 'From date';

  @override
  String get toDateLabel => 'To date';

  @override
  String get plateNumberLabel => 'Plate number';

  @override
  String get plateNumberExample => 'e.g., 30A-123.45';

  @override
  String get resetFilterButton => 'Reset';

  @override
  String get applyFilterButton => 'Apply';

  @override
  String get failedToLoadData => 'Failed to load data';

  @override
  String get paidStatus => 'Paid';

  @override
  String get unpaidStatus => 'Unpaid';

  @override
  String get enumBookingActive => 'Active';
  @override
  String get enumBookingExpired => 'Expired';
  @override
  String get enumBookingPendingPayment => 'Pending Payment';
  @override
  String get enumBookingCanceled => 'Canceled';
  @override
  String get enumBookingPendingActivation => 'Pending Activation';
  @override
  String get enumBookingNeedsAttention => 'Needs Attention';
  @override
  String get enumBookingPartialPayment => 'Partial Payment';
  @override
  String get enumBookingDraft => 'Draft';
  @override
  String get enumBookingComplete => 'Complete';
  @override
  String get enumPaymentPending => 'Pending';
  @override
  String get enumPaymentSuccess => 'Success';
  @override
  String get enumPaymentFailed => 'Failed';
  @override
  String get enumPaymentRefunded => 'Refunded';
  @override
  String get enumPaymentCanceled => 'Canceled';
  @override
  String get enumPaymentMethodCash => 'Cash';
  @override
  String get enumPaymentMethodPayos => 'PayOS';
  @override
  String get enumPaymentMethodVnpay => 'VNPay';
  @override
  String get enumPaymentMethodBankTransfer => 'Bank Transfer';
  @override
  String get enumPaymentMethodCreditCard => 'Credit Card';
  @override
  String get enumPaymentMethodQr => 'VietQR';
  @override
  String get enumPaymentMethodOther => 'Other';
  @override
  String get enumSessionOngoing => 'Ongoing';
  @override
  String get enumSessionCompleted => 'Completed';
  @override
  String get enumVehicleActive => 'Active';
  @override
  String get enumVehicleExpiringSoon => 'Expiring Soon';
  @override
  String get enumVehicleExpired => 'Expired';
  @override
  String get enumComplaintPending => 'Pending';
  @override
  String get enumComplaintProcessing => 'Processing';
  @override
  String get enumComplaintResolved => 'Resolved';
  @override
  String get enumComplaintRejected => 'Rejected';
  @override
  String get enumNotifDebt => 'Debt Reminder';
  @override
  String get enumNotifSecurity => 'Security Alert';
  @override
  String get enumNotifSystem => 'System';
  @override
  String get enumNotifBroadcast => 'Broadcast';
  @override
  String get errPaymentNotRecorded => 'The system has not recorded the payment yet. Please wait a moment and try again.';
  @override
  String errInvalidStatus(String status) => 'Invoice has status: $status';
}
