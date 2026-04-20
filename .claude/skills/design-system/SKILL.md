# KOmpass Design System Skill

Read this BEFORE creating any UI composable, screen, or component. Follow these rules exactly.

## Design Philosophy

KOmpass is a premium travel guide. The UI must feel like a curated travel magazine — warm, confident, photo-forward, effortlessly navigable. The user is a tourist, often tired, in bright sunlight, on a slow connection. Every design decision serves that context.

**Inspirations:** Airbnb (card layouts, photo treatment, warmth), Spotify (bold typography hierarchy, confident spacing), Bolt (dead-simple navigation, zero cognitive load).

## Absolute Rules

- NO EMOJIS — anywhere, ever, in any composable
- NO placeholder illustrations or clipart
- NO generic Material 3 default styling — always override with KOmpass tokens
- Photos are the primary visual element — they do the storytelling
- Every tap target minimum 48dp
- Maximum 2 font weights visible on any single screen (regular + bold)
- No more than 3 distinct text sizes on any single screen

## Platform Adaptation

KOmpass uses Compose Multiplatform with adaptive platform feel:

**Android:** Material 3 components with KOmpass theming overrides. Standard top app bar, M3 bottom navigation bar, M3 cards and surfaces. Back handled via system gesture.

**iOS:** Expect/actual for navigation chrome. iOS-style large title collapsing headers, native swipe-back gesture, iOS bottom tab bar feel (no labels on inactive, slightly thinner). Status bar light content on dark headers, dark content on light surfaces.

**Shared across both:** Color tokens, typography scale, spacing system, card styles, content layout patterns, photo treatment.

## Color Palette

Single brand theme (light base). No dark mode for MVP.

```
// Primary brand — deep navy, conveys trust and sophistication
brand_900 = #102A43   // Text primary, nav backgrounds
brand_800 = #243B53   // Text secondary emphasis
brand_700 = #334E68   // Active states
brand_600 = #486581   // Icons primary
brand_500 = #627D98   // Text secondary
brand_400 = #829AB1   // Placeholder text, disabled
brand_300 = #9FB3C8   // Borders, dividers
brand_200 = #BCCCDC   // Subtle borders
brand_100 = #D9E2EC   // Card borders, input borders
brand_50  = #F0F4F8   // Surface tinted backgrounds

// Accent — warm amber, draws attention without screaming
accent_600 = #D97706
accent_500 = #F59E0B   // Primary CTA, highlights
accent_400 = #FBBF24   // Hover/press states
accent_100 = #FEF3C7   // Accent surface backgrounds

// Surfaces
surface_0 = #FFFFFF     // Cards, modals
surface_1 = #F8F9FB     // Page background
surface_2 = #F1F3F7     // Inset backgrounds, search bars
surface_3 = #E4E7ED     // Pressed states

// Semantic
success   = #059669     // Active badges, available
error     = #DC2626     // Errors, destructive
sponsored = #F59E0B     // Sponsored/experience badges
```

## Typography

Use system font stack with weight differentiation. Do NOT import custom fonts for MVP — use the platform default (Roboto on Android, SF Pro on iOS) for maximum readability and performance.

```
// Scale (dp)
display    = 32sp, Bold (700)     // Screen titles, hero text
headline   = 24sp, Bold (700)     // Section headers
title      = 20sp, SemiBold (600) // Card titles, modal headers
body_large = 16sp, Regular (400)  // Descriptions, long text
body       = 14sp, Regular (400)  // Default body text, list items
caption    = 12sp, Medium (500)   // Labels, badges, metadata
overline   = 10sp, SemiBold (600) // Category labels, uppercase tags

// Line heights
display/headline: 1.2
title: 1.3
body_large/body: 1.5
caption/overline: 1.4

// Letter spacing
overline: +0.8sp (uppercase tracking)
All others: default
```

## Spacing System

8dp base grid. All spacing uses multiples of 4dp.

```
xxs  = 4dp     // Tight internal padding (badge padding, tag gaps)
xs   = 8dp     // Between related elements (icon + text)
sm   = 12dp    // List item internal padding
md   = 16dp    // Standard content padding, card padding
lg   = 24dp    // Section gaps, card margins
xl   = 32dp    // Major section separators
xxl  = 48dp    // Screen top/bottom breathing room

// Screen horizontal padding: 16dp (phone), 24dp (tablet)
// Card internal padding: 16dp
// Between cards in a list: 12dp
// Bottom nav height: 64dp (Android), 83dp (iOS with home indicator)
```

## Corner Radius

```
sm   = 8dp     // Buttons, tags, badges, chips
md   = 12dp    // Cards, inputs, search bars
lg   = 16dp    // Bottom sheets, modals, photo thumbnails
xl   = 24dp    // Feature cards, hero cards
full = 999dp   // Circular avatars, round buttons
```

## Elevation & Shadows

Minimal shadow use. Depth communicated through surface color changes, not shadows.

```
card_default:   0dp elevation, border 1dp brand_100
card_elevated:  2dp elevation (only for floating elements like FAB, bottom sheet)
modal:          8dp elevation with scrim overlay
bottom_nav:     0dp elevation, top border 1dp brand_100
```

## Photo Treatment

- All place/event photos: aspect ratio 16:9 for lists, 4:3 for detail headers
- Corner radius on photos: lg (16dp) in cards, 0dp for full-width headers
- Always show a subtle brand_50 placeholder while loading (no skeleton shimmer for MVP)
- Photo overlays: gradient from transparent to brand_900/60 at bottom for text overlay
- Maximum 3 photos visible per card (1 hero + 2 small thumbnails)

## Component Patterns

### Place Card (list item)
- Height: ~100dp
- Layout: photo thumbnail (80x80, rounded lg) | content area | chevron
- Content: name (title size), category + zone (caption, brand_500), price indicator
- Active/inactive badge: small dot indicator, not text badge
- Tap: navigates to detail

### Place Card (featured/home)
- Full-width, 200dp height
- Hero photo fills card, text overlaid at bottom with gradient scrim
- Name (headline, white), category tag (caption, accent badge), zone (caption, white/80)
- Subtle shadow on text for readability over photos

### Event Card
- Horizontal layout: date block (day number + month abbreviation, stacked, accent color) | content
- Content: name (title), venue (body, brand_500), time (caption)
- If upcoming within 7 days: subtle accent_100 background tint

### Category Chip/Tab
- Pill shape (radius full)
- Inactive: surface_2 bg, brand_500 text
- Active: brand_900 bg, white text
- Height: 36dp, horizontal padding 16dp
- Horizontal scrollable row, no wrapping

### Bottom Navigation
- 5 items: Home, Categories, Events, Experiences, Essentials
- Icons only when inactive (brand_400), icon + label when active (brand_900)
- Active indicator: subtle accent_100 pill behind icon (M3 style on Android)
- No badge counts for MVP

### Detail Screen Header
- Photo carousel: full-width, 280dp height, page indicator dots
- Content starts below with standard md padding
- Sticky top bar: transparent initially, fills to surface_0 on scroll with title

### Search Bar
- surface_2 background, rounded md
- Search icon (brand_400) + placeholder text (brand_300)
- Height: 48dp
- Lives at top of category/list screens, not in a toolbar

### Empty State
- Centered vertically
- Single icon (brand_200, 48dp), heading (title, brand_700), body (body, brand_400)
- Optional CTA button below
- No illustrations

## Navigation Flow

```
Bottom Nav
  ├── Home
  │     ├── Nearby section (horizontal scroll of place cards)
  │     ├── Upcoming events (vertical list, max 3)
  │     └── Featured itineraries (horizontal scroll)
  │
  ├── Categories
  │     ├── Category tabs (horizontal scroll)
  │     ├── Sub-filter chips (horizontal scroll below tabs)
  │     └── Place list (vertical, filterable)
  │
  ├── Events
  │     ├── Date filter
  │     └── Event list (vertical)
  │
  ├── Experiences
  │     ├── "Sponsored" label at top
  │     └── Experience cards (vertical)
  │
  └── Essentials
        ├── Category tabs (transport, customs, emergency, tips)
        └── Content cards (expandable)

Detail screens push on top of the nav (full screen):
  Place Detail → photo carousel, info, map preview, local's tip
  Event Detail → photo, time, venue, description, ticket link
  Experience Detail → photos, operator, booking, description
  Itinerary Detail → day-by-day timeline with place references
```

## Animation Guidelines

Minimal, purposeful animation. No gratuitous motion.

- Screen transitions: default platform (slide on iOS, shared element on Android where possible)
- List items: no staggered entrance animation (harms perceived performance)
- Pull to refresh: standard platform behavior
- Bottom sheet: standard spring animation
- Photo carousel: standard pager snap
- The ONLY custom animation: subtle fade on category tab switching content

## Accessibility

- All images: contentDescription from place/event name
- All interactive elements: minimum 48dp touch target
- Color contrast: text on backgrounds must meet WCAG AA (4.5:1 for body, 3:1 for large text)
- Screen reader: announce category changes, loading states
- Support dynamic text sizing (don't use fixed dp for text, always sp)

## Anti-Patterns (NEVER DO)

- Gradient backgrounds on screens (only on photo overlays)
- Rounded corners greater than 24dp on cards
- Animated icons or micro-interactions (too playful for brand)
- Skeleton shimmer loading (use simple brand_50 placeholder)
- Floating action buttons (not appropriate for this app)
- Snackbars for success (use toast only for errors)
- Tabs with more than 5 items
- Horizontal scroll lists with more than 10 items without "See all" link
- Any emoji, emoticon, or decorative unicode character
