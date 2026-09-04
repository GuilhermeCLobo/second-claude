## Backend (`backend/src/main/java/com/marketplace/backend/`)

- Listing CRUD (browse/get/create/edit/delete/buy) — `listing/ListingController.java` (routes), `listing/ListingService.java` (logic), `listing/Listing.java` (entity)
- Listing photos (add/remove/reorder, 1-6 ordered, first is cover) — `listing/Photo.java` (entity), same controller/service as above
- Listing validation shape (create + edit) — `listing/CreateListingRequest.java`; reorder payload shape — `listing/ReorderPhotosRequest.java`
- Listing domain exceptions/status codes — `listing/ListingExceptionHandler.java`
- Auth (register/login/logout/session) — `auth/` (multiple files, no single entry point)
- Password reset flow — `auth/` (look for ResetToken/PasswordReset classes)

## Frontend (`frontend/src/app/`)

- Browse listings — `listings/browse-listings.component.ts`
- Listing detail (view/edit-link/delete/buy/manage photos) — `listings/listing-detail.component.ts`
- Create a listing (multi-photo upload) — `listings/create-listing.component.ts`
- Edit a listing — `listings/edit-listing.component.ts`
- My listings (posted/bought) — `listings/my-listings.component.ts`
- Listings HTTP calls (incl. photo add/remove/reorder) — `listings/listings.service.ts`
- Routes — `app.routes.ts`

## Docs

- Feature specs — `docs/specs/`
- ADRs — `docs/adr/`
- Domain glossary — `CONTEXT.md`
- Feature backlog — `improvements.md`
