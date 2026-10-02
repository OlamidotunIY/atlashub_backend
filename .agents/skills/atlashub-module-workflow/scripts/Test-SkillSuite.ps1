[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$skillsRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$errors = [Collections.Generic.List[string]]::new()

$skillFiles = Get-ChildItem -LiteralPath $skillsRoot -Recurse -Filter 'SKILL.md' -File
foreach ($file in $skillFiles) {
    $text = Get-Content -LiteralPath $file.FullName -Raw
    $relative = $file.FullName.Substring($skillsRoot.Length).TrimStart('\', '/')
    if ($text -notmatch '(?s)^---\s*\r?\nname:\s*[a-z0-9-]+\s*\r?\ndescription:') {
        $errors.Add("$relative :: invalid or missing YAML frontmatter")
    }
    if ($file.Directory.Name -ne 'atlashub-module-workflow' -and $text -notmatch 'atlashub-module-workflow') {
        $errors.Add("$relative :: does not load the mandatory workflow")
    }
    if ($text -match '(?im)^\s*git\s+push\b|MUST.*push') {
        $errors.Add("$relative :: contains automatic push instructions")
    }
    if ($text -match 'invoke_subagent|replace_file_content|view_file') {
        $errors.Add("$relative :: contains agent-vendor-specific tool instructions")
    }
    if ($text -match '[\x00-\x08\x0B\x0C\x0E-\x1F]') {
        $errors.Add("$relative :: contains control characters")
    }
}

$parserErrors = [Collections.Generic.List[string]]::new()
$scriptFiles = Get-ChildItem -LiteralPath $skillsRoot -Recurse -Filter '*.ps1' -File
foreach ($file in $scriptFiles) {
    $tokens = $null
    $parseErrors = $null
    [void][Management.Automation.Language.Parser]::ParseFile($file.FullName, [ref]$tokens, [ref]$parseErrors)
    foreach ($parseError in $parseErrors) {
        $relative = $file.FullName.Substring($skillsRoot.Length).TrimStart('\', '/')
        $parserErrors.Add("${relative}:$($parseError.Extent.StartLineNumber) :: $($parseError.Message)")
    }
}

foreach ($error in $parserErrors) { $errors.Add($error) }
if ($errors.Count -gt 0) {
    Write-Error ("Skill-suite validation failed ({0}):`n- {1}" -f $errors.Count, ($errors -join "`n- "))
    exit 1
}

Write-Host "PASS: validated $($skillFiles.Count) skills and parsed $($scriptFiles.Count) PowerShell scripts."
