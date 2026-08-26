$files = Get-ChildItem -Path "." -Include *.java, *.yml, *.yaml, *.properties, *.xml, *.json, *.sql -Recurse -File | Where-Object { $_.FullName -notmatch "\\build\\" -and $_.FullName -notmatch "\\\.git\\" }

foreach ($file in $files) {
    $content = Get-Content -Path $file.FullName -Raw
    $original = $content

    $content = $content -replace "Merchant", "Organization"
    $content = $content -replace "merchant", "organization"
    $content = $content -replace "MERCHANT", "ORGANIZATION"
    
    $content = $content -replace "SubAccount", "SplitRecipient"
    $content = $content -replace "subAccount", "splitRecipient"
    $content = $content -replace "SUB_ACCOUNT", "SPLIT_RECIPIENT"
    $content = $content -replace "SUBACCOUNT", "SPLITRECIPIENT"
    
    $content = $content -replace "Customer", "User"
    $content = $content -replace "customer", "user"
    $content = $content -replace "CUSTOMER", "USER"
    
    if ($content -cne $original) {
        Set-Content -Path $file.FullName -Value $content -NoNewline
    }
}

# Now rename files and directories bottom-up
$renamePairs = @(
    @("Merchant", "Organization"),
    @("SubAccount", "SplitRecipient"),
    @("Customer", "User")
)

foreach ($pair in $renamePairs) {
    $old = $pair[0]
    $new = $pair[1]
    
    # Do this multiple times for case-insensitivity because -match is case-insensitive in PS but we want exact case sometimes?
    # Wait, Windows is case insensitive, so rename "merchant" to "organization" is tricky if we just use -replace which is case-insensitive.
    # We will use case-sensitive -creplace
    $items = Get-ChildItem -Path "." -Recurse | Where-Object { $_.Name -cnotmatch "^build$" -and $_.FullName -notmatch "\\\.git\\" -and ($_.Name -cmatch $old -or $_.Name -cmatch $old.ToLower()) } | Sort-Object -Property @{Expression={$_.FullName.Length}; Descending=$true}
    
    foreach ($item in $items) {
        if (Test-Path $item.FullName) {
            $newName = $item.Name -creplace $old, $new
            $newName = $newName -creplace $old.ToLower(), $new.ToLower()
            if ($newName -cne $item.Name) {
                Rename-Item -Path $item.FullName -NewName $newName
            }
        }
    }
}
