# Food Left at Closing Time (Spring Boot + React)

A restaurant posts leftover food in one sentence. The system offers it to **one** shelter that can really
collect it before it spoils, and moves on to the next shelter if that one says no, stays silent, or does not show up.

## Run it

**Backend** (Java 17+, Maven). In Spring Tool Suite: File > Import > Maven > Existing Maven Projects > `backend`,
then run `FoodRescueApplication` as a Spring Boot App. Or from a terminal:

```bash
cd backend
mvn spring-boot:run          # http://localhost:8080  (H2 console: /h2-console)
mvn test                     # runs the matching/flow tests
```

**Frontend** (Node 18+):

```bash
cd frontend
npm install
npm run dev                  # http://localhost:5173
```

## What the scenario asked for, and where it lives

| Scenario point | How it is handled |
|---|---|
| Posting must take under a minute, no phoning | One free-text sentence (`QuickFoodParser`) plus a ready-time field. Portions, trays, hold-until time and pickup spot are read from the sentence. Preset examples are one tap. |
| Shelters are all different | `Shelter`: capacity (`maxPortions`), collection cutoff (`latestPickupMinutes`), `canTakeTrays` (van or rickshaw), `travelMinutes`. |
| "Only before ten" / "about fifty and no more" / "rickshaw cannot take trays" | Hard rules in `FoodRescueService.evaluate()`. A shelter that fails any rule is **not eligible**. |
| Food spoils | A shelter is eligible only if `ready time + travel time <= min(food hold-until, shelter cutoff)`. If nobody qualifies the batch is `UNMATCHED` and the restaurant is told immediately. |
| Two shelters want the same food | The system picks ONE (best reliability, then closest) and offers it only to that shelter. Anyone else who tries to accept gets `409`. `@Version` on `FoodPost` stops two simultaneous actions from both winning. |
| Shelters with a history of not turning up | Reliability = collected / (collected + no-shows). It ranks shelters; a no-show lowers it, a collection raises it. A shelter with a bad record can still act as the backup, but is ranked last. |
| Said yes and never turned up | Restaurant taps "Did not show up": the shelter is penalised and excluded for that batch, and the food is immediately offered to the next shelter. |
| Said nothing | Offers expire after `app.offer-timeout-minutes` (default 5) and pass to the next shelter automatically (`@Scheduled`). |

## Flow of one batch

`POST /api/foods` -> **OFFERED** (one shelter) -> shelter accepts -> **ACCEPTED** (ETA shown) -> restaurant confirms -> **PICKED_UP**
decline / timeout / no-show -> next best shelter is **OFFERED**; nobody left -> **UNMATCHED** (can retry with "Try matching again").

## API

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/shelters` | all shelters with reliability |
| GET | `/api/shelters/{id}/offers` | what one shelter sees (offered to it / accepted by it) |
| GET | `/api/foods` | all batches with the ranked shelter list and reasons |
| POST | `/api/foods` | `{restaurantName, rawDescription, readyAt:"21:45"}` creates a batch and offers it |
| POST | `/api/foods/{id}/accept/{shelterId}` | only the shelter it is offered to can accept |
| POST | `/api/foods/{id}/decline/{shelterId}` | pass it to the next shelter |
| POST | `/api/foods/{id}/pickup` | restaurant confirms collection |
| POST | `/api/foods/{id}/noshow` | restaurant reports a no-show, food re-offered |
| POST | `/api/foods/{id}/rematch` | try again for an `UNMATCHED` batch |

Errors always come back as `{"message": "..."}`.

## Demo script (2 minutes)

1. **Restaurant** tab: tap the first example, set ready time `21:45`, **Post**. It is offered to *Alo Community Kitchen* (van, most reliable).
   Open "Show shelter ranking": *Shobuj* (rickshaw, no trays) and *Early Night* (would arrive after its 10 PM cutoff) are marked "Cannot", with the reason.
2. **Shelter inbox** tab, act as *Metro Night Relief*: nothing there, because the food is offered to one shelter only.
3. Act as *Alo*: **Decline**. The offer moves to *Metro* (backup, low trust).
4. Act as *Metro*: **Accept**. Back on **Restaurant**: "Metro is coming, ETA ...".
5. Tap **Did not show up**: Metro's trust drops, nobody is left, the batch becomes **UNMATCHED** straight away.
6. Post again, accept as *Alo*, tap **Food collected**: batch is **PICKED_UP**.
7. Change the ready time to `21:00` and post again: *Early Night* now qualifies. The result depends on time, not on a fixed list.

## Assumptions (state these to the examiner)

- Times are same-night, 24h in the form; the sentence accepts `11:30pm` style. Times with no am/pm are read as evening. After-midnight times (e.g. `12:30am`) count as later than 11:59 pm.
- A tray counts as about 15 portions; "no trays" means no trays.
- "Ready from" defaults to 21:45 in the form (closing time) so the demo is the same at any hour of the day.
- No login: the Shelter inbox has a switcher to act as any shelter. A real version would authenticate shelters.
- Data lives in an in-memory H2 database and resets on restart. Five shelters are seeded by `DataSeeder`.
- An unanswered offer is passed on without a reliability penalty. Only an accepted-then-missed pickup counts as a no-show.

## Possible next steps

SMS or WhatsApp notification to shelters, real travel times from a maps API, shelter login, history and reports, a "re-check every minute" job for unmatched batches.
