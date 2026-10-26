param([string]$Jar = (Join-Path $PSScriptRoot '../target/appointease-0.1.0-SNAPSHOT.jar'))
$ErrorActionPreference = 'Stop'
try { $zone = [TimeZoneInfo]::FindSystemTimeZoneById('Europe/Vilnius') }
catch [TimeZoneNotFoundException] { $zone = [TimeZoneInfo]::FindSystemTimeZoneById('FLE Standard Time') }
$date = [TimeZoneInfo]::ConvertTimeFromUtc([DateTime]::UtcNow, $zone).Date.AddDays(1)
while ($date.DayOfWeek -in @('Saturday', 'Sunday')) { $date = $date.AddDays(1) }
$day = $date.ToString('yyyy-MM-dd')

function Booking([string]$At, [string]$Policy, [bool]$Confirm) {
    $lines = @('4', '1', 'provider-health-001', '1', '1', $day, $At, '1', 'y', 'y', $Policy)
    if ($Confirm) { $lines += 'y' }
    return $lines
}
function Run-Jar([string[]]$Lines, [string]$Name) {
    $start = New-Object System.Diagnostics.ProcessStartInfo
    $start.FileName = 'java'
    $start.Arguments = '-jar "' + (Resolve-Path -LiteralPath $Jar).Path + '"'
    $start.UseShellExecute = $false
    $start.CreateNoWindow = $true
    $start.RedirectStandardInput = $true
    $start.RedirectStandardOutput = $true
    $start.RedirectStandardError = $true
    $process = New-Object System.Diagnostics.Process
    $process.StartInfo = $start
    try {
        [void]$process.Start()
        $stdout = $process.StandardOutput.ReadToEndAsync()
        $stderr = $process.StandardError.ReadToEndAsync()
        foreach ($line in $Lines) { $process.StandardInput.WriteLine($line) }
        $process.StandardInput.Close()
        if (-not $process.WaitForExit(20000)) {
            $process.Kill()
            throw "$Name timed out"
        }
        $output = $stdout.GetAwaiter().GetResult()
        $errors = $stderr.GetAwaiter().GetResult()
        $destination = Join-Path $PSScriptRoot "../target/demo-$Name.txt"
        [IO.File]::WriteAllText($destination, $output + $errors)
        if ($process.ExitCode -ne 0 -or $errors) { throw "$Name failed: exit $($process.ExitCode), $errors" }
        Write-Host "$Name : exit 0; transcript $destination"
        return $output
    } finally { $process.Dispose() }
}
$lines = @('1', '2', 'invalid')
$lines += Booking '10:00' '1' $true
$lines += Booking '10:00' '1' $false
$lines += Booking '10:30' '1' $true
$lines += @('7', '@1', '8', '@2', $day, '10:30', '2', 'y', '6', '@2')
$lines += @('8', '@1', $day, '11:30', '2', 'y', '9', '@1')
$lines += Booking '11:30' '1' $true
$lines += @('4', '2', 'provider-salon-001', '2', '1', $day, '10:00', '2', 'n', 'n', '1', 'y')
$lines += @('10', 'seed-ready-001', '6', 'seed-ready-001', '5', '1', '11', '0')
$normal = Run-Jar $lines 'workflow'
foreach ($expected in @('Booked successfully', 'Provider slot is unavailable', 'RESCHEDULED', 'CANCELLED',
        'COMPLETED', 'EUR 52.00', 'Mode: IN_PERSON', 'Mode: ONLINE', 'Audit history', 'Notifications (simulated)', 'Goodbye.')) {
    if (-not $normal.Contains($expected)) { throw "Missing workflow evidence: $expected" }
}
if (($normal.Split(@('Booked successfully'), [StringSplitOptions]::None).Length - 1) -ne 4) {
    throw 'Expected exactly four successful interactive bookings'
}
if (-not $normal.Contains('Unable to complete request: Provider slot is unavailable')) {
    throw 'Missing rejected buffered reschedule'
}
if ($normal.Contains('End of input')) { throw 'Normal workflow did not take explicit exit' }
$eof = Run-Jar @() 'eof'
if (-not $eof.Contains('End of input')) { throw 'Missing EOF handling' }
$partial = Run-Jar @('4', '1') 'partial-eof'
if (-not $partial.Contains('End of input') -or $partial.Contains('Booked successfully')) {
    throw 'Partial EOF did not discard unfinished booking'
}
Write-Host "Demonstration passed for $day : four bookings, lifecycle/errors/history, normal exit and two EOF paths."
