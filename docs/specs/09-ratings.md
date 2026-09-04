# Rate the other party after a sale

## Problem Statement

After a sale completes, neither party has any way to signal whether the transaction went well — there's no trust or reputation signal anywhere in the marketplace, which matters more as the user base and transaction volume grow.

## Solution

Let both the buyer and seller of a completed sale each leave one immutable 1-5 star rating, with an optional comment, about the other party, tied to the specific Listing that was sold — and surface the aggregate (average and count) on the rated User's profile page.

## User Stories

1. As a buyer who completed a purchase, I want to rate the seller, 1-5 stars with an optional comment, so that I can share my experience with future buyers.
2. As a seller whose listing sold, I want to rate the buyer, so that I can share my experience with future sellers.
3. As a User who has already rated the other party for a given sale, I want to be prevented from submitting a second rating for that same sale.
4. As a User, I want my submitted rating to be immutable, so that reviews stay trustworthy and can't be altered after the fact.
5. As a User, I want to rate the other party at any point after the sale, with no deadline.
6. As a User viewing another User's profile, I want to see their average rating and total rating count, so that I can gauge their trustworthiness before dealing with them.
7. As a User who hasn't yet rated a completed sale, I want a clear way to submit that rating, e.g. from My Listings' posted/bought views.
8. As a User attempting to rate a sale I wasn't part of, I want to be rejected.
9. As a User attempting to rate a listing that hasn't sold yet, I want to be rejected.
10. As any visitor viewing a User's profile with zero ratings, I want a clear "no ratings yet" state rather than a broken or misleading average.

## Implementation Decisions

- New `Rating` entity: id, `listingId` (FK), rater user id, rated user id, score (1-5), comment (nullable), `createdAt`; unique constraint on `(listingId, raterId)`.
- New endpoint to submit a rating on a sold listing: validates the listing's status is `SOLD`; validates the requester is either the listing's owner or its buyer (the rated party is inferred as the other one); rejects a duplicate rating from the same rater on the same listing.
- The user-profile endpoint (Feature 6) is extended with an average rating (nullable if none) and a rating count, computed by aggregating `Rating` rows for that User.
- No edit or delete endpoint for a `Rating` — immutability is enforced simply by not exposing one.
- Frontend: a "rate this sale" action surfaced on My Listings' posted/bought views for eligible entries (sold, not yet rated by the current User); profile page updated to render the average and count.

## Testing Decisions

- Backend: new API test following the existing `*ApiTest` seam covering: buyer rates seller and vice versa; a duplicate rating from the same rater on the same listing is rejected; rating an `ACTIVE` (not yet sold) listing is rejected; rating by a non-participant is rejected; the profile endpoint reflects the correct average/count after one or more ratings; the profile shows a no-ratings state when the count is zero.
- Frontend: extend the My Listings component spec for the rate-this-sale action, and the user-profile component spec (from Feature 6) for average/count rendering.

## Out of Scope

- Editing or deleting a submitted rating.
- Rating disputes or moderation.
- Weighting or decaying older ratings in the average.
- Reminding a User to rate — could be a future notification trigger, not in Feature 8's initial scope.

## Further Notes

Closes the loop with Feature 6, which explicitly deferred showing reputation data until this feature existed.
