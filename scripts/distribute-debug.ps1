param(
    [string]$ReleaseNotes = 'RunMate Compose Lab: native Health Connect dashboard.',
    [string]$Testers = $env:RUNMATE_COMPOSE_TESTERS
)

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
$servicesPath = Join-Path $repoRoot 'app\google-services.json'
$apkPath = Join-Path $repoRoot 'app\build\outputs\apk\debug\app-debug.apk'
$packageName = 'com.runmate.compose.debug'

if (-not (Test-Path -LiteralPath $servicesPath)) {
    throw 'app/google-services.json is required and must contain the Compose Lab Firebase client.'
}

$services = Get-Content -LiteralPath $servicesPath -Raw | ConvertFrom-Json
$firebaseClient = $services.client | Where-Object {
    $_.client_info.android_client_info.package_name -eq $packageName
} | Select-Object -First 1
if (-not $firebaseClient) {
    throw "Firebase client for $packageName was not found in app/google-services.json."
}

$appId = $firebaseClient.client_info.mobilesdk_app_id
if (-not $appId) { throw 'The Compose Lab Firebase App ID is missing.' }

$versionCode = [int][DateTimeOffset]::UtcNow.ToUnixTimeSeconds()

Push-Location $repoRoot
try {
    & .\gradlew.bat clean assembleDebug -PnativeHealthDashboard=true "-PlabVersionCode=$versionCode" --console=plain
    if ($LASTEXITCODE -ne 0) { throw 'Compose Lab debug build failed.' }
    if (-not (Test-Path -LiteralPath $apkPath)) { throw 'Compose Lab debug APK was not produced.' }

    $arguments = @(
        'appdistribution:distribute',
        $apkPath,
        '--app', $appId,
        '--release-notes', $ReleaseNotes
    )
    if ($Testers) { $arguments += @('--testers', $Testers) }

    & firebase.cmd @arguments
    if ($LASTEXITCODE -ne 0) { throw 'Firebase App Distribution failed.' }
} finally {
    Pop-Location
}
