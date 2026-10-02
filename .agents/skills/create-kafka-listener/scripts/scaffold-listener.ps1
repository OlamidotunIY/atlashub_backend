[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][string]$Module,
    [string]$SubModule = '',
    [Parameter(Mandatory = $true)][ValidatePattern('^[A-Z][A-Za-z0-9]*$')][string]$ListenerName,
    [Parameter(Mandatory = $true)][string]$Topic,
    [Parameter(Mandatory = $true)][string]$GroupId,
    [Parameter(Mandatory = $true)][string]$EventClassFqn,
    [Parameter(Mandatory = $true)][string]$EventType,
    [Parameter(Mandatory = $true)][string]$HandlerClassFqn,
    [Parameter(Mandatory = $true)][string]$CommandClassFqn,
    [Parameter(Mandatory = $true)][string]$CommandExpression
)

$ErrorActionPreference = 'Stop'
$contextScript = Join-Path $PSScriptRoot '..\..\atlashub-module-workflow\scripts\Get-AtlashubContext.ps1'
$context = (& $contextScript -Module $Module -SubModule $SubModule -Artifact listener) | ConvertFrom-Json
$eventClass = ($EventClassFqn -split '\.')[-1]
$handlerClass = ($HandlerClassFqn -split '\.')[-1]
$commandClass = ($CommandClassFqn -split '\.')[-1]
$targetDir = Join-Path $context.sourceRoot 'infrastructure\messaging\listeners'
$targetFile = Join-Path $targetDir "$ListenerName`Listener.java"
if (Test-Path $targetFile) { throw "Refusing to overwrite: $targetFile" }
$beanName = (($context.javaPackage -replace '^com\.atlashub\.', '') -replace '\.', '') + $ListenerName + 'Listener'
$beanName = $beanName.Substring(0,1).ToLowerInvariant() + $beanName.Substring(1)

New-Item -ItemType Directory -Force -Path $targetDir | Out-Null
$content = @"
package $($context.javaPackage).infrastructure.messaging.listeners;

import $CommandClassFqn;
import $HandlerClassFqn;
import $EventClassFqn;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component("$beanName")
public class $ListenerName`Listener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger($ListenerName`Listener.class);
    private static final String GROUP_ID = "$GroupId";
    private final $handlerClass handler;

    public $ListenerName`Listener(ObjectMapper objectMapper, $handlerClass handler) {
        super(objectMapper);
        this.handler = handler;
    }

    @PostConstruct
    public void init() {
        registerSubscription($eventClass.class.getName(), GROUP_ID);
    }

    @KafkaListener(topics = "$Topic", groupId = GROUP_ID)
    public void listen(String messagePayload) {
        processEventIfMatches(messagePayload, "$EventType", $eventClass.class, log, GROUP_ID,
                error -> error instanceof TimeoutException,
                event -> handler.execute($CommandExpression));
    }
}
"@
Set-Content -LiteralPath $targetFile -Value $content -Encoding utf8NoBOM
Write-Host "CREATED: $targetFile"
