# Marketplace

A classifieds-style marketplace for buying and selling physical goods — one user pool, no distinct buyer/seller account types.

## Language

**User**:
A person with an account who can both post items for sale and buy items from others. There is no separate Buyer or Seller account type — the same User plays both roles depending on the action they're taking. Browsing Listings is public; being a logged-in User is only required to create a Listing or buy one.
_Avoid_: Buyer, Seller, Account (as a distinct type from User)

**Listing**:
An item a User has posted for sale, with a title, description, price, category, and single required photo. A Listing has a status: `ACTIVE` (available to buy) or `SOLD` (bought by a User, recorded on the Listing itself). There is no separate Order or Transaction record — buying is a state change on the Listing, not a new entity. The owning User can delete their own `ACTIVE` Listing; editing a Listing's fields is out of scope for now.
_Avoid_: Product, Item (when referring to the posted-for-sale record specifically), Ad, Order, Transaction

**Category**:
One of a fixed set of values a Listing is classified under, chosen from a predefined list rather than free text: Electronics, Furniture, Clothing & Accessories, Books & Media, Home & Garden, Vehicles, Other.
_Avoid_: Tag
