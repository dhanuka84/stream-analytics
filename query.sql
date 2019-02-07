SELECT DriverID,'Rule1' as RuleName,mytime(ts) as ts1 , mytime(CURRENT_TIMESTAMP) as ts2 FROM dataTable WHERE HireID = 4508724 AND PassengerName = 'Dhanuka' AND CorrelationID LIKE '%193400835%' 
