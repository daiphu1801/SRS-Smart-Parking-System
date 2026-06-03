import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:intl/intl.dart' as intl;

import 'app_localizations_en.dart';
import 'app_localizations_vi.dart';

// ignore_for_file: type=lint

/// Callers can lookup localized strings with an instance of AppLocalizations
/// returned by `AppLocalizations.of(context)`.
///
/// Applications need to include `AppLocalizations.delegate()` in their app's
/// `localizationDelegates` list, and the locales they support in the app's
/// `supportedLocales` list. For example:
///
/// ```dart
/// import 'l10n/app_localizations.dart';
///
/// return MaterialApp(
///   localizationsDelegates: AppLocalizations.localizationsDelegates,
///   supportedLocales: AppLocalizations.supportedLocales,
///   home: MyApplicationHome(),
/// );
/// ```
///
/// ## Update pubspec.yaml
///
/// Please make sure to update your pubspec.yaml to include the following
/// packages:
///
/// ```yaml
/// dependencies:
///   # Internationalization support.
///   flutter_localizations:
///     sdk: flutter
///   intl: any # Use the pinned version from flutter_localizations
///
///   # Rest of dependencies
/// ```
///
/// ## iOS Applications
///
/// iOS applications define key application metadata, including supported
/// locales, in an Info.plist file that is built into the application bundle.
/// To configure the locales supported by your app, you’ll need to edit this
/// file.
///
/// First, open your project’s ios/Runner.xcworkspace Xcode workspace file.
/// Then, in the Project Navigator, open the Info.plist file under the Runner
/// project’s Runner folder.
///
/// Next, select the Information Property List item, select Add Item from the
/// Editor menu, then select Localizations from the pop-up menu.
///
/// Select and expand the newly-created Localizations item then, for each
/// locale your application supports, add a new item and select the locale
/// you wish to add from the pop-up menu in the Value field. This list should
/// be consistent with the languages listed in the AppLocalizations.supportedLocales
/// property.
abstract class AppLocalizations {
  AppLocalizations(String locale)
    : localeName = intl.Intl.canonicalizedLocale(locale.toString());

  final String localeName;

  static AppLocalizations? of(BuildContext context) {
    return Localizations.of<AppLocalizations>(context, AppLocalizations);
  }

  static const LocalizationsDelegate<AppLocalizations> delegate =
      _AppLocalizationsDelegate();

  /// A list of this localizations delegate along with the default localizations
  /// delegates.
  ///
  /// Returns a list of localizations delegates containing this delegate along with
  /// GlobalMaterialLocalizations.delegate, GlobalCupertinoLocalizations.delegate,
  /// and GlobalWidgetsLocalizations.delegate.
  ///
  /// Additional delegates can be added by appending to this list in
  /// MaterialApp. This list does not have to be used at all if a custom list
  /// of delegates is preferred or required.
  static const List<LocalizationsDelegate<dynamic>> localizationsDelegates =
      <LocalizationsDelegate<dynamic>>[
        delegate,
        GlobalMaterialLocalizations.delegate,
        GlobalCupertinoLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
      ];

  /// A list of this localizations delegate's supported locales.
  static const List<Locale> supportedLocales = <Locale>[
    Locale('en'),
    Locale('vi'),
  ];

  /// No description provided for @appTitle.
  ///
  /// In en, this message translates to:
  /// **'Smart Parking'**
  String get appTitle;

  /// No description provided for @monthlyContracts.
  ///
  /// In en, this message translates to:
  /// **'Monthly Contracts'**
  String get monthlyContracts;

  /// No description provided for @dataLoadError.
  ///
  /// In en, this message translates to:
  /// **'Data loading error'**
  String get dataLoadError;

  /// No description provided for @noContractsYet.
  ///
  /// In en, this message translates to:
  /// **'No contracts yet'**
  String get noContractsYet;

  /// No description provided for @monthlyPackageSavesMoney.
  ///
  /// In en, this message translates to:
  /// **'Monthly parking packages help you save significant parking costs.'**
  String get monthlyPackageSavesMoney;

  /// No description provided for @vehicleList.
  ///
  /// In en, this message translates to:
  /// **'Vehicle List'**
  String get vehicleList;

  /// No description provided for @addVehicle.
  ///
  /// In en, this message translates to:
  /// **'Add Vehicle'**
  String get addVehicle;

  /// No description provided for @noVehiclesYet.
  ///
  /// In en, this message translates to:
  /// **'No vehicles registered'**
  String get noVehiclesYet;

  /// No description provided for @clickAddVehicleToStart.
  ///
  /// In en, this message translates to:
  /// **'Click \"Add Vehicle\" to start registering a monthly package.'**
  String get clickAddVehicleToStart;

  /// No description provided for @monthlyPackageDetails.
  ///
  /// In en, this message translates to:
  /// **'Monthly Package Details'**
  String get monthlyPackageDetails;

  /// No description provided for @contract.
  ///
  /// In en, this message translates to:
  /// **'Contract'**
  String get contract;

  /// No description provided for @vehicleManagement.
  ///
  /// In en, this message translates to:
  /// **'Vehicle Management'**
  String get vehicleManagement;

  /// No description provided for @groupRepresentative.
  ///
  /// In en, this message translates to:
  /// **'Group / Rep:'**
  String get groupRepresentative;

  /// No description provided for @createdAt.
  ///
  /// In en, this message translates to:
  /// **'Created at:'**
  String get createdAt;

  /// No description provided for @registeredVehiclesCount.
  ///
  /// In en, this message translates to:
  /// **'Registered vehicles:'**
  String get registeredVehiclesCount;

  /// No description provided for @vehicles.
  ///
  /// In en, this message translates to:
  /// **'vehicles'**
  String get vehicles;

  /// No description provided for @paymentMethod.
  ///
  /// In en, this message translates to:
  /// **'Payment method:'**
  String get paymentMethod;

  /// No description provided for @addVehicleToBooking.
  ///
  /// In en, this message translates to:
  /// **'Add Vehicle to Booking'**
  String get addVehicleToBooking;

  /// No description provided for @bookingDetailList.
  ///
  /// In en, this message translates to:
  /// **'Booking Detail List'**
  String get bookingDetailList;

  /// No description provided for @childAccounts.
  ///
  /// In en, this message translates to:
  /// **'Child Accounts'**
  String get childAccounts;

  /// No description provided for @addChildAccount.
  ///
  /// In en, this message translates to:
  /// **'Add Child Account'**
  String get addChildAccount;

  /// No description provided for @fullName.
  ///
  /// In en, this message translates to:
  /// **'Full Name'**
  String get fullName;

  /// No description provided for @address.
  ///
  /// In en, this message translates to:
  /// **'Address'**
  String get address;

  /// No description provided for @phoneNumber.
  ///
  /// In en, this message translates to:
  /// **'Phone Number'**
  String get phoneNumber;

  /// No description provided for @cancel.
  ///
  /// In en, this message translates to:
  /// **'Cancel'**
  String get cancel;

  /// No description provided for @add.
  ///
  /// In en, this message translates to:
  /// **'Add'**
  String get add;

  /// No description provided for @nameAndPhoneRequired.
  ///
  /// In en, this message translates to:
  /// **'Name and phone number are required'**
  String get nameAndPhoneRequired;

  /// No description provided for @childAccountAdded.
  ///
  /// In en, this message translates to:
  /// **'Child account added successfully'**
  String get childAccountAdded;

  /// No description provided for @errorLoadingChildAccounts.
  ///
  /// In en, this message translates to:
  /// **'Error loading child accounts'**
  String get errorLoadingChildAccounts;

  /// No description provided for @noChildAccountsYet.
  ///
  /// In en, this message translates to:
  /// **'No child accounts yet'**
  String get noChildAccountsYet;

  /// No description provided for @canAddChildAccountsForVehicles.
  ///
  /// In en, this message translates to:
  /// **'You can add child accounts to manage their own vehicles.'**
  String get canAddChildAccountsForVehicles;

  /// No description provided for @pleaseEnterLicensePlate.
  ///
  /// In en, this message translates to:
  /// **'Please enter license plate'**
  String get pleaseEnterLicensePlate;

  /// No description provided for @addedToCartSuccessfully.
  ///
  /// In en, this message translates to:
  /// **'Added to cart successfully!'**
  String get addedToCartSuccessfully;

  /// No description provided for @errorAddingVehicle.
  ///
  /// In en, this message translates to:
  /// **'An error occurred while adding vehicle'**
  String get errorAddingVehicle;

  /// No description provided for @addNewVehicleRegistration.
  ///
  /// In en, this message translates to:
  /// **'New Vehicle Registration'**
  String get addNewVehicleRegistration;

  /// No description provided for @vehicleInformation.
  ///
  /// In en, this message translates to:
  /// **'Vehicle Information'**
  String get vehicleInformation;

  /// No description provided for @licensePlate.
  ///
  /// In en, this message translates to:
  /// **'License Plate'**
  String get licensePlate;

  /// No description provided for @licensePlateExample.
  ///
  /// In en, this message translates to:
  /// **'E.g., 30A-123.45'**
  String get licensePlateExample;

  /// No description provided for @vehicleType.
  ///
  /// In en, this message translates to:
  /// **'Vehicle Type'**
  String get vehicleType;

  /// No description provided for @selectPackage.
  ///
  /// In en, this message translates to:
  /// **'Select Package'**
  String get selectPackage;

  /// No description provided for @pleaseSelectVehicleTypeFirst.
  ///
  /// In en, this message translates to:
  /// **'Please select vehicle type first'**
  String get pleaseSelectVehicleTypeFirst;

  /// No description provided for @noSuitablePackageForThisVehicleType.
  ///
  /// In en, this message translates to:
  /// **'No suitable package for this vehicle type'**
  String get noSuitablePackageForThisVehicleType;

  /// No description provided for @addToCart.
  ///
  /// In en, this message translates to:
  /// **'Add to Cart'**
  String get addToCart;

  /// No description provided for @months.
  ///
  /// In en, this message translates to:
  /// **'months'**
  String get months;

  /// No description provided for @duration.
  ///
  /// In en, this message translates to:
  /// **'Duration'**
  String get duration;

  /// No description provided for @validFrom.
  ///
  /// In en, this message translates to:
  /// **'Valid from'**
  String get validFrom;

  /// No description provided for @validUntil.
  ///
  /// In en, this message translates to:
  /// **'Until'**
  String get validUntil;

  /// No description provided for @total.
  ///
  /// In en, this message translates to:
  /// **'Total'**
  String get total;

  /// No description provided for @bookingDetailDetails.
  ///
  /// In en, this message translates to:
  /// **'Booking Detail Details'**
  String get bookingDetailDetails;

  /// No description provided for @id.
  ///
  /// In en, this message translates to:
  /// **'ID:'**
  String get id;

  /// No description provided for @bookingDetailId.
  ///
  /// In en, this message translates to:
  /// **'Booking Detail ID:'**
  String get bookingDetailId;

  /// No description provided for @bookingId.
  ///
  /// In en, this message translates to:
  /// **'Booking ID:'**
  String get bookingId;

  /// No description provided for @customerId.
  ///
  /// In en, this message translates to:
  /// **'Customer ID:'**
  String get customerId;

  /// No description provided for @packagePriceId.
  ///
  /// In en, this message translates to:
  /// **'Package Price ID:'**
  String get packagePriceId;

  /// No description provided for @licensePlateLabel.
  ///
  /// In en, this message translates to:
  /// **'License plate:'**
  String get licensePlateLabel;

  /// No description provided for @vehicleTypeLabel.
  ///
  /// In en, this message translates to:
  /// **'Vehicle type:'**
  String get vehicleTypeLabel;

  /// No description provided for @packageTypeLabel.
  ///
  /// In en, this message translates to:
  /// **'Package:'**
  String get packageTypeLabel;

  /// No description provided for @durationLabel.
  ///
  /// In en, this message translates to:
  /// **'Duration:'**
  String get durationLabel;

  /// No description provided for @statusLabel.
  ///
  /// In en, this message translates to:
  /// **'Status:'**
  String get statusLabel;

  /// No description provided for @statusActive.
  ///
  /// In en, this message translates to:
  /// **'Active'**
  String get statusActive;

  /// No description provided for @statusExpiringSoon.
  ///
  /// In en, this message translates to:
  /// **'Expiring soon'**
  String get statusExpiringSoon;

  /// No description provided for @statusExpired.
  ///
  /// In en, this message translates to:
  /// **'Expired'**
  String get statusExpired;

  /// No description provided for @priceLabel.
  ///
  /// In en, this message translates to:
  /// **'Price:'**
  String get priceLabel;

  /// No description provided for @startDateLabel.
  ///
  /// In en, this message translates to:
  /// **'Start date:'**
  String get startDateLabel;

  /// No description provided for @endDateLabel.
  ///
  /// In en, this message translates to:
  /// **'End date:'**
  String get endDateLabel;

  /// No description provided for @pay.
  ///
  /// In en, this message translates to:
  /// **'Pay'**
  String get pay;

  /// No description provided for @pendingPayment.
  ///
  /// In en, this message translates to:
  /// **'Pending payment'**
  String get pendingPayment;

  /// No description provided for @renewContract.
  ///
  /// In en, this message translates to:
  /// **'Renew Contract'**
  String get renewContract;

  /// No description provided for @renewSuccess.
  ///
  /// In en, this message translates to:
  /// **'Renewal successful!'**
  String get renewSuccess;

  /// No description provided for @renewFailedServerUpdate.
  ///
  /// In en, this message translates to:
  /// **'Renewal failed at server update.'**
  String get renewFailedServerUpdate;

  /// No description provided for @renewBookingDetail.
  ///
  /// In en, this message translates to:
  /// **'Renew Booking Detail'**
  String get renewBookingDetail;

  /// No description provided for @packageType.
  ///
  /// In en, this message translates to:
  /// **'Package Type'**
  String get packageType;

  /// No description provided for @renew.
  ///
  /// In en, this message translates to:
  /// **'Renew'**
  String get renew;

  /// No description provided for @selectRenewalMonths.
  ///
  /// In en, this message translates to:
  /// **'Select renewal months:'**
  String get selectRenewalMonths;

  /// No description provided for @totalAmount.
  ///
  /// In en, this message translates to:
  /// **'Total amount:'**
  String get totalAmount;

  /// No description provided for @payAndRenew.
  ///
  /// In en, this message translates to:
  /// **'Pay & Renew'**
  String get payAndRenew;

  /// No description provided for @welcomeMorning.
  ///
  /// In en, this message translates to:
  /// **'Good morning'**
  String get welcomeMorning;

  /// No description provided for @welcomeAfternoon.
  ///
  /// In en, this message translates to:
  /// **'Good afternoon'**
  String get welcomeAfternoon;

  /// No description provided for @welcomeEvening.
  ///
  /// In en, this message translates to:
  /// **'Good evening'**
  String get welcomeEvening;

  /// No description provided for @accountVerified.
  ///
  /// In en, this message translates to:
  /// **'Verified account'**
  String get accountVerified;

  /// No description provided for @myVehicles.
  ///
  /// In en, this message translates to:
  /// **'Your vehicles'**
  String get myVehicles;

  /// No description provided for @currentParkingSession.
  ///
  /// In en, this message translates to:
  /// **'Current parking session'**
  String get currentParkingSession;

  /// No description provided for @quickActions.
  ///
  /// In en, this message translates to:
  /// **'Quick actions'**
  String get quickActions;

  /// No description provided for @tryAgain.
  ///
  /// In en, this message translates to:
  /// **'Try again'**
  String get tryAgain;

  /// No description provided for @complaintStatusPending.
  ///
  /// In en, this message translates to:
  /// **'Pending'**
  String get complaintStatusPending;

  /// No description provided for @complaintStatusProcessing.
  ///
  /// In en, this message translates to:
  /// **'Processing'**
  String get complaintStatusProcessing;

  /// No description provided for @complaintStatusResolved.
  ///
  /// In en, this message translates to:
  /// **'Resolved'**
  String get complaintStatusResolved;

  /// No description provided for @complaintStatusRejected.
  ///
  /// In en, this message translates to:
  /// **'Rejected'**
  String get complaintStatusRejected;

  /// No description provided for @complaintCodeLabel.
  ///
  /// In en, this message translates to:
  /// **'ID: {id}'**
  String complaintCodeLabel(String id);

  /// No description provided for @expiringSoonAlert.
  ///
  /// In en, this message translates to:
  /// **'Package expiring soon'**
  String get expiringSoonAlert;

  /// No description provided for @expiringSoonMessage.
  ///
  /// In en, this message translates to:
  /// **'You have {count} vehicles expiring soon. Please renew to avoid interruption.'**
  String expiringSoonMessage(int count);

  /// No description provided for @noVehiclesHome.
  ///
  /// In en, this message translates to:
  /// **'No vehicles registered'**
  String get noVehiclesHome;

  /// No description provided for @noVehiclesRegistered.
  ///
  /// In en, this message translates to:
  /// **'You have not registered any vehicles yet.'**
  String get noVehiclesRegistered;

  /// No description provided for @defaultPackage.
  ///
  /// In en, this message translates to:
  /// **'Default package'**
  String get defaultPackage;

  /// No description provided for @expired.
  ///
  /// In en, this message translates to:
  /// **'Expired'**
  String get expired;

  /// No description provided for @daysLeft.
  ///
  /// In en, this message translates to:
  /// **'{days} days left'**
  String daysLeft(int days);

  /// No description provided for @noParkingSession.
  ///
  /// In en, this message translates to:
  /// **'No active session'**
  String get noParkingSession;

  /// No description provided for @notInAnyParkingLot.
  ///
  /// In en, this message translates to:
  /// **'You are not currently in any parking lot.'**
  String get notInAnyParkingLot;

  /// No description provided for @entryTimeLabel.
  ///
  /// In en, this message translates to:
  /// **'Entry: {time}'**
  String entryTimeLabel(String time);

  /// No description provided for @parkingDurationLabel.
  ///
  /// In en, this message translates to:
  /// **'Duration: {duration}'**
  String parkingDurationLabel(String duration);

  /// No description provided for @parkingSessionAction.
  ///
  /// In en, this message translates to:
  /// **'Parking Session'**
  String get parkingSessionAction;

  /// No description provided for @billingAction.
  ///
  /// In en, this message translates to:
  /// **'Billing'**
  String get billingAction;

  /// No description provided for @navHome.
  ///
  /// In en, this message translates to:
  /// **'Home'**
  String get navHome;

  /// No description provided for @navParking.
  ///
  /// In en, this message translates to:
  /// **'Parking'**
  String get navParking;

  /// No description provided for @navPackages.
  ///
  /// In en, this message translates to:
  /// **'Packages'**
  String get navPackages;

  /// No description provided for @navHistory.
  ///
  /// In en, this message translates to:
  /// **'History'**
  String get navHistory;

  /// No description provided for @navAccount.
  ///
  /// In en, this message translates to:
  /// **'Account'**
  String get navAccount;

  /// No description provided for @accountTitle.
  ///
  /// In en, this message translates to:
  /// **'Account'**
  String get accountTitle;

  /// No description provided for @errorLoadingProfile.
  ///
  /// In en, this message translates to:
  /// **'Error loading profile'**
  String get errorLoadingProfile;

  /// No description provided for @settingsAndSupport.
  ///
  /// In en, this message translates to:
  /// **'Settings & Support'**
  String get settingsAndSupport;

  /// No description provided for @profileDetails.
  ///
  /// In en, this message translates to:
  /// **'Profile Details'**
  String get profileDetails;

  /// No description provided for @profileDetailsSubtitle.
  ///
  /// In en, this message translates to:
  /// **'View full info, ID, address'**
  String get profileDetailsSubtitle;

  /// No description provided for @childAccountsManage.
  ///
  /// In en, this message translates to:
  /// **'Manage child accounts & vehicles'**
  String get childAccountsManage;

  /// No description provided for @changePassword.
  ///
  /// In en, this message translates to:
  /// **'Change Password'**
  String get changePassword;

  /// No description provided for @changePasswordSubtitle.
  ///
  /// In en, this message translates to:
  /// **'Update account security password'**
  String get changePasswordSubtitle;

  /// No description provided for @supportAndComplaints.
  ///
  /// In en, this message translates to:
  /// **'Support & Complaints'**
  String get supportAndComplaints;

  /// No description provided for @supportAndComplaintsSubtitle.
  ///
  /// In en, this message translates to:
  /// **'Submit your feedback'**
  String get supportAndComplaintsSubtitle;

  /// No description provided for @notifications.
  ///
  /// In en, this message translates to:
  /// **'Notifications'**
  String get notifications;

  /// No description provided for @notificationsSubtitle.
  ///
  /// In en, this message translates to:
  /// **'Manage notifications'**
  String get notificationsSubtitle;

  /// No description provided for @logout.
  ///
  /// In en, this message translates to:
  /// **'Log Out'**
  String get logout;

  /// No description provided for @editProfileTooltip.
  ///
  /// In en, this message translates to:
  /// **'Edit Profile'**
  String get editProfileTooltip;

  /// No description provided for @phoneLabel.
  ///
  /// In en, this message translates to:
  /// **'Phone'**
  String get phoneLabel;

  /// No description provided for @groupLabel.
  ///
  /// In en, this message translates to:
  /// **'Group'**
  String get groupLabel;

  /// No description provided for @history.
  ///
  /// In en, this message translates to:
  /// **'History'**
  String get history;

  /// No description provided for @complaintsFeedback.
  ///
  /// In en, this message translates to:
  /// **'Complaints / Feedback'**
  String get complaintsFeedback;

  /// No description provided for @filter.
  ///
  /// In en, this message translates to:
  /// **'Filter'**
  String get filter;

  /// No description provided for @noComplaintsYet.
  ///
  /// In en, this message translates to:
  /// **'No complaints yet'**
  String get noComplaintsYet;

  /// No description provided for @complaintsFeedbackEncourage.
  ///
  /// In en, this message translates to:
  /// **'Your feedback helps us improve our service.'**
  String get complaintsFeedbackEncourage;

  /// No description provided for @createComplaint.
  ///
  /// In en, this message translates to:
  /// **'Create Complaint'**
  String get createComplaint;

  /// No description provided for @submitNewComplaint.
  ///
  /// In en, this message translates to:
  /// **'Submit New Complaint'**
  String get submitNewComplaint;

  /// No description provided for @complaintsListenMessage.
  ///
  /// In en, this message translates to:
  /// **'We always listen to your feedback to improve our service.'**
  String get complaintsListenMessage;

  /// No description provided for @complaintTitle.
  ///
  /// In en, this message translates to:
  /// **'Title'**
  String get complaintTitle;

  /// No description provided for @complaintTitlePlaceholder.
  ///
  /// In en, this message translates to:
  /// **'E.g., Plate recognition error at gate A'**
  String get complaintTitlePlaceholder;

  /// No description provided for @pleaseEnterTitle.
  ///
  /// In en, this message translates to:
  /// **'Please enter title'**
  String get pleaseEnterTitle;

  /// No description provided for @complaintContent.
  ///
  /// In en, this message translates to:
  /// **'Description'**
  String get complaintContent;

  /// No description provided for @complaintContentPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'Describe your issue in detail...'**
  String get complaintContentPlaceholder;

  /// No description provided for @pleaseEnterContent.
  ///
  /// In en, this message translates to:
  /// **'Please enter content'**
  String get pleaseEnterContent;

  /// No description provided for @attachedImages.
  ///
  /// In en, this message translates to:
  /// **'Attached images (if any)'**
  String get attachedImages;

  /// No description provided for @tapToUploadImage.
  ///
  /// In en, this message translates to:
  /// **'Tap to upload image'**
  String get tapToUploadImage;

  /// No description provided for @imagePickerInDev.
  ///
  /// In en, this message translates to:
  /// **'Image picker is currently under development'**
  String get imagePickerInDev;

  /// No description provided for @submitComplaint.
  ///
  /// In en, this message translates to:
  /// **'Submit Complaint'**
  String get submitComplaint;

  /// No description provided for @complaintSubmitSuccess.
  ///
  /// In en, this message translates to:
  /// **'Complaint submitted successfully! We will process it soon.'**
  String get complaintSubmitSuccess;

  /// No description provided for @complaintSubmitError.
  ///
  /// In en, this message translates to:
  /// **'An error occurred, please try again'**
  String get complaintSubmitError;

  /// No description provided for @readAll.
  ///
  /// In en, this message translates to:
  /// **'Mark all read'**
  String get readAll;

  /// No description provided for @cannotLoadNotifications.
  ///
  /// In en, this message translates to:
  /// **'Cannot load notifications'**
  String get cannotLoadNotifications;

  /// No description provided for @noNotificationsYet.
  ///
  /// In en, this message translates to:
  /// **'No notifications yet'**
  String get noNotificationsYet;

  /// No description provided for @systemNotificationsAppearHere.
  ///
  /// In en, this message translates to:
  /// **'System notifications will appear here.'**
  String get systemNotificationsAppearHere;

  /// No description provided for @parkingSessions.
  ///
  /// In en, this message translates to:
  /// **'Parking Sessions'**
  String get parkingSessions;

  /// No description provided for @filterAll.
  ///
  /// In en, this message translates to:
  /// **'All'**
  String get filterAll;

  /// No description provided for @filterOngoing.
  ///
  /// In en, this message translates to:
  /// **'Ongoing'**
  String get filterOngoing;

  /// No description provided for @filterCompleted.
  ///
  /// In en, this message translates to:
  /// **'Completed'**
  String get filterCompleted;

  /// No description provided for @noParkingSessions.
  ///
  /// In en, this message translates to:
  /// **'No parking sessions yet'**
  String get noParkingSessions;

  /// No description provided for @parkingHistoryAppearHere.
  ///
  /// In en, this message translates to:
  /// **'Your parking history will appear here.'**
  String get parkingHistoryAppearHere;

  /// No description provided for @noFilterResults.
  ///
  /// In en, this message translates to:
  /// **'No results'**
  String get noFilterResults;

  /// No description provided for @noMatchingParkingSession.
  ///
  /// In en, this message translates to:
  /// **'No parking sessions match the filter.'**
  String get noMatchingParkingSession;

  /// No description provided for @manualGateOpen.
  ///
  /// In en, this message translates to:
  /// **'Manual gate opening'**
  String get manualGateOpen;

  /// No description provided for @entryLabel.
  ///
  /// In en, this message translates to:
  /// **'Entry:'**
  String get entryLabel;

  /// No description provided for @exitLabel.
  ///
  /// In en, this message translates to:
  /// **'Exit:'**
  String get exitLabel;

  /// No description provided for @sessionDurationLabel.
  ///
  /// In en, this message translates to:
  /// **'Duration:'**
  String get sessionDurationLabel;

  /// No description provided for @vehicleInLot.
  ///
  /// In en, this message translates to:
  /// **'— Vehicle is in lot'**
  String get vehicleInLot;

  /// No description provided for @gracePeriodPrompt.
  ///
  /// In en, this message translates to:
  /// **'Please pick up vehicle in: {mins}m {secs}s'**
  String gracePeriodPrompt(int mins, String secs);

  /// No description provided for @parkingSessionDetail.
  ///
  /// In en, this message translates to:
  /// **'Parking Session Detail'**
  String get parkingSessionDetail;

  /// No description provided for @lprCameraImage.
  ///
  /// In en, this message translates to:
  /// **'LPR Camera Plate Image'**
  String get lprCameraImage;

  /// No description provided for @car.
  ///
  /// In en, this message translates to:
  /// **'Car'**
  String get car;

  /// No description provided for @motorbike.
  ///
  /// In en, this message translates to:
  /// **'Motorbike'**
  String get motorbike;

  /// No description provided for @packageCode.
  ///
  /// In en, this message translates to:
  /// **'Package ID'**
  String get packageCode;

  /// No description provided for @attention.
  ///
  /// In en, this message translates to:
  /// **'Note'**
  String get attention;

  /// No description provided for @gatesInOut.
  ///
  /// In en, this message translates to:
  /// **'Gates entry / exit'**
  String get gatesInOut;

  /// No description provided for @gateIn.
  ///
  /// In en, this message translates to:
  /// **'Entry gate'**
  String get gateIn;

  /// No description provided for @gateOut.
  ///
  /// In en, this message translates to:
  /// **'Exit gate'**
  String get gateOut;

  /// No description provided for @notExitedYet.
  ///
  /// In en, this message translates to:
  /// **'— Not exited yet'**
  String get notExitedYet;

  /// No description provided for @parkingTime.
  ///
  /// In en, this message translates to:
  /// **'Parking time'**
  String get parkingTime;

  /// No description provided for @entryTime.
  ///
  /// In en, this message translates to:
  /// **'Entry time'**
  String get entryTime;

  /// No description provided for @exitTime.
  ///
  /// In en, this message translates to:
  /// **'Exit time'**
  String get exitTime;

  /// No description provided for @vehicleNotExited.
  ///
  /// In en, this message translates to:
  /// **'— Vehicle has not exited'**
  String get vehicleNotExited;

  /// No description provided for @totalDuration.
  ///
  /// In en, this message translates to:
  /// **'Total duration'**
  String get totalDuration;

  /// No description provided for @pleaseRetrieveVehicle.
  ///
  /// In en, this message translates to:
  /// **'Please retrieve your vehicle'**
  String get pleaseRetrieveVehicle;

  /// No description provided for @gracePeriodRemaining.
  ///
  /// In en, this message translates to:
  /// **'You have {mins}m {secs}s remaining to exit'**
  String gracePeriodRemaining(int mins, String secs);

  /// No description provided for @timeIn.
  ///
  /// In en, this message translates to:
  /// **'Entry'**
  String get timeIn;

  /// No description provided for @timeOut.
  ///
  /// In en, this message translates to:
  /// **'Exit'**
  String get timeOut;

  /// No description provided for @noImageAvailable.
  ///
  /// In en, this message translates to:
  /// **'No image'**
  String get noImageAvailable;

  /// No description provided for @qrPayment.
  ///
  /// In en, this message translates to:
  /// **'QR Payment'**
  String get qrPayment;

  /// No description provided for @scanToPayTitle.
  ///
  /// In en, this message translates to:
  /// **'Scan code to pay'**
  String get scanToPayTitle;

  /// No description provided for @scanToPaySubtitle.
  ///
  /// In en, this message translates to:
  /// **'Use your banking app to scan the QR code below.'**
  String get scanToPaySubtitle;

  /// No description provided for @paymentInitError.
  ///
  /// In en, this message translates to:
  /// **'Payment initialization error'**
  String get paymentInitError;

  /// No description provided for @transferInfo.
  ///
  /// In en, this message translates to:
  /// **'Transfer Information'**
  String get transferInfo;

  /// No description provided for @amount.
  ///
  /// In en, this message translates to:
  /// **'Amount'**
  String get amount;

  /// No description provided for @transferContent.
  ///
  /// In en, this message translates to:
  /// **'Description'**
  String get transferContent;

  /// No description provided for @checkingPaymentStatus.
  ///
  /// In en, this message translates to:
  /// **'Checking payment status...'**
  String get checkingPaymentStatus;

  /// No description provided for @iHaveTransferred.
  ///
  /// In en, this message translates to:
  /// **'I have transferred'**
  String get iHaveTransferred;

  /// No description provided for @paymentSuccess.
  ///
  /// In en, this message translates to:
  /// **'Payment successful!'**
  String get paymentSuccess;

  /// No description provided for @done.
  ///
  /// In en, this message translates to:
  /// **'Done'**
  String get done;

  /// No description provided for @afterTransferHint.
  ///
  /// In en, this message translates to:
  /// **'After transferring, press the button below to confirm.'**
  String get afterTransferHint;

  /// No description provided for @noQrAvailable.
  ///
  /// In en, this message translates to:
  /// **'No QR available'**
  String get noQrAvailable;

  /// No description provided for @login.
  ///
  /// In en, this message translates to:
  /// **'Login'**
  String get login;

  /// No description provided for @enterPhoneNumber.
  ///
  /// In en, this message translates to:
  /// **'Enter phone number'**
  String get enterPhoneNumber;

  /// No description provided for @phoneVerifyMessage.
  ///
  /// In en, this message translates to:
  /// **'The system will verify your account and guide you to the next step.'**
  String get phoneVerifyMessage;

  /// No description provided for @phoneLabel2.
  ///
  /// In en, this message translates to:
  /// **'Phone Number'**
  String get phoneLabel2;

  /// No description provided for @phoneExample.
  ///
  /// In en, this message translates to:
  /// **'E.g., 0987654321'**
  String get phoneExample;

  /// No description provided for @continueBtn.
  ///
  /// In en, this message translates to:
  /// **'Continue'**
  String get continueBtn;

  /// No description provided for @noAccountMessage.
  ///
  /// In en, this message translates to:
  /// **'Don\'t have an account? Please contact parking manager.'**
  String get noAccountMessage;

  /// No description provided for @phoneVerifyError.
  ///
  /// In en, this message translates to:
  /// **'Phone verification error'**
  String get phoneVerifyError;

  /// No description provided for @stepPhone.
  ///
  /// In en, this message translates to:
  /// **'Phone'**
  String get stepPhone;

  /// No description provided for @stepOtp.
  ///
  /// In en, this message translates to:
  /// **'OTP'**
  String get stepOtp;

  /// No description provided for @stepPassword.
  ///
  /// In en, this message translates to:
  /// **'Password'**
  String get stepPassword;

  /// No description provided for @activateAccount.
  ///
  /// In en, this message translates to:
  /// **'Activate Account'**
  String get activateAccount;

  /// No description provided for @enterOtp.
  ///
  /// In en, this message translates to:
  /// **'Enter OTP code'**
  String get enterOtp;

  /// No description provided for @otpSentTo.
  ///
  /// In en, this message translates to:
  /// **'A verification code has been sent to {phone}.'**
  String otpSentTo(String phone);

  /// No description provided for @otpCode.
  ///
  /// In en, this message translates to:
  /// **'OTP Code'**
  String get otpCode;

  /// No description provided for @enter6Digits.
  ///
  /// In en, this message translates to:
  /// **'Enter 6 digits'**
  String get enter6Digits;

  /// No description provided for @otpVerifyError.
  ///
  /// In en, this message translates to:
  /// **'OTP verification error'**
  String get otpVerifyError;

  /// No description provided for @otpResent.
  ///
  /// In en, this message translates to:
  /// **'OTP resent successfully'**
  String get otpResent;

  /// No description provided for @resendOtp.
  ///
  /// In en, this message translates to:
  /// **'Resend OTP'**
  String get resendOtp;

  /// No description provided for @resendOtpIn.
  ///
  /// In en, this message translates to:
  /// **'Resend OTP in {seconds}s'**
  String resendOtpIn(int seconds);

  /// No description provided for @createPassword.
  ///
  /// In en, this message translates to:
  /// **'Create Password'**
  String get createPassword;

  /// No description provided for @enterPassword.
  ///
  /// In en, this message translates to:
  /// **'Enter Password'**
  String get enterPassword;

  /// No description provided for @createPasswordFor.
  ///
  /// In en, this message translates to:
  /// **'Create password to activate account {phone}.'**
  String createPasswordFor(String phone);

  /// No description provided for @loginWith.
  ///
  /// In en, this message translates to:
  /// **'Login with number {phone}.'**
  String loginWith(String phone);

  /// No description provided for @password.
  ///
  /// In en, this message translates to:
  /// **'Password'**
  String get password;

  /// No description provided for @createNewPassword.
  ///
  /// In en, this message translates to:
  /// **'Create new password'**
  String get createNewPassword;

  /// No description provided for @loginFailed.
  ///
  /// In en, this message translates to:
  /// **'Login failed'**
  String get loginFailed;

  /// No description provided for @forgotPassword.
  ///
  /// In en, this message translates to:
  /// **'Forgot password?'**
  String get forgotPassword;

  /// No description provided for @changePasswordTitle.
  ///
  /// In en, this message translates to:
  /// **'Change Password'**
  String get changePasswordTitle;

  /// No description provided for @accountSecurity.
  ///
  /// In en, this message translates to:
  /// **'Account Security'**
  String get accountSecurity;

  /// No description provided for @passwordMinLength.
  ///
  /// In en, this message translates to:
  /// **'New password must be at least 6 characters long.'**
  String get passwordMinLength;

  /// No description provided for @currentPassword.
  ///
  /// In en, this message translates to:
  /// **'Current Password'**
  String get currentPassword;

  /// No description provided for @enterCurrentPassword.
  ///
  /// In en, this message translates to:
  /// **'Enter current password'**
  String get enterCurrentPassword;

  /// No description provided for @pleaseEnterOldPassword.
  ///
  /// In en, this message translates to:
  /// **'Please enter old password'**
  String get pleaseEnterOldPassword;

  /// No description provided for @newPassword.
  ///
  /// In en, this message translates to:
  /// **'New Password'**
  String get newPassword;

  /// No description provided for @enterNewPassword.
  ///
  /// In en, this message translates to:
  /// **'Enter new password'**
  String get enterNewPassword;

  /// No description provided for @pleaseEnterNewPassword.
  ///
  /// In en, this message translates to:
  /// **'Please enter new password'**
  String get pleaseEnterNewPassword;

  /// No description provided for @passwordTooShort.
  ///
  /// In en, this message translates to:
  /// **'Password must be at least 6 characters'**
  String get passwordTooShort;

  /// No description provided for @passwordMustDiffer.
  ///
  /// In en, this message translates to:
  /// **'New password must differ from current password'**
  String get passwordMustDiffer;

  /// No description provided for @confirmNewPassword.
  ///
  /// In en, this message translates to:
  /// **'Confirm New Password'**
  String get confirmNewPassword;

  /// No description provided for @reenterNewPassword.
  ///
  /// In en, this message translates to:
  /// **'Re-enter new password'**
  String get reenterNewPassword;

  /// No description provided for @pleaseConfirmPassword.
  ///
  /// In en, this message translates to:
  /// **'Please confirm new password'**
  String get pleaseConfirmPassword;

  /// No description provided for @passwordMismatch.
  ///
  /// In en, this message translates to:
  /// **'Password confirmation does not match'**
  String get passwordMismatch;

  /// No description provided for @processing.
  ///
  /// In en, this message translates to:
  /// **'Processing...'**
  String get processing;

  /// No description provided for @confirmChangePassword.
  ///
  /// In en, this message translates to:
  /// **'Confirm password change'**
  String get confirmChangePassword;

  /// No description provided for @changePasswordSuccess.
  ///
  /// In en, this message translates to:
  /// **'Password changed successfully!'**
  String get changePasswordSuccess;

  /// No description provided for @changePasswordFailed.
  ///
  /// In en, this message translates to:
  /// **'Password change failed.'**
  String get changePasswordFailed;

  /// No description provided for @packageLabel.
  ///
  /// In en, this message translates to:
  /// **'Package:'**
  String get packageLabel;

  /// No description provided for @validityPeriod.
  ///
  /// In en, this message translates to:
  /// **'Validity:'**
  String get validityPeriod;

  /// No description provided for @daysRemainingRenewNow.
  ///
  /// In en, this message translates to:
  /// **'Only {days} days left — Renew now!'**
  String daysRemainingRenewNow(int days);

  /// No description provided for @contractWithId.
  ///
  /// In en, this message translates to:
  /// **'Contract {id}'**
  String contractWithId(String id);

  /// No description provided for @overviewInfo.
  ///
  /// In en, this message translates to:
  /// **'Overview'**
  String get overviewInfo;

  /// No description provided for @groupRepresentativeLabel.
  ///
  /// In en, this message translates to:
  /// **'Group / representative'**
  String get groupRepresentativeLabel;

  /// No description provided for @createdAtLabel.
  ///
  /// In en, this message translates to:
  /// **'Created date'**
  String get createdAtLabel;

  /// No description provided for @registeredVehiclesCountLabel.
  ///
  /// In en, this message translates to:
  /// **'Registered vehicles'**
  String get registeredVehiclesCountLabel;

  /// No description provided for @vehiclesCountText.
  ///
  /// In en, this message translates to:
  /// **'{count} vehicles'**
  String vehiclesCountText(int count);

  /// No description provided for @pendingPaymentsBannerTitle.
  ///
  /// In en, this message translates to:
  /// **'You have {count} packages pending payment'**
  String pendingPaymentsBannerTitle(int count);

  /// No description provided for @pendingPaymentsBannerSubtitle.
  ///
  /// In en, this message translates to:
  /// **'Please complete payment to activate package or Cancel transaction in History to restore draft.'**
  String get pendingPaymentsBannerSubtitle;

  /// No description provided for @goToHistoryTooltip.
  ///
  /// In en, this message translates to:
  /// **'Go to History'**
  String get goToHistoryTooltip;

  /// No description provided for @selectAllDraftsCount.
  ///
  /// In en, this message translates to:
  /// **'Select all drafts ({count})'**
  String selectAllDraftsCount(int count);

  /// No description provided for @draftsCountBadge.
  ///
  /// In en, this message translates to:
  /// **'{count} drafts'**
  String draftsCountBadge(int count);

  /// No description provided for @checkoutWithCount.
  ///
  /// In en, this message translates to:
  /// **'Checkout ({count} vehicles)'**
  String checkoutWithCount(int count);

  /// No description provided for @historyTitle.
  ///
  /// In en, this message translates to:
  /// **'History'**
  String get historyTitle;

  /// No description provided for @failedToLoadPaymentHistory.
  ///
  /// In en, this message translates to:
  /// **'Failed to load payment history'**
  String get failedToLoadPaymentHistory;

  /// No description provided for @retry.
  ///
  /// In en, this message translates to:
  /// **'Retry'**
  String get retry;

  /// No description provided for @noBillsYet.
  ///
  /// In en, this message translates to:
  /// **'No invoices yet'**
  String get noBillsYet;

  /// No description provided for @paymentHistorySubtitle.
  ///
  /// In en, this message translates to:
  /// **'Payment invoices will appear here.'**
  String get paymentHistorySubtitle;

  /// No description provided for @pendingPaymentHeader.
  ///
  /// In en, this message translates to:
  /// **'Pending payment'**
  String get pendingPaymentHeader;

  /// No description provided for @processedHeader.
  ///
  /// In en, this message translates to:
  /// **'Processed'**
  String get processedHeader;

  /// No description provided for @continuePaymentButton.
  ///
  /// In en, this message translates to:
  /// **'Continue payment'**
  String get continuePaymentButton;

  /// No description provided for @cancelInvoiceDialogTitle.
  ///
  /// In en, this message translates to:
  /// **'Cancel invoice?'**
  String get cancelInvoiceDialogTitle;

  /// No description provided for @cancelInvoiceDialogBody.
  ///
  /// In en, this message translates to:
  /// **'Invoice {code} will be canceled. Related packages will be restored to their previous state.'**
  String cancelInvoiceDialogBody(String code);

  /// No description provided for @no.
  ///
  /// In en, this message translates to:
  /// **'No'**
  String get no;

  /// No description provided for @invoiceCancelSuccess.
  ///
  /// In en, this message translates to:
  /// **'Invoice canceled successfully.'**
  String get invoiceCancelSuccess;

  /// No description provided for @invoiceCancelFailed.
  ///
  /// In en, this message translates to:
  /// **'Failed to cancel invoice.'**
  String get invoiceCancelFailed;

  /// No description provided for @parkingHistoryTitle.
  ///
  /// In en, this message translates to:
  /// **'Parking History'**
  String get parkingHistoryTitle;

  /// No description provided for @filterButtonText.
  ///
  /// In en, this message translates to:
  /// **'Filter'**
  String get filterButtonText;

  /// No description provided for @filterAppliedText.
  ///
  /// In en, this message translates to:
  /// **'Filter applied'**
  String get filterAppliedText;

  /// No description provided for @clearFilterButton.
  ///
  /// In en, this message translates to:
  /// **'Clear filter'**
  String get clearFilterButton;

  /// No description provided for @noParkingHistoryFound.
  ///
  /// In en, this message translates to:
  /// **'No parking history found'**
  String get noParkingHistoryFound;

  /// No description provided for @changeFilterSubtitle.
  ///
  /// In en, this message translates to:
  /// **'Try changing your filter settings.'**
  String get changeFilterSubtitle;

  /// No description provided for @searchFilterTitle.
  ///
  /// In en, this message translates to:
  /// **'Search Filter'**
  String get searchFilterTitle;

  /// No description provided for @timeRangeLabel.
  ///
  /// In en, this message translates to:
  /// **'Time range'**
  String get timeRangeLabel;

  /// No description provided for @fromDateLabel.
  ///
  /// In en, this message translates to:
  /// **'From date'**
  String get fromDateLabel;

  /// No description provided for @toDateLabel.
  ///
  /// In en, this message translates to:
  /// **'To date'**
  String get toDateLabel;

  /// No description provided for @plateNumberLabel.
  ///
  /// In en, this message translates to:
  /// **'Plate number'**
  String get plateNumberLabel;

  /// No description provided for @plateNumberExample.
  ///
  /// In en, this message translates to:
  /// **'e.g., 30A-123.45'**
  String get plateNumberExample;

  /// No description provided for @resetFilterButton.
  ///
  /// In en, this message translates to:
  /// **'Reset'**
  String get resetFilterButton;

  /// No description provided for @applyFilterButton.
  ///
  /// In en, this message translates to:
  /// **'Apply'**
  String get applyFilterButton;

  /// No description provided for @failedToLoadData.
  ///
  /// In en, this message translates to:
  /// **'Failed to load data'**
  String get failedToLoadData;

  /// No description provided for @paidStatus.
  ///
  /// In en, this message translates to:
  /// **'Paid'**
  String get paidStatus;

  /// No description provided for @unpaidStatus.
  ///
  /// In en, this message translates to:
  /// **'Unpaid'**
  String get unpaidStatus;
}

class _AppLocalizationsDelegate
    extends LocalizationsDelegate<AppLocalizations> {
  const _AppLocalizationsDelegate();

  @override
  Future<AppLocalizations> load(Locale locale) {
    return SynchronousFuture<AppLocalizations>(lookupAppLocalizations(locale));
  }

  @override
  bool isSupported(Locale locale) =>
      <String>['en', 'vi'].contains(locale.languageCode);

  @override
  bool shouldReload(_AppLocalizationsDelegate old) => false;
}

AppLocalizations lookupAppLocalizations(Locale locale) {
  // Lookup logic when only language code is specified.
  switch (locale.languageCode) {
    case 'en':
      return AppLocalizationsEn();
    case 'vi':
      return AppLocalizationsVi();
  }

  throw FlutterError(
    'AppLocalizations.delegate failed to load unsupported locale "$locale". This is likely '
    'an issue with the localizations generation tool. Please file an issue '
    'on GitHub with a reproducible sample app and the gen-l10n configuration '
    'that was used.',
  );
}
