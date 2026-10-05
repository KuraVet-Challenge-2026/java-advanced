az webapp config appsettings set -g $RG -n $WEBAPP --settings `
    SPRING_DATASOURCE_URL="jdbc:sqlserver://$SQLSERVER.database.windows.net:1433;database=$SQLDB;encrypt=true;trustServerCertificate=false;loginTimeout=30;" `
    SPRING_DATASOURCE_USERNAME=$SQLUSER `
    SPRING_DATASOURCE_PASSWORD=$SQLPASS `
    SPRING_PROFILES_ACTIVE="azure"