var netPower : TimeVaryingMap = TimeVaryingMap("", "/Users/ayelaman/git/kservices/src/kTestCases/BusPower.csv", null, Double.class)
var netPowerIntegrated : TimeVaryingMap = netPower.integrate()
var netDeltaWattHours : TimeVaryingMap = netPowerIntegrated.getDeltaMap()
var batteryVoltage : TimeVaryingMap = TimeVaryingMap("", "/Users/ayelaman/git/kservices/src/kTestCases/batteryVoltage.csv", null, Double.class)
var netDeltaAmpHours: TimeVaryingMap = netDeltaWattHours/ batteryVoltage
