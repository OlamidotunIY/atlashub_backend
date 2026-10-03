[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][ValidateNotNullOrEmpty()][string]$Module,
    [string]$SubModule = "",
    [switch]$AllFiles
)

$ErrorActionPreference = "Stop"
$contextScript = Join-Path $PSScriptRoot "Get-AtlashubContext.ps1"
$context = (& $contextScript -Module $Module -SubModule $SubModule -Artifact audit) | ConvertFrom-Json
$repoRoot = $context.repositoryRoot
$sourceRoot = $context.sourceRoot
$moduleRoot = Split-Path (Split-Path (Split-Path (Split-Path $sourceRoot -Parent) -Parent) -Parent) -Parent

if ($AllFiles) {
    $files = Get-ChildItem -LiteralPath $moduleRoot -Recurse -Filter '*.java' -File
} else {
    $changed = @(& git -C $repoRoot diff --name-only --diff-filter=ACMR 2>$null; & git -C $repoRoot ls-files --others --exclude-standard 2>$null)
    $files = $changed | Where-Object { $_ -like '*.java' } | ForEach-Object {
        $candidate = Join-Path $repoRoot $_
        if ((Test-Path -LiteralPath $candidate) -and $candidate.StartsWith($moduleRoot, [StringComparison]::OrdinalIgnoreCase)) {
            Get-Item -LiteralPath $candidate
        }
    } | Sort-Object FullName -Unique
}

$errors = [Collections.Generic.List[string]]::new()
function Add-RuleError([IO.FileInfo]$File, [string]$Rule) {
    $relative = $File.FullName.Substring($repoRoot.Length).TrimStart('\', '/')
    $errors.Add("$relative :: $Rule")
}

foreach ($file in $files) {
    $text = Get-Content -LiteralPath $file.FullName -Raw
    $normalized = $file.FullName.Replace('/', '\')

    if ($text -match '(?m)^import\s+[\w.]+\.\*;') { Add-RuleError $file 'Wildcard imports are forbidden.' }
    if ($text -match '(?m)\bTODO\b|\bFIXME\b|UnsupportedOperationException\s*\(') { Add-RuleError $file 'Placeholder code is forbidden.' }
    if ($text -match '\bjsonb\b') { Add-RuleError $file 'MySQL mappings must use json, not jsonb.' }

    if ($normalized -match '\\domain\\') {
        if ($text -match 'import\s+org\.springframework\.|import\s+jakarta\.persistence\.|import\s+.*\.infrastructure\.|import\s+.*\.presentation\.') {
            Add-RuleError $file 'Domain code imports framework or outer-layer code.'
        }
    }

    if ($normalized -match '\\domain\\entities\\') {
        if ($text -match 'throw\s+new\s+(BusinessRuleException|ConflictException|NotFoundException|ValidationException|AuthorizationException|DomainException)\s*\(') {
            Add-RuleError $file 'Entities must throw a module-specific domain exception, not a shared base exception.'
        }
        if ($text -notmatch 'import\s+lombok\.Getter\s*;' -or $text -notmatch '(?m)^@Getter\s*$') {
            Add-RuleError $file 'Domain entities must use Lombok @Getter for field access.'
        }
        $explicitAccessors = [regex]::Matches($text, '(?ms)public\s+[\w<>?,.\[\]\s]+\s+(?<name>(?:get|is)[A-Z]\w*)\s*\(\s*\)\s*\{(?<body>.*?)\}')
        foreach ($accessor in $explicitAccessors) {
            $accessorName = $accessor.Groups['name'].Value
            $body = $accessor.Groups['body'].Value.Trim()
            $isSimpleFieldGetter = $body -match '^return\s+(?:this\.)?\w+\s*;$' -or $body -match '^return\s+new\s+(?:ArrayList|HashSet|LinkedHashSet|HashMap|LinkedHashMap)\s*<>?\s*\(\s*(?:this\.)?\w+\s*\)\s*;$'
            if ($accessorName -ne 'getId' -and $isSimpleFieldGetter) {
                Add-RuleError $file "Hand-written field getter '$accessorName' is forbidden; use Lombok @Getter."
            }
        }
    }

    if ($normalized -match '\\domain\\(?:port|ports)\\' -and $text -match '\binterface\s+\w+QueryPort\b') {
        Add-RuleError $file 'Cross-module query ports must live in atlashub-shared/com.atlashub.shared.application.port, never a module domain.'
    }

    if ($normalized -match '\\domain\\(?:repository|repositories)\\' -and $text -match '\bextends\s+Repository\s*<') {
        $baseRepositoryMethods = @('nextIdentity', 'save', 'findById', 'deleteById', 'existsById', 'findAll')
        foreach ($methodName in $baseRepositoryMethods) {
            if ($text -match "(?m)^\s*(?:[\w<>?,.\[\]]+\s+)+$methodName\s*\(") {
                Add-RuleError $file "Repository redeclares inherited base method '$methodName'."
            }
        }
    }

    if ($normalized -match '\\infrastructure\\messaging\\listeners\\') {
        if ($text -match 'import\s+.*\.(repositories|persistence|external)\.' -or $text -match '\b\w+Repository\b') {
            Add-RuleError $file 'Listeners may depend only on command handlers, never repositories/persistence/external adapters.'
        }
        if ($text -notmatch '\b\w+Handler\b' -or $text -notmatch '\.execute\s*\(') {
            Add-RuleError $file 'Listener must delegate to a command handler.'
        }
    }

    if ($text -match '\bimplements\s+\w+QueryPort\b' -and $normalized -notmatch '\\infrastructure\\persistence\\adapters\\') {
        Add-RuleError $file 'Shared query-port adapters must live in infrastructure/persistence/adapters.'
    }

    if ($text -match '@Scheduled\s*\(' -and $normalized -notmatch '\\infrastructure\\messaging\\schedulers\\') {
        Add-RuleError $file 'Schedulers must live in infrastructure/messaging/schedulers.'
    }

    if ($normalized -match '\\presentation\\rest\\') {
        if ($text -match 'import\s+.*\.(repositories|persistence)\.') { Add-RuleError $file 'Controllers must not access repositories or persistence.' }
        if ($text -match '\brecord\s+\w+(Request|Response)\s*\(') { Add-RuleError $file 'Request/response DTOs belong in presentation/dto.' }
        if ($text -notmatch 'ApiResponse<') { Add-RuleError $file 'Controller responses must use ApiResponse<T>.' }
    }

    if ($normalized -match '\\infrastructure\\persistence\\entities\\') {
        if ($text -match '@(OneToMany|ManyToOne|ManyToMany|OneToOne|GeneratedValue)\b') {
            Add-RuleError $file 'JPA relationships and generated IDs are forbidden; store scalar IDs.'
        }
    }

    $imports = [regex]::Matches($text, '(?m)^import\s+com\.atlashub\.([a-z0-9_]+)(?:\.([a-z0-9_]+))?\.')
    foreach ($match in $imports) {
        $first = $match.Groups[1].Value
        $second = $match.Groups[2].Value
        $ownFirst = $context.module
        $ownSecond = $context.subModule
        $isShared = $first -eq 'shared'
        $isOwn = $first -eq $ownFirst -and ((-not $ownSecond) -or $second -eq $ownSecond)
        if (-not $isShared -and -not $isOwn -and $normalized -notmatch '\\atlashub-main\\') {
            Add-RuleError $file "Direct cross-module import is forbidden: $($match.Value.Trim())"
        }
    }
}

if ($errors.Count -gt 0) {
    Write-Error ("AtlasHub architecture validation failed ({0}):`n- {1}" -f $errors.Count, ($errors -join "`n- "))
    exit 1
}

Write-Host "PASS: AtlasHub architecture validation checked $($files.Count) changed Java file(s) in $($context.gradleProject)."
