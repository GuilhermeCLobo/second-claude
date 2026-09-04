# Favorites / saved listings

## Problem Statement

A buyer interested in a Listing but not ready to buy has no way to keep track of it — they'd have to remember it or bookmark the URL externally, and risk losing it in the flow of Browse.

## Solution

Let a logged-in User favorite any Listing, regardless of status or ownership, as a personal bookmark, and view their favorited listings in a dedicated list.

## User Stories

1. As a logged-in User, I want to favorite a Listing, so that I can find it again later without searching.
2. As a logged-in User, I want to unfavorite a Listing, so that I can remove items I'm no longer interested in.
3. As a logged-in User, I want to see which listings I've already favorited while browsing, so that I don't lose track of my state.
4. As a logged-in User, I want to favorite my own listing if I want to, so that the feature doesn't impose an arbitrary restriction.
5. As a logged-in User, I want to favorite a listing that has since sold, so that I can keep a personal record of it even though I can no longer buy it.
6. As a logged-in User, I want a dedicated "My Favorites" view listing everything I've favorited, so that I can revisit them in one place.
7. As a logged-in User, I want a favorited listing to disappear from my favorites automatically if its owner deletes it, so that I'm never looking at a broken reference.
8. As an anonymous visitor, I want favoriting to be unavailable (or to prompt me to log in), so that favorites are always tied to a real account.
9. As a logged-in User, I want favoriting/unfavoriting to feel instant, so that browsing isn't interrupted.

## Implementation Decisions

- New `Favorite` entity: id, `userId`, `listingId`, `createdAt`; unique constraint on `(userId, listingId)`.
- New endpoints to add and remove a favorite on a given listing, mirroring the existing REST style used for buying and deleting listings (a POST-style action to add, a DELETE to remove).
- New endpoint mirroring the existing "my posted"/"my bought" pattern, returning the current User's favorited listings.
- Listing responses gain a boolean indicating whether the current requester has favorited that listing, so the frontend can render favorited state without a separate lookup.
- Deleting a `Listing` cascades to delete its `Favorite` rows.

## Testing Decisions

- Backend: new API test following the existing `*ApiTest` seam covering: add favorite; duplicate add handled cleanly; remove favorite; favoriting your own listing succeeds; favoriting a `SOLD` listing succeeds; deleting a listing removes its favorites; the "my favorites" endpoint returns only the requester's favorites; unauthenticated favorite attempts are rejected.
- Frontend: extend the browse and listing-detail component specs for the favorite toggle, and add a new "my favorites" component spec following the existing "my listings" pattern.

## Out of Scope

- Favorite collections/folders.
- Notifying a User when a favorited listing's price changes or it sells.

## Further Notes

None.
