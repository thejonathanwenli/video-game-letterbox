# Video Game Letterbox: presentation guide

The slides' speaker notes contain the script, demo actions, technology explanation, and wrap-up. This guide only holds setup information, the text those notes tell you to paste, and possible Q&A.

## Start before presenting

MySQL must already be running. Run this in PowerShell before the presentation, not during it:

```powershell
Set-Location 'C:\Users\Jonathan Li\Dev10\video-game-letterbox'
.\presentation\start-demo.ps1
```

This starts the backend on port 8080 and frontend on port 5173, using the separate `video_game_letterbox_demo` database and prepared demo accounts. It does not log you into the website. Leave both servers running throughout the presentation. Internet access is needed for IGDB.

If PowerShell blocks the script:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File 'C:\Users\Jonathan Li\Dev10\video-game-letterbox\presentation\start-demo.ps1'
```

When it says Ready, open [the app](http://127.0.0.1:5173) in a regular browser tab. Log out if needed. Log in briefly when the speaker notes say to, then make adding your biography the first feature you demonstrate:

- Email: `demo_jonathan@example.com`
- Password: `DemoOnly9!`

These are demonstration credentials. If ports 8080 or 5173 are already occupied, do not launch another copy. Stop the previous demo using the command below, or stop a manually launched server in its original terminal.

### Reset between rehearsals, if needed

With the dedicated demo server running:

```powershell
$env:VGL_DEMO_DATABASE = 'video_game_letterbox_demo'
node .\presentation\prepare-demo.mjs --reset
```

This restores the six fictional seed accounts' library entries, reviews, and biographies and removes Jonathan's follows of those seeded players. Jonathan's biography starts blank so you can add it live. Celeste starts outside his library. It preserves the imported catalog. Use it only with the demo database, never for accounts containing data you want to keep. Log out afterward so the next run starts logged out.

The six accounts are demo_jonathan, demo_maya, alex_afterwork, nina_coop, sam_checkpoint, and riley_weekends. Their fourteen reviews include different tastes, ratings, and recommendations. These are fictional demonstration opinions, not real user testimonials.

## Stop afterward

From the same project folder:

```powershell
.\presentation\stop-demo.ps1
```

The sample database remains for your next rehearsal.

## Text referenced by the speaker notes

**Review A**

Challenging platformer with quick retries. Every small improvement feels rewarding.

**Review B (edited)**

Challenging platformer with quick retries. The movement is precise, and every improvement feels rewarding.

**Biography**

Playing indie games and sharing the ones I would recommend.

## Likely questions and short answers

These are practice answers grounded in the project. Adjust subjective wording so it reflects what you personally learned; do not claim a personal experience that does not fit yours.

### What was the hardest part?

“One of the biggest challenges was switching the game provider from RAWG to IGDB after I had issues getting API access working. IGDB had different authentication and a different response shape, so I adapted the backend and game mapping while keeping the same library experience.”

### Why did you choose IGDB?

“The app needs an existing game catalog with cover art, genres, and release information. IGDB let me integrate that instead of manually maintaining all the metadata, and it became the replacement when I had issues getting RAWG API access working.”

### Why do requests go through your backend?

“The backend can keep the application credentials private, obtain the Twitch token, and turn the provider's response into the shape my UI expects. It also combines that metadata with our own reviews and ratings.”

### Does a player need a Twitch account?

“No. The backend authenticates as an application to call IGDB. Players create an account in Video Game Letterbox and use our own login.”

### What is the difference between JPA and Hibernate?

“JPA defines the persistence API and mapping rules. Hibernate implements those rules. Spring Data JPA supplies the repository interfaces that I use with Hibernate and MySQL.”

### Why use JPA instead of JDBC?

“I wanted to learn entity relationships and reduce the repeated SQL and row-mapping work in my repositories. I still needed to understand the schema and write the business rules; JPA does not remove those responsibilities.”

### What did Spring Security do for you?

“It provides authentication infrastructure and request-access rules. I configured public versus protected routes, integrated my JWT filter, and used BCrypt for passwords. My services still enforce which records a user owns.”

### Could I change a URL or request body to edit another person's review?

“The backend uses the authenticated user's ID when checking the requested record. Another user's review does not satisfy that ownership check, even if someone manually sends its ID. The protection is not just the missing Edit button.”

### How long does login last? What happens at logout?

“The current JWT lifetime is sixty minutes. The UI clears the local session when it expires, and the backend rejects expired tokens. Logout removes the browser's stored token. There is no server-side token blacklist or refresh-token system, so a copied unexpired token remains valid until expiry. More sophisticated session management would be future work.”

### What are the different ratings?

“A personal rating is one user's score out of ten. The community rating averages the nonempty personal ratings in this app. The recommendation percentage counts Yes answers out of users who answered Yes or No, excluding No answer. IGDB's rating is a separate external score shown out of one hundred.”

### What if a person has not played a game?

“Want to Play entries cannot have a rating, recommendation, or review. The interface reflects that rule, and the service validates it as well. To return a reviewed game to Want to Play, the user must remove their review first.”

### How does Following work? Is it an algorithm?

“No algorithm is guessing preferences. The database stores who follows whom. The backend finds the selected users' recommended library entries and recent reviews, and the page shows those in separate sections.”

### Does searching save every game? What happens when IGDB is unavailable?

“Search results are not all imported. A game gets a local record when someone adds it to a library. Local library and profile data remain in MySQL, but search and fresh game-detail metadata depend on IGDB. The current app does not have a full offline catalog fallback.”

### How did you test it?

“I used backend automated tests for service rules and API behavior, including ownership checks. The latest backend suite had 73 passing tests. I also checked the React build and lint, and exercised the browser flows. The presentation sequence was checked separately with demo data.”

### What did you learn? What would you do differently?

“A useful lesson was keeping external API details separate from the application's own data model. Next time I would verify the outside provider's account setup and authentication earlier, before building as much around it.”

### What was the most fun part?

Suggested answer if it matches your experience: “Seeing the separate pieces come together: a real catalog search becoming a personal library entry, then appearing in another player's recommendations.”

### What would you add with more time?

“Custom game lists would give players more control over organization. Comments on reviews would make the social side more interactive.”

### Is it deployed publicly?

“This demonstration runs locally. Public deployment and production hardening are separate next steps.”

### Did you use AI?

“Yes. I used AI for implementation assistance, explanations, and review. I checked the result against the requirements and tested the flows. I can explain the parts shown in this presentation.” Only use the last sentence after you have practiced and can actually explain those parts. Do not claim to have written every line unaided.

### If you do not know an answer

“I have not implemented that part, so I do not want to guess. I would need to investigate it.” Answer the question briefly. Do not invent a capability, apologize repeatedly, or take the audience on a tour of unrelated classes.
