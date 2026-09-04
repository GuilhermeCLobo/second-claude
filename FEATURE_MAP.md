## Backend (`backend/src/main/java/com/marketplace/backend/`)

- Listing CRUD (browse/get/create/edit/delete/buy) — `listing/ListingController.java` (routes), `listing/ListingService.java` (logic), `listing/Listing.java` (entity)
- Listing validation shape (create + edit) — `listing/CreateListingRequest.java`
- Listing domain exceptions/status codes — `listing/ListingExceptionHandler.java`
- Auth (register/login/logout/session) — `auth/` (multiple files, no single entry point)
- Password reset flow — `auth/` (look for ResetToken/PasswordReset classes)

## Frontend (`frontend/src/app/`)

- Browse listings — `listings/browse-listings.component.ts`
- Listing detail (view/edit-link/delete/buy) — `listings/listing-detail.component.ts`
- Create a listing — `listings/create-listing.component.ts`
- Edit a listing — `listings/edit-listing.component.ts`
- My listings (posted/bought) — `listings/my-listings.component.ts`
- Listings HTTP calls — `listings/listings.service.ts`
- Routes — `app.routes.ts`

## Docs

- Feature specs — `docs/specs/`
- ADRs — `docs/adr/`
- Domain glossary — `CONTEXT.md`
- Feature backlog — `improvements.md`
