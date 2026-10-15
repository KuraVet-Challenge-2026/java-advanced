# Cria o Web App com runtime Java 21 e restringe o acesso a HTTPS.

foreach ($nome in @("RG", "PLANO", "WEBAPP")) {
    if (-not (Get-Variable -Name $nome -ValueOnly -ErrorAction SilentlyContinue)) {
        Write-Host "Variavel `$$nome nao definida. Rode primeiro: . .\00-Variaveis.ps1" -ForegroundColor Red
        exit 1
    }
}

az webapp create -g $RG --plan $PLANO -n $WEBAPP --runtime "JAVA:21-java21"
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

az webapp update -g $RG -n $WEBAPP --https-only true
