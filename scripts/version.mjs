#!/usr/bin/env node
// The project's version lives in one file, VERSION, at the repository root.
//   - the Android build reads it directly (versionName, and versionCode = major*10000 + minor*100 + patch);
//   - the web app has it as the APP_VERSION constant in web/euclids-compass.html, which this script keeps in step.
//
//   node scripts/version.mjs                 print the version
//   node scripts/version.mjs set 1.3.0       change the version everywhere
//   node scripts/version.mjs check [tag]     fail if anything disagrees (and if `tag`, e.g. v1.3.0, is not this version)
import { readFileSync, writeFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { join } from 'node:path';

const root = fileURLToPath(new URL('..', import.meta.url));
const read = (p) => readFileSync(join(root, p), 'utf8');
const SEMVER = /^\d+\.\d+\.\d+$/;
const HTML = 'web/euclids-compass.html';
const APP_RE = /(const APP_VERSION = ')[^']*(')/;

const currentVersion = () => read('VERSION').trim();

function mismatches(version) {
  const bad = [];
  const m = read(HTML).match(/const APP_VERSION = '([^']*)'/);
  if (!m || m[1] !== version) bad.push(`${HTML} APP_VERSION says ${m && m[1]}`);
  if (!/rootProject\.file\("\.\.\/VERSION"\)/.test(read('Android/app/build.gradle.kts'))) bad.push('Android/app/build.gradle.kts does not read VERSION');
  return bad;
}

const [cmd, arg] = process.argv.slice(2);
if (!cmd) {
  console.log(currentVersion());
} else if (cmd === 'set') {
  if (!SEMVER.test(arg || '')) { console.error('Usage: node scripts/version.mjs set MAJOR.MINOR.PATCH'); process.exit(2); }
  writeFileSync(join(root, 'VERSION'), `${arg}\n`);
  writeFileSync(join(root, HTML), read(HTML).replace(APP_RE, `$1${arg}$2`));
  console.log(`Version is now ${arg} (VERSION and ${HTML}). Also update the version line in README.md.`);
} else if (cmd === 'check') {
  const version = currentVersion();
  const bad = mismatches(version);
  if (arg && arg !== `v${version}`) bad.push(`tag ${arg} does not match VERSION ${version}`);
  if (bad.length) { console.error(bad.join('\n')); process.exit(1); }
  console.log(`All version files agree: ${version}`);
} else {
  console.error('Unknown command'); process.exit(2);
}
