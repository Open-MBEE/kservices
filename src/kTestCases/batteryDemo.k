var netPower : TimeVaryingMap = TimeVaryingMap("", "/Users/ayelaman/git/kservices/src/kTestCases/BusPower.csv", null, Double.class)
var netPowerIntegrated : TimeVaryingMap = netPower.integrate()
var netDeltaWattHours : TimeVaryingMap = netPowerIntegrated.getDeltaMap()
var netDeltaAmpHours: TimeVaryingMap = netDeltaWattHours/ (if batteryVoltage = null then 32.8 else batteryVoltage)
var batteryVoltage : TimeVaryingMap = 32.8 - (1.0 - (if stateOfCharge = null then 1.0 else stateOfCharge )) * 6.8
var stateOfCharge : Consumable = Consumable("", 1.0, deltaEnergy, 0.0, 1.0, TimeVaryingMap.LINEAR)
var rechargeEfficiency : Real = 0.95
var ahCapacityBattery : Real = 183
var rechargeDeltaEnergy : TimeVaryingMap = Min(0.0, netDeltaAmpHours) * rechargeEfficiency/ahCapacityBattery
var negativeDeltaEnergy : TimeVaryingMap = Min(0.0, netDeltaAmpHours)
var deltaEnergy: TimeVaryingMap = rechargeDeltaEnergy + negativeDeltaEnergy
