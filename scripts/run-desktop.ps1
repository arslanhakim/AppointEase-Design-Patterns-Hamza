param([switch]$Verify, [switch]$VerifyS7B, [switch]$VerifyS7C, [switch]$VerifyS8A, [switch]$VerifyS8B, [switch]$VerifyS8C, [switch]$VerifySidebar, [switch]$ReduceMotion, [switch]$Capture, [switch]$CaptureIconFinal, [string]$MavenRepository = (Join-Path $env:USERPROFILE '.m2/repository'))
$ErrorActionPreference = 'Stop'
if ($CaptureIconFinal) { $VerifySidebar = $true; $Capture = $true }
$root = Split-Path $PSScriptRoot -Parent
[xml]$pom = Get-Content -LiteralPath (Join-Path $root 'pom.xml')
$version = $pom.project.properties.'javafx.version'
$modules = @('base', 'graphics', 'controls') | ForEach-Object {
    $path = Join-Path $MavenRepository "org/openjfx/javafx-$_/$version/javafx-$_-$version-win.jar"
    if (-not (Test-Path -LiteralPath $path)) { throw "Missing JavaFX $version. Run mvn clean verify first, or supply -MavenRepository." }
    $path
}
$jar = Join-Path $root 'target/appointease-0.1.0-SNAPSHOT.jar'
if (-not (Test-Path -LiteralPath $jar)) { throw 'Run mvn clean verify first.' }
$entrypoint = 'com.appointease.DesktopMain'
$classpath = $jar
if ($Verify -or $VerifyS7B -or $VerifyS7C -or $VerifyS8A -or $VerifyS8B -or $VerifyS8C -or $VerifySidebar -or $Capture) {
    $entrypoint = 'com.appointease.presentation.desktop.DesktopVisualCheck'
    $classpath = (Join-Path $root 'target/test-classes') + [IO.Path]::PathSeparator + $jar
}
if ($VerifyS7B -or $VerifyS7C -or $Capture) { $entrypoint = 'com.appointease.presentation.desktop.DesktopS7BCheck' }
if ($VerifySidebar) { $entrypoint = 'com.appointease.presentation.desktop.DesktopSidebarCheck' }
if ($VerifyS8A) { $entrypoint = 'com.appointease.presentation.desktop.DesktopS8ACheck' }
$applicationArgs = @()
if ($VerifyS8B) { $entrypoint = 'com.appointease.presentation.desktop.DesktopS8BCheck' }
if ($VerifyS8C) { $entrypoint = 'com.appointease.presentation.desktop.DesktopS8CCheck' }
if ($Capture) { $applicationArgs = @('--capture') }
if ($CaptureIconFinal) { $applicationArgs += '--icon-final' }
if ($VerifyS7C) { $applicationArgs += '--s7c' }
# Read the Windows client-area animation preference at launch; unknown environments stay still.
$reduceSidebarMotion = $true
if (-not $ReduceMotion) {
    try {
        Add-Type -AssemblyName PresentationFramework
        $reduceSidebarMotion = -not [System.Windows.SystemParameters]::ClientAreaAnimation
    } catch { $reduceSidebarMotion = $true }
}
$motionOption = '-Dappointease.reduceMotion=' + $reduceSidebarMotion.ToString().ToLowerInvariant()
# Uses only downloaded local modules: no Maven process, network or console dependency.
& java $motionOption --module-path ($modules -join [IO.Path]::PathSeparator) --add-modules javafx.controls -cp $classpath $entrypoint @applicationArgs
exit $LASTEXITCODE
