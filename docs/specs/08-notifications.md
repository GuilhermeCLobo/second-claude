# Email or in-app notifications

## Problem Statement

A User has no way to know something happened that concerns them — their listing sold, someone messaged them — unless they happen to revisit the relevant page. There's no alerting of any kind today.

## Solution

Add a unified in-app Notification system, mirrored through the same stubbed email channel built for password reset, covering two triggers: a User's listing being sold, and a User receiving a new message.

## User Stories

1. As a Listing owner, I want to be notified when my listing sells, so that I know without having to keep checking My Listings.
2. As a User in a conversation, I want to be notified when I receive a new message, so that I know to check and reply.
3. As a User, I want a single notifications list covering both triggers, so that I don't need to check multiple separate places.
4. As a User, I want unread notifications visually distinguished from read ones.
5. As a User, I want a notification automatically marked read when I view the thing it refers to — the sold listing, the conversation — so that I don't have to manually dismiss it.
6. As a User, I want notifications to also arrive via the same stubbed email channel used for password reset, so that the full pipeline is demonstrated end-to-end even without real SMTP.
7. As a User, I want new notifications to appear without a full page reload, using the same polling approach as messaging.
8. As a User, I want notifications for events unrelated to me to never appear in my list.

## Implementation Decisions

- New `Notification` entity: id, `userId`, type (an enum covering listing-sold and new-message, extensible later), a reference id interpreted per type (listing id or conversation id), `createdAt`, `readAt` (nullable).
- The buy flow creates a listing-sold notification for the seller upon a successful sale.
- The send-message endpoint (Feature 7) creates a new-message notification for the recipient upon each new message.
- Both triggers also invoke the `EmailSender` interface from Feature 1 to log a corresponding email.
- New endpoint returning the current User's notifications, most recent first; read-marking reuses the same approach as Feature 7's conversation-read marking (mark-on-view of the referenced content), applied analogously to the listing-sold case (viewing the listing detail / My Listings).
- Delivery via polling, consistent with messaging — no new real-time infrastructure.

## Testing Decisions

- Backend: new API test following the existing `*ApiTest` seam covering: buying a listing creates a listing-sold notification for the seller only; sending a message creates a new-message notification for the recipient only; the notifications endpoint returns only the current User's notifications; viewing the referenced content marks the notification read; the stubbed email sender is invoked for both trigger types (verifiable via a test double capturing sent messages).
- Frontend: new notifications component spec covering list rendering, unread-count display, and polling-driven refresh.

## Out of Scope

- Real email delivery.
- Notification preferences/opt-out.
- Additional trigger types (e.g. price drop on a favorited listing, rating received) — the entity is designed to support these later without a schema change.
- Push notifications (browser/mobile).

## Further Notes

Deliberately designed as a generic, extensible entity so future trigger types can be added without a schema change.
