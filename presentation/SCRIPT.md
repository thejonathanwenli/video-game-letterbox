# Capstone presentation script

Read the normal paragraphs aloud. Bracketed lines are private actions or timing cues. Use the full script in Google Slides speaker notes.

## Slide 1: Video Game Letterbox (0:00–1:00)

[0:00 | SLIDE 1 | Say this at a relaxed pace.]
Video Game Letterbox helps players keep track of their backlog and share what they think about the games they play.

The problem I wanted to solve is simple: it is easy to collect a list of games, but harder to remember what you wanted to play, what you already finished, and which games you would recommend to someone else.

I chose a personal library because that information belongs together. A game can move from Want to Play to Playing or Completed, and the player can record their rating and review alongside it.

The social side adds another way to discover games: looking at the opinions of people you choose to follow. Let me show you how that works.

[Advance to Live Demo. Switch the shared window to the app. Target: 1:00.]

## Slide 2: Live demo (1:00–5:00)

[SLIDE 2: LIVE DEMO
Start around 1:00. Finish around 5:00.
Bracketed instructions are private actions, not words to read aloud.

Before presenting: servers running, demo reset, regular browser tab open at http://127.0.0.1:5173 on Discover, logged out. Your biography starts blank.
Share only the app window. Keep these notes private.]

### Quick login

[Click Log in or sign up. Briefly point to Create an account, then use the login form.

Email: demo_jonathan@example.com
Password: DemoOnly9!

Click Log in.]

Anyone can explore the game catalog without an account. I am using a prepared account to show how a player sets up their profile and starts building a library.

### Create your biography first

[Click My Profile, then Edit Biography. Paste this text and click Save Biography.

COPY BIOGRAPHY:
Playing indie games and sharing the ones I would recommend.]

I will start with a short biography so other players can understand my interests when they visit my profile. I can come back and change it later.

### Find a game and show the reviews

[Click Discover. Search Celeste. Wait for the results, then click View details for the exact title Celeste.
Show the ratings and scroll briefly through a couple of reviews. Do not read every review aloud.]

The game page combines catalog information with players' opinions. IGDB's rating comes from the external catalog, while the community rating comes from people using this app. These game pages and reviews are public.

### Add the game

[Return to the Add to My Library section on the same page. Leave Add with status set to Want to Play, then click Add to My Library.]

I want to remember this game, so I add it as Want to Play. I cannot rate or review it while it has that status.

### Update status, rating, and recommendation

[Click My Library. On Celeste's card, set:
Status: Playing
Rating: 9
Recommend: Yes
Click Save.]

Once I start playing, I can update the status and record my opinion. My rating is out of ten. Recommendation is a separate choice about whether I would suggest the game to someone else.

### Write and edit a review

[Click the Celeste title link in your library.
Paste Review A into the Review field, then click Save Review.

COPY REVIEW A:
Challenging platformer with quick retries. Every small improvement feels rewarding.

Find your review under demo_jonathan and click Edit. Replace its text with Review B, then click Save Review.

COPY REVIEW B:
Challenging platformer with quick retries. The movement is precise, and every improvement feels rewarding.]

A review lets me explain my rating. I can update my own review as my experience changes, and other players can read it and visit my profile.

### Filter the library and remove an entry

[Click My Library. In the filters at the top, set Status to Playing, type Platform into Genre, and click Apply.
Briefly show the result.
Set Status back to All statuses, clear Genre, and click Apply.
On Hollow Knight, click Remove and confirm. Keep Celeste.]

As the library grows, I can narrow it by status or genre. I can also remove a game from my personal list without removing the shared game from the catalog.

### Visit another player and follow them

[Open Celeste again. Scroll through the reviews to find demo_maya and click her username.
Briefly show her biography, public library, and reviews.
Click Follow, then Following in the navigation.
Show Recommended Games and Recent Reviews.]

Maya's review leads to her profile, where I can see what she enjoys. Following her brings her recommended games and recent reviews together. This was my completed stretch goal: discovering games through people whose opinions interest me.

### Unfollow and delete your review

[Click demo_maya to return to her profile, then click Unfollow.
Click My Profile. Find your Celeste review under Reviews, click Delete, and confirm. Do not delete the Portal 2 review.]

I can unfollow someone or delete a review I wrote. Deleting this review leaves my library entry and rating in place.

### Return to the slides

[Click Log out.
Switch the shared window back to the slideshow and advance to slide 3, Technologies.]

That is the main experience: finding games, tracking my time with them, and discovering recommendations from other players.

[Target: approximately 5:00 on the overall presentation clock.]

## Slide 3: Technologies (5:00–5:30)

[5:00 | SLIDE 3 | Briefly acknowledge both columns.]
For the familiar stack, I used React and React Router for the interface, Bootstrap for styling, and a Java Spring Boot API backed by MySQL. I used JUnit and Mockito for testing.

For my new technologies, I chose JPA with Hibernate to reduce repetitive database code, Spring Security to manage authentication and protected requests, and IGDB to provide an existing game catalog instead of maintaining one manually.

I’ll walk through how game information reaches the app, then how the app protects a user’s changes.

[Advance at about 5:30.]

## Slide 4: Getting game information (5:30–6:40)

[5:30 | SLIDE 4 | Point across the diagram, then to the Twitch connection.]
The first path starts when a player searches for a game. React sends the title to my backend. The backend's IgdbClient makes the request to IGDB and translates the response into the game information the interface needs.

IGDB requires an application access token from Twitch. My backend requests that token using credentials kept outside the source code, reuses it while it is valid, and obtains another when necessary. The player does not need a Twitch account, and these are not the login tokens for my users.

One challenge was changing providers. I originally built around RAWG, but I had issues getting API access working, so I switched to IGDB. That meant adapting authentication, requests, and fields such as cover images and release dates. My RAWG integration used an API key, while IGDB required my backend to obtain and reuse a Twitch access token. I also had to translate IGDB’s different response format into the game information my application expected so the library and review features could continue working.

Searching does not import every result into my database. When a player adds a game, the application saves it if it is not already there. That leads to the database side.

[Advance at about 6:40.]

## Slide 5: Saving a personal library (6:40–7:50)

[6:40 | SLIDE 5 | Point to User, Library entry, and Game.]
A game is shared catalog information. A library entry is one person's relationship to that game.

That distinction is important. Maya and I can both have Celeste in our libraries, but our playing statuses, ratings, recommendations, and reviews can be different. The user_game record connects the user and game and stores the status, rating, and recommendation. A separate review record connects to that library entry.

I used JPA annotations to describe those relationships in Java. This small excerpt says that many library entries can refer to one user. The game relationship follows the same pattern.

JPA is the persistence standard, Hibernate is the implementation, and Spring Data JPA provides the repository interfaces. Together, they handle much of the SQL work I previously wrote in JDBC repositories. I still write the service rules, such as preventing a Want to Play game from having a rating or review.

[Point briefly to the code; do not read the annotation arguments. Advance at about 7:50.]

## Slide 6: Protecting user actions (7:50–9:00)

[7:50 | SLIDE 6 | Follow the request from left to right.]
The second path is an authenticated action, such as editing a review.

At registration, the backend hashes the password with BCrypt. At login, Spring Security authenticates the credentials, and my application issues a signed JWT. React sends that token with later protected requests.

My JWT filter validates the token and supplies the authenticated identity to Spring Security. The configured rules allow public browsing but require authentication for personal changes.

Authentication alone is not enough. A logged-in user still should not be able to edit someone else's review. My service and repository checks use the authenticated user ID to find a record owned by that user before changing it. Hiding an Edit button would not provide that protection by itself.

That separation was the key idea for me: establish who is asking, then check whether the record belongs to them.

[Advance at about 9:00.]

## Slide 7: What’s next (9:00–10:00)

[9:00 | SLIDE 7 | Keep this to approximately 20 seconds.]
With more time, I would add named custom game lists so players could organize collections around their own themes. I would also add comments on reviews so recommendations could start a conversation.

For now, the app brings a player's backlog, opinions, and followed recommendations together. Thank you. I am happy to take questions.

[Stop talking. Leave this slide visible. Reserve the remaining time for one or two questions. Use the separate Q&A section in the guide only if needed.]
