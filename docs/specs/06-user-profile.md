# User profile page

## Problem Statement

There's no way to see a given seller's other active listings or even identify who posted a listing — listing responses only expose a numeric owner id, so the frontend can't display "posted by X," let alone link to a public view of that User's listings.

## Solution

Add a public profile page at a username-based URL showing that User's username and their currently `ACTIVE` listings, and expose the owner's username on every listing response so it can be displayed and linked to wherever a listing is shown.

## User Stories

1. As any visitor, logged in or not, I want to view a User's public profile by their username, so that I can see who I'm dealing with.
2. As any visitor, I want a User's profile to show their `ACTIVE` listings, so that I can browse everything else they currently have for sale.
3. As a buyer viewing a listing's detail page, I want to see the owner's username, so that I know who posted it.
4. As a buyer viewing a listing card or detail page, I want to click through to the owner's profile, so that I can see their other listings.
5. As a visitor navigating to a profile for a username that doesn't exist, I want a clear not-found response.
6. As a User, I want my `SOLD` listings excluded from my public profile, so that only what's currently available is shown to visitors.
7. As a developer, I want listing responses to carry the owner's username directly, so that rendering a listing never needs a separate lookup call.

## Implementation Decisions

- Listing responses gain the owner's username, populated via a join/lookup against the `User` table.
- New public, unauthenticated endpoint returning a User's public info (username) and their `ACTIVE` listings, keyed by username; not-found if the username doesn't exist.
- Frontend: new profile route keyed by username, linked from listing cards and detail pages via the new owner-username field.
- Deliberately designed to be extended later (Feature 9) with an average rating and count once ratings exist — no rating field added yet.

## Testing Decisions

- Backend: new API test following the existing `*ApiTest` seam covering: an existing username returns a profile with only that User's `ACTIVE` listings (`SOLD` excluded); a nonexistent username returns not-found; no auth header is required; listing responses on Browse/detail include the correct owner username.
- Frontend: new profile component spec following existing component-spec conventions; extend the browse and listing-detail specs to assert the owner-username link renders.

## Out of Scope

- Rating/reputation display (deferred to Feature 9).
- Editable profile fields (bio, avatar, display name distinct from username).
- Private profile info — the profile is public and minimal.

## Further Notes

This is the feature that establishes the owner-username field on listing responses, which Feature 7 (Messaging) and Feature 9 (Ratings) are also expected to rely on for displaying who's on the other side of a conversation or rating.
