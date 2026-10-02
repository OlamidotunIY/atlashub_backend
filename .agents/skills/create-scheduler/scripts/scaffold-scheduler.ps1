[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][string]$Module,
    [string]$SubModule = '',
    [Parameter(Mandatory = $true)][ValidatePattern('^[A-Z][A-Za-z0-9]*$')][string]$SchedulerName,
    [Parameter(Mandatory = $true)][string]$CronExpression,
    [Parameter(Mandatory = $true)][string]$HandlerClassFqn,
    [Parameter(Mandatory = $true)][string]$CommandClassFqn,
    [Parameter(Mandatory = $true)][string]$CommandExpression
)

$ErrorActionPreference = 'Stop'
$contextScript = Join-Path $PSScriptRoot '..\..\atlashub-module-workflow\scripts\Get-AtlashubContext.ps1'
$context = (& $contextScript -Module $Module -SubModule $SubModule -Artifact scheduler) | ConvertFrom-Json
$handlerClass = ($HandlerClassFqn -split '\.')[-1]
$targetDir = Join-Path $context.sourceRoot 'infrastructure\messaging\schedulers'
$targetFile = Join-Path $targetDir "$SchedulerName`Scheduler.java"
if (Test-Path $targetFile) { throw "Refusing to overwrite: $targetFile" }

New-Item -ItemType Directory -Force -Path $targetDir | Out-Null
$content = @"
package $($context.javaPackage).$((($targetDir.Substring($context.sourceRoot.Length).TrimStart('\')) -replace '\\', '.'));

import $CommandClassFqn;
import $HandlerClassFqn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class $SchedulerName`Scheduler {

    private static final Logger log = LoggerFactory.getLogger($SchedulerName`Scheduler.class);
    private final $handlerClass handler;

    public $SchedulerName`Scheduler($handlerClass handler) {
        this.handler = handler;
    }

    @Scheduled(cron = "$CronExpression")
    public void run() {
        log.info("$SchedulerName scheduler triggered");
        handler.execute($CommandExpression);
    }
}
"@
Set-Content -LiteralPath $targetFile -Value $content -Encoding utf8NoBOM
Write-Host "CREATED: $targetFile"
