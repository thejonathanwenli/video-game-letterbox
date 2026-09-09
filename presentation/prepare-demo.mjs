// Presentation-only sample data. Run against the dedicated demo server, not production.
// All people and opinions below are fictional presentation data.
// --reset replaces only these six seed accounts' entries, reviews, and biographies.
import fs from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

if (process.env.VGL_DEMO_DATABASE !== 'video_game_letterbox_demo') {
  throw new Error('Use the demo setup command in PRESENTATION-GUIDE.md first.');
}
const base = 'http://localhost:8080/api';
const password = 'DemoOnly9!';
const reset = process.argv.includes('--reset');
async function request(route, method = 'GET', body, token) {
  const response = await fetch(base + route, {
    method, signal: AbortSignal.timeout(40000),
    headers: { ...(body ? { 'Content-Type': 'application/json' } : {}), ...(token ? { Authorization: `Bearer ${token}` } : {}) },
    ...(body ? { body: JSON.stringify(body) } : {}),
  });
  const text = await response.text();
  let data; try { data = JSON.parse(text); } catch { data = text; }
  if (!response.ok) {
    const error = new Error(`${method} ${route}: ${response.status} ${JSON.stringify(data)}`);
    error.status = response.status;
    throw error;
  }
  return data;
}
async function account(username, bio) {
  const email = `${username}@example.com`;
  let login;
  let created = false;
  try { login = await request('/auth/login', 'POST', { email, password }); }
  catch (error) {
    if (error.status !== 401) throw error;
    await request('/auth/register', 'POST', { username, email, password });
    login = await request('/auth/login', 'POST', { email, password });
    created = true;
  }
  if (created || reset) await request('/users/me', 'PUT', { bio }, login.token);
  if (reset) {
    for (const entry of await request('/library', 'GET', null, login.token)) {
      await request(`/library/${entry.userGameId}`, 'DELETE', null, login.token);
    }
  }
  return login;
}
// Resolve the catalog before changing accounts. Already imported games do not
// need another IGDB call just to restore presentation data.
const games = {};
const catalog = await request('/games');
for (const title of ['Celeste', 'Hollow Knight', 'Stardew Valley', 'Portal 2']) {
  const saved = catalog.find(game => game.title.toLowerCase() === title.toLowerCase());
  const results = saved ? [saved] : await request(`/games/search?title=${encodeURIComponent(title)}`);
  const game = results.find(g => g.title.toLowerCase() === title.toLowerCase());
  if (!game) throw new Error(`No exact IGDB result for ${title}. Stop and select a real game before presenting.`);
  games[title] = game;
}
const jonathan = await account('demo_jonathan', '');
const maya = await account('demo_maya', 'Mostly indie games and adventures I can get lost in. I like a challenge, but I usually skip the optional collectibles.');
const alex = await account('alex_afterwork', 'Usually playing for an hour after work. Puzzle games and short sessions suit me better than a huge open world.');
const nina = await account('nina_coop', 'Looking for the next game to play with my sister. Co-op is my favorite, especially when we can go at our own pace.');
const sam = await account('sam_checkpoint', 'Platformers, hidden paths, and one more attempt at the boss. Slowly catching up on the indie games I missed.');
const riley = await account('riley_weekends', 'Weekend player with a longer backlog than I need. I like trying different genres, even when a popular game does not click for me.');
const otherPlayers = [maya, alex, nina, sam, riley];
if (reset) {
  for (const player of otherPlayers) {
    const response = await fetch(`${base}/follows/${player.user.userId}`, {
      method: 'DELETE', signal: AbortSignal.timeout(40000),
      headers: { Authorization: `Bearer ${jonathan.token}` },
    });
    if (![204, 404].includes(response.status)) throw new Error(`Unable to reset demo follow: ${response.status}`);
  }
}
async function entry(login, title, status, personalRating = null, recommended = null, review = null) {
  const library = await request('/library', 'GET', null, login.token);
  let item = library.find(e => e.igdbId === games[title].igdbId);
  if (!item) item = await request('/library', 'POST', { igdbId: games[title].igdbId, status, personalRating, recommended }, login.token);
  if (review) {
    const profile = await request(`/users/${login.user.username}`, 'GET', null, login.token);
    if (!profile.reviews.some(r => r.igdbId === games[title].igdbId)) {
      await request('/reviews', 'POST', { userGameId: item.userGameId, body: review }, login.token);
    }
  }
}
await entry(maya, 'Celeste', 'COMPLETED', 9, true, 'The quick restarts kept me trying even when a room took ages. I finished the main climb, but left most of the strawberries for another day. A few sections tested my patience, though the ending made the effort worth it.');
await entry(maya, 'Hollow Knight', 'COMPLETED', 10, true, 'Finding a new route back to somewhere familiar was my favorite part. I got lost more than once, but exploring never felt wasted. The boss fights took practice, so I would recommend it to someone who enjoys a challenge.');
await entry(maya, 'Stardew Valley', 'PLAYING', 9, true, 'I went in planning to build the perfect farm and ended up spending most of my time fishing. There is always a small goal to work toward. I like it most when I stop worrying about doing everything efficiently.');
await entry(jonathan, 'Portal 2', 'COMPLETED', 9, true, 'Finished the single-player story and immediately wanted more puzzles. The new mechanics build on each other without needing long explanations. A couple of rooms had me stuck, but solving them felt great.');
await entry(jonathan, 'Stardew Valley', 'WANT_TO_PLAY');
await entry(jonathan, 'Hollow Knight', 'DROPPED');

await entry(alex, 'Portal 2', 'COMPLETED', 9, true, 'This worked really well in short evening sessions. I could finish a few chambers and feel like I had made progress. The humor was a nice bonus, but the puzzles are what kept me coming back.');
await entry(alex, 'Celeste', 'PLAYING', 8, true, 'Only partway through, but the movement feels precise and the retries are quick. I can practice one room for ten minutes and come back later. I am skipping the harder collectibles for now.');
await entry(alex, 'Hollow Knight', 'DROPPED', 6, false, 'I liked the atmosphere and combat, but I kept forgetting where I was going between sessions. Repeating the route after a tough fight wore me down. I can see the appeal, but it did not fit the time I had to play.');

await entry(nina, 'Stardew Valley', 'PLAYING', 9, true, 'My sister handles the animals while I spend far too long arranging the crops. We usually have completely different plans for the day, and that is half the fun. Easy to lose an entire evening to just one more day.');
await entry(nina, 'Portal 2', 'COMPLETED', 9, true, 'The co-op puzzles made us actually talk through our ideas instead of both trying things at random. We still managed to drop each other into danger a few times. I would happily play another set of chambers like these.');
await entry(nina, 'Celeste', 'WANT_TO_PLAY');

await entry(sam, 'Celeste', 'COMPLETED', 10, true, 'I kept saying one more room and playing for another hour. The fast restarts made failure feel like practice instead of lost progress. Finishing a section that seemed impossible at first was incredibly satisfying.');
await entry(sam, 'Hollow Knight', 'COMPLETED', 9, true, 'I loved piecing the map together and finding paths I had walked past earlier. Some boss attempts involved more walking than I wanted, but the exploration kept pulling me back. One I will probably replay.');
await entry(sam, 'Portal 2', 'WANT_TO_PLAY');

await entry(riley, 'Portal 2', 'COMPLETED', 8, true, 'The single-player puzzles still hold up, and the pacing kept things moving. I had seen some of the jokes before, so those landed less strongly for me. Still an easy recommendation if you enjoy puzzle games.');
await entry(riley, 'Stardew Valley', 'DROPPED', 6, false, 'I wanted a relaxing game, but started treating every day like a checklist. That is probably more about how I played than the game itself. The routine did not click for me, so I moved on after a few sessions.');
await entry(riley, 'Celeste', 'PLAYING', 7, null, 'I am enjoying the music and the short rooms, but need breaks when I get stuck. It is harder than what I usually play. I want to get farther before deciding whether I would recommend it to friends.');
const dir = path.dirname(fileURLToPath(import.meta.url));
await fs.mkdir(path.join(dir, '..', 'server', 'target', 'presentation-build'), { recursive: true });
await fs.writeFile(path.join(dir, '..', 'server', 'target', 'presentation-build', 'demo-manifest.json'), JSON.stringify({ games, users: { jonathan: jonathan.user, maya: maya.user, alex: alex.user, nina: nina.user, sam: sam.user, riley: riley.user } }, null, 2));
console.log('Demo data ready. Login: demo_jonathan@example.com. Password: DemoOnly9!');
console.log('Six fictional users, eighteen library entries, and fourteen reviews are prepared.');
if (reset) console.log('Starting state restored: your biography is blank, Celeste is absent from your library, and the seeded players are not followed.');
