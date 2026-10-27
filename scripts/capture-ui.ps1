param(
    [ValidateSet('Desktop', 'Compact', 'Navigation')]
    [string]$View = 'Desktop',
    [ValidatePattern('^[0-9]{2}-[a-z0-9-]+$')]
    [string]$S7BName,
    [ValidateSet('S7B', 'S7C', 'S8A', 'S8B', 'S8C', 'sidebar-toggle', 'sidebar-icon-final')]
    [string]$Slice = 'S7B'
)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
Add-Type -AssemblyName System.Windows.Forms
Add-Type @'
using System;
using System.Runtime.InteropServices;
public static class AppointEaseCapture {
    [StructLayout(LayoutKind.Sequential)]
    public struct Rect { public int Left, Top, Right, Bottom; }
    [StructLayout(LayoutKind.Sequential)]
    public struct Point { public int X, Y; }
    [DllImport("user32.dll")] public static extern bool SetProcessDPIAware();
    [DllImport("user32.dll")] public static extern bool SetForegroundWindow(IntPtr handle);
    [DllImport("user32.dll")] public static extern bool ShowWindow(IntPtr handle, int command);
    [DllImport("user32.dll")] public static extern bool SetWindowPos(IntPtr handle, IntPtr after, int x, int y, int width, int height, uint flags);
    [DllImport("user32.dll")] public static extern bool GetWindowRect(IntPtr handle, out Rect rect);
    [DllImport("user32.dll")] public static extern IntPtr WindowFromPoint(Point point);
    [DllImport("user32.dll")] public static extern IntPtr GetAncestor(IntPtr handle, uint flags);
    [DllImport("dwmapi.dll")] public static extern int DwmGetWindowAttribute(IntPtr handle, int attribute, out Rect rect, int size);
}
'@
[AppointEaseCapture]::SetProcessDPIAware() | Out-Null
$application = @(Get-Process java, javaw -ErrorAction SilentlyContinue | Where-Object { $_.MainWindowTitle.StartsWith('AppointEase | ') })
if ($application.Count -ne 1) { throw 'Launch exactly one AppointEase desktop window before capture.' }
$handle = $application[0].MainWindowHandle
$screen = [System.Windows.Forms.Screen]::PrimaryScreen.Bounds
$width = 1440
$height = 900
$filename = '01-overview-desktop.png'
if ($View -eq 'Compact') { $width = 1024; $height = 768; $filename = '02-overview-compact.png' }
if ($View -eq 'Navigation') { $filename = '03-navigation.png' }
if ($screen.Width -lt $width -or $screen.Height -lt $height) { throw "Display is $($screen.Width)x$($screen.Height); requested window does not fit. No screenshot saved." }
[AppointEaseCapture]::ShowWindow($handle, 9) | Out-Null
[AppointEaseCapture]::SetWindowPos($handle, [IntPtr](-1), $screen.Left + 20, $screen.Top + 20, $width, $height, 0x0040) | Out-Null
[AppointEaseCapture]::SetForegroundWindow($handle) | Out-Null
# Allow native resizing, the JavaFX layout pulse and desktop compositing to finish.
Start-Sleep -Milliseconds 900
$rect = New-Object AppointEaseCapture+Rect
$result = [AppointEaseCapture]::DwmGetWindowAttribute($handle, 9, [ref]$rect, 16)
if ($result -ne 0) { [AppointEaseCapture]::GetWindowRect($handle, [ref]$rect) | Out-Null }
$captureWidth = $rect.Right - $rect.Left
$captureHeight = $rect.Bottom - $rect.Top
if ($captureWidth -le 0 -or $captureHeight -le 0) { throw 'Invalid application window bounds.' }
$directory = Join-Path (Split-Path $PSScriptRoot -Parent) 'docs/ui-review/screenshots'
if ($S7BName) { $directory = Join-Path $directory $Slice; $filename = "$S7BName.png" }
New-Item -ItemType Directory -Force -Path $directory | Out-Null
$path = Join-Path $directory $filename
$bitmap = New-Object System.Drawing.Bitmap($captureWidth, $captureHeight)
$graphics = [System.Drawing.Graphics]::FromImage($bitmap)
try {
    # Refuse to capture an obscuring window if Windows denies foreground activation.
    foreach ($offset in @(30, [int]($captureWidth / 2), ($captureWidth - 30))) {
        $point = New-Object AppointEaseCapture+Point
        $point.X = $rect.Left + $offset
        $point.Y = $rect.Top + [int]($captureHeight / 2)
        $visibleRoot = [AppointEaseCapture]::GetAncestor([AppointEaseCapture]::WindowFromPoint($point), 2)
        if ($visibleRoot -ne $handle) { throw "AppointEase is obscured at ($($point.X), $($point.Y)); visible handle $visibleRoot, expected $handle. No screenshot saved." }
    }
    # Raw visible window pixels only: no desktop-wide capture, crop editing, scaling or enhancement.
    $graphics.CopyFromScreen($rect.Left, $rect.Top, 0, 0, $bitmap.Size)
    $bitmap.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
} finally {
    $graphics.Dispose(); $bitmap.Dispose()
    [AppointEaseCapture]::SetWindowPos($handle, [IntPtr](-2), 0, 0, 0, 0, 0x0003) | Out-Null
}
$image = [System.Drawing.Image]::FromFile($path)
try {
    if ($image.RawFormat.Guid -ne [System.Drawing.Imaging.ImageFormat]::Png.Guid) { throw 'Saved file is not a PNG.' }
    if ((Get-Item -LiteralPath $path).Length -eq 0) { throw 'Saved PNG is empty.' }
    Write-Output "$path | valid PNG | $($image.Width)x$($image.Height) | $((Get-Item -LiteralPath $path).Length) bytes"
} finally { $image.Dispose() }
