$moves = @{
    "atlaspay-shared-kernel" = "atlashub-shared"
    "atlaspay-identity" = "atlashub-platform\identity"
    "atlaspay-auth" = "atlashub-platform\auth"
    "atlaspay-admin" = "atlashub-platform\admin"
    "atlaspay-accounts" = "atlashub-pay\accounts"
    "atlaspay-ledger" = "atlashub-pay\ledger"
    "atlaspay-transfers" = "atlashub-pay\transfers"
    "atlaspay-charges" = "atlashub-pay\charges"
    "atlaspay-subscriptions" = "atlashub-pay\subscriptions"
    "atlaspay-escrow" = "atlashub-pay\escrow"
    "atlaspay-settlement" = "atlashub-pay\settlement"
    "atlaspay-transaction-splits" = "atlashub-pay\splits"
    "atlaspay-transactions-query" = "atlashub-pay\transactions-query"
    "atlaspay-eventbus" = "atlashub-infrastructure\eventbus"
    "atlaspay-notifications" = "atlashub-infrastructure\notifications"
    "atlaspay-rate-limiter" = "atlashub-infrastructure\rate-limiter"
    "atlaspay-audit" = "atlashub-infrastructure\audit"
    "atlaspay-app" = "atlashub-app"
}

foreach ($old in $moves.Keys) {
    $new = $moves[$old]
    if (Test-Path $old) {
        # Move src
        if (Test-Path "$old\src") {
            if (Test-Path "$new\src") {
                Remove-Item -Path "$new\src" -Recurse -Force
            }
            Move-Item -Path "$old\src" -Destination $new -Force
        }
        # Move build.gradle
        if (Test-Path "$old\build.gradle") {
            Move-Item -Path "$old\build.gradle" -Destination "$new\build.gradle" -Force
        }
        # Remove old directory
        Remove-Item -Path $old -Recurse -Force
    }
}

# Now update build.gradle dependencies
$files = Get-ChildItem -Path "." -Include build.gradle -Recurse -File | Where-Object { $_.FullName -notmatch "\\build\\" }
foreach ($file in $files) {
    $content = Get-Content -Path $file.FullName -Raw
    $newContent = $content -replace "':atlaspay-shared-kernel'", "':atlashub-shared'"
    $newContent = $newContent -replace "':atlaspay-identity'", "':atlashub-platform:identity'"
    $newContent = $newContent -replace "':atlaspay-auth'", "':atlashub-platform:auth'"
    $newContent = $newContent -replace "':atlaspay-admin'", "':atlashub-platform:admin'"
    $newContent = $newContent -replace "':atlaspay-accounts'", "':atlashub-pay:accounts'"
    $newContent = $newContent -replace "':atlaspay-ledger'", "':atlashub-pay:ledger'"
    $newContent = $newContent -replace "':atlaspay-transfers'", "':atlashub-pay:transfers'"
    $newContent = $newContent -replace "':atlaspay-charges'", "':atlashub-pay:charges'"
    $newContent = $newContent -replace "':atlaspay-subscriptions'", "':atlashub-pay:subscriptions'"
    $newContent = $newContent -replace "':atlaspay-escrow'", "':atlashub-pay:escrow'"
    $newContent = $newContent -replace "':atlaspay-settlement'", "':atlashub-pay:settlement'"
    $newContent = $newContent -replace "':atlaspay-transaction-splits'", "':atlashub-pay:splits'"
    $newContent = $newContent -replace "':atlaspay-transactions-query'", "':atlashub-pay:transactions-query'"
    $newContent = $newContent -replace "':atlaspay-eventbus'", "':atlashub-infrastructure:eventbus'"
    $newContent = $newContent -replace "':atlaspay-notifications'", "':atlashub-infrastructure:notifications'"
    $newContent = $newContent -replace "':atlaspay-rate-limiter'", "':atlashub-infrastructure:rate-limiter'"
    $newContent = $newContent -replace "':atlaspay-audit'", "':atlashub-infrastructure:audit'"
    $newContent = $newContent -replace "':atlaspay-app'", "':atlashub-app'"
    
    if ($content -cne $newContent) {
        Set-Content -Path $file.FullName -Value $newContent -NoNewline
    }
}
