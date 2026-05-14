# Smart Parking System — UI Design Prompt

## Design System

### Color Palette
Primary background: #ffffff (white)
Primary foreground: #052e16 (green-950)
Surface: #ffffff
On-surface text: #052e16
Border: #052e16 at 12% opacity
Disabled state: #052e16 at 30% opacity
Hover state on filled: background switches to white, text switches to #052e16
Hover state on outlined: background switches to #052e16, text switches to white

### Typography
Font family: Inter
Font weights allowed: regular (400), medium (500), semibold (600) only
Font weights banned: bold (700), extrabold (800), black (900)
Text transform: normal case and Title Case only — uppercase is strictly banned
Display: 32px / semibold
Heading 1: 24px / semibold
Heading 2: 20px / semibold
Heading 3: 16px / semibold
Body: 14px / regular
Body small: 12px / regular
Label: 13px / medium
Caption: 11px / regular

### Shape
Border radius token: rounded — 8px for cards and inputs, 6px for buttons and chips, 4px for small badges
Rounded is the only allowed variant
Sharp corners are banned
Full pill (border-radius: 9999px) is banned

### Spacing
Base unit: 4px
Page padding: 24px
Section gap: 32px
Card padding: 20px
Input height: 44px
Button height: 40px

### Button Rules
Filled button: background #052e16, text white, border none
Outlined button: background white, text #052e16, border 1.5px solid #052e16
On hover filled: background becomes white, text becomes #052e16, border 1.5px solid #052e16 appears
On hover outlined: background becomes #052e16, text becomes white, border stays
Primary actions use filled buttons
Secondary, cancel, and back actions use outlined buttons
Destructive actions use outlined buttons with no special color — stay within the 2-color system
Icon-only actions use outlined style

### Icon Style
Line icons only (stroke, not filled icons)
Stroke weight: 1.5px
Icon size: 20px inline, 24px standalone
Icon color inherits text color

### Elevation and Shadow
No shadows allowed
Separation between sections is achieved with borders at 1px, color #052e16 at 10% opacity

### Gradient
Gradients are strictly banned on all surfaces, backgrounds, buttons, and text

---

## Mobile App — Flutter (Customer and Guard Roles)

### App Shell
Bottom navigation bar with 4 tabs
Bottom nav background: white, top border 1px
Active tab icon and label: #052e16, semibold label
Inactive tab icon and label: #052e16 at 40% opacity
Status bar style: dark content on white background
No floating action button

### Screen: Login (Shared)
Full white background
Logo centered at top 20% of screen height
App name below logo, 20px semibold, #052e16
Phone number input field below, label "Phone number", placeholder "+84 xxx xxx xxx"
"Send Otp" filled button, full width
At bottom, support link "Having trouble? Contact support", 12px, #052e16, underlined

### Screen: Otp Verification (Shared)
Back arrow icon top left
Heading "Verify your number", 24px semibold
Subtext "Enter the 6-digit code sent to [phone]", 14px regular, 60% opacity
6 individual digit input boxes in a row, each 44x52px, border 1.5px, rounded 8px
Active box border: #052e16 full opacity
Inactive box border: #052e16 at 20% opacity
"Verify" filled button, full width, below inputs
"Resend code" outlined button, below filled button
Timer countdown label "Resend available in 00:45", 12px, centered

### Role: Customer

#### Screen: Home (Dashboard)
Header bar: app logo left, notification bell icon right
Section "Your Vehicles" — horizontal scroll list
Vehicle card: 160x100px, white background, rounded 8px, border 1px
Vehicle card content: plate number 16px semibold, package status label 12px, days remaining 12px
Section "Active Session" — if a vehicle is currently parked
Session card: full width, shows plate, entry time, duration running, fee estimate
Section "Quick Actions" — 2x2 grid of icon+label cards
Actions: "My Packages", "History", "Submit Complaint", "Add Vehicle"
Each action card: white, border 1px, rounded 8px, 24px icon top, label 13px medium below

#### Screen: My Packages
Page title "My Packages", 24px semibold
Tab row: "Active" | "Expired" — outlined tab style, active tab fills
Each package card: plate number bold 16px, package name 14px, start and end date 12px, status badge
Status badge: small 6px rounded, white background, border 1px, label "Active" or "Expired" in 11px
"Renew" outlined button inline right of each expired card
"Buy New Package" filled button pinned at bottom

#### Screen: Buy / Renew Package
Page title "Register Package"
Form fields: plate number (read-only if coming from a vehicle), package selector (radio list), duration selector (1 month, 3 months, 6 months)
Price summary card at bottom: line items, total in semibold
"Proceed to Payment" filled button
QR code payment modal: bottom sheet, shows QR image centered, amount, countdown timer, "Cancel" outlined button

#### Screen: History
Segmented control: "Parking Sessions" | "Payments"
Parking sessions list: each item shows plate, entry date, exit date, duration, fee
Payment list: each item shows payment code, amount, method (Qr or Cash), date
Each list item has a right chevron icon
Pull to refresh supported

#### Screen: Submit Complaint
Page title "Submit Complaint"
Dropdown: complaint category
Textarea: "Describe the issue", 5 rows
Image attachment row: "Attach photos" with a plus icon, shows thumbnail previews
"Submit" filled button at bottom

#### Screen: Add Vehicle
Page title "Add Vehicle"
Input: "Plate number" — character limit shown
Info text below input: "Your plate must match the official registration format", 12px
"Add Vehicle" filled button
Quota indicator at top: "2 of 3 slots used", progress bar 1px height, #052e16 fill on white track

### Role: Guard (Mobile — For Patrols)

#### Screen: Home (Guard Dashboard)
Header: "Guard Panel", current shift status chip — "On Duty" outlined chip or "Off Duty" chip
If off duty: centered message "You are not on duty. Please check in at the security booth."
If on duty: show active alert count, quick access tiles

#### Screen: Alert Feed
Page title "Active Alerts"
Each alert card: plate number, violation type, zone location, time detected, thumbnail image
Card has two actions: "View Details" outlined and "Mark Resolved" filled
Resolved alerts are shown with 40% opacity, "Resolved" label replaces actions

#### Screen: Gate Override (Manual Entry)
Page title "Manual Gate Control"
Plate number input, large font, 20px
"Look Up" outlined button
Result card: vehicle status, owner name or "Guest", session info
If subscription valid: "Open Gate" filled button
If no subscription: session info shows as guest, "Open Gate" filled, "Collect Cash" outlined
"Collect Cash" flow: confirmation dialog with amount, "Confirm Collection" filled

#### Screen: Vehicle Lookup
Search input at top, autofocus
Results show: plate, status (In Parking / Not Found), type (Resident / Guest), unit name if resident, session duration if inside

---

## Desktop App — Python Flet (Admin, Manager, Guard Roles)

### App Shell
Fixed left sidebar: 240px wide, white background, right border 1px
Sidebar header: logo + app name "Smart Parking", 16px semibold
Navigation items: icon + label, 40px height, rounded 6px hover
Active nav item: #052e16 background, white text and icon
Inactive nav item: transparent background, #052e16 text and icon
Role label chip at bottom of sidebar: small outlined chip showing current role
Main content area: white, padding 32px
Top bar: page title left, action buttons right

### Sidebar Navigation by Role

#### Admin Sidebar Items
Dashboard
Group Management
Employee Accounts
Pricing and Packages
Zones and Devices
Reports and Audit
Complaints
Settings

#### Manager Sidebar Items
Dashboard
Reports and Audit
Complaints

#### Guard (Desktop) Sidebar Items
Gate Control
Alert Feed
Vehicle Lookup
Shift Log

### Screen: Dashboard (Admin and Manager)

#### Layout
2-column grid at top: stat cards row
4 stat cards: "Total Revenue Today", "Active Sessions", "Occupied Slots", "Open Complaints"
Stat card: white, border 1px, rounded 8px, 20px padding
Stat card content: label 12px medium top, number 32px semibold below, change indicator 12px at bottom
Change indicator: small arrow icon + percentage, use #052e16 full opacity for positive, 50% opacity for negative

#### Revenue Chart Section
Section title "Revenue Overview", 20px semibold
Placeholder chart area: white, border 1px dashed, rounded 8px, 280px height
Below chart: two legend items "Subscription" and "Guest" as outlined chips

#### Recent Sessions Table
Section title "Recent Sessions"
Table: full width, header row with bottom border 1px
Header cells: 12px semibold, #052e16, normal case
Data rows: 14px regular, alternating white and #052e16 at 3% opacity for zebra
Columns: Plate, Entry Time, Exit Time, Duration, Fee, Status, Type

### Screen: Group Management

#### List View
Top bar: page title "Group Management", "Add Group" filled button right
Search input left of button: "Search by name or code"
Table: Group Name, Code, Profile, Owner, Vehicle Count, Actions
Actions column: "View" outlined button, "Edit" outlined button — both small 32px height

#### Group Detail Panel (Right slide-in)
Panel width 480px, border-left 1px, white background
Section "Group Info": fields displayed as label + value pairs
Section "Members": list of customer names with phone, each has "Remove" text link 12px
Section "Vehicles": list of plates with package status badge
"Close" outlined button top right of panel

### Screen: Pricing and Packages

#### Tab Layout
Two tabs: "Subscription Packages" | "Guest Tariffs"
Tabs use outlined style; active tab becomes filled

#### Subscription Packages Tab
"Add Package" filled button top right
Table: Package Name, Vehicle Type, Duration, Price, Status, Actions
Inline edit: clicking a row expands it into an editable form with "Save" filled and "Cancel" outlined buttons

#### Guest Tariffs Tab
"Add Tariff Rule" filled button top right
Table: Vehicle Type, Day Type, Time Range, Base Block, Base Price, Next Block, Next Price, Max Daily
Each row expandable for editing

### Screen: Zones and Devices

#### Left Panel: Zone Tree
240px width, border-right 1px
Tree structure: Building → Floor → Area → Gate
Each node: icon (based on zone type) + zone name
Active node: #052e16 background, white text
"Add Zone" outlined button at bottom of panel

#### Right Panel: Zone Detail and Devices
Zone name heading 20px semibold
Zone stats: capacity, current occupancy shown as "18 / 40 slots"
Section "Assigned Devices": table with Device Name, Type, Direction, Ip Address, Status, Last Ping
Status indicator: small 8px circle, filled #052e16 for Online, outlined for Offline, 50% opacity for Maintenance
"Add Device" outlined button
"Ping All" outlined button

### Screen: Reports and Audit

#### Filter Bar
Date range picker, guard selector dropdown, payment method filter, "Generate Report" filled button

#### Revenue Report Section
Three summary cards: "Subscription Revenue", "Guest Revenue (Qr)", "Cash Revenue"
Session log table below: all columns from parking sessions + guard name for cash entries

#### Audit Log Section
Tab: "Audit Log"
Table: Timestamp, Guard Name, Action Type, Plate, Notes, Image
Action types: "Manual Gate Open", "Cash Collected", "Override Entry", "Override Exit"
"Export to Csv" outlined button top right

### Screen: Complaints

#### List View
Table: Ticket Id, Customer Name, Category, Submitted At, Status
Status badge: "Open" outlined, "Resolved" filled (reversed to draw less attention to resolved)
Clicking a row opens detail panel

#### Complaint Detail Panel
Panel 480px, slide in from right
Customer info, submission date, category, full description
Attached photo thumbnails — clickable to enlarge
"Evidence Images" section pulls matching session photos from system
Status selector: "Open" / "In Progress" / "Resolved" — outlined segmented control
"Reply to Customer" textarea
"Submit Response" filled button, "Close Panel" outlined button

### Screen: Gate Control (Guard Desktop)

#### Layout
Two-column: left 60% for main control, right 40% for alert feed

#### Left: Gate Control Panel
Large plate input: 36px font, 56px height input
"Check In" filled button and "Check Out" filled button side by side
Result area: white card showing session info, ownership, fee
Action buttons: "Open Gate" filled, "Mark Cash Collected" outlined
Override log below: last 10 manual actions today listed as rows

#### Right: Live Alert Feed
Section title "Live Alerts"
Scrollable list of alert cards
Each card: thumbnail image, plate, zone, violation type, time, "Resolve" filled button
"No active alerts" empty state with a centered icon and message when list is empty

### Screen: Vehicle Lookup (Guard Desktop)

#### Search Area
Large search input, 48px height, "Search plate number" placeholder
"Search" filled button inline

#### Result Card
White card, border 1px, rounded 8px
Top row: plate number 24px semibold, status chip right
Status chip: "In Parking" outlined / "Not Found" outlined
Details grid: Session Start, Duration, Vehicle Type, Owner, Unit
If session found: "View Full Session" outlined button
If not found: informational message "No active session found for this plate"

### Screen: Shift Log (Guard Desktop)

#### Today's Summary
Cards row: "Total Gate Opens", "Cash Collected Total", "Alerts Resolved", "Shift Start"
Timeline list below: chronological list of all actions in current shift
Each timeline item: timestamp left, action description right, icon per action type

---

## Shared Components

### Data Table
Header: 40px row height, bottom border 1px, labels 12px semibold
Data rows: 48px row height, bottom border 1px at 6% opacity
Hover row: background #052e16 at 4% opacity
Pagination: previous / next outlined buttons, page info "Page 1 of 8" label, rows-per-page selector

### Empty State
Centered in container
24px line icon
Message title 16px semibold
Subtitle 14px regular, 60% opacity
Optional action button below

### Modal / Dialog
Overlay: white at 60% opacity behind modal
Modal container: white, rounded 8px, border 1px, padding 24px, max-width 480px
Title 20px semibold, close icon top right
Content area, footer with action buttons right-aligned
Primary action filled, secondary action outlined

### Toast / Snackbar
White background, border 1px, rounded 6px
Line icon left indicating type
Message 14px regular
Auto dismiss after 4 seconds
Positioned bottom-center on mobile, bottom-right on desktop

### Input Field
Height 44px, border 1.5px, rounded 8px
Label above input, 13px medium
Placeholder text 14px, #052e16 at 40% opacity
Focus state: border opacity 100%
Error state: border remains #052e16, error message below in 12px, no color change to keep 2-color rule
Helper text below input, 12px, 60% opacity

### Badge / Status Chip
Small rounded 4px, not pill-shaped
White background, border 1px, text 11px medium
No background fill except when representing the active/selected state in a group

### Image Preview
Aspect ratio 4:3
Rounded 8px
Border 1px
Clicking opens full-screen overlay with close icon