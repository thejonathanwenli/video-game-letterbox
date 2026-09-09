# Stops only the process trees started by start-demo.ps1.
$ErrorActionPreference = 'Stop'
$repoPath = Split-Path -Parent $PSScriptRoot
$statePath = Join-Path $repoPath 'server\target\presentation-build\demo-processes.json'
if (-not (Test-Path -LiteralPath $statePath)) { Write-Host 'No recorded demo processes.'; return }
$state = Get-Content -LiteralPath $statePath -Raw | ConvertFrom-Json
if ($state.repo -ne $repoPath) { throw 'Process record belongs to another project.' }
function Stop-DemoTree([int]$processId) {
    $p = Get-CimInstance Win32_Process -Filter "ProcessId=$processId" -ErrorAction SilentlyContinue
    if (-not $p) { return }
    if ($p.CommandLine -notmatch 'mvn\.cmd|vite[/\\]bin[/\\]vite\.js') { throw "PID $processId no longer matches a demo launcher. Not stopping it." }
    & taskkill.exe /PID $processId /T /F | Out-Null
}
Stop-DemoTree $state.backend
Stop-DemoTree $state.frontend
Remove-Item -LiteralPath $statePath
Write-Host 'Demo servers stopped. Demo data remains for rehearsal.'
