[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][ValidateNotNullOrEmpty()][string]$Module,
    [string]$SubModule = "",
    [ValidateSet("entity", "value-object", "error", "event", "repository", "command", "query", "port", "adapter", "listener", "scheduler", "dto", "controller", "test", "audit", "other")]
    [string]$Artifact = "other"
)

$ErrorActionPreference = "Stop"
$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..\..\..")).Path
$settings = Join-Path $repoRoot "settings.gradle"
if (-not (Test-Path -LiteralPath $settings)) {
    throw "Run this script from the AtlasHub repository; settings.gradle was not found."
}

$normalizedModule = $Module.Trim().ToLowerInvariant()
$normalizedSubModule = $SubModule.Trim().ToLowerInvariant()
$packageSegments = if ($normalizedSubModule) { @($normalizedModule, $normalizedSubModule) } else { @($normalizedModule) }
$packageRelative = [string]::Join([IO.Path]::DirectorySeparatorChar, $packageSegments)
$packageSuffix = Join-Path "src\main\java\com\atlashub" $packageRelative

$candidates = Get-ChildItem -LiteralPath $repoRoot -Directory -Recurse |
    Where-Object { $_.FullName.EndsWith($packageSuffix, [StringComparison]::OrdinalIgnoreCase) }

if ($candidates.Count -ne 1) {
    $found = ($candidates.FullName -join "`n  ")
    throw "Expected exactly one source root ending in '$packageSuffix'; found $($candidates.Count).`n  $found"
}

$sourceRoot = $candidates[0].FullName
$gradleDir = Split-Path (Split-Path (Split-Path (Split-Path (Split-Path $sourceRoot -Parent) -Parent) -Parent) -Parent) -Parent
while ($gradleDir -and -not (Test-Path -LiteralPath (Join-Path $gradleDir "build.gradle"))) {
    $parent = Split-Path $gradleDir -Parent
    if ($parent -eq $gradleDir) { break }
    $gradleDir = $parent
}
if (-not $gradleDir -or -not (Test-Path -LiteralPath (Join-Path $gradleDir "build.gradle"))) {
    throw "Could not resolve the Gradle project containing '$sourceRoot'."
}

$moduleDoc = if ($normalizedModule -eq "pay" -and $normalizedSubModule) {
    Join-Path $repoRoot "docs\modules\pay-$normalizedSubModule-design.md"
} elseif ($normalizedModule -eq "authentication") {
    Join-Path $repoRoot "docs\modules\auth-design.md"
} elseif ($normalizedModule -in @('eventbus', 'audit', 'ratelimiter', 'storage')) {
    Join-Path $repoRoot "docs\modules\infrastructure-design.md"
} elseif ($normalizedModule -in @('main', 'shared')) {
    Join-Path $repoRoot "docs\design.md"
} else {
    Join-Path $repoRoot "docs\modules\$normalizedModule-design.md"
}
if (-not (Test-Path -LiteralPath $moduleDoc)) {
    throw "Required module design document is missing: $moduleDoc"
}

$requiredDocs = [Collections.Generic.List[string]]::new()
$requiredDocs.Add($moduleDoc)
$requiredDocs.Add((Join-Path $repoRoot "docs\architecture\module-communication.md"))

if ($Artifact -in @("command", "query", "controller")) {
    $requiredDocs.Add((Join-Path $repoRoot "docs\architecture\rbac-design.md"))
    $requiredDocs.Add((Join-Path $repoRoot "docs\architecture\multi-tenancy.md"))
}
if ($Artifact -in @("event", "listener")) {
    $requiredDocs.Add((Join-Path $repoRoot "docs\architecture\sagas-design.md"))
}

$missingDocs = $requiredDocs | Where-Object { -not (Test-Path -LiteralPath $_) }
if ($missingDocs) {
    throw "Required documentation is missing:`n$($missingDocs -join "`n")"
}

$relativeGradle = $gradleDir.Substring($repoRoot.Length).TrimStart('\', '/') -replace '[\\/]', ':'
$gradleProject = ":$relativeGradle"

[pscustomobject]@{
    repositoryRoot = $repoRoot
    module = $normalizedModule
    subModule = $normalizedSubModule
    artifact = $Artifact
    sourceRoot = $sourceRoot
    javaPackage = "com.atlashub." + ($packageSegments -join '.')
    gradleProject = $gradleProject
    requiredDocs = @($requiredDocs | Select-Object -Unique)
} | ConvertTo-Json -Depth 4
