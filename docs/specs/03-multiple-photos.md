# Multiple photos per listing

## Problem Statement

A Listing today supports exactly one required photo. Buyers can't see an item from multiple angles or inspect close-ups of condition/flaws, which falls short of typical marketplace UX and limits how much a buyer can trust a listing's accuracy.

## Solution

Allow a Listing to have one to six ordered photos, with the first acting as the cover photo shown in Browse. Photo management (add, remove, reorder) is handled through dedicated endpoints, independent of the text-field edit flow from Feature 2.

## User Stories

1. As a Listing owner creating a new listing, I want to upload between 1 and 6 photos, so that buyers can see the item from multiple angles.
2. As a Listing owner, I want the first photo in my chosen order to act as the cover photo shown in Browse, so that I control the primary impression of my listing.
3. As a Listing owner with an `ACTIVE` listing, I want to add a new photo without resubmitting the others, so that updating photos is quick.
4. As a Listing owner, I want to remove a single photo from my listing, so that I can drop a bad or outdated one.
5. As a Listing owner, I want to be prevented from removing my last remaining photo, so that a listing can never end up with zero photos.
6. As a Listing owner, I want to be prevented from adding a 7th photo, so that listings stay within a manageable, consistent size.
7. As a Listing owner, I want to reorder my photos, so that I can change the cover photo or present them more logically.
8. As a Listing owner, I want photo management blocked once my listing is `SOLD`, consistent with text-field edits.
9. As a non-owner, I want to be prevented from adding, removing, or reordering another User's listing photos.
10. As a buyer browsing listings, I want to see each listing's cover photo in the Browse grid.
11. As a buyer viewing a listing's detail page, I want to see all of its photos in order.
12. As a Listing owner creating a listing, I want the "at least one photo required" rule enforced at creation, consistent with today's behavior.

## Implementation Decisions

- New `Photo` entity: id, `listingId` (FK), storage reference (reusing the existing photo storage mechanism), `sortOrder`, `createdAt`.
- `Listing`'s single photo-reference column is replaced by a one-to-many relationship to `Photo`, ordered by `sortOrder`. Existing single-photo values migrate to the first `Photo` row per listing.
- Listing responses change from a single photo reference to an ordered list of photo references; the first entry is the cover photo.
- New endpoints, separate from the listing edit endpoint, each enforcing owner + `ACTIVE`-only:
  - Add one photo (multipart) — appended at the end of the current order; rejected if already at 6.
  - Remove one photo by id — rejected if it's the only remaining photo.
  - Reorder — accepts the full ordered list of the listing's photo ids; rejected if the id set doesn't match the listing's actual photos.
- Listing creation is unchanged: still requires exactly one photo at that point, persisted as the first `Photo` row; additional photos are added afterward via the add-photo endpoint (the frontend can chain add-photo calls to offer a multi-photo upload experience at creation time).

## Testing Decisions

- Backend: new API test following the existing `*ApiTest` seam covering: add photo succeeds and appends; add photo at the 6-photo cap is rejected; remove photo succeeds; removing the last photo is rejected; reorder succeeds and subsequent Browse/detail responses reflect the new cover photo; non-owner is rejected on each action; actions on a `SOLD` listing are rejected.
- Frontend: extend the create-listing and listing-detail component specs (or add a dedicated photo-manager component spec) to cover multi-photo upload, display, and reorder interactions.

## Out of Scope

- Editing an individual photo's content (crop/rotate) — replace-only via remove-then-add.
- Per-photo captions or alt text.
- Any specific reordering UI mechanism (drag-and-drop, up/down buttons, etc. all satisfy the requirement).

## Further Notes

Two deliberate trade-offs were made here that are hard to reverse and non-obvious to a future reader: the 6-photo cap, and dedicated add/remove/reorder endpoints instead of folding photo changes into Feature 2's full-replace edit. Worth an ADR if you want the reasoning on record.
