<#
.SYNOPSIS
    Read-only prerequisite check for commencis-interview-lab.

.DESCRIPTION
    Reports what is present on this machine and what is missing. It NEVER installs, downloads,
    updates or configures anything: no npm install, no SDK manager, no Appium driver install, no
    environment variable is written. Every finding ends with the command YOU would run to fix it.

.PARAMETER AppiumServerUrl
    Appium endpoint to probe. Defaults to http://127.0.0.1:4723.

.EXAMPLE
    .\scripts\preflight.ps1
    .\scripts\preflight.ps1 -AppiumServerUrl http://remote-mac:4723
#>
[CmdletBinding()]
param(
    [string] $AppiumServerUrl = 'http://127.0.0.1:4723'
)

$ErrorActionPreference = 'Continue'
$ProgressPreference = 'SilentlyContinue'

$script:Findings = @()

function Write-Section {
    param([string] $Title)
    Write-Host ''
    Write-Host "-- $Title " -ForegroundColor Cyan -NoNewline
    Write-Host ('-' * [Math]::Max(0, 60 - $Title.Length)) -ForegroundColor DarkGray
}

function Write-Result {
    param(
        [ValidateSet('OK', 'WARN', 'MISSING')] [string] $Status,
        [string] $Label,
        [string] $Detail = '',
        [string] $Hint = ''
    )
    $color = 'Green'
    if ($Status -eq 'WARN') { $color = 'Yellow' }
    if ($Status -eq 'MISSING') { $color = 'Red' }

    Write-Host ('  [{0,-7}] ' -f $Status) -ForegroundColor $color -NoNewline
    Write-Host ('{0,-26}' -f $Label) -NoNewline
    Write-Host $Detail -ForegroundColor Gray

    if ($Hint -and $Status -ne 'OK') {
        Write-Host ('             -> ' + $Hint) -ForegroundColor DarkGray
    }
    $script:Findings += [pscustomobject]@{ Status = $Status; Label = $Label }
}

function Get-CommandPath {
    param([string] $Name)
    $cmd = Get-Command $Name -ErrorAction SilentlyContinue
    if ($cmd) { return $cmd.Source }
    return $null
}

function Invoke-Tool {
    <#
        Runs a tool and returns its combined output as a plain string, or $null on failure.

        ToString() on each item matters: Windows PowerShell wraps every stderr line from a native
        executable in an ErrorRecord, and piping those straight to Out-String renders them as
        "java.exe : openjdk version ...". Tools like `java -version` write to stderr by design.
    #>
    param([string] $FilePath, [string[]] $Arguments)
    try {
        $lines = & $FilePath @Arguments 2>&1 | ForEach-Object { $_.ToString() }
        return (($lines -join [Environment]::NewLine).Trim())
    } catch {
        return $null
    }
}

Write-Host ''
Write-Host 'commencis-interview-lab preflight (read-only)' -ForegroundColor White
Write-Host ('Repository: ' + (Split-Path -Parent $PSScriptRoot)) -ForegroundColor DarkGray

# --------------------------------------------------------------------------------------------
Write-Section 'Java'

if ($env:JAVA_HOME) {
    $javaExe = Join-Path $env:JAVA_HOME 'bin\java.exe'
    if (Test-Path $javaExe) {
        $version = Invoke-Tool $javaExe @('-version')
        $firstLine = ($version -split "`r?`n")[0]
        Write-Result 'OK' 'JAVA_HOME' "$env:JAVA_HOME  ($firstLine)"
    } else {
        Write-Result 'MISSING' 'JAVA_HOME' "set to '$env:JAVA_HOME' but bin\java.exe is not there" `
            'Point JAVA_HOME at a JDK 21 root, e.g. C:\Users\bartu\.jdks\ms-21.0.12'
    }
} else {
    Write-Result 'MISSING' 'JAVA_HOME' 'not set' `
        '$env:JAVA_HOME = "C:\Users\bartu\.jdks\ms-21.0.12"   (this session only)'
}

$javaOnPath = Get-CommandPath 'java'
if ($javaOnPath) {
    Write-Result 'OK' 'java on PATH' $javaOnPath
} else {
    Write-Result 'WARN' 'java on PATH' 'not found (only needed outside Maven)' `
        'The Maven Wrapper uses JAVA_HOME, so this is not fatal.'
}

# --------------------------------------------------------------------------------------------
Write-Section 'Maven Wrapper'

$repoRoot = Split-Path -Parent $PSScriptRoot
foreach ($file in @('mvnw.cmd', 'mvnw', '.mvn\wrapper\maven-wrapper.properties')) {
    $path = Join-Path $repoRoot $file
    $label = Split-Path -Leaf $file
    if (Test-Path $path) {
        Write-Result 'OK' $label $path
    } else {
        Write-Result 'MISSING' $label 'not found' 'The repository is incomplete; re-clone it.'
    }
}

$wrapperProps = Join-Path $repoRoot '.mvn\wrapper\maven-wrapper.properties'
if (Test-Path $wrapperProps) {
    $distLine = Select-String -Path $wrapperProps -Pattern '^distributionUrl=' -ErrorAction SilentlyContinue
    if ($distLine) {
        $distUrl = ($distLine.Line -split '=', 2)[1]
        Write-Result 'OK' 'pinned Maven' (Split-Path -Leaf $distUrl)
    }
}

# --------------------------------------------------------------------------------------------
Write-Section 'Node and npm'

$nodePath = Get-CommandPath 'node'
if ($nodePath) {
    Write-Result 'OK' 'node' ((Invoke-Tool $nodePath @('--version')) + "  $nodePath")
} else {
    Write-Result 'WARN' 'node' 'not found' `
        'Needed for Appium (mobile) and by allure-maven to build the HTML report.'
}

# npm.cmd, not npm.ps1: the PowerShell shim is blocked by the default execution policy.
$npmPath = Get-CommandPath 'npm.cmd'
if ($npmPath) {
    Write-Result 'OK' 'npm.cmd' ((Invoke-Tool $npmPath @('--version')) + "  $npmPath")
} else {
    Write-Result 'WARN' 'npm.cmd' 'not found' 'Install Node.js LTS from https://nodejs.org'
}

# --------------------------------------------------------------------------------------------
Write-Section 'Appium'

# appium.cmd first: the appium.ps1 shim can be blocked by the execution policy, same as npm.ps1.
$appiumPath = Get-CommandPath 'appium.cmd'
if (-not $appiumPath) { $appiumPath = Get-CommandPath 'appium' }

if ($appiumPath) {
    Write-Result 'OK' 'appium' ((Invoke-Tool $appiumPath @('--version')) + "  $appiumPath")

    $drivers = Invoke-Tool $appiumPath @('driver', 'list', '--installed')
    if ($drivers) {
        $installed = ($drivers -split "`r?`n" |
            Where-Object { $_ -match '\S' } |
            ForEach-Object { $_.Trim() }) -join ' | '
        Write-Result 'OK' 'appium drivers' $installed
    } else {
        Write-Result 'WARN' 'appium drivers' 'could not read the driver list' `
            'appium.cmd driver install uiautomator2@7.5.2'
    }
} else {
    Write-Result 'MISSING' 'appium' 'not found' `
        'npm.cmd install -g appium@3.4.2   (then: appium.cmd driver install uiautomator2@7.5.2)'
}

# --------------------------------------------------------------------------------------------
Write-Section 'Android SDK'

$androidHome = $env:ANDROID_HOME
if (-not $androidHome) { $androidHome = $env:ANDROID_SDK_ROOT }

if ($androidHome -and (Test-Path $androidHome)) {
    Write-Result 'OK' 'ANDROID_HOME' $androidHome
} elseif ($androidHome) {
    Write-Result 'MISSING' 'ANDROID_HOME' "set to '$androidHome' but that path does not exist" `
        'Fix it in Windows environment variables, then open a new terminal.'
} else {
    $guess = Join-Path $env:LOCALAPPDATA 'Android\Sdk'
    if (Test-Path $guess) {
        Write-Result 'MISSING' 'ANDROID_HOME' "not set, but an SDK exists at $guess" `
            "`$env:ANDROID_HOME = `"$guess`"   (this session only)"
    } else {
        Write-Result 'MISSING' 'ANDROID_HOME' 'not set and no SDK found in the default location' `
            'Install Android Studio, then SDK Manager -> SDK Platforms + SDK Tools (Platform-Tools, Emulator).'
    }
}

$adbPath = Get-CommandPath 'adb'
if (-not $adbPath -and $androidHome) {
    $candidate = Join-Path $androidHome 'platform-tools\adb.exe'
    if (Test-Path $candidate) { $adbPath = $candidate }
}

if ($adbPath) {
    Write-Result 'OK' 'adb' ((Invoke-Tool $adbPath @('version')) -split "`r?`n")[0]

    $devices = Invoke-Tool $adbPath @('devices', '-l')
    $deviceLines = @()
    if ($devices) {
        $deviceLines = $devices -split "`r?`n" |
            Where-Object { $_ -match '\S' -and $_ -notmatch '^List of devices' -and $_ -notmatch '^\*' }
    }
    if ($deviceLines.Count -gt 0) {
        foreach ($line in $deviceLines) {
            Write-Result 'OK' 'device' $line.Trim()
        }
    } else {
        Write-Result 'MISSING' 'device' 'no emulator or phone attached' `
            'Start an emulator, or plug in the phone and accept the USB debugging prompt.'
    }
} else {
    Write-Result 'MISSING' 'adb' 'not found' `
        'Add %ANDROID_HOME%\platform-tools to PATH.'
}

# --------------------------------------------------------------------------------------------
Write-Section 'Appium server'

$statusUrl = ($AppiumServerUrl.TrimEnd('/')) + '/status'
try {
    $status = Invoke-RestMethod -Uri $statusUrl -TimeoutSec 5 -ErrorAction Stop
    $ready = 'unknown'
    if ($null -ne $status.value -and $null -ne $status.value.ready) { $ready = $status.value.ready }
    Write-Result 'OK' 'appium /status' "$AppiumServerUrl  ready=$ready"
} catch {
    Write-Result 'MISSING' 'appium /status' "no answer from $AppiumServerUrl" `
        'Start it in another terminal:  appium.cmd   (this project never starts it for you)'
}

# --------------------------------------------------------------------------------------------
Write-Section 'Summary'

$missing = @($script:Findings | Where-Object { $_.Status -eq 'MISSING' })

$javaReady = @($script:Findings | Where-Object { $_.Label -eq 'JAVA_HOME' -and $_.Status -eq 'OK' }).Count -gt 0
$deviceReady = @($script:Findings | Where-Object { $_.Label -eq 'device' -and $_.Status -eq 'OK' }).Count -gt 0
$serverReady = @($script:Findings | Where-Object { $_.Label -eq 'appium /status' -and $_.Status -eq 'OK' }).Count -gt 0

if ($javaReady) {
    Write-Host '  API tests      : ready   ->  .\mvnw.cmd clean verify' -ForegroundColor Green
} else {
    Write-Host '  API tests      : blocked ->  set JAVA_HOME first' -ForegroundColor Red
}

if ($javaReady -and $deviceReady -and $serverReady) {
    Write-Host '  Android tests  : ready   ->  .\mvnw.cmd clean verify "-Dcucumber.filter.tags=@mobile" -Dandroid.udid=<serial>' -ForegroundColor Green
} else {
    Write-Host '  Android tests  : blocked ->  needs Appium server + attached device (see above)' -ForegroundColor Yellow
}

Write-Host '  iOS tests      : config keys exist (platform=ios), but XCUITest needs an Appium' -ForegroundColor DarkGray
Write-Host '                   server on macOS + Xcode; not verified on this machine.' -ForegroundColor DarkGray

Write-Host ''
Write-Host ("  {0} check(s) missing. Nothing was installed or changed." -f $missing.Count) -ForegroundColor DarkGray
Write-Host ''

# Reporting-only script: a missing prerequisite is information, not a build failure.
exit 0
