---
name: Technical Blueprint
colors:
  surface: '#f8f9ff'
  surface-dim: '#cbdbf5'
  surface-bright: '#f8f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#eff4ff'
  surface-container: '#e5eeff'
  surface-container-high: '#dce9ff'
  surface-container-highest: '#d3e4fe'
  on-surface: '#0b1c30'
  on-surface-variant: '#434655'
  inverse-surface: '#213145'
  inverse-on-surface: '#eaf1ff'
  outline: '#737686'
  outline-variant: '#c3c6d7'
  surface-tint: '#0053db'
  primary: '#004ac6'
  on-primary: '#ffffff'
  primary-container: '#2563eb'
  on-primary-container: '#eeefff'
  inverse-primary: '#b4c5ff'
  secondary: '#3755c3'
  on-secondary: '#ffffff'
  secondary-container: '#708cfd'
  on-secondary-container: '#00217a'
  tertiary: '#4e565d'
  on-tertiary: '#ffffff'
  tertiary-container: '#676e76'
  on-tertiary-container: '#eaf1fa'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#dbe1ff'
  primary-fixed-dim: '#b4c5ff'
  on-primary-fixed: '#00174b'
  on-primary-fixed-variant: '#003ea8'
  secondary-fixed: '#dde1ff'
  secondary-fixed-dim: '#b8c4ff'
  on-secondary-fixed: '#001453'
  on-secondary-fixed-variant: '#173bab'
  tertiary-fixed: '#dce3ec'
  tertiary-fixed-dim: '#c0c7d0'
  on-tertiary-fixed: '#151c23'
  on-tertiary-fixed-variant: '#40484f'
  background: '#f8f9ff'
  on-background: '#0b1c30'
  surface-variant: '#d3e4fe'
typography:
  headline-xl:
    fontFamily: Space Grotesk
    fontSize: 48px
    fontWeight: '700'
    lineHeight: '1.1'
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Space Grotesk
    fontSize: 24px
    fontWeight: '600'
    lineHeight: '1.2'
    letterSpacing: 0.05em
  body-md:
    fontFamily: Space Grotesk
    fontSize: 16px
    fontWeight: '400'
    lineHeight: '1.6'
    letterSpacing: 0em
  label-sm:
    fontFamily: Space Grotesk
    fontSize: 12px
    fontWeight: '500'
    lineHeight: '1'
    letterSpacing: 0.1em
  mono-code:
    fontFamily: Space Grotesk
    fontSize: 14px
    fontWeight: '400'
    lineHeight: '1.5'
    letterSpacing: -0.01em
spacing:
  unit: 4px
  gutter: 24px
  margin: 48px
  grid_size: 32px
---

## Brand & Style

This design system is inspired by the meticulous clarity of mid-century architectural blueprints and vintage aerospace manuals. It targets engineers, researchers, and power users who value precision and technical transparency. The personality is "Intellectual Utility"—it feels like a living document, balancing the raw logic of a terminal with the sophisticated tactile quality of heavy-stock paper. 

The aesthetic sits at the intersection of **Minimalism** and **Brutalism**, utilizing a structural grid as the primary decorative element. It evokes a sense of "work-in-progress" excellence, where every line and label serves a functional purpose.

## Colors

The palette is anchored by "Architectural Blue" (#2563eb), used for primary actions, structural lines, and data highlights. The background is not a flat white, but a "Paper White" (#f8fafc) with a subtle grain texture to reduce eye strain and provide a tactile feel.

Grid lines are rendered in a faint, cool grey-blue to create a systematic structure without competing with content. Status indicators should follow technical conventions: cyan for processing, deep blue for stable states, and a restrained red for errors, ensuring they feel integrated into the blueprint aesthetic rather than detached from it.

## Typography

This design system exclusively uses **Space Grotesk** to maintain a rigorous, monospaced feel while providing the legibility of a modern sans-serif. 

Headlines and labels should lean heavily into uppercase styling with increased letter spacing to mimic technical drafting annotations. Body text remains mixed-case for readability but retains the geometric "machine" quality of the typeface. For data-heavy sections or terminal outputs, the weight should be kept consistent to ensure vertical alignment of characters, reinforcing the "terminal soul" of the agent.

## Layout & Spacing

The layout is governed by a **fixed 12-column grid** that is visually represented by background lines. Content should align strictly to these grid intersections. 

The spacing rhythm is mathematical, based on a 4px baseline unit. Margins are generous, creating a "technical drawing" feel where the content is framed by whitespace. Elements should feel like they are "plotted" onto the page rather than floating. Horizontal rules and vertical dividers should be used to compartmentalize different functional zones (e.g., input area vs. output log).

## Elevation & Depth

Depth is achieved through **structural layering and low-contrast outlines** rather than traditional drop shadows. 

1.  **Base Layer:** The paper background with the blue-ish grid.
2.  **Inset Panels:** Created using a 1px solid blue border or a very subtle inner shadow to simulate "stamped" or "carved" sections of a manual.
3.  **Active Elements:** Use a "Blue Glow" (box-shadow: 0 0 12px rgba(37, 99, 235, 0.2)) to indicate focus or activity, mimicking the phosphorescence of old specialized monitors or backlit drafting tables.
4.  **Floating Elements:** Should use a "Paper Stack" effect—a thin border with a 2px offset solid blue shadow to look like a sheet of paper placed on top of another.

## Shapes

To maintain the precision of a blueprint, the design system utilizes **Sharp (0px)** corners for all primary containers, buttons, and input fields. 

Roundedness is only permitted for small "indicator dots" (like status lights) to provide a visual contrast against the rigid, rectangular layout. Any "chips" or tags should be rectangular with thin borders, resembling physical labels or tape used on technical equipment.

## Components

-   **Buttons:** Rectangular with a 1px blue border. On hover, the background fills with a faint blue tint (#eff6ff) and the text glows slightly. Primary buttons use a solid blue background with white uppercase text.
-   **Input Fields:** Single lines or boxed areas defined by grid intersections. Use a blinking "pipe" cursor (|) to maintain the terminal aesthetic.
-   **Cards/Panels:** Defined by a header row with a label in the top-left and an optional "ref no." in the top-right. The content area is separated by a 1px horizontal line.
-   **Lists:** Use technical bullet points (e.g., `[01]`, `[02]`) or small blue squares.
-   **Chips:** Tiny, uppercase labels enclosed in a 1px box.
-   **Terminal Output:** Contained within an inset panel. Use a slightly darker "Paper" tint for the background of these areas to distinguish "active work" from "documentation."
-   **Grid Indicators:** Small "+" symbols at the intersections of major grid lines to enhance the drafting aesthetic.