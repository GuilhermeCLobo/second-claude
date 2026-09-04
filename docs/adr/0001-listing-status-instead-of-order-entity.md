# Listing status instead of a separate Order entity

For MVP, buying a Listing does not create a separate Order/Transaction record — it transitions the Listing's `status` from `ACTIVE` to `SOLD` and sets a `buyerId` on the Listing itself, with the transition guarded to only succeed from `ACTIVE` (so two buyers racing for the same Listing can't both win). We chose this over a persisted Order entity to keep the schema small while learning the stack; the trade-off is no purchase history or audit trail, which is a likely stretch goal once the core browse/list/buy loop works.
