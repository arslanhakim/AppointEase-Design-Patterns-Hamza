param([string]$Jar = (Join-Path $PSScriptRoot '../target/appointease-0.1.0-SNAPSHOT.jar'))
$ErrorActionPreference = 'Stop'
$cases = @(
    @{ Name = 'smoke'; Input = "0`n"; Zone = 'Europe/Vilnius'; Expected = 'Welcome to AppointEase!' },
    @{ Name = 'invalid'; Input = "invalid`n4`nunknown`n2`n0`n"; Zone = 'Europe/Vilnius'; Expected = 'Unable to complete request' },
    @{ Name = 'empty-eof'; Input = ''; Zone = 'Europe/Vilnius'; Expected = 'End of input' },
    @{ Name = 'partial-eof'; Input = "4`n1`n"; Zone = 'Europe/Vilnius'; Expected = 'End of input' },
    @{ Name = 'utc'; Input = "10`nseed-ready-001`n0`n"; Zone = 'UTC'; Expected = 'Status: COMPLETED' },
    @{ Name = 'karachi'; Input = "10`nseed-ready-001`n0`n"; Zone = 'Asia/Karachi'; Expected = 'Status: COMPLETED' }
)
foreach ($case in $cases) {
    $start = New-Object System.Diagnostics.ProcessStartInfo
    $start.FileName = 'java'
    $start.Arguments = '-Dappointease.zone=' + $case.Zone + ' -jar "' + (Resolve-Path -LiteralPath $Jar).Path + '"'
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
        $process.StandardInput.Write($case.Input)
        $process.StandardInput.Close()
        if (-not $process.WaitForExit(20000)) { $process.Kill(); throw "$($case.Name) timed out" }
        $output = $stdout.GetAwaiter().GetResult()
        $errors = $stderr.GetAwaiter().GetResult()
        [IO.File]::WriteAllText((Join-Path $PSScriptRoot "../target/qa-$($case.Name).txt"), $output + $errors)
        if ($process.ExitCode -ne 0 -or $errors -or -not $output.Contains($case.Expected) -or
            -not $output.Contains('Goodbye.') -or -not $output.Contains("Zone: $($case.Zone)")) {
            throw "$($case.Name) failed: exit $($process.ExitCode), $errors"
        }
        if ($output.Contains('Booked successfully')) { throw 'Unexpected booking from incomplete/invalid input' }
        if ($case.Name -eq 'invalid' -and (-not $output.Contains('Invalid menu option') -or -not $output.Contains('EUR 60.00'))) {
            throw 'Invalid input did not recover to the services menu'
        }
        Write-Host "$($case.Name): child exit $($process.ExitCode), assertions passed"
    } finally { $process.Dispose() }
}
