import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

/// Smart Parking Design System — 2-Color Palette
/// Primary: #052e16 (green-950)   Background: #ffffff
class AppTheme {
  AppTheme._();

  // ── Color Tokens ──────────────────────────────────────────
  static const Color primary = Color(0xFF052e16);
  static const Color background = Color(0xFFFFFFFF);
  static const Color surface = Color(0xFFf9faf5);
  static const Color error = Color(0xFFb91c1c);

  /// Border at 10% opacity
  static Color border = primary.withValues(alpha: 0.10);
  /// Border at 12% opacity (stronger)
  static Color borderStrong = primary.withValues(alpha: 0.12);
  /// Disabled text / icon
  static Color disabled = primary.withValues(alpha: 0.30);
  /// Subtle text
  static Color subtle = primary.withValues(alpha: 0.60);

  // ── Spacing Tokens ────────────────────────────────────────
  static const double pagePadding = 24.0;
  static const double sectionGap = 32.0;
  static const double cardPadding = 20.0;
  static const double inputHeight = 44.0;
  static const double buttonHeight = 40.0;

  // ── Shape Tokens ──────────────────────────────────────────
  static const double radiusCard = 8.0;
  static const double radiusButton = 6.0;
  static const double radiusBadge = 4.0;

  static final List<BoxShadow> shadowSmall = [
    BoxShadow(
      color: primary.withValues(alpha: 0.05),
      blurRadius: 8,
      offset: const Offset(0, 2),
    ),
  ];

  // ── Typography Scale ──────────────────────────────────────
  static TextStyle get display => GoogleFonts.inter(fontSize: 32, fontWeight: FontWeight.w600, color: primary);
  static TextStyle get heading1 => GoogleFonts.inter(fontSize: 24, fontWeight: FontWeight.w600, color: primary);
  static TextStyle get heading2 => GoogleFonts.inter(fontSize: 20, fontWeight: FontWeight.w600, color: primary);
  static TextStyle get heading3 => GoogleFonts.inter(fontSize: 16, fontWeight: FontWeight.w600, color: primary);
  static TextStyle get body => GoogleFonts.inter(fontSize: 14, fontWeight: FontWeight.w400, color: primary);
  static TextStyle get bodySmall => GoogleFonts.inter(fontSize: 12, fontWeight: FontWeight.w400, color: primary);
  static TextStyle get label => GoogleFonts.inter(fontSize: 13, fontWeight: FontWeight.w500, color: primary);
  static TextStyle get caption => GoogleFonts.inter(fontSize: 11, fontWeight: FontWeight.w400, color: primary);

  // ── ThemeData ─────────────────────────────────────────────
  static ThemeData get light {
    return ThemeData(
      useMaterial3: true,
      fontFamily: GoogleFonts.inter().fontFamily,
      scaffoldBackgroundColor: background,
      colorScheme: ColorScheme.light(
        primary: primary,
        onPrimary: background,
        surface: surface,
        onSurface: primary,
        outline: border,
      ),
      appBarTheme: AppBarTheme(
        backgroundColor: background,
        foregroundColor: primary,
        elevation: 0,
        scrolledUnderElevation: 0,
        titleTextStyle: heading3,
        iconTheme: const IconThemeData(color: primary, size: 24),
      ),
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: primary,
          foregroundColor: background,
          minimumSize: const Size.fromHeight(buttonHeight),
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(radiusButton)),
          elevation: 0,
          textStyle: label,
        ),
      ),
      outlinedButtonTheme: OutlinedButtonThemeData(
        style: OutlinedButton.styleFrom(
          foregroundColor: primary,
          minimumSize: const Size.fromHeight(buttonHeight),
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(radiusButton)),
          side: const BorderSide(color: primary, width: 1.5),
          elevation: 0,
          textStyle: label,
        ),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: false,
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(radiusCard),
          borderSide: BorderSide(color: borderStrong, width: 1.5),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(radiusCard),
          borderSide: BorderSide(color: borderStrong, width: 1.5),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(radiusCard),
          borderSide: const BorderSide(color: primary, width: 1.5),
        ),
        errorBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(radiusCard),
          borderSide: BorderSide(color: borderStrong, width: 1.5),
        ),
        contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
        labelStyle: label,
        hintStyle: body.copyWith(color: disabled),
      ),
      dividerTheme: DividerThemeData(color: border, thickness: 1, space: 0),
      cardTheme: CardThemeData(
        elevation: 0,
        color: background,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(radiusCard),
          side: BorderSide(color: border),
        ),
        margin: EdgeInsets.zero,
      ),
      bottomNavigationBarTheme: BottomNavigationBarThemeData(
        backgroundColor: background,
        selectedItemColor: primary,
        unselectedItemColor: disabled,
        showSelectedLabels: true,
        showUnselectedLabels: true,
        elevation: 0,
        type: BottomNavigationBarType.fixed,
        selectedLabelStyle: caption.copyWith(fontWeight: FontWeight.w600),
        unselectedLabelStyle: caption,
      ),
    );
  }
}
