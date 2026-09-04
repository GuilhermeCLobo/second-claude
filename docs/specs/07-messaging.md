# In-app messaging between buyer and seller

## Problem Statement

A buyer who has a question about a listing — condition, exact dimensions, willingness to negotiate — has no way to ask the seller before committing to buy. The only interaction the app supports today is buying outright.

## Solution

Add listing-scoped messaging: any logged-in User other than a listing's own owner can start a Conversation about that specific Listing with its owner, and exchange messages within it. Conversations persist even if the listing is later sold or deleted.

## User Stories

1. As a logged-in User browsing a listing that isn't mine, I want to start a conversation with the owner about it, so that I can ask a question before buying.
2. As a Listing owner, I want to see all conversations other Users have started about my listings, so that I can answer questions and manage interest.
3. As a User in a conversation, I want to send and receive messages within it, so that I can have a back-and-forth exchange.
4. As a Listing owner, I want to be prevented from starting a conversation with myself about my own listing.
5. As a User, I want at most one conversation per listing between me and the owner, so that repeated questions stay in one continuous thread.
6. As a User, I want my conversation about a listing to remain accessible even after that listing sells, so that I can refer back to what was discussed.
7. As a User, I want my conversation to remain accessible even after the listing is deleted, with a clear indicator that the listing is gone, so that the conversation's content isn't lost.
8. As a User, I want to see which of my conversations have unread messages, so that I know where to check for a reply.
9. As a User opening a conversation, I want unseen messages marked read automatically, so that I don't have to manually dismiss anything.
10. As a User, I want a list of all my conversations, across listings I own and listings I've asked about, so that I have one place to check for activity.
11. As a User, I want new messages to appear without a full page reload, so that a conversation feels responsive even without real-time push.
12. As a User not part of a given conversation, I want to be blocked from viewing or posting to it.

## Implementation Decisions

- New `Conversation` entity: id, `listingId` (FK), owner user id, other-participant user id, `createdAt`; unique constraint on `(listingId, otherUserId)`.
- New `Message` entity: id, `conversationId` (FK), sender user id, body, `createdAt`.
- Per-participant read tracking (`lastReadAt` per participant per conversation) to compute unread counts/badges.
- New endpoints: start-or-return-existing conversation on a listing; list the current User's conversations (across both roles); list a conversation's messages; post a new message; mark a conversation read (triggered when the frontend opens it).
- Authorization restricts viewing/posting to a conversation's two participants only.
- No cascade-delete of conversations/messages when the referenced listing is deleted — the listing reference is retained for display purposes, but the conversation and its messages remain intact.
- Delivery via polling: the frontend polls for new messages and conversation-list updates on an interval; no WebSocket/real-time infrastructure is introduced.

## Testing Decisions

- Backend: new API test following the existing `*ApiTest` seam covering: starting a conversation creates it once and is idempotent on repeat calls; the owner cannot start a conversation on their own listing; sending and receiving messages; a third User cannot view or post into someone else's conversation; the conversation and its messages remain queryable after the listing is deleted; unread count reflects new messages and clears after marking read.
- Frontend: new conversation-list and conversation-detail component specs, following existing conventions, covering polling-driven refresh and read-marking on open.

## Out of Scope

- General, non-listing-scoped direct messaging between Users.
- Real-time delivery (WebSocket/push).
- Message editing/deletion, attachments, or read receipts beyond the coarse read marker.
- Blocking or reporting abusive messages.

## Further Notes

Two deliberate trade-offs were made here that are hard to reverse and non-obvious to a future reader: keeping messaging strictly listing-scoped rather than a general DM system, and choosing polling over WebSockets. Worth an ADR each if you want the reasoning on record.
