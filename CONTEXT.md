# Marketplace

A classifieds-style marketplace for buying and selling physical goods — one user pool, no distinct buyer/seller account types.

## Language

**User**:
A person with an account who can both post items for sale and buy items from others. There is no separate Buyer or Seller account type — the same User plays both roles depending on the action they're taking. Browsing Listings (which surfaces `ACTIVE` Listings by default) is public; being a logged-in User is only required to create a Listing or buy one.
_Avoid_: Buyer, Seller, Account (as a distinct type from User)

**Listing**:
An item a User has posted for sale, with a title, description, price, category, and one to six photos (ordered; the first is the cover photo shown in Browse). A Listing has a status: `ACTIVE` (available to buy) or `SOLD` (bought by a User, recorded on the Listing itself). There is no separate Order or Transaction record — buying is a state change on the Listing, not a new entity. The owning User can delete or edit their own `ACTIVE` Listing: title, description, price, and category are edited as a full replace; photos are managed independently (add one, remove one — at least one must always remain — reorder), not replaced as a set. A `SOLD` Listing can be neither deleted, edited, nor have its photos changed.
_Avoid_: Product, Item (when referring to the posted-for-sale record specifically), Ad, Order, Transaction

**Favorite**:
A User's personal bookmark on a Listing, independent of purchase or ownership — a User can favorite any Listing regardless of status (including their own). Deleting a Listing removes any Favorites pointing to it.
_Avoid_: Saved listing, Wishlist item, Watchlist item

**Conversation**:
A messaging thread between a Listing's owner and one other User who asked about that Listing, scoped to exactly one `(Listing, other User)` pair — never a general direct-message system. A Conversation persists even if the Listing is later sold or deleted.
_Avoid_: Chat, Thread, DM

**Notification**:
A system-generated alert to a User about an event concerning them — currently either their Listing being sold, or a new message received in a Conversation. Delivered in-app, and mirrored through the same stubbed email channel as password reset; marked read automatically when the User views the referenced content.

**Rating**:
A 1–5 star score with an optional comment that a User leaves about the other party in a completed sale (buyer rates seller, seller rates buyer), tied to the specific Listing that was sold. Immutable once submitted; at most one Rating per `(Listing, rater)`. Shown in aggregate (average + count) on the rated User's profile.
_Avoid_: Review

**Category**:
One of a fixed set of values a Listing is classified under, chosen from a predefined list rather than free text: Electronics, Furniture, Clothing & Accessories, Books & Media, Home & Garden, Vehicles, Other.
_Avoid_: Tag
