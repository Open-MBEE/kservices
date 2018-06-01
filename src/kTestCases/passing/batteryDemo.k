Timepoint.setEpoch("2020-001T00:00:00.000")
Timepoint.setHorizonDuration(seconds(0.1))

var netPower : TimeVaryingMap = TimeVaryingMap("", "src/kTestCases/BusPowerSmall.csv", null, Double.class)
var netPowerIntegrated : TimeVaryingMap = netPower.integrate()
var netDeltaWattHours : TimeVaryingMap = netPowerIntegrated.getDeltaMap()
var batteryVoltage : TimeVaryingMap = TimeVaryingMap("", "src/kTestCases/batteryVoltageSmall.csv", null, Double.class)
var rechargeEfficiency : Real = 0.95
var ahCapacityBattery : Real = 183.0


var netDeltaAmpHours: TimeVaryingMap = netDeltaWattHours/ (if batteryVoltage = null then 32.8 else batteryVoltage)
var rechargeDeltaEnergy : TimeVaryingMap = Min(0.0, netDeltaAmpHours) * rechargeEfficiency/ahCapacityBattery

var negativeDeltaEnergy : TimeVaryingMap = Min(0.0, netDeltaAmpHours)

var deltaEnergy: TimeVaryingMap = rechargeDeltaEnergy + negativeDeltaEnergy

var stateOfCharge : TimeVaryingMap = if deltaEnergy < 0 then 0 else (if deltaEnergy > 1 then 1 else deltaEnergy)

