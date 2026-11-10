# Testing and verification

## Automated checks

Run from the project root:

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
```

The JUnit suite uses H2 and does not need MySQL. Test context startup also verifies JPA mappings, Spring Security wiring and the local sample seed.

## Manual end-to-end checklist

1. Start with the `local` profile; open `/` and verify the hero, local image cards and search results load.
2. Search by city, listing type, type, budget, bedrooms and sort. Confirm result totals/pagination change.
3. Register a buyer; save a home, contact an advisor, request a visit, submit a booking and verify each record appears in the buyer workspace.
4. Submit the public contact form; verify it displays a success message.
5. Set `DEMO_PASSWORD` before starting and sign in as `seller@estatepro.local`; submit/edit/archive a listing.
6. Sign in as `admin@estatepro.local`; approve/reject a pending listing, update a user and export the CSV.
7. Sign in as `agent@estatepro.local`; update a lead and confirm an appointment/booking assigned to the agent.
8. Confirm a buyer cannot access another buyer's records, a seller cannot edit another seller's property, and a non-admin cannot call moderation/user-management endpoints.
9. Use the `mysql` profile against a disposable MySQL database and verify application startup, generated foreign keys and CRUD workflows.
10. Verify all images below `src/main/resources/static/assets/images/` are non-empty and load over the app's static URL.

## Environment-dependent checks

MySQL integration could not be exercised where no MySQL server/client is installed. H2 tests do not replace a MySQL integration run. SMTP, live email, real payment processing, security penetration tests and production deployment checks are not part of this demo. Do not enter real payment credentials.
