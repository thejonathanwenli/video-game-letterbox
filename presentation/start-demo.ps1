# Starts this project's demo only. Does not drop or change production/test databases.
[CmdletBinding()]
param()
$ErrorActionPreference = 'Stop'
$repoPath = Split-Path -Parent $PSScriptRoot
$runtimeDir = Join-Path $repoPath 'server\target\presentation-build'
New-Item -ItemType Directory -Force -Path $runtimeDir | Out-Null
foreach ($port in @(8080, 5173)) {
    if (Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue) {
        throw "Port $port is already running. Stop that server first; this script will not replace it."
    }
}
foreach ($name in @('DB_USERNAME','DB_PASSWORD','VGL_DB_USERNAME','VGL_DB_PASSWORD','IGDB_CLIENT_ID','IGDB_CLIENT_SECRET','JWT_SECRET')) {
    $value = [Environment]::GetEnvironmentVariable($name, 'User')
    if ($value) { [Environment]::SetEnvironmentVariable($name, $value, 'Process') }
}
if (-not $env:VGL_DB_USERNAME) { $env:VGL_DB_USERNAME = $env:DB_USERNAME }
if (-not $env:VGL_DB_PASSWORD) { $env:VGL_DB_PASSWORD = $env:DB_PASSWORD }
if (-not $env:VGL_DB_USERNAME -or -not $env:VGL_DB_PASSWORD -or -not $env:JWT_SECRET -or -not $env:IGDB_CLIENT_ID -or -not $env:IGDB_CLIENT_SECRET) {
    throw 'Missing saved database, JWT, or IGDB credentials. No secrets have been printed.'
}
$mysqlPath = 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe'
$env:MYSQL_PWD = $env:VGL_DB_PASSWORD
try {
    & $mysqlPath --host=localhost "--user=$env:VGL_DB_USERNAME" -e 'CREATE DATABASE IF NOT EXISTS video_game_letterbox_demo;'
    if ($LASTEXITCODE -ne 0) { throw 'Unable to create the demo database.' }
    $tableCount = & $mysqlPath --host=localhost "--user=$env:VGL_DB_USERNAME" --batch --skip-column-names -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'video_game_letterbox_demo';"
    if ($LASTEXITCODE -ne 0) { throw 'Unable to check the demo database.' }
    if ([int]$tableCount -eq 0) {
        $ddl = (Get-Content -LiteralPath (Join-Path $repoPath 'server\database\schema-test.sql') | Select-Object -Skip 3) -join "`n"
        if ($ddl -match '(?im)^\s*(drop|create)\s+database|^\s*use\s') { throw 'Unexpected database directive; refusing to apply schema.' }
        $ddl | & $mysqlPath --host=localhost "--user=$env:VGL_DB_USERNAME" --database=video_game_letterbox_demo
        if ($LASTEXITCODE -ne 0) { throw 'Unable to initialize the demo schema.' }
    }
} finally { Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue }
$env:VGL_DB_URL = 'jdbc:mysql://localhost:3306/video_game_letterbox_demo'
$env:VGL_DEMO_DATABASE = 'video_game_letterbox_demo'
$mavenPath = 'C:\Users\Jonathan Li\.m2\wrapper\dists\apache-maven-3.9.9\8e74001100ff70d6af083c5511fcc5ec49282d7017cde82c3698eee8fdf86698\bin\mvn.cmd'
$nodePath = (Get-Command node.exe).Source
$backend = Start-Process -FilePath $mavenPath -ArgumentList 'spring-boot:run' -WorkingDirectory (Join-Path $repoPath 'server') -WindowStyle Hidden -PassThru -RedirectStandardOutput (Join-Path $runtimeDir 'backend.log') -RedirectStandardError (Join-Path $runtimeDir 'backend-error.log')
$frontend = Start-Process -FilePath $nodePath -ArgumentList 'node_modules/vite/bin/vite.js','--host','127.0.0.1','--port','5173','--strictPort' -WorkingDirectory (Join-Path $repoPath 'client') -WindowStyle Hidden -PassThru -RedirectStandardOutput (Join-Path $runtimeDir 'frontend.log') -RedirectStandardError (Join-Path $runtimeDir 'frontend-error.log')
@{ backend = $backend.Id; frontend = $frontend.Id; repo = $repoPath } | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $runtimeDir 'demo-processes.json')
Write-Host 'Starting the demo server. Waiting for localhost:8080...'
$ready = $false
for ($i = 0; $i -lt 60; $i++) {
    try { Invoke-RestMethod 'http://localhost:8080/api/games' -TimeoutSec 2 | Out-Null; $ready = $true; break } catch { Start-Sleep -Seconds 1 }
}
if (-not $ready) { throw "Server did not start. Logs: $runtimeDir" }
& $nodePath (Join-Path $PSScriptRoot 'prepare-demo.mjs')
if ($LASTEXITCODE -ne 0) { throw 'Demo server started, but sample-data setup failed. See the message above.' }
Write-Host 'Ready: http://127.0.0.1:5173'
Write-Host 'Database: video_game_letterbox_demo (production and test data untouched).'
