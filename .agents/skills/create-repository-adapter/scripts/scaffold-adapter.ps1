[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][string]$Module,
    [string]$SubModule = '',
    [Parameter(Mandatory = $true)][ValidatePattern('^[A-Z][A-Za-z0-9]*$')][string]$EntityName,
    [Parameter(Mandatory = $true)][ValidatePattern('^[A-Z][A-Za-z0-9]*(Jpa|JPA)$')][string]$JpaClassName,
    [Parameter(Mandatory = $true)][ValidatePattern('^[a-z][a-z0-9_]*_seq$')][string]$SequenceName,
    [string]$CustomMethodImplementations = ''
)

$ErrorActionPreference = 'Stop'
$contextScript = Join-Path $PSScriptRoot '..\..\atlashub-module-workflow\scripts\Get-AtlashubContext.ps1'
$context = (& $contextScript -Module $Module -SubModule $SubModule -Artifact adapter) | ConvertFrom-Json
$required = @(
    "domain\repositories\$EntityName`Repository.java",
    "infrastructure\persistence\entities\$JpaClassName.java",
    "infrastructure\persistence\mappers\$EntityName`Mapper.java",
    "infrastructure\persistence\repositories\SpringData$EntityName`Repository.java"
)
foreach ($relative in $required) {
    $path = Join-Path $context.sourceRoot $relative
    if (-not (Test-Path $path)) { throw "Missing prerequisite: $path" }
}
$targetDir = Join-Path $context.sourceRoot 'infrastructure\persistence\adapters'
$targetFile = Join-Path $targetDir "$EntityName`RepositoryAdapter.java"
if (Test-Path $targetFile) { throw "Refusing to overwrite: $targetFile" }
$custom = $CustomMethodImplementations.Trim()
if ($custom) { $custom = "`r`n`r`n$custom" }

New-Item -ItemType Directory -Force -Path $targetDir | Out-Null
$content = @"
package $($context.javaPackage).infrastructure.persistence.adapters;

import $($context.javaPackage).domain.entities.$EntityName;
import $($context.javaPackage).domain.repositories.$EntityName`Repository;
import $($context.javaPackage).infrastructure.persistence.entities.$JpaClassName;
import $($context.javaPackage).infrastructure.persistence.mappers.$EntityName`Mapper;
import $($context.javaPackage).infrastructure.persistence.repositories.SpringData$EntityName`Repository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

@Component
public class $EntityName`RepositoryAdapter extends JpaBaseRepository<$EntityName, $JpaClassName>
        implements $EntityName`Repository {

    public $EntityName`RepositoryAdapter(
            SpringData$EntityName`Repository repository,
            $EntityName`Mapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher) {
        super(repository, mapper, sequenceGenerator, eventPublisher);
    }

    @Override
    protected String getSequenceName() {
        return "$SequenceName";
    }$custom
}
"@
Set-Content -LiteralPath $targetFile -Value $content -Encoding utf8NoBOM
Write-Host "CREATED: $targetFile"
Write-Host 'The module will not compile until every custom domain repository method is implemented.'
