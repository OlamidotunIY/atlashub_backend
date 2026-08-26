$srcDirs = Get-ChildItem -Path "." -Filter "src" -Recurse -Directory | Where-Object { $_.FullName -match "atlashub-" }

foreach ($src in $srcDirs) {
    $baseDirs = Get-ChildItem -Path "$($src.FullName)\main\java\com\atlashub" -Directory -ErrorAction SilentlyContinue | Where-Object { $_.Name -ne "app" }
    
    foreach ($baseDir in $baseDirs) {
        $base = $baseDir.FullName
        
        # 1. Adapter structure
        New-Item -ItemType Directory -Path "$base\adapter\in\web" -Force | Out-Null
        New-Item -ItemType Directory -Path "$base\adapter\in\messaging" -Force | Out-Null
        New-Item -ItemType Directory -Path "$base\adapter\out\persistence" -Force | Out-Null
        New-Item -ItemType Directory -Path "$base\adapter\out\external" -Force | Out-Null
        
        # Move presentation/rest -> adapter/in/web
        if (Test-Path "$base\presentation\rest") {
            Move-Item -Path "$base\presentation\rest\*" -Destination "$base\adapter\in\web\" -Force -ErrorAction SilentlyContinue
            Remove-Item -Path "$base\presentation" -Recurse -Force -ErrorAction SilentlyContinue
        }
        
        # Move presentation/webhook -> adapter/in/web/webhook
        if (Test-Path "$base\presentation\webhook") {
            Move-Item -Path "$base\presentation\webhook\*" -Destination "$base\adapter\in\web\" -Force -ErrorAction SilentlyContinue
            if (Test-Path "$base\presentation") { Remove-Item -Path "$base\presentation" -Recurse -Force -ErrorAction SilentlyContinue }
        }

        # Move infrastructure/messaging/consumer -> adapter/in/messaging
        if (Test-Path "$base\infrastructure\messaging\consumer") {
            Move-Item -Path "$base\infrastructure\messaging\consumer\*" -Destination "$base\adapter\in\messaging\" -Force -ErrorAction SilentlyContinue
            Remove-Item -Path "$base\infrastructure\messaging\consumer" -Recurse -Force -ErrorAction SilentlyContinue
        }
        
        # Move infrastructure/messaging -> adapter/out/external
        if (Test-Path "$base\infrastructure\messaging") {
            $items = Get-ChildItem -Path "$base\infrastructure\messaging"
            if ($items.Count -gt 0) {
                Move-Item -Path "$base\infrastructure\messaging\*" -Destination "$base\adapter\out\external\" -Force -ErrorAction SilentlyContinue
            }
        }
        
        # Move infrastructure/persistence -> adapter/out/persistence
        if (Test-Path "$base\infrastructure\persistence") {
            Move-Item -Path "$base\infrastructure\persistence\*" -Destination "$base\adapter\out\persistence\" -Force -ErrorAction SilentlyContinue
            Remove-Item -Path "$base\infrastructure\persistence" -Recurse -Force -ErrorAction SilentlyContinue
        }
        
        # Move infrastructure/external -> adapter/out/external
        if (Test-Path "$base\infrastructure\external") {
            Move-Item -Path "$base\infrastructure\external\*" -Destination "$base\adapter\out\external\" -Force -ErrorAction SilentlyContinue
            Remove-Item -Path "$base\infrastructure\external" -Recurse -Force -ErrorAction SilentlyContinue
        }
        
        # Move remaining infrastructure to adapter/out/external
        if (Test-Path "$base\infrastructure") {
            $items = Get-ChildItem -Path "$base\infrastructure"
            if ($items.Count -gt 0) {
                Move-Item -Path "$base\infrastructure\*" -Destination "$base\adapter\out\external\" -Force -ErrorAction SilentlyContinue
            }
            Remove-Item -Path "$base\infrastructure" -Recurse -Force -ErrorAction SilentlyContinue
        }
        
        # 2. Application Port Structure
        if (Test-Path "$base\application\port\out") {
            Move-Item -Path "$base\application\port\out\*" -Destination "$base\application\port\" -Force -ErrorAction SilentlyContinue
            Remove-Item -Path "$base\application\port\out" -Force -ErrorAction SilentlyContinue
        }
        
        # 3. Domain Structure
        New-Item -ItemType Directory -Path "$base\domain\valueobject" -Force | Out-Null
        New-Item -ItemType Directory -Path "$base\domain\service" -Force | Out-Null
    }
}
