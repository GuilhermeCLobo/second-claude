# Marketplace

A classifieds-style web app for buying and selling physical goods. There's a single pool of Users — no separate buyer/seller account types — the same person can post items for sale and buy items from others.

Full-stack project: a Java/Spring Boot REST API backend and an Angular single-page frontend.

## Tech stack

**Backend**
- Java 17
- Spring Boot 3.3 (Web, Data JPA, Validation)
- Spring Security Crypto (BCrypt password hashing)
- JWT authentication ([jjwt](https://github.com/jwtk/jjwt))
- H2 file-based database
- Maven

**Frontend**
- Angular 18 (standalone components, Reactive Forms, Angular Router)
- TypeScript
- RxJS
- Karma + Jasmine for unit tests

## Features

- **Accounts & auth** — register, log in, log out, JWT-based sessions, and a password reset flow (a reset token is generated and delivered through a stubbed email channel that logs to the console).
- **Listings** — post a listing with a title, description, price, category, and 1–6 photos; the first photo is the cover shown in Browse. Owners can edit or delete their own `ACTIVE` listings, and manage photos independently (add, remove, reorder) without replacing the whole set.
- **Browse** — public listing feed with text search, category filtering, min/max price filtering, sorting (newest, price ascending/descending), and pagination.
- **Buying** — any logged-in User other than the owner can buy an `ACTIVE` listing; it becomes `SOLD` and is locked from further edits, deletion, or photo changes. There's no separate Order/Transaction entity — buying is just a state change on the Listing.
- **Favorites** — bookmark any listing regardless of its status or ownership, and view them all on a "My Favorites" page.
- **My Listings** — a User's own dashboard of listings they've posted and listings they've bought.
- **Public profiles** — a public, unauthenticated profile page per username showing that User's currently `ACTIVE` listings; listing cards and detail pages link through to the owner's profile.
- **Responsive UI with dark mode** — a shared design system (design tokens, buttons, forms, cards) applied across every page, with light/dark themes following the OS preference.

### Planned / not yet built

Specced in `docs/specs/` but not implemented yet:
- In-app messaging between a listing's owner and an interested buyer
- Notifications (e.g. "your listing sold", "new message")
- Post-sale ratings between buyer and seller

See `improvements.md` for the full backlog of ideas.

## Project structure

```
backend/    Spring Boot REST API (Maven project)
  src/main/java/com/marketplace/backend/
    auth/       registration, login, JWT issuing, password reset
    listing/    listing CRUD, browse/search/sort/pagination, photos
    favorite/   favoriting listings
    user/       user profiles
    photo/      photo storage
    email/      stubbed email sender
  src/test/     backend HTTP-level API tests (MockMvc)

frontend/   Angular SPA
  src/app/
    auth/       login, register, password reset UI
    listings/   browse, listing detail, create/edit, my-listings, my-favorites
    profile/    public user profile page
  src/app/*.component.spec.ts   Angular TestBed component specs

docs/
  specs/      feature specs (implemented and planned)
  adr/        architecture decision records
CONTEXT.md        domain glossary / ubiquitous language
FEATURE_MAP.md    index of where each feature lives in the codebase
improvements.md   feature/styling backlog
```

## Getting started

### Prerequisites
- Java 17+ and Maven
- Node.js and npm

### Run the backend

```
cd backend
mvn spring-boot:run
```

Starts the API on `http://localhost:8080`, backed by a local H2 database file under `backend/data/`.

### Run the frontend

```
cd frontend
npm install
npm start
```

Starts the Angular dev server on `http://localhost:4200`, proxying `/api` requests to the backend (see `frontend/proxy.conf.json`).

## Testing

```
# Backend (JUnit + Spring MockMvc)
cd backend
mvn test

# Frontend (Karma + Jasmine)
cd frontend
npm test
```
