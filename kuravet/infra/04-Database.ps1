az sql server create -n $SQLSERVER -g $RG -l $LOCAL --admin-user $SQLUSER --admin-password $SQLPASS

az sql db create -g $RG --server $SQLSERVER -n $SQLDB --service-objective Basic

az sql server firewall-rule create -g $RG --server $SQLSERVER -n AllowAzureServices --start-ip-address 0.0.0.0 --end-ip-address 0.0.0.0

$meuIp = (Invoke-RestMethod "https://api.ipify.org?format=json").ip
az sql server firewall-rule create -g $RG --server $SQLSERVER -n permitir-cliente-local --start-ip-address $meuIp --end-ip-address $meuIp