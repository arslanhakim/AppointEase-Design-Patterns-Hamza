param([string]$Destination = (Join-Path $PSScriptRoot '../AppointEase_DesignPatterns_Submission.zip'))
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$files = @('pom.xml', 'README.md', 'AGENTS.md', 'PROJECT_MEMORY.md', '.gitignore')
foreach ($directory in @('src', 'docs', 'scripts')) {
    $files += Get-ChildItem -LiteralPath (Join-Path $root $directory) -Recurse -File |
        Where-Object { $_.Extension -in @('.java', '.md', '.puml', '.svg', '.png', '.ps1') } |
        ForEach-Object { $_.FullName.Substring($root.Length + 1).Replace('\', '/') }
}
$files = @($files | Sort-Object -Unique)
foreach ($required in @('src/main/java/com/appointease/Main.java', 'src/test/java/com/appointease/ArchitectureTest.java',
        'docs/rubric-evidence.md', 'docs/evaluation-criteria-draft.md', 'docs/S6-AUDIT.md', 'docs/SESSION_HISTORY.md',
        'PROJECT_MEMORY.md', 'src/main/java/com/appointease/application/CommittedEventDispatcher.java',
        'src/main/java/com/appointease/domain/appointment/Appointment.java', 'scripts/demo.ps1', 'scripts/verify-console.ps1')) {
    if ($required -notin $files) { throw "Missing required file: $required" }
}
foreach ($diagram in @('overview', 'booking-sequence', 'creation-sequence', 'lifecycle-sequence', 'scheduling-sequence', 'console-sequence')) {
    foreach ($extension in @('puml', 'svg', 'png')) {
        if ("docs/uml/$diagram.$extension" -notin $files) { throw "Missing UML artifact: $diagram.$extension" }
    }
}
foreach ($file in $files) {
    if ($file -match '(^|/)(target|\.git|\.idea|\.vscode|\.audit-tools|\.submission-check)(/|$)' -or
        $file -match '(?i)(secret|credential|\.env|\.log$|\.tmp$)') { throw "Excluded file: $file" }
}
$stream = [IO.File]::Open($Destination, [IO.FileMode]::Create, [IO.FileAccess]::ReadWrite)
$archive = New-Object IO.Compression.ZipArchive($stream, [IO.Compression.ZipArchiveMode]::Create)
try {
    foreach ($file in $files) {
        [void][IO.Compression.ZipFileExtensions]::CreateEntryFromFile($archive, (Join-Path $root $file), $file)
    }
} finally { $archive.Dispose(); $stream.Dispose() }
$archive = [IO.Compression.ZipFile]::OpenRead($Destination)
try {
    if ($archive.Entries.Count -ne $files.Count) { throw 'Archive count mismatch' }
    foreach ($entry in $archive.Entries) {
        $inputStream = $entry.Open()
        $sha = [Security.Cryptography.SHA256]::Create()
        try { $hash = [BitConverter]::ToString($sha.ComputeHash($inputStream)).Replace('-', '') }
        finally { $inputStream.Dispose(); $sha.Dispose() }
        if ($hash -ne (Get-FileHash -LiteralPath (Join-Path $root $entry.FullName) -Algorithm SHA256).Hash) {
            throw "Archive content mismatch: $($entry.FullName)"
        }
    }
} finally { $archive.Dispose() }
Write-Host "Validated $($files.Count) entries against source bytes: $Destination"
Get-FileHash -LiteralPath $Destination -Algorithm SHA256
