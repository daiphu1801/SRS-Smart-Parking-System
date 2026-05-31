$result = @()
$files = Get-ChildItem -Path .\src\main\java -Recurse -Filter *Controller.java
foreach ($f in $files) {
    $content = Get-Content $f.FullName
    $basePath = ""
    foreach ($line in $content) {
        if ($line -match '@RequestMapping\(\s*(value\s*=\s*)?"([^"]+)"') {
            $basePath = $matches[2]
            break
        }
    }
    
    foreach ($line in $content) {
        if ($line -match '@(Get|Post|Put|Delete|Patch)Mapping\(\s*(value\s*=\s*)?"([^"]+)"') {
            $method = $matches[1].ToUpper()
            $path = $matches[3]
            $fullPath = ($basePath + $path) -replace '//', '/'
            $result += "[$method] $fullPath"
        } elseif ($line -match '@(Get|Post|Put|Delete|Patch)Mapping') {
            $method = $matches[1].ToUpper()
            $result += "[$method] $basePath"
        }
    }
}
$result | Sort-Object | Get-Unique | Out-File -FilePath .\api_list.txt -Encoding utf8
