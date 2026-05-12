---
name: Retro-Tech AI Agent
colors:
  surface: '#071610'
  surface-dim: '#071610'
  surface-bright: '#2c3d36'
  surface-container-lowest: '#03110b'
  surface-container-low: '#0f1f18'
  surface-container: '#13231c'
  surface-container-high: '#1d2d27'
  surface-container-highest: '#283831'
  on-surface: '#d4e7dd'
  on-surface-variant: '#bbc9c7'
  inverse-surface: '#d4e7dd'
  inverse-on-surface: '#24342d'
  outline: '#869491'
  outline-variant: '#3c4947'
  surface-tint: '#5adace'
  primary: '#6feee1'
  on-primary: '#003733'
  primary-container: '#4fd1c5'
  on-primary-container: '#005750'
  inverse-primary: '#006a63'
  secondary: '#abcec5'
  on-secondary: '#153630'
  secondary-container: '#2d4d46'
  on-secondary-container: '#9abcb4'
  tertiary: '#ffceca'
  on-tertiary: '#68000a'
  tertiary-container: '#ffa7a0'
  on-tertiary-container: '#9e0015'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#79f7ea'
  primary-fixed-dim: '#5adace'
  on-primary-fixed: '#00201d'
  on-primary-fixed-variant: '#00504a'
  secondary-fixed: '#c7eae1'
  secondary-fixed-dim: '#abcec5'
  on-secondary-fixed: '#00201b'
  on-secondary-fixed-variant: '#2d4d46'
  tertiary-fixed: '#ffdad7'
  tertiary-fixed-dim: '#ffb3ad'
  on-tertiary-fixed: '#410004'
  on-tertiary-fixed-variant: '#930013'
  background: '#071610'
  on-background: '#d4e7dd'
  surface-variant: '#283831'
typography:
  headline-lg:
    fontFamily: Space Grotesk
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Space Grotesk
    fontSize: 18px
    fontWeight: '500'
    lineHeight: 24px
    letterSpacing: 0em
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
    letterSpacing: 0.01em
  body-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 18px
    letterSpacing: 0.01em
  label-caps:
    fontFamily: Space Grotesk
    fontSize: 11px
    fontWeight: '700'
    lineHeight: 16px
    letterSpacing: 0.1em
  code:
    fontFamily: Monospace
    fontSize: 13px
    fontWeight: '400'
    lineHeight: 20px
    letterSpacing: 0em
spacing:
  unit: 4px
  gutter: 16px
  margin: 24px
  container-padding: 12px
---

## Brand & Style

This design system is built on a "Retro-Futuristic Terminal" aesthetic, bridging the gap between high-end legacy mainframes and cutting-edge artificial intelligence. It targets power users, developers, and researchers who appreciate information density and technical precision.

The style is a hybrid of **Minimalism** and **Modern Brutalism**, characterized by:
- **Information Density:** Prioritizing data visibility over decorative whitespace.
- **Technical Rigor:** Every element serves a functional purpose, utilizing thin strokes and grid-aligned layouts.
- **Atmospheric Glow:** A subtle "screen emission" effect on critical data and active states, mimicking CRT phosphors.
- **Structured Chaos:** A layout that feels like a multi-window command center, balanced by strict structural alignment.

## Colors

The palette is strictly monochromatic-adjacent, utilizing varying depths of forest green and cyan to create a layered, nocturnal environment.

- **Primary:** The "Phosphor" color (#4fd1c5). Used for text, primary borders, and active status indicators.
- **Background:** The "Deep Void" (#0a1a14). A rich, dark green-black that provides high contrast for mint-green text.
- **Muted/Surface:** A secondary deep green (#1a3a34) used for container backgrounds and subtle UI separators.
- **Alert:** A sharp, technical red (#e53e3e) reserved for system errors or destructive actions, maintaining the terminal-style emergency aesthetic.

**Glow Effect:** Primary text and icons should feature a subtle `drop-shadow` or `text-shadow` in the primary color with low opacity (15-25%) to simulate light bleed.

## Typography

The typography system relies on a high-contrast pairing between technical Sans-Serifs and Monospace fonts.

- **Headlines & Labels:** Space Grotesk provides a geometric, futuristic feel. Labels should frequently use uppercase with increased tracking to evoke blueprint annotations.
- **Body & Content:** Inter is used for its exceptional readability at small sizes, ensuring that dense logs and AI responses remain legible.
- **Technical Data:** Use a standard Monospace system font for all code blocks, logs, and metadata strings to reinforce the "terminal" metaphor.

Text hierarchy is established primarily through weight and opacity rather than massive scale shifts. Secondary information should be rendered at 60-70% opacity.

## Layout & Spacing

This design system employs a **Fixed Grid** philosophy rooted in a 4px base unit. 

- **The Mainframe Layout:** The screen is divided into distinct, paneled regions (Sidebar, Navigation, Primary Workspace, Inspector).
- **Grid Background:** A subtle, repeating 32px grid pattern should be visible in the background layer at 5% opacity to help the eye align elements.
- **Density:** Elements are tightly packed to simulate a dashboard where "all info is at a glance." Margins are kept consistent at 24px, while internal container gutters are 16px.
- **Alignment:** Every panel edge must align to the underlying grid. No floating elements; everything is anchored to a container.

## Elevation & Depth

Depth is conveyed through **Low-contrast Outlines** and **Tonal Layers** rather than shadows.

- **Stacking:** The background is the lowest level. Containers sit "above" it, defined by thin 1px borders of the primary color at 20-30% opacity.
- **Active State:** Focus or "Elevation" is shown by increasing the border opacity to 100% and adding the signature phosphor glow.
- **Glassmorphism:** Use very slight backdrop blurs (4px-8px) on overlays or floating modals to keep the underlying grid faintly visible, suggesting a HUD (Heads-Up Display) overlay.
- **Separator Lines:** Use horizontal and vertical 1px lines to subdivide content within panels, maintaining the structural rigidity of a terminal.

## Shapes

The shape language is **Sharp (0px)**. 

To maintain the retro-tech, high-precision aesthetic, rounded corners are strictly avoided. Every button, input, and container is a perfect rectangle. This reinforces the "engineered" feel of the interface. 

Where visual interest is needed, use "clipped corners" (dog-eared corners) at a 45-degree angle for specialized tags or primary action buttons to evoke a futuristic military or industrial aesthetic.

## Components

- **Containers:** All containers feature a header bar with a label on the left and optional status indicators (e.g., [ LIVE ], [ IDLE ]) on the right. The header is separated from the content by a 1px line.
- **Buttons:** Simple 1px outlines. On hover, the button fills with the primary color, and the text inverts to the background color. Active buttons should have a faint outer glow.
- **Input Fields:** Styled as a "Command Line." No background fill; just a bottom border or a full 1px border. Always include a blinking block cursor custom component at the end of active text strings.
- **Chips/Status Tags:** Rectangular boxes with small font sizes. Use high-contrast backgrounds (Primary color) for high-priority status and outlined boxes for secondary metadata.
- **Scrollbars:** Custom-styled to be ultra-thin (4px) with no track background, only a primary-color thumb.
- **Icons:** Use thin-stroke (1px) SVG icons. Avoid filled icons unless they are being used as active-state indicators.