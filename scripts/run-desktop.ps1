param([switch]$Verify, [switch]$VerifyS7B, [switch]$VerifyS7C, [switch]$Capture, [string]$MavenRepository = (Join-Path $env:USERPROFILE '.m2/repository'))
$ErrorActionPreference = 'Stop'
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
if ($Verify -or $VerifyS7B -or $VerifyS7C -or $Capture) {
    $entrypoint = 'com.appointease.presentation.desktop.DesktopVisualCheck'
    $classpath = (Join-Path $root 'target/test-classes') + [IO.Path]::PathSeparator + $jar
}
if ($VerifyS7B -or $VerifyS7C -or $Capture) { $entrypoint = 'com.appointease.presentation.desktop.DesktopS7BCheck' }
$applicationArgs = @()
if ($Capture) { $applicationArgs = @('--capture') }
if ($VerifyS7C) { $applicationArgs += '--s7c' }
# Uses only downloaded local modules: no Maven process, network or console dependency.
& java --module-path ($modules -join [IO.Path]::PathSeparator) --add-modules javafx.controls -cp $classpath $entrypoint @applicationArgs
exit $LASTEXITCODE
