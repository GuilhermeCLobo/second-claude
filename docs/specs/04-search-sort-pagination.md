# Search, sorting & pagination on Browse

## Problem Statement

Browse today returns every Listing — including `SOLD` ones, which is an existing gap — as a single flat, unordered list with only a category filter. As the number of listings grows, buyers have no way to find what they want, order results meaningfully, or avoid loading everything at once.

## Solution

Extend Browse's query capabilities: free-text search across title and description, an optional price range filter alongside the existing category filter, sorting by newest or price, and numbered pagination — while also fixing Browse to exclude `SOLD` listings by default.

## User Stories

1. As a buyer, I want Browse to show only `ACTIVE` listings by default, so that I don't waste time on items I can no longer buy.
2. As a buyer, I want to search listings by keyword, so that I can quickly find items matching what I'm looking for.
3. As a buyer searching, I want my keyword matched against both title and description, so that relevant listings aren't missed just because the match is in the description.
4. As a buyer searching, I want the match to be case-insensitive.
5. As a buyer, I want to filter by a minimum price, so that I can exclude items below my budget floor.
6. As a buyer, I want to filter by a maximum price, so that I can exclude items above my budget.
7. As a buyer, I want to combine search, category, and price filters together, so that I can narrow results precisely.
8. As a buyer, I want to sort results by newest first, so that I can see what's just been posted.
9. As a buyer, I want to sort results by price low to high.
10. As a buyer, I want to sort results by price high to low.
11. As a buyer, I want results paginated in manageable pages (12 by default), so that the page loads quickly.
12. As a buyer, I want to request a different page size, up to a cap of 48.
13. As a buyer, I want the total result count available, so that I know how many pages of results there are.
14. As a developer, I want "newest" sorting backed by a real timestamp rather than inferred from the auto-increment id, so that the sort is semantically correct and reusable elsewhere (e.g. "posted 3 days ago" copy).

## Implementation Decisions

- `Listing` gains a `createdAt` timestamp, set at creation and immutable.
- The browse query is reworked to accept, all optional except pagination defaults: category (existing), free-text search, minimum price, maximum price, sort (newest / price ascending / price descending — default newest), page, and page size (default 12, max 48).
- Default status filter is `ACTIVE` only; Browse has no way to opt into seeing `SOLD` listings — My Listings' "bought" view remains the place to see what a User has purchased.
- Search is a case-insensitive substring match against title and description — no full-text search engine or relevance ranking.
- Filter/sort/pagination composed via the ORM's query-composition facilities rather than hand-rolled SQL, so filters combine cleanly.
- The browse response shape changes from a bare list to a paginated envelope (items plus total count), a breaking change to the current endpoint contract — the frontend browse component and its data-fetching service are updated to match.

## Testing Decisions

- Backend: extend the existing listing `*ApiTest` seam covering: `SOLD` listings excluded by default; search matches on title-only, description-only, and neither; price range filters (min only, max only, both, neither); each sort option produces correctly ordered results; pagination returns the correct page contents and total count; page size respects the cap.
- Frontend: extend the browse component spec covering: search input triggers a re-query with the right params; sort/price controls update query params; pagination controls request the right page.

## Out of Scope

- Relevance-ranked or fuzzy search.
- Infinite scroll — numbered pagination now; can be layered on the same API later without a contract change.
- Saved searches or search history.

## Further Notes

None.
