# HomeLedger Android design specification

## Product direction

HomeLedger is a calm, local-first household spending ledger for Android. The interface favors a clear amount, recognizable categories, and straightforward actions. The installed app starts empty; it never presents sample expenses as a household's real financial history. Illustrative sample values belong only in prototypes and documentation.

## Design tokens

| Token | Value | Use |
|---|---|---|
| Forest | `#145C43` | Primary actions, selected states, chart emphasis |
| Deep forest | `#0D3B2E` | App bar, high emphasis, dark surfaces |
| Fresh green | `#35A879` | Positive feedback, chart secondary |
| Background | `#F6F8F6` | Main light canvas |
| Surface | `#FFFFFF` | Cards, sheets, text fields |
| Primary text | `#17231D` | Titles, amounts, labels |
| Secondary text | `#77827B` | Hints, supporting labels |
| Border | `#E5EBE6` | Dividers and outlined controls |
| Error | `#B42318` | Validation and destructive confirmation |
| Dark canvas | `#101713` | Dark theme background |
| Dark surface | `#19231D` | Dark theme cards |

### Typography

Use the Android system sans-serif with tabular figures for currency amounts. Display title 28sp/700; screen title 24sp/700; section title 16sp/600; primary amount 32sp/700; body 14sp/400; supporting text 12sp/400; compact metadata 11sp/500. Do not use labels below 11sp for essential information.

### Spacing, shape, elevation

Base spacing unit is 4dp. Use 4, 8, 12, 16, 20, 24, 32, and 40dp. Page horizontal inset is 20dp on compact phones and 24dp above 600dp width. Cards use 16dp corners, fields 12dp, chips 20dp, and full-screen sheets 24dp top corners. Prefer a 1dp border and no elevation; use 2-4dp tonal elevation for the floating add action and modal surfaces. Avoid stacked shadows.

### Icons and categories

Use Material Symbols/Compose Material icons, 20-24dp, rounded stroke/fill style. Category mapping: Utilities - bolt; Groceries - shopping cart; Housing - home; Repairs - build; Transport - directions car; Healthcare - medical services; Education - school; Subscriptions - subscriptions; Other - category. Custom categories can choose from these same icon names. Icons always accompany readable category names.

## Navigation

Use four bottom destinations: Home, History, Reports, Settings. A forest-green extended FAB labelled **Add expense** is available on the primary ledger destinations. Categories live under Settings because they are configuration, not a frequent top-level destination. Opening an expense presents an edit/detail sheet; back returns to the originating destination. Saving from the add flow returns Home and refreshes its totals.

## Screen specifications

### Home dashboard

Top app bar: compact HomeLedger mark and the selected reporting period. Greeting: "Your household at a glance". Period control offers Week, Month, Year, and Custom. The largest card presents total GHS spending with a previous-period comparison only when both periods contain data. Follow with a category breakdown sorted by amount and the five newest transactions. Empty state replaces the comparison and chart with a short explanation and Add expense action. Never seed fictitious expenses into Room.

### Add/edit expense

Present as a full-height modal sheet with a visible close/back affordance. Amount is the first field, accepts decimal digits and displays the configured currency. Category selection uses a 3-column icon grid. Date defaults to today and opens a native Material date picker. Description is optional and capped at 100 characters. Save is full width and pinned above the IME/navigation inset. Reject blank, zero, negative, non-finite, or more-than-two-decimal values inline; preserve entered values on validation failure. Edit uses the same form and adds a destructive delete action with explicit confirmation.

### History

Search field and filter chips precede a chronologically descending list. Group rows by local calendar day with a date heading. Each row shows icon, category, optional description, and signed GHS amount. Search matches note and category. Category and date filters compose. Empty history and no-matches states use different copy. Selecting an item opens its editable details. Deletion requires confirmation and leaves the current filter intact.

### Reports

Reuse the Week/Month/Year/Custom period control. Show the period total, previous-period comparison if available, a simple labelled trend chart, and category totals with percentage-of-total. The chart is derived only from persisted expense rows. With fewer than two buckets, replace the trend with an explanatory empty state; never plot invented values.

### Categories

Show built-in and custom groups. Built-ins cover utilities, groceries, housing, repairs, transport, healthcare, education, subscriptions, and other. Users can add or edit custom category name and icon, and archive a category. Archiving prevents future selection but retains its category record and all linked expenses. An archived category can be restored.

### Settings

Currency selector defaults to GHS; week-start selector defaults Monday; theme selector offers System, Light, and Dark. Backup and Restore use Android document pickers and a local JSON file. Explain that backup files are user-managed and that uninstalling/clearing app data may remove the database. Include app version and a concise offline/local-storage statement. No sign-in, cloud, analytics, subscriptions, or network permission.

## Interaction and states

- Period changes update amount, comparison, category totals, and chart together.
- Comparison is hidden when the previous equivalent interval has no expenses; zero is not presented as a percentage change.
- Loading uses a restrained progress indicator while Room emits its initial result.
- Database/read failures use a recoverable inline error with retry.
- Save and delete produce accessible snackbar feedback.
- Destructive actions use a confirmation dialog naming the selected expense.
- Empty data shows a calm, actionable message; no decorative fake chart.
- Respect system font scale, content descriptions, 48dp minimum touch targets, keyboard traversal, contrast, and system reduced-motion preferences.

## Responsive Android behavior

Prioritize 360x800dp phones. Keep the form within one scrollable screen and avoid horizontal scrolling. At widths above 600dp, constrain content to 640dp and center it; use a two-column report/category layout only when each column remains at least 260dp wide. Support portrait first and landscape through scrolling. System bars, display cutouts, IME, and gesture navigation are handled with Compose insets.

## Compose handoff

Suggested hierarchy: `HomeLedgerApp` -> `Scaffold` -> `LedgerBottomBar` + destination `NavHost`/screen switch -> `HomeScreen`, `HistoryScreen`, `ReportsScreen`, `SettingsScreen`; shared `PeriodSelector`, `MoneyAmount`, `CategoryIcon`, `CategoryBreakdown`, `ExpenseRow`, `EmptyState`, `ExpenseEditorSheet`, `ConfirmDeleteDialog`, and `LedgerSnackbar`. Data layer: Room entities/DAOs -> repository -> `LedgerViewModel`; reporting math is a pure Kotlin domain layer with unit tests.
