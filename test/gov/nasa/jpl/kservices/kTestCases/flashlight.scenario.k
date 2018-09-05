// BEGIN MANUAL
ParameterListenerImpl.setMaxLoopsWithNoProgress(100)
ParameterListenerImpl.setArcConsistencyQuiet(true)

var epoch : Time = 0
// var switchOnTime : Time = epoch + seconds(5.0)
// var switchOffTime : Time = epoch + seconds(10.0)
// END MANUAL

class Controller extends DurativeEvent {
    var Cstate : TimeVaryingMap[ControllerState]
    class activity_325 extends DurativeEvent {
        req time >= startTime && time < endTime => Cstate = Off
        Cstate.setValue(startTime, Off) // MANUAL explicit state change
    }
    class activity_326 extends DurativeEvent {
        req time >= startTime && time < endTime => Cstate = Standby
        Cstate.setValue(startTime, Standby) // MANUAL explicit state change
    }
    class activity_327 extends DurativeEvent {
        req time >= startTime && time < endTime => Cstate = On
        Cstate.setValue(startTime, On) // MANUAL explicit state change
    }
    class activity_328 extends DurativeEvent {
        req time >= startTime && time < endTime => Cstate = Standby
        Cstate.setValue(startTime, Standby) // MANUAL explicit state change
    }
    class ControllerState
    val Standby : ControllerState
    val On : ControllerState
    val Off : ControllerState

    // var Cstate_init : ControllerState
    // req Cstate_init = Standby ||
    //     Cstate_init = On      ||
    //     Cstate_init = Off
    // Cstate.setValue(epoch, Off) // MANUAL initialization
}
// var Constroller_global_instance : Controller // MANUAL for testing only
class Battery extends DurativeEvent {
    var V : TimeVaryingMap[Real]
    var deltaV : TimeVaryingMap[Real]
    var i : TimeVaryingMap[Real]
    req V = TimeVaryingMap.zero + 10 // MANUAL addition
    req V = deltaV
}
// var Battery_global_instance : Battery // MANUAL for testing only
class Switch extends DurativeEvent {
    var deltaV : TimeVaryingMap[Real]
    var i : TimeVaryingMap[Real]
    var Ps : TimeVaryingMap[SwitchPosition]
    // var Ps_rules : SwitchPosition_Rules // MANUAL removed rules class
    // req Ps_rules.sm = Ps // MANUAL edit - changed from constructor to constraint
    class SwitchPosition_trigger
    val OPEN : SwitchPosition_trigger
    val CLOSE : SwitchPosition_trigger
    
    var SwitchPosition_cmd : TimeVaryingMap[SwitchPosition_trigger]
    class activity_312 extends DurativeEvent {
        req time >= startTime && time < endTime => Ps = Open
        Ps.setValue(startTime, Open) // MANUAL explicit state change
    }
    class activity_313 extends DurativeEvent {
        req time >= startTime && time < endTime => Ps = Closed
        Ps.setValue(startTime, Closed) // MANUAL explicit state change
    }
    class activity_314 extends DurativeEvent {
        req time >= startTime && time < endTime => Ps = Open
        Ps.setValue(startTime, Open) // MANUAL explicit state change
    }
    class activity_329 extends DurativeEvent {
        // req time >= startTime && time < endTime => i > 0
        // REVIEW - what should control i here?
    }
    // class SwitchPosition_Rules extends DurativeEvent {
        // var sm : TimeVaryingMap[SwitchPosition]
        var tr_Closed_Open : TimeVaryingMap[boolean] = (Ps = Closed && i > 0 && SwitchPosition_cmd = OPEN)
        // req el_Closed_Open : elaborates(null, Exec_Closed_Open.class, tr_Closed_Open)
        // class Exec_Closed_Open extends DurativeEvent {
        //     var nextMoment : Time = startTime + 1
        //     Ps.setValue(nextMoment, Open)
        //     i.setValue(nextMoment, 0) // MANUAL stateConstraint / behavior
        // }
        var tr_Open_Closed : TimeVaryingMap[boolean] = (Ps = Open && deltaV > 0 && SwitchPosition_cmd = CLOSE)
        // req el_Open_Closed : elaborates(null, Exec_Open_Closed.class, tr_Open_Closed)
        // class Exec_Open_Closed extends DurativeEvent {
        //     var nextMoment : Time = startTime + 1
        //     Ps.setValue(nextMoment, Closed)
        //     deltaV.setValue(nextMoment, 0) // MANUAL stateConstraint / behavior
        // }
    // }
    class SwitchPosition
    val Open : SwitchPosition
    val Closed : SwitchPosition
    
    req Ps != Closed || deltaV = 0
    req Ps != Open || i = 0
    
    deltaV.setValue(epoch, 10.0) // MANUAL initialization
    i.setValue(epoch, 0.0) // MANUAL initialization
    // Ps.setValue(epoch, Open) // MANUAL initialization

    // MANUAL firing:
    // SwitchPosition_cmd.setValue(switchOnTime, CLOSE)
    // SwitchPosition_cmd.setValue(switchOffTime, OPEN)
}
// var Switch_global_instance : Switch // MANUAL for testing only
class Lamp extends DurativeEvent {
    var R : TimeVaryingMap[Real]
    var eta : TimeVaryingMap[Real]
    var L : TimeVaryingMap[Real]
    var deltaV : TimeVaryingMap[Real]
    var i : TimeVaryingMap[Real]
    req R = TimeVaryingMap.zero + 50  // MANUAL addition
    req eta = TimeVaryingMap.zero + 4 // MANUAL addition
    req L = deltaV * i * eta

    deltaV.setValue(epoch, 0.0) // MANUAL initialization
    i.setValue(epoch, 0.0) // MANUAL initialization
}
// var Lamp_global_instance : Lamp // MANUAL for testing only
class SystemClass {
    var instance_Battery: Battery
    var instance_Lamp: Lamp
    var instance_Switch: Switch
    var instance_Controller: Controller
    req instance_Switch.i = instance_Battery.i
}

class LampON {
    // BEGIN MANUAL
    // req tp_224 = 0
    // req tp_224 <= seconds(500.0)
    // req tp_226 <= seconds(500.0)
    // req tp_232 <= seconds(500.0)
    // req tp_238 <= seconds(500.0)
    // req tp_240 <= seconds(500.0)
    // req tp_242 <= seconds(500.0)
    // req tp_246 <= seconds(500.0)
    // req tp_248 <= seconds(500.0)
    // req tp_254 <= seconds(500.0)
    // req tp_260 <= seconds(500.0)
    // req tp_266 <= seconds(500.0)
    // END MANUAL
    
    var tp_224: Time
    var tp_232: Time
    var tp_238: Time
    var tp_246: Time
    var tp_248: Time
    var tp_240: Time
    var tp_254: Time
    var tp_242: Time
    var tp_260: Time
    var tp_266: Time
    var tp_226: Time
    
    req tp_232 + seconds(5.0) <= tp_246
    req tp_232 + seconds(5.0) >= tp_246
    req tp_248 + seconds(10.0) <= tp_240
    req tp_248 + seconds(15.0) >= tp_240
    req tp_238 <= tp_240
    req tp_266 <= tp_242
    req tp_254 + seconds(30.0) <= tp_242
    req tp_254 + seconds(30.0) >= tp_242
    req tp_260 + seconds(150.0) <= tp_226
    req tp_260 + seconds(200.0) >= tp_226
    var instance_SystemClass: SystemClass
    
    var goal00000: activity_312 = instance_SystemClass.instance_Switch.activity_312(startTime :: tp_224, endTime :: tp_238)
    var goal00001: activity_313 = instance_SystemClass.instance_Switch.activity_313(startTime :: tp_240, endTime :: tp_266)
    var goal00002: activity_314 = instance_SystemClass.instance_Switch.activity_314(startTime :: tp_242, endTime :: tp_226)
    var goal00003: activity_325 = instance_SystemClass.instance_Controller.activity_325(startTime :: tp_224, endTime :: tp_232)
    var goal00004: activity_326 = instance_SystemClass.instance_Controller.activity_326(startTime :: tp_246, endTime :: tp_248)
    var goal00005: activity_327 = instance_SystemClass.instance_Controller.activity_327(startTime :: tp_240, endTime :: tp_254)
    var goal00006: activity_328 = instance_SystemClass.instance_Controller.activity_328(startTime :: tp_242, endTime :: tp_260)
    var goal00007: activity_329 = instance_SystemClass.instance_Switch.activity_329(startTime :: tp_240, endTime :: tp_266)
}
var instance_LampON : LampON

