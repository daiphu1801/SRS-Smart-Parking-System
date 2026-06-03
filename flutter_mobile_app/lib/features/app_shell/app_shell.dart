import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:smart_parking_mobile/core/l10n/app_localizations.dart';
import 'package:smart_parking_mobile/core/router/app_router.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';

/// Persistent bottom navigation shell for all customer routes.
/// go_router ShellRoute keeps each tab's state alive.
class AppShell extends StatelessWidget {
  final Widget child;
  const AppShell({super.key, required this.child});

  static const _tabs = [
    AppRoutes.customerHome,
    AppRoutes.parkingSessionList,
    AppRoutes.customerPackages,
    AppRoutes.customerHistory,
    AppRoutes.accountMenu,
  ];

  int _currentIndex(BuildContext context) {
    final location = GoRouterState.of(context).matchedLocation;
    final idx = _tabs.indexWhere((t) => location.startsWith(t));
    return idx < 0 ? 0 : idx;
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    return Scaffold(
      body: child,
      bottomNavigationBar: Container(
        decoration: BoxDecoration(
          color: AppTheme.background,
          border: Border(top: BorderSide(color: AppTheme.border)),
        ),
        child: BottomNavigationBar(
          currentIndex: _currentIndex(context),
          onTap: (i) => context.go(_tabs[i]),
          items: [
            BottomNavigationBarItem(
              icon: const Icon(Icons.home_outlined),
              activeIcon: const Icon(Icons.home),
              label: l10n.navHome,
            ),
            BottomNavigationBarItem(
              icon: const Icon(Icons.local_parking_outlined),
              activeIcon: const Icon(Icons.local_parking),
              label: l10n.navParking,
            ),
            BottomNavigationBarItem(
              icon: const Icon(Icons.card_membership_outlined),
              activeIcon: const Icon(Icons.card_membership),
              label: l10n.navPackages,
            ),
            BottomNavigationBarItem(
              icon: const Icon(Icons.history_outlined),
              activeIcon: const Icon(Icons.history),
              label: l10n.navHistory,
            ),
            BottomNavigationBarItem(
              icon: const Icon(Icons.person_outline),
              activeIcon: const Icon(Icons.person),
              label: l10n.navAccount,
            ),
          ],
          type: BottomNavigationBarType.fixed,
        ),
      ),
    );
  }
}

