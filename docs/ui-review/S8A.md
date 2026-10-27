# S8A management review

Historical review record; S8A-S8C subsequently passed visual review and were authorized for feature-branch publication. See [publication verification](../engineering-history.md). The publication pass corrected only this harness's ambiguous Morgan history search to use the created customer's ID; all 40 assertions remain.

Local implementation on `feature/javafx-ui`, October 27, 2026. Awaiting user review; no commit, push, merge or S8B implementation.

## Capabilities

Customers has searchable records, Add/Edit and appointment history. New Appointment has a compact **+ New customer** action beside its Customer label. The shared form validates through the existing Customer constructor, immediately selects the saved customer and preserves the draft. Success appears above the booking form. Customer fields remain the existing name only.

Providers retains its availability workflow and adds name/category, supported services, Online/In-Person modes, seven-day working periods, preparation minutes and enabled state. Working periods accept split shifts; closed days are blank. Services has search, Add/Edit of name/category/type/duration/EUR base fee and access to provider relationship editing. These are finite existing categories/types/modes, not free-text extensions of the Abstract Factory families.

Every mutation goes through DesktopController and BookingFacade. Repository commands share the appointment lock, reject stale editors and protect active appointments. Booked service/details/duration/fees remain snapshots. See [architecture](../architecture.md) for the exact editing and rescheduling policies.

## Real screenshot index

The runner calls the existing Windows DWM screen-capture helper. Files contain unmodified window pixels, not JavaFX snapshots or generated mockups. Requested physical outer windows are 1440x900 and 1024x768; at this host's 125% scaling the captured DWM bounds are respectively **1424x892** and **1008x760**. PNGs remain local and ignored by Git.

| Capture | Content |
| --- | --- |
| [01-customers-list.png](screenshots/S8A/01-customers-list.png) | Customers list and discoverable actions |
| [02-new-customer-form.png](screenshots/S8A/02-new-customer-form.png) | New customer form |
| [03-customer-created.png](screenshots/S8A/03-customer-created.png) | Created customer, search and success feedback |
| [04-providers-management.png](screenshots/S8A/04-providers-management.png) | Provider browse/edit and availability |
| [05-new-provider-form.png](screenshots/S8A/05-new-provider-form.png) | Provider identity, services, modes and weekly hours |
| [06-services-management.png](screenshots/S8A/06-services-management.png) | Service catalog with categories, durations, fees and providers |
| [07-new-service-form.png](screenshots/S8A/07-new-service-form.png) | New service form |
| [08-booking-new-customer.png](screenshots/S8A/08-booking-new-customer.png) | Booking with newly created customer/provider/service |
| [09-inline-customer-created.png](screenshots/S8A/09-inline-customer-created.png) | Inline creation success and preserved booking draft |
| [10-new-provider-compact.png](screenshots/S8A/10-new-provider-compact.png) | Compact provider form, upper fields |
| [11-booking-compact.png](screenshots/S8A/11-booking-compact.png) | Compact booking with selected new customer |
| [12-customers-compact.png](screenshots/S8A/12-customers-compact.png) | Compact customer management |
| [13-services-compact.png](screenshots/S8A/13-services-compact.png) | Compact service management after catalog edits |
| [14-new-service-compact.png](screenshots/S8A/14-new-service-compact.png) | Compact new service form |
| [15-provider-hours-compact.png](screenshots/S8A/15-provider-hours-compact.png) | Scrolled weekly hours and save/cancel actions |
| [16-new-customer-compact.png](screenshots/S8A/16-new-customer-compact.png) | Compact new customer form |

The design retains Georgia/Segoe UI typography and the existing ink, ivory, terracotta and sage palette. Tables use restrained borders; controls wrap and pages scroll vertically. The provider's lower weekly fields and some service-relationship controls require scrolling, particularly at compact size. Table tooltips retain ellipsized values. The initial screenshot review moved the inline customer action into its label row to preserve desktop booking action visibility, then moved inline success feedback above the form.

## Reproduction and limits

Executed results: Maven **312/0/0/0** tests/failures/errors/skips; S8A **40** Windows assertions; S7C **85**; sidebar **106** with motion and **106** reduced; Overview **13**; console QA **nine** successful JAR processes. All 16 captures decode successfully. Exact build sequencing and the initial sidebar hover failure are retained in engineering history.

After `mvn clean verify`, run `powershell -NoProfile -File scripts/run-desktop.ps1 -VerifyS8A -Capture`. Only one AppointEase window may be open during capture. The harness enters and edits real native controls in the packaged desktop, validates rejection/recovery, creates and books new records, verifies draft/snapshot integrity and checks management action widths at both sizes. It closes its stage afterward. Retained S7C/sidebar/Overview harnesses cover the existing workflows; exact final counts are recorded in [engineering history](../engineering-history.md).

All data is process-local, and the startup demo catalog remains available. Customer contact fields, deletion, durable storage, import/export, roles and catalog audit events are outside this slice. Provider modes apply to all supported services. Reschedules preserve old snapshots and must fit both original intrinsic hours and current provider configuration. Native tooltip/foreground input remains timing-sensitive; one sidebar hover assertion failed before the unchanged rerun passed. Other OSes, exhaustive screen-reader behavior and very large catalogs are unverified. No S8B scope is implied by this handoff.
