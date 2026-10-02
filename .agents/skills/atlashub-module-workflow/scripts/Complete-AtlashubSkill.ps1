[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][string]$Module,
    [string]$SubModule = '',
    [Parameter(Mandatory = $true)][ValidateNotNullOrEmpty()][string[]]$Paths,
    [Parameter(Mandatory = $true)][ValidateNotNullOrEmpty()][string[]]$TestPaths,
    [Parameter(Mandatory = $true)][ValidatePattern('^(feat|fix|refactor|test|chore|docs)\([a-z0-9-]+\): .+')][string]$CommitMessage,
    [switch]$RequiresIntegrationTest,
    [string[]]$IntegrationTestPaths = @(),
    [string[]]$AdditionalTasks = @()
)

$ErrorActionPreference = 'Stop'
$contextScript = Join-Path $PSScriptRoot 'Get-AtlashubContext.ps1'
$architectureScript = Join-Path $PSScriptRoot 'Test-AtlashubArchitecture.ps1'
$context = (& $contextScript -Module $Module -SubModule $SubModule -Artifact test) | ConvertFrom-Json
$repoRoot = $context.repositoryRoot

$preStaged = @(& git -C $repoRoot diff --cached --name-only)
if ($preStaged.Count -gt 0) {
    throw "Refusing to commit because unrelated work may already be staged:`n$($preStaged -join "`n")"
}

function Normalize-ScopedPath([string]$Path) {
    if ([string]::IsNullOrWhiteSpace($Path) -or $Path -match '[*?]' -or $Path -in @('.', './', '.\')) {
        throw "Unsafe or broad commit path: '$Path'"
    }
    $candidate = if ([IO.Path]::IsPathRooted($Path)) { $Path } else { Join-Path $repoRoot $Path }
    $full = [IO.Path]::GetFullPath($candidate)
    if (-not $full.StartsWith($repoRoot + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase)) {
        throw "Commit path escapes repository: '$Path'"
    }
    return $full.Substring($repoRoot.Length).TrimStart('\', '/') -replace '\\', '/'
}

$scopedPaths = @($Paths | ForEach-Object { Normalize-ScopedPath $_ } | Sort-Object -Unique)
$scopedTests = @($TestPaths | ForEach-Object { Normalize-ScopedPath $_ } | Sort-Object -Unique)
$scopedIntegrationTests = @($IntegrationTestPaths | ForEach-Object { Normalize-ScopedPath $_ } | Sort-Object -Unique)
foreach ($testPath in $scopedTests) {
    if ($testPath -notmatch '(Test|Tests|IT|IntegrationTest)\.java$') {
        throw "Test path does not follow a recognized Java test name: $testPath"
    }
    if (-not (Test-Path -LiteralPath (Join-Path $repoRoot $testPath))) {
        throw "Required test file does not exist: $testPath"
    }
    if ($testPath -notin $scopedPaths) {
        throw "Every test path must also be included in -Paths: $testPath"
    }
}

if ($RequiresIntegrationTest) {
    if ($scopedIntegrationTests.Count -eq 0) { throw 'Integration testing is required but no -IntegrationTestPaths were supplied.' }
    foreach ($integrationTest in $scopedIntegrationTests) {
        if ($integrationTest -notin $scopedTests) {
            throw "Every integration test must also be included in -TestPaths: $integrationTest"
        }
    }
    if ($AdditionalTasks.Count -eq 0) { throw 'Integration testing is required but no -AdditionalTasks were supplied.' }
}

& $architectureScript -Module $Module -SubModule $SubModule
if ($LASTEXITCODE -ne 0) { throw 'Architecture validation failed; refusing to test or commit.' }

$gradlew = Join-Path $repoRoot 'gradlew.bat'
if (-not (Test-Path -LiteralPath $gradlew)) { $gradlew = Join-Path $repoRoot 'gradlew' }
$tasks = @("$($context.gradleProject):compileJava", "$($context.gradleProject):test") + $AdditionalTasks
& $gradlew @($tasks | Select-Object -Unique)
if ($LASTEXITCODE -ne 0) { throw 'Required Gradle verification failed; refusing to commit.' }

foreach ($path in $scopedPaths) {
    & git -C $repoRoot add -- $path
    if ($LASTEXITCODE -ne 0) { throw "Failed to stage scoped path: $path" }
}

$staged = @(& git -C $repoRoot diff --cached --name-only)
if ($staged.Count -eq 0) { throw 'No scoped changes were staged; refusing to create an empty commit.' }
$outsideScope = @($staged | Where-Object { $_ -notin $scopedPaths })
if ($outsideScope.Count -gt 0) {
    throw "Refusing to commit files outside this skill's scope:`n$($outsideScope -join "`n")"
}
$missingTests = @($scopedTests | Where-Object { $_ -notin $staged })
if ($missingTests.Count -gt 0) {
    throw "The matching tests were not changed and staged. Update them before committing:`n$($missingTests -join "`n")"
}

& git -C $repoRoot commit -m $CommitMessage -- @scopedPaths
if ($LASTEXITCODE -ne 0) { throw 'Scoped commit failed.' }
Write-Host "COMMITTED: $CommitMessage"
