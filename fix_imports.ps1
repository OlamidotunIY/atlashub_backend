$javaFiles = Get-ChildItem -Path "." -Include *.java -Recurse -File | Where-Object { $_.FullName -notmatch "\\build\\" }

foreach ($file in $javaFiles) {
    $content = Get-Content -Path $file.FullName -Raw
    $original = $content

    # Application Layer
    $content = $content -replace "\.application\.port\.out", ".application.port"

    # Adapter In
    $content = $content -replace "\.presentation\.rest", ".adapter.in.web"
    $content = $content -replace "\.presentation\.webhook", ".adapter.in.web"
    $content = $content -replace "\.infrastructure\.messaging\.consumer", ".adapter.in.messaging"
    
    # Adapter Out
    $content = $content -replace "\.infrastructure\.persistence", ".adapter.out.persistence"
    $content = $content -replace "\.infrastructure\.external", ".adapter.out.external"
    $content = $content -replace "\.infrastructure\.cache", ".adapter.out.external"
    $content = $content -replace "\.infrastructure\.security", ".adapter.out.external"
    $content = $content -replace "\.infrastructure\.config", ".adapter.out.external"
    
    # Any other infrastructure messaging that wasn't consumer is likely publisher (external)
    $content = $content -replace "\.infrastructure\.messaging", ".adapter.out.external"
    
    # Any remaining infrastructure fallback
    $content = $content -replace "\.infrastructure", ".adapter.out.external"
    
    if ($content -cne $original) {
        Set-Content -Path $file.FullName -Value $content -NoNewline
    }
}
