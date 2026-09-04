# Edit a listing

## Problem Statement

Once a Listing is posted, its owner has no way to correct mistakes — a typo'd title, wrong price, wrong category, or an outdated description. The only recourse today is deleting an `ACTIVE` Listing and re-posting from scratch.

## Solution

Let the owning User edit their own `ACTIVE` Listing's title, description, price, and category via a full-replace update that reuses the same validation as creation. A `SOLD` Listing remains locked, consistent with the existing delete restriction.

## User Stories

1. As a Listing owner, I want to edit my `ACTIVE` Listing's title, description, price, and category, so that I can correct mistakes without deleting and re-posting.
2. As a Listing owner, I want to submit all editable fields together, so that the edit form mirrors the create-listing form I already know.
3. As a User who does not own a Listing, I want to be prevented from editing it, so that only the rightful owner can change it.
4. As a Listing owner, I want to be prevented from editing a Listing once it's `SOLD`, so that the record of what was actually sold isn't altered after the fact.
5. As a Listing owner, I want validation errors on edit (blank title, non-positive price, etc.) to match creation's rules, so that the experience is consistent.
6. As a Listing owner, I want the edit form pre-filled with my listing's current values, so that I only change what's actually wrong.
7. As a Listing owner submitting an edit, I want my listing's photos to be unaffected by this specific request, so that changing my price doesn't force me to also manage photos.
8. As a buyer browsing listings, I want to see a listing's up-to-date details immediately after the owner edits it.
9. As a Listing owner attempting to edit a listing that's already been deleted, I want a clear not-found error.
10. As a developer, I want the edit endpoint to reuse the create-listing request shape, so that validation logic stays consistent between the two flows.

## Implementation Decisions

- New endpoint accepting a JSON body (not multipart — photos are out of scope here, handled by Feature 3) with the same fields and validation as the create-listing request: title, description, price, category, all required.
- Edit flow: load the Listing; reject if the requester isn't the owner; reject if status isn't `ACTIVE`; update title/description/price/category; save. Mirrors the existing delete flow's owner-then-status check order.
- `Listing` needs a way to update title/description/price/category after construction (currently only settable via the constructor).
- No changes to photo handling in this endpoint.
- Frontend: new edit-listing route/component, reusing the create-listing form's template and validation, pre-populated from a fetch of the listing and submitting the full-replace update.

## Testing Decisions

- Backend: extend the existing listing `*ApiTest` seam (or add a sibling `*ApiTest` for edit) covering: owner successfully edits an `ACTIVE` listing; non-owner is rejected; edit on a `SOLD` listing is rejected; edit on a nonexistent listing returns not-found; invalid payload returns a validation error.
- Frontend: new edit-listing component spec following the `create-listing.component.spec.ts` pattern — form pre-fill from a mocked fetch, submission posts the expected full-replace body.

## Out of Scope

- Editing photos (Feature 3).
- Editing a `SOLD` listing.
- Partial-update (PATCH) semantics.
- An edit history/audit log of prior values.

## Further Notes

This closes the scope gap that `CONTEXT.md` previously called out — editing was explicitly out of scope; it's now a first-class capability.
