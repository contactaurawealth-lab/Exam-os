# StudyOffline — design.md
*Complete design specification. Native Android, Jetpack Compose + Material3 (heavily re-themed).*

---

## 1. Design principles

1. **One accent, used sparingly.** Everything else is neutral. The accent marks the single most important action or piece of info on a screen — never decoration.
2. **Flat over floating.** Hairline borders (`divider` token), not drop shadows. Shadows read as generic Material-default; borders read as considered.
3. **Warm neutrals, not pure black/white.** Backgrounds are paper-toned, text is near-black, never `#000000`/`#FFFFFF` pure values.
4. **Calm urgency.** Deadlines, low scores, and overdue reviews are communicated with muted amber/terracotta, never saturated red or exclamation-heavy copy.
5. **Every state is designed.** Empty, loading, error, and offline states are first-class screens, not afterthoughts.
6. **Restraint over template.** No default Material purple, no card-grid-with-shadow dashboard look, no mixed icon sets — the most common tells of an AI-generated UI.

---

## 2. Color system

### 2.1 Token table — Light

| Token | Hex | Role |
|---|---|---|
| `background` | `#FAF7F2` | Screen background |
| `surface` | `#FFFFFF` | Cards, sheets, dialogs |
| `surfaceMuted` | `#F1ECE3` | Secondary cards, chips, input fields (unfocused) |
| `surfaceSelected` | `#E4EDE6` | Selected chip/tab/day background (derived from accent) |
| `divider` | `#E7E1D6` | Hairline borders, separators |
| `textPrimary` | `#2B2A28` | Headings, primary body text |
| `textSecondary` | `#7A756C` | Captions, timestamps, placeholder text |
| `textDisabled` | `#C9C2B4` | Disabled labels |
| `accent` | `#7C9A82` (default, user-customizable) | Primary buttons, active states, links, progress fills |
| `onAccent` | `#FFFFFF` | Text/icons on top of accent-filled surfaces |
| `success` | `#7C9A82` | Correct answers, completed states |
| `warning` | `#D9A25B` | Due-soon, low-time-remaining |
| `error` | `#C77B6B` | Validation errors, destructive actions, missed answers |
| `onError` | `#FFFFFF` | Text on error-filled surfaces |

### 2.2 Token table — Dark

| Token | Hex |
|---|---|
| `background` | `#1C1B19` |
| `surface` | `#252420` |
| `surfaceMuted` | `#2E2C27` |
| `surfaceSelected` | `#2E3B31` |
| `divider` | `#3A3833` |
| `textPrimary` | `#EDE9E2` |
| `textSecondary` | `#A39D91` |
| `textDisabled` | `#5C584F` |
| `accent` | `#8FB396` (lightened from light-mode accent for dark contrast) |
| `onAccent` | `#1C1B19` |
| `success` | `#8FB396` |
| `warning` | `#E0B378` |
| `error` | `#D6917F` |
| `onError` | `#1C1B19` |

### 2.3 Subject tag palette (auto-assigned, editable)
A fixed set of 8 low-saturation pastel hues, cycled in order as subjects are added, each with a light and dark variant so tags stay legible in both themes:
`#B8C9E1` (blue) · `#D9C6E8` (lavender) · `#E8D0B0` (tan) · `#C7DFC2` (mint) · `#E9C3C3` (rose) · `#C9E1DC` (teal) · `#E3D6A8` (ochre) · `#D3CBE3` (violet)

### 2.4 Custom accent rule
When a user picks a custom accent (HSV wheel in Settings):
- Compute contrast ratio of accent against `background` and `surface`.
- If ratio < 4.5:1 (WCAG AA for normal text used on buttons), show inline warning "This color may be hard to read on buttons" — still allow it, but auto-generate `onAccent` as whichever of black/white gives better contrast, rather than always defaulting to white.
- `surfaceSelected` and progress-fill tints are algorithmically derived (20% opacity of accent over `surface`), never hand-picked, so custom accents stay coherent everywhere automatically.

### 2.5 Color usage rules
- Never place two saturated colors adjacent (e.g., warning next to error) — separate with neutral space.
- Destructive actions (delete, reset) always use `error` as text/icon color on a neutral (not filled) background — a filled red button is reserved for nothing in this app; even Reset's final confirm button is outlined, not filled, to avoid a jarring red block.
- Success states use color *and* an icon (check mark), never color alone (color-blind accessibility).

---

## 3. Typography

| Style | Size / Line height | Weight | Letter spacing | Use |
|---|---|---|---|---|
| Display | 28 / 36 | Medium | −0.2sp | Countdown numbers, streak count |
| Heading | 22 / 28 | Medium | −0.2sp | Screen titles |
| Subheading | 18 / 24 | Medium | 0 | Section headers, card titles |
| Body | 15 / 22 | Regular | 0 | Default text |
| BodyStrong | 15 / 22 | Medium | 0 | Emphasized inline text |
| Caption | 13 / 18 | Regular | 0.1sp | Timestamps, hints, metadata |
| Label | 12 / 16 | Medium | 0.4sp, uppercase | Chip labels, tab labels |

Font: **Manrope** (bundled as a variable font asset — not system default, keeps the app visually distinct). Fallback: system sans-serif if font fails to load (defensive, no crash).

Font scaling: all text uses `sp`, respects system font-size accessibility setting up to 130% before layouts start using scrollable containers instead of fixed heights to avoid clipping.

---

## 4. Spacing, grid & shape

- Base unit: **8dp**. Allowed spacing values: 4, 8, 12, 16, 20, 24, 32, 40.
- Screen horizontal padding: **20dp**.
- Card internal padding: **16dp**.
- Vertical rhythm between sections: **24dp**.
- Corner radius: cards/sheets **16dp**, buttons **24dp** (pill), chips **20dp** (pill), input fields **12dp**, small icon containers **10dp**.
- Border: 1dp `divider`, used instead of elevation shadow on all cards.
- Minimum touch target: **48×48dp** for every tappable element, including widget checkboxes.

---

## 5. Iconography & imagery

- Icon set: **Phosphor Icons, "Regular" weight** (1.5dp stroke, line-style) — one set used everywhere, never mixed with filled Material defaults.
- Icon sizes: 20dp (inline/body), 24dp (nav bar, toolbar), 32dp (empty-state feature icons).
- No stock photography, no illustrated mascots — empty states use simple single-color line illustrations built from the same icon set's shapes, keeping the whole app visually self-consistent.
- Subject tag "icon": a filled 8dp dot in the subject's tag color, not an image.

---

## 6. Core components

### 6.1 Buttons
| Variant | Background | Text | Use |
|---|---|---|---|
| Primary | `accent` fill, pill | `onAccent` | One per screen — the single main action |
| Secondary | Transparent, 1dp `divider` border, pill | `textPrimary` | Alternative actions |
| Text | Transparent | `accent` | Low-emphasis actions (Skip, Cancel) |
| Destructive | Transparent, 1dp `error` border, pill | `error` | Delete, Reset |

Height 48dp, horizontal padding 24dp, disabled state = 40% opacity + `textDisabled`.

### 6.2 Cards
- `surface` background, 1dp `divider` border, 16dp radius, 16dp padding.
- Tappable cards get a subtle `surfaceMuted` background shift on press (no ripple color changes, ripple uses `accent` at 12% opacity).

### 6.3 Input fields
- `surfaceMuted` fill, 12dp radius, no visible border until focused.
- Focused: 1.5dp `accent` border replaces fill-only state.
- Error: 1.5dp `error` border + caption below in `error` text — inline, never a toast-only error (see PRD §12).
- Label floats above on focus (Material3 outlined-style behavior, but re-themed to match tokens).

### 6.4 Chips (subject tags, filter chips)
- Unselected: `surfaceMuted` background, `textSecondary` text.
- Selected: `surfaceSelected` background, `accent` text, 1dp `accent` border.
- Subject chips additionally show the 8dp color dot at the leading edge.

### 6.5 Bottom navigation
- `surface` background, 1dp top `divider` border (no shadow).
- 5 items, icon 24dp + Label style text.
- Active: icon + label in `accent`, with a 4dp accent dot above the icon (not a filled pill background — keeps it minimal).
- Inactive: `textSecondary`.

### 6.6 Progress indicators
- Linear (subject completion, quiz progress): 4dp height, `surfaceMuted` track, `accent` fill, fully rounded ends.
- Circular (countdown ring, Pomodoro): 6dp stroke, `surfaceMuted` track, `accent` progress, `warning` when Pomodoro has <20% time left.

### 6.7 Dialogs / bottom sheets
- Confirmations use bottom sheets (thumb-reachable) rather than centered dialogs, except the two-step Reset confirmation, which uses a centered dialog to force deliberate attention.
- Sheet: `surface` background, 20dp top radius, drag handle bar in `divider`.

### 6.8 Snackbars / toasts
- `textPrimary`-colored background (inverted), `background`-colored text, single action max, 12dp radius, appears above bottom nav — reserved for confirmations (e.g., "Export saved"), never for validation errors (those are always inline).

---

## 7. Motion

| Transition | Duration | Easing |
|---|---|---|
| Screen push (forward flow) | 220ms | Standard ease-out, horizontal slide 100%→0 with fade |
| Screen pop (back) | 180ms | Reverse of above |
| Tab switch (bottom nav) | 150ms | Fade-through, no slide |
| Card press | 100ms | Scale 1.0 → 0.98 |
| Flashcard flip | 300ms | 3D rotateY, ease-in-out |
| Dialog/sheet appear | 200ms | Slide-up + fade, decelerate |
| Progress fill animation | 400ms | Ease-out, animates on value change only, not on every recomposition |

No bounce/spring/overshoot curves anywhere — keeps the app feeling calm rather than playful.

---

## 8. Screen layout specs (key screens)

### 8.1 Home
```
[Greeting text]                [🔥 Streak count]
┌─────────────────────────────────────┐
│  Countdown card (if goal set)        │
│  "12 days to Physics Final"          │
│  ●●●●●●●●○○  progress ring, right    │
└─────────────────────────────────────┘
Today's Plan
┌─────────────────────────────────────┐
│ ● Kinematics            [subject tag]│
│ ● Thermodynamics                     │
│ ● Waves                              │
└─────────────────────────────────────┘
[Due for review: 8 flashcards →]  (only if >0)
```
Empty state (no subjects): centered icon + "Add your first subject to get started" + primary button.

### 8.2 Subject list
Vertical list of cards, each: tag dot + name (Subheading) + "N topics, X% complete" (Caption) + thin progress bar. FAB bottom-right, `accent` fill, "+" icon.

### 8.3 Topic detail
Top: topic name (Heading) + subject tag chip. Tab row (Notes | Flashcards | Quiz) using the Label style, active tab underlined in `accent` (2dp), not a filled pill.

### 8.4 Quiz session
Progress bar at top (question N of total). Question text (Subheading). Options as full-width tappable cards, stacked, 12dp gap. On answer: selected option border becomes `success` or `error` with a small check/x icon leading — background stays neutral, only the border and icon carry the color, keeping it calm.

### 8.5 Flashcard review
Centered card, 4:3 ratio, tap to flip (front → back). Below: 4 buttons in a row — Again (`error` outline) / Hard (`warning` outline) / Good (`accent` outline) / Easy (`accent` fill) — sized equally, 8dp gaps.

### 8.6 Pomodoro timer
Large circular progress ring centered, `Display`-style time text inside. Below: Start/Pause primary button + Reset text button. When running, a persistent status row appears: "Notification active" with a small ongoing-notification icon, reinforcing that the always-on notification is tied to this session.

### 8.7 Settings
Grouped list (Theme / Notifications / Widgets / Data / About), each group a `surfaceMuted` card containing rows separated by 1dp `divider`. Toggles use `accent` for the on-state track, never default green.

---

## 9. Dark mode rules

- Never pure black background — `#1C1B19` keeps the warm-neutral character.
- Accent is lightened (not just reused) in dark mode for sufficient contrast against dark surfaces.
- Elevation is communicated by a slightly lighter `surface` tone layered on `background`, consistent with the border-only philosophy (no added shadows in dark mode either).
- Images/illustrations (empty-state line art) invert stroke color to `textSecondary` dark-mode value, never left as a hardcoded dark stroke on a dark background.

---

## 10. Accessibility checklist

- All text/background combinations meet **WCAG AA** (4.5:1 normal text, 3:1 large text) — verified for both fixed tokens and the custom-accent derivation logic (§2.4).
- Every interactive element has a TalkBack content description (icons-only buttons especially: e.g., FAB describes "Add subject", not just "Button").
- Color is never the only signal (see §2.5 success/error icon rule).
- Touch targets ≥48×48dp everywhere, including widget tap zones.
- Respects system font scaling up to 130% without clipping (§3).
- Respects system "reduce motion" setting — when enabled, all transitions in §7 collapse to a simple 100ms fade, no slides/flips/scales.
- Focus order in forms follows visual top-to-bottom order; no orphaned focus traps in dialogs.

---

## 11. Widget design (Glance)

### Widget A — Exam Countdown
- Background: `surface` token (reads current theme), 16dp radius, 1dp `divider` border — matches in-app card styling exactly.
- Layout: exam name (Caption, `textSecondary`) top-left, days-remaining number (Display, `accent`) below, thin circular progress ring top-right.
- Tap target: entire widget → deep link to `ExamCountdownDetail`.

### Widget B — Today's Plan
- Same card styling as Widget A.
- Up to 3 rows: color dot + topic name (Body) + tap-to-complete checkbox (24dp, `accent` when checked).
- Empty state row: "Nothing scheduled — tap to plan" (Caption, `textSecondary`), whole widget tappable → opens Planner.

Both widgets re-render using the same token values as the in-app theme at update time, so a custom accent color or dark mode is reflected on the home screen widget without a separate design pass.

---

## 12. What this deliberately avoids (anti-patterns)

- ❌ Default Material purple/indigo (`#6750A4`) anywhere
- ❌ Drop-shadow card grids
- ❌ Mixed icon styles (filled + outline in the same screen)
- ❌ Saturated red for anything non-destructive
- ❌ Toast-only form validation
- ❌ Generic "lorem ipsum"-feeling empty states with no action
- ❌ Bouncy/spring motion curves
- ❌ Pure `#000000`/`#FFFFFF` as primary text/background colors
