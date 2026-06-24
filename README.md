# LaundryDemoServer
This repo is a snapshot of an application written for a friend's laundromat.  The goal was to build out a mobile app for them.  Development continued from here in a private repo, with this repo kept as a portfolio piece, and a place for me to play with different patterns on server architecture.

This is meant to be a multi-tenant SAAS POS backend service.  It has multiple laundromats (Organizations) each of which has 1 or more locations.  Pricing for dry cleaning and wash and fold is per location.  Customer accounts are tied to a phone number and organization, meaning a single user can have accounts at multiple organizations without knowing that they run the same backed.  In addition to customer logins, we have admin and employee logins.  Admins are meant to be employees of the SAAS company, and can create new organizations and admins.  Employees are tied to a single organization.  

For authorization, we have role based authorization for both admins and employees.  Employees can have a role at either an entire organization, or at individual location(s).  This allows an owner of a multi-site laundromat to have global edit ability, while allowing manager and employees to only access individual locations.

Everything here is built to production quality except for two things.  First, the  AvailableTimesController.  That controller is meant to serve the available pickup and dropoff times for a
location.  It's currently a stub serving fixed data for a prototype, and needs to be restructured for rea data.  In particular, we hadn't decided on how to populate this without requiring the owner to set individual slots weekly while still respecting days off and holidays.  It was the next feature to be built.  

The other fakes out part is the Customer->location mapping for prices and orders.  Right now, we're looking for a location with a matching postcode to the user's default address.  In the end we will probably want a more intelligent algorithm, but this was sufficient for a prototype to show location based pricing.



# Architecture

This server uses Spring Boot, Postgres and Hibernate for its  database access, with Flyway managing its migrations.

