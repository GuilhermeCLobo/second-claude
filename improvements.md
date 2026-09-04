# Improvements

Ideas for where to take the marketplace next. Not a commitment or a sequenced roadmap — pick off whatever's interesting.

## New features

- **Edit a listing** — CONTEXT.md calls this out of scope today; owners can only delete an `ACTIVE` listing, not correct a typo'd price or description.
- **Search and filters on Browse** — text search plus filtering by `Category` and price range. Right now browsing is presumably just a flat list.
- **Sorting** — newest first, price low-to-high/high-to-low.
- **Pagination or infinite scroll** — once there's more than a page of listings.
- **Multiple photos per listing** — currently a single required photo (`MissingPhotoException`); a gallery would match typical marketplace UX.
- **User profile page** — a public view of a user's active listings (separate from the private "My Listings" buy/sell view).
- **In-app messaging between buyer and seller** — ask a question about a listing before buying.
- **Favorites / saved listings** — let a user bookmark listings without buying.
- **Email or in-app notifications** — e.g. notify a seller when their listing sells.
- **Password reset flow** — auth currently only covers register/login.
- **Rate the other party after a sale** — lightweight trust signal since there's no separate Order/Transaction entity to hang this off of.

## Frontend styling

The Angular app currently has almost no styling — `app.component.css` is empty and the global `styles.css` is a single line. Everything is unstyled HTML.

- **Establish a design system**: pick a color palette, spacing scale, and typography, and apply them as CSS custom properties in the global stylesheet so every component shares them.
- **Style the core flows first**: browse listings (grid/card layout), listing detail, create-listing form, login/register forms, my-listings.
- **Responsive layout** — verify the listing grid and forms work on mobile widths, not just desktop.
- **Loading and empty states** — spinners/skeletons while listings load, a friendly empty state for "no listings yet" or "no results".
- **Form validation feedback** — visible inline errors (e.g. missing photo, price format) instead of relying on default browser validation or console errors.
- **Consistent buttons, inputs, and cards** — extract shared styles/classes instead of repeating rules per component.
- **Dark mode** — nice-to-have once the base palette exists, via `prefers-color-scheme` or a toggle.

## Suggested first steps

1. Pick a small set of design tokens (colors, spacing, font) and drop them into `styles.css`.
2. Style the browse-listings grid and listing-detail page — these are the pages a user sees most.
3. Add search/filter to Browse, since it's the highest-value feature gap.
4. Add listing editing once the UI patterns from #2 exist to reuse.
